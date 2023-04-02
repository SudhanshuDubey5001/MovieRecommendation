package com.sudhanshu.movierecd

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appbarIsCorrect(){
        composeTestRule.setContent { MainActivity() }

        //see if the add movies is shown on the top app bar
        composeTestRule.onNodeWithText("Add movies").assertExists()
    }

//    @Test
//    fun checkEditFieldIsOkay(){
//        composeTestRule.setContent { MainActivity() }
//
//        composeTestRule.onNode(
//            hasText("Search any movie")
//        )
//    }
}