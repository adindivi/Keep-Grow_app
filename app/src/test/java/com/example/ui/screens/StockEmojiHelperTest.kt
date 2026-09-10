package com.example.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for StockEmojiHelper.
 * Verifies accurate emoji mapping for major domestic and international stocks,
 * industry keywords, and fallback cases.
 */
class StockEmojiHelperTest {

    @Test
    fun getStockEmoji_forMajorTechAndAutoStocks_returnsAppropriateEmoji() {
        assertEquals("📱", getStockEmoji("005930", "삼성전자"))
        assertEquals("💾", getStockEmoji("000660", "SK하이닉스"))
        assertEquals("🔋", getStockEmoji("373220", "LG에너지솔루션"))
        assertEquals("🚗", getStockEmoji("005380", "현대자동차"))
        assertEquals("🚙", getStockEmoji("000270", "기아"))
    }

    @Test
    fun getStockEmoji_forPlatformAndInternetStocks_returnsAppropriateEmoji() {
        assertEquals("🔍", getStockEmoji("035420", "NAVER"))
        assertEquals("💬", getStockEmoji("035720", "카카오"))
        assertEquals("💳", getStockEmoji("323410", "카카오뱅크"))
    }

    @Test
    fun getStockEmoji_forUSGiants_returnsIconicEmoji() {
        assertEquals("🍏", getStockEmoji("AAPL", "Apple Inc."))
        assertEquals("🎮", getStockEmoji("NVDA", "NVIDIA Corp."))
        assertEquals("⚡", getStockEmoji("TSLA", "Tesla, Inc."))
    }

    @Test
    fun getStockEmoji_forFinancialInstitutions_returnsBankEmoji() {
        assertEquals("🏦", getStockEmoji("105560", "KB금융"))
        assertEquals("🏦", getStockEmoji("055550", "신한지주"))
        assertEquals("🏦", getStockEmoji("000810", "삼성화재"))
        assertEquals("🏦", getStockEmoji("032830", "삼성생명"))
    }

    @Test
    fun getStockEmoji_withMixedCaseInput_handlesCorrectly() {
        assertEquals("🍏", getStockEmoji("aapl", "apple inc."))
        assertEquals("⚡", getStockEmoji("tsla", "tesla"))
        assertEquals("🔍", getStockEmoji("035420", "naver"))
    }

    @Test
    fun getStockEmoji_forUnknownStock_returnsStarFallback() {
        assertEquals("⭐", getStockEmoji("UNKNOWN", "알 수 없는 회사"))
        assertEquals("⭐", getStockEmoji("999999", ""))
    }
}
