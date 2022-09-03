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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static sh.kss.finmgr.Currency.CAD;
import static sh.kss.finmgr.InvestmentAction.DEPOSIT;
import static sh.kss.finmgr.Symbol.EMPTY;

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
                .symbol(EMPTY)
                .description("deposit on dec 21")
                .quantity(ZERO)
                .price(ZERO)
                .gross(ZERO)
                .commission(ZERO)
                .net(new BigDecimal("1500"))
                .currency(CAD)
                .build();
    }
}