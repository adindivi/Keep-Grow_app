package com.example.domain

/**
 * Pure domain calculation logic for Keep & Grow.
 * Follows Single Responsibility Principle (SRP) and provides deterministic, easily testable functions.
 */
object FinanceCalculator {

    /**
     * Calculates the probability of price increase over a given number of 20-trading-day intervals.
     *
     * @param closeList Chronological list of closing prices (oldest to newest).
     * @param months Number of months (each considered 20 trading days).
     * @return Probability between 0.0 and 1.0. Returns 0.0 if not enough data.
     */
    fun calculateMonthlyUpProbability(closeList: List<Double>, months: Int): Double {
        if (closeList.size < 2 || months <= 0) return 0.0
        var upCount = 0
        var totalIntervals = 0
        val n = closeList.size

        for (k in 1..months) {
            val startIdx = n - 1 - (k * 20)
            val endIdx = n - 1 - ((k - 1) * 20)
            if (startIdx in 0 until n && endIdx in 0 until n) {
                val startVal = closeList[startIdx]
                val endVal = closeList[endIdx]
                if (startVal > 0 && endVal > 0) {
                    totalIntervals++
                    if (endVal > startVal) {
                        upCount++
                    }
                }
            }
        }
        return if (totalIntervals > 0) upCount.toDouble() / totalIntervals else 0.0
    }

    /**
     * Calculates the return rate (%) over a specified number of months (20 trading days each).
     *
     * @param closeList Chronological list of closing prices.
     * @param months Number of months to look back.
     * @return Percentage return (e.g. +12.5 for 12.5%). Returns 0.0 if empty or start value <= 0.
     */
    fun calculatePeriodReturn(closeList: List<Double>, months: Int): Double {
        if (closeList.isEmpty() || months <= 0) return 0.0
        val days = months * 20
        val startIdx = (closeList.size - days - 1).coerceAtLeast(0)
        val startVal = closeList[startIdx]
        val latestVal = closeList.last()
        return if (startVal > 0) {
            ((latestVal - startVal) / startVal) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Calculates daily returns for recent trading days and formats as CSV string.
     */
    fun calculateDailyReturnsCsv(closeList: List<Double>, targetDays: Int = 30): String {
        val dailyReturns = mutableListOf<Double>()
        if (closeList.size >= 2) {
            val startIdx = (closeList.size - targetDays - 1).coerceAtLeast(0)
            for (i in startIdx + 1 until closeList.size) {
                val todayClose = closeList[i]
                val prevCloseVal = closeList[i - 1]
                if (prevCloseVal > 0) {
                    dailyReturns.add(((todayClose - prevCloseVal) / prevCloseVal) * 100.0)
                }
            }
        }
        while (dailyReturns.size < targetDays) {
            dailyReturns.add(0, 0.0)
        }
        return dailyReturns.takeLast(targetDays).joinToString(",") { String.format("%.2f", it) }
    }

    /**
     * Calculates total asset value safely.
     */
    fun calculateTotalAssets(stockHoldingsValue: Double, userCash: Double): Double {
        val safeHoldings = if (stockHoldingsValue.isNaN() || stockHoldingsValue < 0) 0.0 else stockHoldingsValue
        val safeCash = if (userCash.isNaN() || userCash < 0) 0.0 else userCash
        return safeHoldings + safeCash
    }

    /**
     * Calculates daily percentage change given current and previous day asset totals.
     */
    fun calculateDailyChangePercentage(totalAssetsValue: Double, prevDayValue: Double): Double {
        if (prevDayValue <= 0.0 || prevDayValue.isNaN() || totalAssetsValue.isNaN()) return 0.0
        return ((totalAssetsValue - prevDayValue) / prevDayValue) * 100.0
    }
}
