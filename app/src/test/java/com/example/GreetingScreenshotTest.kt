package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.network.models.QuoteResponse
import com.example.ui.screens.FintechStockItemCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HighGrowthStock
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun stockItemCard_screenshot() {
    val sampleStock = HighGrowthStock(
      symbol = "AAPL",
      name = "Apple Inc.",
      quote = QuoteResponse(
        currentPrice = 244.50,
        percentChange = 1.25
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        FintechStockItemCard(stock = sampleStock)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/stock_item_card.png")
  }
}
