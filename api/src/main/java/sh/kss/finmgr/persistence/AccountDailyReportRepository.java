package sh.kss.finmgr.persistence;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import sh.kss.finmgr.domain.AccountDailyReport;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.H2)
public interface AccountDailyReportRepository
        extends CrudRepository<AccountDailyReport, UUID> {

    List<AccountDailyReport> listOrderByDateAsc();
}