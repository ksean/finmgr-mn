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
package sh.kss.finmgr.service.impl;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.domain.SymbolFixing;
import sh.kss.finmgr.domain.SymbolFixingKey;
import sh.kss.finmgr.persistence.SymbolFixingRepository;
import sh.kss.finmgr.service.SymbolFixingService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
public class SymbolFixingServiceImpl implements SymbolFixingService {

    private static final Logger log = LoggerFactory.getLogger(SymbolFixingServiceImpl.class);

    private final SymbolFixingRepository repository;

    @SneakyThrows
    public SymbolFixingServiceImpl(SymbolFixingRepository repository) {
        this.repository = repository;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Arrays.stream(getFixingFiles())
                .parallel()
                .forEach(file -> {
                    String symbolStr = file.getName();
                    log.info("Importing SymbolFixing file: {}", symbolStr);
                    try (FileReader reader = new FileReader(file)) {
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
                        log.info("Saved: " + symbolFixings.size() + " " + symbolStr + " fixing rows");
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                });

        stopWatch.stop();
        log.info("Imported SymbolFixings in: " + stopWatch.getTime(MILLISECONDS) + " milliseconds");
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
        URL url = ClassLoader.getSystemResource("close_fixings");
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