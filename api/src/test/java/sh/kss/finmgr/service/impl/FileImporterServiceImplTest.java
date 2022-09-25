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

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sh.kss.finmgr.InvestmentTransactionTest;
import sh.kss.finmgr.domain.InvestmentTransaction;
import sh.kss.finmgr.service.*;
import sh.kss.finmgr.service.parser.InvestmentTransactionParser;
import sh.kss.finmgr.service.registry.FixingParserRegistry;
import sh.kss.finmgr.service.registry.InvestmentTransactionParserRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MicronautTest
public class FileImporterServiceImplTest extends InvestmentTransactionTest {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    InvestmentTransactionService transactionService = mock(InvestmentTransactionService.class);
    AccountService accountService = mock(AccountService.class);
    SymbolFixingService symbolFixingService = mock(SymbolFixingService.class);
    InvestmentTransactionParserRegistry investmentTransactionParserRegistry = mock(InvestmentTransactionParserRegistry.class);
    FixingParserRegistry fixingParserRegistry = mock(FixingParserRegistry.class);
    DailyReportService dailyReportService = mock(DailyReportService.class);
    InvestmentTransactionParser investmentTransactionParser = mock(InvestmentTransactionParser.class);

    FileImporterService service = new FileImporterServiceImpl(
            transactionService,
            accountService,
            symbolFixingService,
            investmentTransactionParserRegistry,
            fixingParserRegistry,
            dailyReportService
    );

    @Test
    void canImportQuestradeCsv() throws IOException {

        when(investmentTransactionParserRegistry.findParser(anyString()))
                .thenReturn(Optional.of(investmentTransactionParser));
        when(accountService.findAll())
                .thenReturn(Set.of());
        when(investmentTransactionParser.parse(isA(BufferedReader.class)))
                .thenReturn(Map.of(
                        MARGIN_ACCOUNT, TEST_TRANSACTIONS.stream().filter(t -> t.account() == MARGIN_ACCOUNT).toList(),
                        TFSA_ACCOUNT, TEST_TRANSACTIONS.stream().filter(t -> t.account() == TFSA_ACCOUNT).toList()
                ));
        when(accountService.save(MARGIN_ACCOUNT))
                .thenReturn(MARGIN_ACCOUNT.withId(null));
        when(accountService.save(TFSA_ACCOUNT))
                .thenReturn(TFSA_ACCOUNT.withId(null));

        String filename = "test_transactions.csv";
        try(InputStream inputStream = classLoader.getResourceAsStream(filename)) {
            assert inputStream != null;
            service.ingest(inputStream.readAllBytes(), filename);
        }

        ArgumentCaptor<Collection<InvestmentTransaction>> transactionCaptor = ArgumentCaptor.forClass((Class<ArrayList<InvestmentTransaction>>)(Class)ArrayList.class);
        verify(accountService).save(MARGIN_ACCOUNT);
        verify(accountService).save(TFSA_ACCOUNT);
        verify(investmentTransactionParserRegistry).findParser(isA(String.class));
        verify(transactionService).saveAll(transactionCaptor.capture());
        verify(accountService).findAll();
        assertTrue(transactionCaptor.getValue().containsAll(TEST_TRANSACTIONS));
        assertEquals(
                5,
                transactionCaptor.getValue().size());
        verify(dailyReportService).refresh();
        verifyNoMoreInteractions(
                transactionService,
                accountService,
                symbolFixingService,
                investmentTransactionParserRegistry,
                fixingParserRegistry,
                dailyReportService
        );
    }

    @Test
    void canImportFixingCsv() {

    }
}