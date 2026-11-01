package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.E50Repository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("E50", appName)
    }

    @Test
    fun `verify repository initialization and persistent defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = E50Repository(context)

        assertEquals("Priya Sharma", repo.userProfile.value.displayName)
        assertEquals("Ahmedabad", repo.userProfile.value.city)
        assertTrue(repo.experiments.value.isNotEmpty())
        assertTrue(repo.footprintEntries.value.isNotEmpty())
    }
}
