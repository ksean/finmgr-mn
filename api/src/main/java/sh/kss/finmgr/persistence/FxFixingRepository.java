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
package sh.kss.finmgr.persistence;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import sh.kss.finmgr.domain.FxFixing;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.H2)
public interface FxFixingRepository
        extends CrudRepository<FxFixing, UUID> {

    Optional<FxFixing> findByCurrencyPairAndDate(String currencyPair, Instant date);

    @Query("SELECT * FROM fx_fixing fx WHERE fx.currency_pair = :currencyPair AND fx.date <= :date ORDER BY fx.date DESC LIMIT 1")
    Optional<FxFixing> findNearestQuote(String currencyPair, Instant date);
}