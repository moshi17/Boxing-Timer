package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetWorkout
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.timer.PresetChip
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
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.background(Color(0xFF121212)).padding(24.dp)) {
          Column {
            Text(
              text = "BOXING TIMER PRESETS",
              color = Color(0xFFFFA000),
              fontSize = 14.sp,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            PresetChip(
              preset = PresetWorkout(
                name = "Amateur Match",
                rounds = 3,
                workSeconds = 120,
                restSeconds = 60,
                isCustom = false
              ),
              isSelected = true,
              onSelect = {},
              onDelete = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            PresetChip(
              preset = PresetWorkout(
                name = "Heavy Bag Custom",
                rounds = 6,
                workSeconds = 180,
                restSeconds = 30,
                isCustom = true
              ),
              isSelected = false,
              onSelect = {},
              onDelete = {}
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
