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
import lombok.SneakyThrows;
import sh.kss.finmgr.domain.Account;
import sh.kss.finmgr.domain.Fixing;
import sh.kss.finmgr.domain.InvestmentTransaction;
import sh.kss.finmgr.domain.SymbolFixing;
import sh.kss.finmgr.service.parser.FixingParser;
import sh.kss.finmgr.service.parser.InvestmentTransactionParser;
import sh.kss.finmgr.service.registry.FixingParserRegistry;
import sh.kss.finmgr.service.registry.InvestmentTransactionParserRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@AllArgsConstructor
public class FileImporterServiceImpl implements FileImporterService {

    private final InvestmentTransactionService transactionService;
    private final AccountService accountService;
    private final FixingService fixingService;
    private final InvestmentTransactionParserRegistry investmentTransactionParserRegistry;
    private final FixingParserRegistry fixingParserRegistry;


    @Override
    @SneakyThrows
    public void ingest(byte[] bytes, String filename) {
        File tempFile = File.createTempFile("temp", "temp");
        Path path = Paths.get(tempFile.getAbsolutePath());
        Files.write(path, bytes);
        try (FileReader fileReader = new FileReader(tempFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                bufferedReader.mark(1);
                String header = bufferedReader.readLine();
                bufferedReader.reset();
                Optional<InvestmentTransactionParser> investmentParser = investmentTransactionParserRegistry.findParser(header);
                if (investmentParser.isPresent()) {
                    saveTransactions(bufferedReader, investmentParser.get());
                } else {
                    Optional<FixingParser> fixingParser = fixingParserRegistry.findParser(header);
                    if (fixingParser.isPresent()) {
                        saveFixings(bufferedReader, fixingParser.get(), filename);
                    } else {
                        throw new RuntimeException("Cannot find parser match for header: " + header);
                    }
                }
            }
        }
    }

    private void saveTransactions(BufferedReader bufferedReader, InvestmentTransactionParser parser) {
            Map<Account, List<InvestmentTransaction>> transactions = parser.parse(bufferedReader);
            Map<String, Account> accountMap = new HashMap<>();
            accountService.findAll().forEach(
                    account -> accountMap.put(account.value(), account));
            List<InvestmentTransaction> transactionsToSave = new ArrayList<>();
            transactions.keySet().forEach(account -> {
                if (!accountMap.containsKey(account.value())) {
                    Account persistedAccount = accountService.save(account);
                    accountMap.put(account.value(), persistedAccount);
                }
                transactions.get(account).forEach(investmentTransaction -> {
                    transactionsToSave.add(investmentTransaction.withAccount(accountMap.get(account.value())));
                });
            });
            transactionService.saveAll(transactionsToSave);
    }

    private void saveFixings(BufferedReader bufferedReader, FixingParser parser, String symbol) {
        Set<Fixing> fixings = parser.parse(bufferedReader);
        Set<SymbolFixing> symbolFixings = fixings.stream()
                .map(fixing -> SymbolFixing.builder()
                        .symbol(symbol)
                        .date(fixing.date())
                        .amount(fixing.amount())
                        .build()
                )
                .collect(Collectors.toSet());
        fixingService.saveAll(symbolFixings);
    }
}