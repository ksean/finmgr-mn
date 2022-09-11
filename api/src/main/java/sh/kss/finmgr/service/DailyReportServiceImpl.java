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
package sh.kss.finmgr.service;


import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import sh.kss.finmgr.domain.*;
import sh.kss.finmgr.persistence.DailyReportRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Singleton
@AllArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {

    private final DailyReportRepository repository;
    private final InvestmentTransactionService investmentTransactionService;
    private final FixingService fixingService;
    private static final ZoneId zoneId = ZoneId.of("America/Toronto");



    @Override
    public void refresh() {
        repository.deleteAll();

        List<InvestmentTransaction> transactions = investmentTransactionService.findAll();
        if (transactions.size() > 0) {
            BigDecimal latestCash = BigDecimal.ZERO;
            Set<Account> accounts = transactions.stream()
                    .map(InvestmentTransaction::account)
                    .collect(toSet());

            Instant start = transactions.get(0).transactionDate();
            Instant end = transactions.get(transactions.size() - 1).transactionDate();

            LocalDate startDate = LocalDate.ofInstant(start, zoneId);
            LocalDate endDate = LocalDate.ofInstant(end, zoneId);

            Map<LocalDate, List<InvestmentTransaction>> transactionsByDate = transactions.stream()
                    .collect(Collectors.groupingBy(transaction -> LocalDate.ofInstant(transaction.transactionDate(), zoneId)));

            LocalDate cursorDate = startDate;
            while (cursorDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                BigDecimal cashFlow = BigDecimal.ZERO;
                Instant instant = cursorDate.atStartOfDay(zoneId).toInstant();
                if (transactionsByDate.containsKey(cursorDate)) {
                    for (InvestmentTransaction transaction: transactionsByDate.get(cursorDate)) {
                        switch (transaction.action()) {
                            case WITHDRAWAL:
                            case DEPOSIT:
                                cashFlow = cashFlow.add(transaction.net());
                            case DISTRIBUTION:
                            case FEE_OR_REBATE:
                                latestCash = latestCash.add(transaction.net());
                                break;
                            case TRADE:
                            case JOURNAL:
                            case FX_CONVERSION:
                            case CORPORATE_ACTION:
                            case REINVESTMENT:
                            default:
                                break;
                        }
                    }
                }

                for(Account account: accounts) {

                    repository.save(DailyReport.builder()
                            .date(instant)
                            .totalAmount(latestCash)
                            .distributions(BigDecimal.ZERO)
                            .capitalGains(BigDecimal.ZERO)
                            .cashFlow(cashFlow)
                            .account(account)
                            .build());
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
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Holding holding: holdings) {
            BigDecimal fixingValue = fixingService.findNearest(new SymbolFixingKey(holding.symbol(), date))
                            .orElse(new SymbolFixing(null, holding.symbol().value(), date, BigDecimal.ZERO))
                            .amount();
            BigDecimal holdingValue = holding.amount().multiply(fixingValue);
            totalValue = totalValue.add(holdingValue);
        }

        return totalValue;
    }
}