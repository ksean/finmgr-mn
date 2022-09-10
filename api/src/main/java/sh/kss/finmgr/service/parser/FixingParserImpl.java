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
package sh.kss.finmgr.service.parser;

import sh.kss.finmgr.domain.Fixing;

import java.io.BufferedReader;
import java.util.Set;

public class FixingParserImpl implements FixingParser {

    @Override
    public boolean canConvert(String header) {
        String trimmed = header.trim().toLowerCase();
        return trimmed.equals("date,close") ||
                trimmed.equals("date,close/last");
    }

    @Override
    public Set<Fixing> parse(BufferedReader reader) {
        return Set.of();
    }
}