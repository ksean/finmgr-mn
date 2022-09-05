package sh.kss.finmgr.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@MappedEntity
@Builder
@JsonInclude(ALWAYS)
public record AccountDailyReport (
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