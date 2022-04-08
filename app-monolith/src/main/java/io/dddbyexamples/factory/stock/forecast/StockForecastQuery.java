package io.dddbyexamples.factory.stock.forecast;

import io.dddbyexamples.factory.demand.forecasting.projection.CurrentDemandDao;
import io.dddbyexamples.factory.demand.forecasting.projection.CurrentDemandEntity;
import io.dddbyexamples.factory.production.planning.projection.ProductionDailyOutputDao;
import io.dddbyexamples.factory.production.planning.projection.ProductionDailyOutputEntity;
import io.dddbyexamples.factory.shortages.prediction.calculation.Stock;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.dddbyexamples.factory.product.management.RefNoId;
import io.dddbyexamples.factory.stock.forecast.StockForecast.StockForecastBuilder;
import io.dddbyexamples.factory.warehouse.WarehouseService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@Transactional(readOnly = true)
@AllArgsConstructor
public class StockForecastQuery {

    private final WarehouseService stocks;
    private final CurrentDemandDao demands;
    private final ProductionDailyOutputDao outputs;
    private final Clock clock;

    public StockForecast get(RefNoId refNo) {
        Stock stock = stocks.forRefNo(refNo);
        LocalDate today = LocalDate.now(clock);
        return build(today, stock,
                this.demands
                        .findByRefNoAndDateGreaterThanEqual(refNo.getRefNo(), today).stream()
                        .collect(toMap(
                                CurrentDemandEntity::getDate,
                                CurrentDemandEntity::getLevel
                        )),
                this.outputs
                        .findByRefNoAndDateGreaterThanEqual(refNo.getRefNo(), today).stream()
                        .collect(toMap(
                                ProductionDailyOutputEntity::getDate,
                                ProductionDailyOutputEntity::getOutput
                        ))
        );
    }

    private StockForecast build(LocalDate today,
                                Stock stock,
                                Map<LocalDate, Long> demands,
                                Map<LocalDate, Long> outputs) {
        LocalDate stopAtDay = today.plusDays(15);
        long level = stock.getLevel();
        StockForecastBuilder builder = StockForecast.builder();
        for (LocalDate date = today; date.isBefore(stopAtDay); date = date.plusDays(1)) {
            long withLocked = level + stock.getLocked();
            long demand = demands.getOrDefault(date, 0L);
            long output = outputs.getOrDefault(date, 0L);
            builder.forecast(
                    new StockForecast.DailyForecast(
                            date,
                            level,
                            withLocked,
                            demand,
                            output
                    )
            );
            level = level - demand + output;
        }
        return builder.build();
    }
}
