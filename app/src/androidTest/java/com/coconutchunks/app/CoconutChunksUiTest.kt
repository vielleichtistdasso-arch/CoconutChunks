package com.coconutchunks.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        composeRule.onNodeWithTag("example_1").performTextInput("Das ist mein Beispiel.")
        composeRule.onNodeWithTag("save_chunk").performClick()

        composeRule.onNodeWithText(chunk).assertExists()
    }

    @Test
    fun librarySearchCanMatchSavedNote() {
        val suffix = System.nanoTime().toString()
        val chunk = "Search chunk $suffix"
        val note = "private-note-$suffix"

        composeRule.onNodeWithTag("add_chunk_fab").performClick()
        composeRule.onNodeWithTag("chunk_text").performTextInput(chunk)
        composeRule.onNodeWithTag("chunk_note").performTextInput(note)
        composeRule.onNodeWithTag("save_chunk").performClick()

        composeRule.onNodeWithTag("library_search").performTextInput(note)
        composeRule.onNodeWithText(chunk).assertExists()
    }

    @Test
    fun reviewCardHidesThenRevealsExamples() {
        val chunk = "Recall chunk ${System.nanoTime()}"

        composeRule.onNodeWithTag("add_chunk_fab").performClick()
        composeRule.onNodeWithTag("chunk_text").performTextInput(chunk)
        composeRule.onNodeWithTag("example_1").performTextInput("Ich erinnere mich daran.")
        composeRule.onNodeWithTag("save_chunk").performClick()

        composeRule.onNodeWithTag("nav_review").performClick()
        composeRule.onNodeWithText("Start Review").performClick()

        composeRule.onNodeWithTag("review_card").assertExists()
        composeRule.onNodeWithTag("review_card").performClick()
        composeRule.onNodeWithTag("revealed_examples").assertExists()
    }

    @Test
    fun groupsCanBeCreatedFromGroupsScreen() {
        val group = "UI Group ${System.nanoTime()}"

        composeRule.onNodeWithTag("nav_groups").performClick()
        composeRule.onNodeWithTag("create_group").performClick()
        composeRule.onNodeWithTag("dialog_text_input").performTextInput(group)
        composeRule.onNodeWithText("Save").performClick()

        composeRule.onNodeWithText(group).assertExists()
    }
}
