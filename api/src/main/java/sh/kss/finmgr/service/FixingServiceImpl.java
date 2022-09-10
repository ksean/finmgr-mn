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

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.inject.Singleton;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.api.InvestmentTransactionController;
import sh.kss.finmgr.domain.Fixing;
import sh.kss.finmgr.domain.Symbol;
import sh.kss.finmgr.domain.SymbolFixing;
import sh.kss.finmgr.domain.SymbolFixingKey;
import sh.kss.finmgr.persistence.SymbolFixingRepository;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.*;

@Singleton
public class FixingServiceImpl implements FixingService {

    private static final Logger log = LoggerFactory.getLogger(FixingServiceImpl.class);

    private final SymbolFixingRepository repository;

    @SneakyThrows
    public FixingServiceImpl(SymbolFixingRepository repository) {
        this.repository = repository;

        for (File file: getFixingFiles()) {
            String symbolStr = file.getName();
            log.info("Importing resource file: {}", symbolStr);
            FileReader reader = new FileReader(file);
            List<ResourceFixing> fixings = new CsvToBeanBuilder(reader)
                    .withType(ResourceFixing.class)
                    .withSeparator(',')
                    .withSkipLines(1)
                    .build()
                    .parse();

            List<SymbolFixing> symbolFixings = fixings.stream()
                    .filter(fixing -> !fixing.price.equals("null"))
                    .map(fixing -> toSymbolFixing(symbolStr, fixing))
                    .toList();

            repository.saveAll(symbolFixings);
        }
    }

    @Override
    public void saveAll(Collection<SymbolFixing> symbolFixings) {
        repository.saveAll(symbolFixings);
    }

    @Override
    public Optional<SymbolFixing> find(SymbolFixingKey key) {
        return repository.findBySymbolAndDate(key.symbol().value(), key.date());
    }

    @Override
    public Optional<SymbolFixing> findNearest(SymbolFixingKey key) {
        return repository.findNearestQuote(key.symbol().value(), key.date());
    }

    private static File[] getFixingFiles() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("fixings");
        assert url != null;

        return new File(url.getPath()).listFiles();
    }

    @Data
    @NoArgsConstructor
    public static class ResourceFixing {
        @CsvBindByPosition(position = 0) String date;
        @CsvBindByPosition(position = 1) String price;
    }

    private SymbolFixing toSymbolFixing(String symbol, ResourceFixing fixing) {
        String date = fixing.getDate();
        if (date.contains("/")) {
            String[] dateParts = date.split("/");
            date = StringUtils.joinWith("-", dateParts[2], dateParts[0], dateParts[1]);
        }
        return SymbolFixing.builder()
                .symbol(StringUtils.substringBefore(symbol, "."))
                .amount(new BigDecimal(fixing.getPrice().replace("$", "")))
                .date(Instant.parse(date + "T00:00:00Z"))
                .build();
    }
}