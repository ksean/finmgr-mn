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
import sh.kss.finmgr.domain.InvestmentTransaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Singleton
@AllArgsConstructor
public class FileImporterServiceImpl implements FileImporterService {

    private final InvestmentTransactionService transactionService;
    private final AccountService accountService;
    private final CsvParserRegistry csvParserRegistry;


    @Override
    @SneakyThrows
    public void ingest(byte[] bytes) {
        File tempFile = File.createTempFile("temp", "temp");
        Path path = Paths.get(tempFile.getAbsolutePath());
        Files.write(path, bytes);
        try (FileReader fileReader = new FileReader(tempFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                bufferedReader.mark(1);
                String header = bufferedReader.readLine();
                bufferedReader.reset();
                Optional<CsvParser> parser = csvParserRegistry.findParser(header);
                if (parser.isPresent()) {
                    Map<Account, List<InvestmentTransaction>> transactions = parser.get().parse(bufferedReader);
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
                } else {
                    throw new RuntimeException("Cannot find parser match for header: " + header);
                }
            }
        }
    }
}