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
package sh.kss.finmgr.persistence.converter;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;
import sh.kss.finmgr.domain.Symbol;

import static sh.kss.finmgr.domain.Symbol.EMPTY;

@Singleton
public class SymbolConverter implements AttributeConverter<Symbol, String> {
    @Override
    public String convertToPersistedValue(Symbol symbol, ConversionContext context) {
        return symbol == null ? EMPTY.value() : symbol.value();
    }

    @Override
    public Symbol convertToEntityValue(String value, ConversionContext context) {
        return value == null ? EMPTY : new Symbol(value);
    }
}