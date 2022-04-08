package io.dddbyexamples.factory.shortages.prediction.monitoring

import io.dddbyexamples.factory.shortages.prediction.Shortage
import io.dddbyexamples.factory.shortages.prediction.calculation.TimeGrammar
import spock.lang.Specification

import java.time.LocalDateTime

class ShortageDiffPolicySpec extends Specification {

    def now = LocalDateTime.now()

    void setup() {
        TimeGrammar.apply()
    }

    def "'ValuesAreEquals policy' ignores time when shortage was found"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now + 5.min)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        expect:
        !policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' ignores current locked stock"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 1000, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        expect:
        !policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' can NOT ignore product refNo"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000XXX", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        expect:
        policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' can NOT ignore shortage level diff"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 1500L)
                .build().orElse(null)

        expect:
        policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' even minimal level diff is distinguished"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 499L)
                .build().orElse(null)

        expect:
        policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' can NOT ignore shortage in different days"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now)
                .missing(now + 2.day, 500L)
                .build().orElse(null)

        expect:
        policy.areDifferent(one, another)
    }

    def "'ValuesAreEquals policy' can NOT ignore shortage in different time"() {
        given:
        def policy = ShortageDiffPolicy.ValuesAreNotSame

        Shortage one = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day, 500L)
                .build().orElse(null)

        Shortage another = Shortage.builder("3009000", 0, now)
                .missing(now + 1.day + 1.min, 500L)
                .build().orElse(null)

        expect:
        policy.areDifferent(one, another)
    }
}
