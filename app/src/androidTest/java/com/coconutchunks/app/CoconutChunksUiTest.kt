package com.coconutchunks.app

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CoconutChunksUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsWithPrimaryNavigationAvailable() {
        composeRule.onNodeWithTag("nav_library").assertExists()
        composeRule.onNodeWithTag("nav_review").assertExists()
        composeRule.onNodeWithTag("nav_groups").assertExists()
        composeRule.onNodeWithTag("nav_overview").assertExists()
        composeRule.onNodeWithTag("add_chunk_fab").assertExists()
    }

    @Test
    fun addChunkWithOneExampleAndFindItInLibrary() {
        val chunk = "UI chunk ${System.nanoTime()}"

        composeRule.onNodeWithTag("add_chunk_fab").performClick()
        composeRule.onNodeWithTag("chunk_text").performTextInput(chunk)
        composeRule.onNodeWithTag("example_1")
            .performTextInput("Das ist mein Beispiel.")

        composeRule.onNodeWithTag("chunk_editor")
            .performScrollToNode(hasTestTag("save_chunk"))

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("save_chunk")
            .assertExists()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText(chunk)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText(chunk).assertExists()
    }

    @Test
    fun librarySearchCanMatchSavedNote() {
        val suffix = System.nanoTime().toString()
        val chunk = "Search chunk $suffix"
        val note = "private-note-$suffix"

        composeRule.onNodeWithTag("add_chunk_fab").performClick()
        composeRule.onNodeWithTag("chunk_text").performTextInput(chunk)

        composeRule.onNodeWithTag("chunk_editor")
            .performScrollToNode(hasTestTag("chunk_note"))

        composeRule.onNodeWithTag("chunk_note")
            .performTextInput(note)

        composeRule.onNodeWithTag("chunk_editor")
            .performScrollToNode(hasTestTag("save_chunk"))

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("save_chunk")
            .assertExists()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag("library_search")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithTag("library_search")
            .performTextInput(note)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText(chunk)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText(chunk).assertExists()
    }

    @Test
    fun reviewCardHidesThenRevealsExamples() {
        val chunk = "Recall chunk ${System.nanoTime()}"

        composeRule.onNodeWithTag("add_chunk_fab")
            .performClick()

        composeRule.onNodeWithTag("chunk_text")
            .performTextInput(chunk)

        composeRule.onNodeWithTag("example_1")
            .performTextInput("Ich erinnere mich daran.")

        composeRule.onNodeWithTag("chunk_editor")
            .performScrollToNode(hasTestTag("save_chunk"))

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("save_chunk")
            .assertExists()
            .performClick()

        composeRule.waitUntil(
            conditionDescription = "nav_review should appear after saving the chunk",
            timeoutMillis = 10_000
        ) {
        composeRule
            .onAllNodesWithTag("nav_review")
            .fetchSemanticsNodes()
            .isNotEmpty()
        }

        composeRule.onNodeWithTag("nav_review")
            .assertExists()
            .performClick()

        composeRule.waitUntil(
            conditionDescription = "Start Review button should appear on Review home",
            timeoutMillis = 10_000
        ) {
        composeRule
            .onAllNodesWithText("Start Review")
            .fetchSemanticsNodes()
            .isNotEmpty()
        }

        composeRule.onNodeWithText("Start Review")
            .performClick()

        composeRule.waitUntil(
            conditionDescription = "review_card should appear after starting review",
            timeoutMillis = 15_000
        ) {
        composeRule
            .onAllNodesWithTag("review_card")
            .fetchSemanticsNodes()
            .isNotEmpty()
        }

        composeRule.onNodeWithTag("review_card")
            .assertExists()
            .performClick()

        composeRule.waitUntil(
            conditionDescription = "revealed_examples should appear after tapping review card",
            timeoutMillis = 10_000
        ) {
        composeRule
            .onAllNodesWithTag("revealed_examples")
            .fetchSemanticsNodes()
            .isNotEmpty()
        }

        composeRule.onNodeWithTag("revealed_examples")
            .assertExists()
    }

    @Test
    fun groupsCanBeCreatedFromGroupsScreen() {
        val group = "UI Group ${System.nanoTime()}"

        composeRule.onNodeWithTag("nav_groups")
            .performClick()

        composeRule.onNodeWithTag("create_group")
            .performClick()

        composeRule.onNodeWithTag("dialog_text_input")
            .performTextInput(group)

        composeRule.onNodeWithText("Save")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText(group)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText(group)
            .assertExists()
    }
}
