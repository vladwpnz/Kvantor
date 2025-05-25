package com.bambiloff.kvantor

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TIMEOUT = 10_000L

@RunWith(AndroidJUnit4::class)
class CourseSelectionNavigationTest {

    @get:Rule
    val compose = createAndroidComposeRule<CourseSelectionActivity>()

    /** PYTHON */
    @Test fun pythonButton_opensPython() {
        compose.onNodeWithTag("btn_python").performClick()

        compose.waitUntil(TIMEOUT) {
            compose.onAllNodes(
                hasTestTag("python_header") or hasText("Python"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNode(
            hasTestTag("python_header") or hasText("Python"),
            useUnmergedTree = true
        ).assertExists()
    }

    /** JAVASCRIPT */
    @Test fun jsButton_opensJavaScript() {
        compose.onNodeWithTag("btn_js").performClick()

        compose.waitUntil(TIMEOUT) {
            compose.onAllNodes(
                hasTestTag("js_header") or hasText("JavaScript"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNode(
            hasTestTag("js_header") or hasText("JavaScript"),
            useUnmergedTree = true
        ).assertExists()
    }

    /** AI-помічник */
    @Test fun aiButton_opensAiHelper() {
        compose.onNodeWithTag("btn_ai").performClick()

        compose.waitUntil(TIMEOUT) {
            compose.onAllNodes(
                hasTestTag("ai_header") or hasText("AI"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNode(
            hasTestTag("ai_header") or hasText("AI"),
            useUnmergedTree = true
        ).assertExists()
    }
}
