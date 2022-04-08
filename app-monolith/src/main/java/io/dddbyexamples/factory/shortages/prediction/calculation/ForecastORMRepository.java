package io.dddbyexamples.factory.shortages.prediction.calculation;

import io.dddbyexamples.factory.production.planning.projection.ProductionOutputDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import io.dddbyexamples.factory.delivery.planning.projection.DeliveryForecastDao;
import io.dddbyexamples.factory.delivery.planning.projection.DeliveryForecastEntity;
import io.dddbyexamples.factory.product.management.RefNoId;
import io.dddbyexamples.factory.shortages.prediction.calculation.ProductionForecast.Item;
import io.dddbyexamples.factory.warehouse.WarehouseService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
@AllArgsConstructor
class ForecastORMRepository implements ShortageForecasts {

    private final WarehouseService stocks;
    private final DeliveryForecastDao deliveries;
    private final ProductionOutputDao outputs;
    private final Clock clock;

    @Override
    public ShortageForecast get(RefNoId refNo, int daysAhead) {
        Stock stock = stocks.forRefNo(refNo);
        LocalDateTime time = LocalDateTime.now(clock);
        LocalDateTime max = time.plusDays(daysAhead).truncatedTo(ChronoUnit.DAYS);

        Map<LocalDateTime, Long> deliveries = this.deliveries
                .findByRefNoAndTimeBetween(refNo.getRefNo(), time, max).stream()
                .collect(toMap(
                        DeliveryForecastEntity::getTime,
                        DeliveryForecastEntity::getLevel
                ));
        SortedSet<LocalDateTime> deliveryTimes = new TreeSet<>(deliveries.keySet());

        DeliveriesForecast demand = new DeliveriesForecast(deliveries);

        ProductionOutputs outputs = new ProductionForecast(
                this.outputs.findByRefNoAndEndGreaterThanAndStartLessThan(refNo.getRefNo(), time, max).stream()
                        .map(e -> new Item(
                                e.getStart(),
                                e.getDuration(),
                                e.getPartsPerMinute()))
                        .collect(Collectors.toList())
        ).outputsInTimes(time, deliveryTimes);

        return new ShortageForecast(refNo.getRefNo(), time, deliveryTimes, stock, outputs, demand);
    }
}
