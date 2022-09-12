/*
    finmgr - a financial management framework
    Copyright (C) 2022  Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    sean <at> kennedy <dot> software
 */
package sh.kss.finmgr.service.impl;


import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.domain.*;
import sh.kss.finmgr.persistence.DailyReportRepository;
import sh.kss.finmgr.service.DailyReportService;
import sh.kss.finmgr.service.FixingService;
import sh.kss.finmgr.service.InvestmentTransactionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;

@Singleton
@AllArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {

    private static final Logger log = LoggerFactory.getLogger(DailyReportServiceImpl.class);

    private final DailyReportRepository repository;
    private final InvestmentTransactionService investmentTransactionService;
    private final FixingService fixingService;
    private static final ZoneId zoneId = ZoneId.of("America/Toronto");



    @Override
    public void refresh() {
        repository.deleteAll();

        List<InvestmentTransaction> transactions = investmentTransactionService.findAll();
        if (transactions.size() > 0) {
            BigDecimal latestCash = ZERO;
            Set<Account> accounts = transactions.stream()
                    .map(InvestmentTransaction::account)
                    .collect(toSet());

            Instant start = transactions.get(0).transactionDate();
            Instant end = transactions.get(transactions.size() - 1).transactionDate();

            LocalDate startDate = LocalDate.ofInstant(start, zoneId);
            LocalDate endDate = LocalDate.ofInstant(end, zoneId);

            Map<LocalDate, List<InvestmentTransaction>> transactionsByDate = transactions.stream()
                    .collect(Collectors.groupingBy(transaction -> LocalDate.ofInstant(transaction.transactionDate(), zoneId)));

            Map<Account, Set<Holding>> holdings = accounts.stream()
                    .collect(Collectors.toMap(
                            a -> a,
                            a -> new HashSet<>()
                    ));

            Map<String, Symbol> symbolCache = new HashMap<>();

            LocalDate cursorDate = startDate;
            while (cursorDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                BigDecimal cashFlow = ZERO;
                Instant instant = cursorDate.atStartOfDay(zoneId).toInstant();
                if (transactionsByDate.containsKey(cursorDate)) {
                    for (InvestmentTransaction transaction: transactionsByDate.get(cursorDate)) {
                        switch (transaction.action()) {
                            case WITHDRAWAL:
                            case DEPOSIT:
                                cashFlow = cashFlow.add(transaction.net());
                            case DISTRIBUTION:
                            case FEE_OR_REBATE:
                            case FX_CONVERSION:
                                latestCash = latestCash.add(transaction.net());
                                break;
                            case TRADE:
                            case REINVESTMENT:
                                Account account = transaction.account();
                                Set<Holding> accountHoldings = holdings.get(account);
                                holdings.remove(account);
                                Symbol symbol;
                                if (symbolCache.containsKey(transaction.symbol())) {
                                    symbol = symbolCache.get(transaction.symbol());
                                } else {
                                    symbol = new Symbol(transaction.symbol());
                                    symbolCache.put(transaction.symbol(), symbol);
                                }
                                Optional<Holding> symbolHolding = accountHoldings.stream()
                                        .filter(h -> h.symbol() == symbol)
                                        .findFirst();
                                Holding newHolding;
                                if (symbolHolding.isPresent()) {
                                    Holding holding = symbolHolding.get();
                                    accountHoldings.remove(holding);
                                    BigDecimal totalCost = holding.quantity().multiply(holding.averageCost());
                                    totalCost = totalCost.add(transaction.net());
                                    BigDecimal totalQuantity = holding.quantity().add(transaction.quantity());
                                    if (totalQuantity.compareTo(ZERO) > 0) {
                                        newHolding = Holding.builder()
                                                .symbol(symbol)
                                                .quantity(totalQuantity)
                                                .averageCost(totalCost.divide(totalQuantity, 8, RoundingMode.HALF_UP))
                                                .build();
                                        accountHoldings.add(newHolding);
                                    }
                                } else {
                                    newHolding = Holding.builder()
                                            .symbol(symbol)
                                            .quantity(transaction.quantity())
                                            .averageCost(transaction.net().divide(transaction.quantity(), 8, RoundingMode.HALF_UP))
                                            .build();
                                    accountHoldings.add(newHolding);
                                }
                                holdings.put(account, accountHoldings);

                                latestCash = latestCash.add(transaction.net());
                                break;
                            case JOURNAL:
                            case CORPORATE_ACTION:
                            default:
                                break;
                        }
                    }
                }

                for(Account account: accounts) {

                    repository.save(DailyReport.builder()
                            .date(instant)
                            .totalAmount(latestCash.add(valueHoldings(holdings.get(account), instant)))
                            .distributions(ZERO)
                            .capitalGains(ZERO)
                            .cashFlow(cashFlow)
                            .account(account)
                            .build());
                    log.info("Saved report for: {}", cursorDate);
                }

                cursorDate = cursorDate.plusDays(1);
            }
        }
    }

    @Override
    public List<DailyReport> findPastYear(Account account) {
        return repository.findAllByAccountAndDateAfter(account, Instant.now().minus(365, ChronoUnit.DAYS)); // Doesn't support YEARS
    }

    @Override
    public List<DailyReport> findAllPastYear() {
        return repository.findAllByDateAfter(Instant.now().minus(365, ChronoUnit.DAYS)); // Doesn't support YEARS
    }

    private BigDecimal valueHoldings(Collection<Holding> holdings, Instant date) {
        BigDecimal totalValue = ZERO;
        for (Holding holding: holdings) {
            BigDecimal fixingValue = fixingService.findNearest(new SymbolFixingKey(holding.symbol(), date))
                            .orElse(new SymbolFixing(null, holding.symbol().value(), date, ZERO))
                            .amount();
            BigDecimal holdingValue = holding.quantity().multiply(fixingValue);
            totalValue = totalValue.add(holdingValue);
        }

        return totalValue;
    }
}