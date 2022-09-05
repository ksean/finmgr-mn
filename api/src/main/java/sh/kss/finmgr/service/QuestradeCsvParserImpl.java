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

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;
import sh.kss.finmgr.domain.*;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static sh.kss.finmgr.domain.InvestmentAction.*;

@Singleton
public class QuestradeCsvParserImpl implements CsvParser {

    private final Map<String, Account> knownAccountIds = new HashMap<>();
    private final Map<String, Symbol> knownSymbols = new HashMap<>();

    @Override
    public boolean canConvert(String header) {
        return header.trim().equals("TransactionDate,SettlementDate,Action,Symbol,Description,Quantity,Price,Gross,Commission,NetAmount,Currency,Account #,ActivityType,Account Type");
    }

    @Override
    public Map<Account, List<InvestmentTransaction>> parse(BufferedReader reader) {
        List<QuestradeInvestmentTransaction> questradeTransactions = new CsvToBeanBuilder(reader)
                .withType(QuestradeInvestmentTransaction.class)
                .withSeparator(',')
                .build()
                .parse();

        return questradeTransactions.stream()
                .map(this::convert)
                .collect(groupingBy(InvestmentTransaction::account));

    }

    private InvestmentTransaction convert(QuestradeInvestmentTransaction questradeTransaction) {
        return InvestmentTransaction.builder()
                .transactionDate(Instant.parse(formatDate(questradeTransaction.getTransactionDate())))
                .settlementDate(Instant.parse(formatDate(questradeTransaction.getSettlementDate())))
                .action(parseAction(questradeTransaction.getActivityType()))
                .symbol(parseSymbol(questradeTransaction.getSymbol()))
                .description(questradeTransaction.getDescription())
                .quantity(new BigDecimal(questradeTransaction.getQuantity()))
                .price(new BigDecimal(currencyToNumber(questradeTransaction.getPrice())))
                .net(new BigDecimal(currencyToNumber(questradeTransaction.getNet())))
                .currency(Currency.valueOf(questradeTransaction.getCurrency()))
                .account(parseAccount(questradeTransaction.getAccountId(), questradeTransaction.getAccountType()))
                .build();
    }

    private String formatDate(String date) {
        return date.substring(0, 10) + "T00:00:00Z";
    }

    private String currencyToNumber(String currency) {
        return currency.replaceAll("[$,]", "");
    }

    private Symbol parseSymbol(String symbolStr) {
        if (knownSymbols.containsKey(symbolStr)) {
            return knownSymbols.get(symbolStr);
        } else {
            Symbol symbol = new Symbol(symbolStr);
            knownSymbols.put(symbolStr, symbol);

            return symbol;
        }
    }

    private Account parseAccount(String accountId, String accountType) {
        if (knownAccountIds.containsKey(accountId)) {
            return knownAccountIds.get(accountId);
        } else {
            Account account = Account.builder()
                    .value(accountId)
                    .alias(accountId)
                    .accountType(parseAccountType(accountType))
                    .build();
            knownAccountIds.put(accountId, account);

            return account;
        }
    }

    private AccountType parseAccountType(String accountType) {
        return switch (accountType) {
            case "Individual TFSA" -> AccountType.TFSA;
            case "Individual RRSP" -> AccountType.RRSP;
            default -> AccountType.NON_REGISTERED;
        };
    }

    private InvestmentAction parseAction(String activityType) {
        return switch (activityType) {
            case "Deposits" -> DEPOSIT;
            case "Trades" -> TRADE;
            case "Interest", "Fees and rebates" -> FEE_OR_REBATE;
            case "FX conversion" -> FX_CONVERSION;
            case "Corporate actions" -> CORPORATE_ACTION;
            case "Dividend reinvestment" -> REINVESTMENT;
            case "Dividends" -> DISTRIBUTION;
            case "Other" -> JOURNAL;
            case "Withdrawals" -> WITHDRAWAL;
            default -> throw new RuntimeException("Unexpected activityType: " + activityType);
        };
    }

    @Data
    @NoArgsConstructor
    public static class QuestradeInvestmentTransaction {
        @CsvBindByName(column = "TransactionDate", required = true)
        String transactionDate;
        @CsvBindByName(column = "SettlementDate", required = true)
        String settlementDate;
        @CsvBindByName(column = "Action")
        String action;
        @CsvBindByName(column = "Symbol")
        String symbol;
        @CsvBindByName(column = "Description", required = true)
        String description;
        @CsvBindByName(column = "Quantity", required = true)
        String quantity;
        @CsvBindByName(column = "Price", required = true)
        String price;
        @CsvBindByName(column = "Gross")
        String gross;
        @CsvBindByName(column = "Commission")
        String commission;
        @CsvBindByName(column = "NetAmount", required = true)
        String net;
        @CsvBindByName(column = "Currency", required = true)
        String currency;
        @CsvBindByName(column = "Account #", required = true)
        String accountId;
        @CsvBindByName(column = "ActivityType", required = true)
        String activityType;
        @CsvBindByName(column = "Account Type", required = true)
        String accountType;
    }
}