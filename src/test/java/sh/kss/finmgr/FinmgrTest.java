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
package sh.kss.finmgr;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static sh.kss.finmgr.Currency.CAD;
import static sh.kss.finmgr.InvestmentAction.DEPOSIT;
import static sh.kss.finmgr.Symbol.EMPTY;

@MicronautTest
class FinmgrTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    AccountRepository accountRepository;

    @Inject
    InvestmentTransactionRepository investmentTransactionRepository;

    @Test
    void testItWorks() {
        assertTrue(application.isRunning());
    }

    @Test
    void testRepositorySave() {
        Account account = accountRepository.save(new Account(null, "Account id", "Account alias"));
        assertEquals(1, accountRepository.count());
        assertNotNull(account.id());

        InvestmentTransaction transaction = investmentTransactionRepository.save(
                InvestmentTransaction.builder()
                        .transactionDate(Instant.parse("2011-12-21T12:00:00.00Z"))
                        .settlementDate(Instant.parse("2011-12-21T12:00:00.00Z"))
                        .action(DEPOSIT)
                        .symbol(EMPTY)
                        .description("foo")
                        .quantity(ZERO)
                        .price(ZERO)
                        .gross(ZERO)
                        .commission(ZERO)
                        .net(new BigDecimal("1500"))
                        .currency(CAD)
                        .account(account)
                        .build());
        assertEquals(1, investmentTransactionRepository.count());
        assertNotNull(transaction.id());

        Optional<Account> retrievedAccount = accountRepository.findById(account.id());
        Optional<InvestmentTransaction> retrievedTransaction = investmentTransactionRepository.findById(transaction.id());

        assertTrue(retrievedAccount.isPresent());
        assertTrue(retrievedTransaction.isPresent());

        assertEquals(account, retrievedAccount.get());
        assertEquals(transaction, retrievedTransaction.get());
    }

}