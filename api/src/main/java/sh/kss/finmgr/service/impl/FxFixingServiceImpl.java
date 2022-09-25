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
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.domain.CurrencyPair;
import sh.kss.finmgr.domain.FxFixing;
import sh.kss.finmgr.domain.FxFixingKey;
import sh.kss.finmgr.persistence.FxFixingRepository;
import sh.kss.finmgr.service.FxFixingService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
public class FxFixingServiceImpl implements FxFixingService {

    private static final Logger log = LoggerFactory.getLogger(FxFixingServiceImpl.class);

    private final FxFixingRepository repository;

    @SneakyThrows
    public FxFixingServiceImpl(FxFixingRepository repository) {
        this.repository = repository;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("fx_fixings/fx_2011-2022.csv")))) {
            log.info("Importing FxFixings");
            List<ResourceFixing> fixings = new CsvToBeanBuilder(reader)
                    .withType(ResourceFixing.class)
                    .withSeparator(',')
                    .withSkipLines(1)
                    .build()
                    .parse();

            List<FxFixing> fxFixings = fixings.stream()
                    .filter(fixing -> !fixing.getRate().equals("null"))
                    .map(this::toFxFixing)
                    .toList();

            repository.saveAll(fxFixings);
            log.info("Saved: " + fxFixings.size() + " fx rows");
        }

        stopWatch.stop();
        log.info("Imported FxFixings in: " + stopWatch.getTime(MILLISECONDS) + " milliseconds");
    }

    @Override
    public void saveAll(Collection<FxFixing> symbolFixings) {
        repository.saveAll(symbolFixings);
    }

    @Override
    public Optional<FxFixing> find(FxFixingKey key) {
        return repository.findByCurrencyPairAndDate(key.currencyPair().value(), key.date());
    }

    @Override
    public Optional<FxFixing> findNearest(FxFixingKey key) {
        return repository.findNearestQuote(key.currencyPair().value(), key.date());
    }

    @Data
    @NoArgsConstructor
    public static class ResourceFixing {
        @CsvBindByPosition(position = 0) String date;
        @CsvBindByPosition(position = 1) String rate;
    }

    private FxFixing toFxFixing(ResourceFixing fixing) {
        return FxFixing.builder()
                .currencyPair(CurrencyPair.USDCAD.value())
                .amount(new BigDecimal(fixing.getRate()))
                .date(Instant.parse(fixing.getDate() + "T00:00:00Z"))
                .build();
    }
}