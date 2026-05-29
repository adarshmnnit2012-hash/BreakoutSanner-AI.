package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.TradingRepository
import com.example.data.local.TradingDatabase
import com.example.ui.TradingViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.MyApplicationTheme
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
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { androidx.compose.material3.Text("Robolectric") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun auth_screen_renders_and_account_chooser_works() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, TradingDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val repository = TradingRepository(database.tradingDao())
    val viewModel = TradingViewModel(context as Application, repository)

    composeTestRule.setContent {
      MyApplicationTheme {
        AuthScreen(viewModel = viewModel, isHindi = false)
      }
    }

    // Capture initial AuthScreen preview image
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/auth_screen_setup.png")

    // Find and click 'Continue with Gmail'
    composeTestRule.onNodeWithText("Continue with Gmail").performClick()

    // Assert account picker is showing by verifying Google Title in overlay
    composeTestRule.onNodeWithText("Google").assertExists()

    // Capture Google account chooser state
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/google_dialog.png")

    // Click 'Adarsh' account to simulate secure Google log in flow
    composeTestRule.onNodeWithText("Adarsh").performClick()

    // Confirm that we are in signing-in state list
    composeTestRule.onNodeWithText("adarsh.mnnit.2012@gmail.com").assertExists()
    
    // Close the database to cleanup resource leaks
    database.close()
  }

  @Test
  fun dashboard_ema_crossover_tab_navigates_and_displays_properly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, TradingDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val repository = TradingRepository(database.tradingDao())
    val viewModel = TradingViewModel(context as Application, repository)

    // Simulate login
    viewModel.saveBrokerConnection("API_KEY", "API_SECRET")
    viewModel.saveAccountCredentials("adarsh.mnnit.2012@gmail.com", "any_password")

    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.DashboardScannerTab(
          viewModel = viewModel,
          isHindi = false,
          onStockSelect = {}
        )
      }
    }

    // Verify 'EMA Crossover' tab is visible
    composeTestRule.onNodeWithText("EMA Crossover").assertExists()

    // Click 'EMA Crossover' tab
    composeTestRule.onNodeWithText("EMA Crossover").performClick()

    // Assert that the instruction/title "EMA 10/20 Crossover" is rendered on the screen
    composeTestRule.onNodeWithText("EMA 10/20 Crossover").assertExists()

    // Capture visual layout of the active EMA crossover scanners
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/ema_crossover_tab.png")

    // Close resource leaks
    database.close()
  }
}
