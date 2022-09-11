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

import sh.kss.finmgr.domain.Account;
import sh.kss.finmgr.domain.InvestmentTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static sh.kss.finmgr.domain.AccountType.NON_REGISTERED;
import static sh.kss.finmgr.domain.AccountType.TFSA;
import static sh.kss.finmgr.domain.Currency.CAD;
import static sh.kss.finmgr.domain.InvestmentAction.*;

public abstract class InvestmentTransactionTest {

    protected static final Instant DEC_21 = Instant.parse("2011-12-21T12:00:00.00Z");
    protected static final List<InvestmentTransaction> LATEST_TRANSACTIONS = List.of(
            createTransaction(),
            createTransaction(),
            createTransaction()
    );

    protected static InvestmentTransaction createTransaction() {
        return InvestmentTransaction.builder()
                .transactionDate(DEC_21)
                .settlementDate(DEC_21)
                .action(DEPOSIT)
                .symbol("")
                .description("deposit on dec 21")
                .quantity(ZERO)
                .price(ZERO)
                .commission(ZERO)
                .net(new BigDecimal("1500"))
                .currency(CAD)
                .build();
    }

    protected static final Account MARGIN_ACCOUNT = Account.builder()
            .accountType(NON_REGISTERED)
            .alias("Margin")
            .value("12345679")
            .build();

    protected static final Account TFSA_ACCOUNT = Account.builder()
            .accountType(TFSA)
            .alias("TFSA")
            .value("12345678")
            .build();

    protected static final List<InvestmentTransaction> TEST_TRANSACTIONS = List.of(
            InvestmentTransaction.builder()
                    .transactionDate(Instant.parse("2021-12-24T00:00:00Z"))
                    .settlementDate(Instant.parse("2021-12-24T00:00:00Z"))
                    .action(DEPOSIT)
                    .symbol("")
                    .description("CONT 1234567890")
                    .quantity(ZERO)
                    .price(ZERO)
                    .commission(ZERO)
                    .net(new BigDecimal(1500))
                    .currency(CAD)
                    .account(TFSA_ACCOUNT)
                    .build(),
            InvestmentTransaction.builder()
                    .transactionDate(Instant.parse("2021-12-24T00:00:00Z"))
                    .settlementDate(Instant.parse("2021-12-24T00:00:00Z"))
                    .action(DEPOSIT)
                    .symbol("")
                    .description("1234567891 CIBC DIR DEP")
                    .quantity(ZERO)
                    .price(ZERO)
                    .commission(ZERO)
                    .net(new BigDecimal(1500))
                    .currency(CAD)
                    .account(MARGIN_ACCOUNT)
                    .build(),
            InvestmentTransaction.builder()
                    .transactionDate(Instant.parse("2022-01-06T00:00:00Z"))
                    .settlementDate(Instant.parse("2022-01-11T00:00:00Z"))
                    .action(TRADE)
                    .symbol("BB")
                    .description("RESEARCH IN MOTION LTD AS AGENTS, WE HAVE BOUGHT OR SOLD FOR YOUR ACCOUNT")
                    .quantity(new BigDecimal(94))
                    .price(new BigDecimal("15.88"))
                    .commission(new BigDecimal("-5.28"))
                    .net(new BigDecimal(-1498))
                    .currency(CAD)
                    .account(MARGIN_ACCOUNT)
                    .build(),
            InvestmentTransaction.builder()
                    .transactionDate(Instant.parse("2022-01-12T00:00:00Z"))
                    .settlementDate(Instant.parse("2022-01-12T00:00:00Z"))
                    .action(FEE_OR_REBATE)
                    .symbol("")
                    .description("AFFLT REBATE")
                    .quantity(ZERO)
                    .price(ZERO)
                    .commission(ZERO)
                    .net(new BigDecimal("4.95"))
                    .currency(CAD)
                    .account(TFSA_ACCOUNT)
                    .build(),
            InvestmentTransaction.builder()
                    .transactionDate(Instant.parse("2022-01-12T00:00:00Z"))
                    .settlementDate(Instant.parse("2022-01-17T00:00:00Z"))
                    .action(TRADE)
                    .symbol("BB")
                    .description("RESEARCH IN MOTION LTD AS AGENTS, WE HAVE BOUGHT OR SOLD FOR YOUR ACCOUNT")
                    .quantity(new BigDecimal(-94))
                    .price(new BigDecimal("16.60"))
                    .commission(new BigDecimal("-5.28"))
                    .net(new BigDecimal("1555.12"))
                    .currency(CAD)
                    .account(MARGIN_ACCOUNT)
                    .build()
            );
}