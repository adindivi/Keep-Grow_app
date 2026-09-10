package com.example

import com.example.domain.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FinanceCalculator domain business logic.
 * Covers normal cases, edge cases (empty lists, single elements, zero base values),
 * and boundary conditions.
 */
class ExampleUnitTest {

    // ==========================================
    // 1. calculateMonthlyUpProbability Tests
    // ==========================================

    @Test
    fun calculateMonthlyUpProbability_withConsistentUpwardTrend_returnsHighProbability() {
        // Given 81 trading days of data with steady price growth every 20 days
        // Day 0: 100, Day 20: 110, Day 40: 120, Day 60: 130, Day 80: 140
        val closeList = (0..80).map { i -> 100.0 + i * 0.5 }

        // When evaluating for 3 months (3 intervals of 20 days)
        val prob = FinanceCalculator.calculateMonthlyUpProbability(closeList, 3)

        // Then all 3 intervals had price increase -> probability should be 1.0 (100%)
        assertEquals(1.0, prob, 0.001)
    }

    @Test
    fun calculateMonthlyUpProbability_withDownwardTrend_returnsZero() {
        // Given 81 trading days with continuous drop
        val closeList = (0..80).map { i -> 200.0 - i * 1.0 }

        val prob = FinanceCalculator.calculateMonthlyUpProbability(closeList, 3)

        assertEquals(0.0, prob, 0.001)
    }

    @Test
    fun calculateMonthlyUpProbability_withEmptyList_returnsZero() {
        val prob = FinanceCalculator.calculateMonthlyUpProbability(emptyList(), 3)
        assertEquals(0.0, prob, 0.001)
    }

    @Test
    fun calculateMonthlyUpProbability_withInsufficientData_returnsZero() {
        // Less than 2 elements
        val prob = FinanceCalculator.calculateMonthlyUpProbability(listOf(100.0), 3)
        assertEquals(0.0, prob, 0.001)
    }

    @Test
    fun calculateMonthlyUpProbability_withZeroOrNegativeMonths_returnsZero() {
        val closeList = (0..80).map { 100.0 + it }
        val probZero = FinanceCalculator.calculateMonthlyUpProbability(closeList, 0)
        val probNeg = FinanceCalculator.calculateMonthlyUpProbability(closeList, -2)

        assertEquals(0.0, probZero, 0.001)
        assertEquals(0.0, probNeg, 0.001)
    }

    // ==========================================
    // 2. calculatePeriodReturn Tests
    // ==========================================

    @Test
    fun calculatePeriodReturn_withValidPrices_calculatesCorrectPercentage() {
        // 61 days: start at index (61 - 20 - 1) = 40, end at index 60
        val closeList = MutableList(61) { 100.0 }
        closeList[40] = 1000.0 // start price
        closeList[60] = 1200.0 // latest price (+20%)

        val returnRate = FinanceCalculator.calculatePeriodReturn(closeList, 1)

        assertEquals(20.0, returnRate, 0.01)
    }

    @Test
    fun calculatePeriodReturn_withLoss_calculatesNegativePercentage() {
        val closeList = MutableList(61) { 100.0 }
        closeList[40] = 1000.0
        closeList[60] = 850.0 // -15%

        val returnRate = FinanceCalculator.calculatePeriodReturn(closeList, 1)

        assertEquals(-15.0, returnRate, 0.01)
    }

    @Test
    fun calculatePeriodReturn_withEmptyList_returnsZero() {
        val returnRate = FinanceCalculator.calculatePeriodReturn(emptyList(), 3)
        assertEquals(0.0, returnRate, 0.001)
    }

    @Test
    fun calculatePeriodReturn_withZeroBasePrice_avoidsDivisionByZero() {
        val closeList = listOf(0.0, 100.0)
        val returnRate = FinanceCalculator.calculatePeriodReturn(closeList, 1)
        assertEquals(0.0, returnRate, 0.001)
    }

    // ==========================================
    // 3. calculateTotalAssets & calculateDailyChangePercentage Tests
    // ==========================================

    @Test
    fun calculateTotalAssets_sumsHoldingsAndCashCorrectly() {
        val total = FinanceCalculator.calculateTotalAssets(1_500_000.0, 500_000.0)
        assertEquals(2_000_000.0, total, 0.01)
    }

    @Test
    fun calculateTotalAssets_withNegativeOrNaN_handlesGracefully() {
        val totalWithNaN = FinanceCalculator.calculateTotalAssets(Double.NaN, 500_000.0)
        assertEquals(500_000.0, totalWithNaN, 0.01)

        val totalWithNeg = FinanceCalculator.calculateTotalAssets(-100.0, 300_000.0)
        assertEquals(300_000.0, totalWithNeg, 0.01)
    }

    @Test
    fun calculateDailyChangePercentage_withNormalValues_calculatesGain() {
        val change = FinanceCalculator.calculateDailyChangePercentage(1_100_000.0, 1_000_000.0)
        assertEquals(10.0, change, 0.01)
    }

    @Test
    fun calculateDailyChangePercentage_withZeroPreviousValue_avoidsDivisionByZero() {
        val change = FinanceCalculator.calculateDailyChangePercentage(1_000_000.0, 0.0)
        assertEquals(0.0, change, 0.001)
    }

    // ==========================================
    // 4. calculateDailyReturnsCsv Tests
    // ==========================================

    @Test
    fun calculateDailyReturnsCsv_padsToRequestedTargetDays() {
        val closeList = listOf(100.0, 105.0, 102.0)
        val csv = FinanceCalculator.calculateDailyReturnsCsv(closeList, 5)
        val items = csv.split(",")

        assertEquals(5, items.size)
        // Last item was 105 -> 102 (-2.86%)
        assertTrue(items.last().toDouble() < 0)
    }
}
