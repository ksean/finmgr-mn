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
import sh.kss.finmgr.service.*;
import sh.kss.finmgr.service.registry.FixingParserRegistry;
import sh.kss.finmgr.service.registry.InvestmentTransactionParserRegistry;

import static org.mockito.Mockito.mock;

@MicronautTest
public class FileImporterServiceImplTest {

    InvestmentTransactionService transactionService = mock(InvestmentTransactionService.class);
    AccountService accountService = mock(AccountService.class);
    FixingService fixingService = mock(FixingService.class);
    InvestmentTransactionParserRegistry investmentTransactionParserRegistry = mock(InvestmentTransactionParserRegistry.class);
    FixingParserRegistry fixingParserRegistry = mock(FixingParserRegistry.class);
    DailyReportService dailyReportService = mock(DailyReportService.class);

    FileImporterService service = new FileImporterServiceImpl(
            transactionService,
            accountService,
            fixingService,
            investmentTransactionParserRegistry,
            fixingParserRegistry,
            dailyReportService
    );


}