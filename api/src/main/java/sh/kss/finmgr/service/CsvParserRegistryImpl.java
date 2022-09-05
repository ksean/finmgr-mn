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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Singleton
public class CsvParserRegistryImpl implements CsvParserRegistry{

    Set<CsvParser> parsers = new HashSet<>();

    public CsvParserRegistryImpl() {
        this.register(new QuestradeCsvParserImpl());
    }

    @Override
    public void register(CsvParser csvParser) {
        parsers.add(csvParser);
    }

    @Override
    public Optional<CsvParser> findParser(String header) {
        return parsers.stream()
                .filter(p -> p.canConvert(header))
                .findFirst();
    }
}