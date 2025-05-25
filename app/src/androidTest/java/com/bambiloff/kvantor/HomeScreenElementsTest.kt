package com.bambiloff.kvantor

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenElementsTest {

    @get:Rule
    val compose = createAndroidComposeRule<CourseSelectionActivity>()

    /** на головному екрані видно ВСІ базові елементи */
    @Test fun allCoreWidgetsAreVisible() {
        val tags = listOf(
            "toggle_theme",
            "btn_shop",
            "avatar",
            "btn_python",
            "btn_js",
            "btn_ai"
        )

        tags.forEach { tag ->
            compose.onNodeWithTag(tag, useUnmergedTree = true)
                .assertExists("Елемент із тегом <$tag> відсутній!")
                .assertIsDisplayed()
        }
    }
}
