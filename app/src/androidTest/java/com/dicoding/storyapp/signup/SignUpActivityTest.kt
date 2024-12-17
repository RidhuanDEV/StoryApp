package com.dicoding.storyapp.signup

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.storyapp.R
import com.dicoding.storyapp.login.LoginActivity
import com.dicoding.storyapp.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SignUpActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun testSignUpSuccess() {

        onView(withId(R.id.ed_register_name)).perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.ed_register_email)).perform(typeText("testuserRidhuadnee121@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_register_password)).perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.signupButton)).perform(click())

        onView(withText("Account created successfully! Please log in"))
            .check(matches(isDisplayed()))

    }

    @Test
    fun testSignUpFailure_EmptyFields() {
        onView(withId(R.id.signupButton)).perform(click())

        onView(withText("Please fill in all fields."))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSignUpFailure_ShortPassword() {
        onView(withId(R.id.ed_register_name)).perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.ed_register_email)).perform(typeText("testuser@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_register_password)).perform(typeText("pass"), closeSoftKeyboard())

        onView(withId(R.id.signupButton)).perform(click())

        onView(withText("Password must be at least 8 characters long."))
            .check(matches(isDisplayed()))
    }
}
