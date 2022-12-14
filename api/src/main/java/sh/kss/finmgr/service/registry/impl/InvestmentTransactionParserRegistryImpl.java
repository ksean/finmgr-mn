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
package sh.kss.finmgr.service.registry.impl;

import jakarta.inject.Singleton;
import sh.kss.finmgr.service.parser.InvestmentTransactionParser;
import sh.kss.finmgr.service.parser.impl.QuestradeInvestmentTransactionParserImpl;
import sh.kss.finmgr.service.registry.InvestmentTransactionParserRegistry;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Singleton
public class InvestmentTransactionParserRegistryImpl implements InvestmentTransactionParserRegistry {

    Set<InvestmentTransactionParser> parsers = new HashSet<>();

    public InvestmentTransactionParserRegistryImpl() {
        this.register(new QuestradeInvestmentTransactionParserImpl());
    }

    @Override
    public void register(InvestmentTransactionParser investmentTransactionParser) {
        parsers.add(investmentTransactionParser);
    }

    @Override
    public Optional<InvestmentTransactionParser> findParser(String header) {
        return parsers.stream()
                .filter(p -> p.canConvert(header))
                .findFirst();
    }
}