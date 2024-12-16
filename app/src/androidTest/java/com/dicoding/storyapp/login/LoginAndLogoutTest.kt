package com.dicoding.storyapp.login

import androidx.test.espresso.Espresso.onView
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
import com.dicoding.storyapp.home.HomeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginAndLogoutTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginAndLogout() {
        Intents.init()

        onView(withId(R.id.ed_login_email))
            .perform(click(), typeText("wantod@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(click(), typeText("12341234"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())
        Thread.sleep(15000)
        Intents.intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        Thread.sleep(3000)
        onView(withId(R.id.action_logout)).perform(click())
        Thread.sleep(3000)
        onView(withText(R.string.logout_confirmation))
            .check(matches(isDisplayed()))
        Thread.sleep(2000)
        onView(withText(R.string.yes)).perform(click())
        Thread.sleep(4000)
        Intents.intended(IntentMatchers.hasComponent(LoginActivity::class.java.name))

        Intents.release()
    }


    @Test
    fun testLoginFailed() {
        onView(withId(R.id.ed_login_email))
            .perform(click(), typeText("invalid@example.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(click(), typeText("wrongpassword"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        onView(withText("User not found"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyEmail() {
        onView(withId(R.id.ed_login_email)).perform(click(), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password))
            .perform(click(), typeText("12312323"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())
        Thread.sleep(2000)
        onView(withText("Email tidak boleh kosong")).check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyPassword() {
        onView(withId(R.id.ed_login_email))
            .perform(click(), typeText("testuser@example.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(click(), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())
        Thread.sleep(2000)
        onView(withText("Password tidak boleh kosong")).check(matches(isDisplayed()))
    }
}
