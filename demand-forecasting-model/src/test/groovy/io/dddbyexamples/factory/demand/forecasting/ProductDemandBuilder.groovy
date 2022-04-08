package io.dddbyexamples.factory.demand.forecasting

import io.dddbyexamples.factory.product.management.RefNoId

import java.time.*

import static ReviewRequired.ToReview

class ProductDemandBuilder {

    def refNo = "3009000"
    def demands = new DemandsFake(refNo, clock)
    def clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    DemandEvents events

    def demand(long ... levels) {
        def date = LocalDate.now(clock)
        for (long level : levels) {
            demands.demanded(date, level)
            date = date.plusDays(1)
        }
        this
    }

    def adjusted(Map<LocalDate, Long> adjustments) {
        adjustments.each { date, level ->
            demands.adjusted(date, level)
        }
        this
    }

    def stronglyAdjusted(Map<LocalDate, Long> adjustments) {
        adjustments.each { date, level ->
            demands.stronglyAdjusted(date, level)
        }
        this
    }

    def build() {
        new ProductDemand(new RefNoId(refNo), new ArrayList<>(), demands, clock, events)
    }

    def document(LocalDate date, long ... levels) {
        def created = date.atTime(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC)).toInstant()
        SortedMap<LocalDate, Demand> results = new TreeMap<>()
        for (def level : levels) {
            results.put(date, Demand.of(level))
            date = date.plusDays(1)
        }
        new Document(created, refNo, results)
    }

    def adjustDemand(Map<LocalDate, Long> adjustments) {
        Map<LocalDate, Adjustment> results = new HashMap<>()
        adjustments.forEach { date, level ->
            results.put(date, Adjustment.weak(Demand.of(level)))
        }
        new AdjustDemand(refNo, results)
    }

    def levelChanged(List<Long>... changes) {
        def date = LocalDate.now(clock)
        Map<DailyId, DemandedLevelsChanged.Change> results = new HashMap<>()
        for (def change : changes) {
            if (change.size() == 2) {
                results.put(new DailyId(refNo, date), new DemandedLevelsChanged.Change(
                        Demand.of(change[0]),
                        Demand.of(change[1])))
            } else if (!change.empty) throw new IllegalAccessException()
            date = date.plusDays(1)
        }
        new DemandedLevelsChanged(new RefNoId(refNo), results)
    }

    ReviewRequired reviewRequest(ToReview... reviews) {
        new ReviewRequired(new RefNoId(refNo), reviews as List)
    }

    ToReview review(LocalDate date,
                    long previousDocumented,
                    long strongAdjustment,
                    long newDocumented) {
        new ToReview(
                new DailyId(refNo, date),
                Demand.of(previousDocumented),
                Demand.of(strongAdjustment),
                Demand.of(newDocumented))
    }

    ApplyReviewDecision reviewDecision(ToReview review,
                                       ReviewDecision decision) {
        new ApplyReviewDecision(review, decision)
    }
}
