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
package sh.kss.finmgr.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.data.annotation.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@MappedEntity
@Builder
@JsonInclude(ALWAYS)
public record DailyReport(
        @Id
        @AutoPopulated
        UUID id,
        Instant date,
        BigDecimal totalAmount,
        BigDecimal distributions,
        BigDecimal capitalGains,
        BigDecimal cashFlow,
        @Relation(value = Relation.Kind.MANY_TO_ONE)
        Account account
) implements Entity<UUID> {}