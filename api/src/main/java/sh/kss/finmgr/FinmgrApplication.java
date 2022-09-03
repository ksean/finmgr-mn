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

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import sh.kss.finmgr.domain.Account;
import sh.kss.finmgr.domain.Currency;
import sh.kss.finmgr.domain.InvestmentAction;
import sh.kss.finmgr.domain.InvestmentTransaction;
import sh.kss.finmgr.persistence.AccountRepository;
import sh.kss.finmgr.persistence.InvestmentTransactionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

import static sh.kss.finmgr.domain.Currency.CAD;
import static sh.kss.finmgr.domain.InvestmentAction.DEPOSIT;

@Singleton
public class FinmgrApplication {

    private final InvestmentTransactionRepository investmentTransactionRepository;
    private final AccountRepository accountRepository;

    public FinmgrApplication(InvestmentTransactionRepository investmentTransactionRepository, AccountRepository accountRepository) {
        this.investmentTransactionRepository = investmentTransactionRepository;
        this.accountRepository = accountRepository;
    }

    public static void main(String[] args) {
        Micronaut.run(FinmgrApplication.class, args);
    }

    @EventListener
    @Transactional
    void startup(StartupEvent startupEvent) {
        Account account = accountRepository.save(
                Account.builder()
                        .value("accountid")
                        .alias("alias")
                        .build());

        investmentTransactionRepository.save(
                InvestmentTransaction.builder()
                        .transactionDate(Instant.now())
                        .settlementDate(Instant.now())
                        .action(DEPOSIT)
                        .description("Deposited some money")
                        .net(new BigDecimal("1500"))
                        .currency(CAD)
                        .account(account)
                        .build()
        );
    }
}