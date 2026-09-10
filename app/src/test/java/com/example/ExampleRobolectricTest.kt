package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    // The actual defined app name in strings.xml is "Keep & Grow"
    assertEquals("Keep & Grow", appName)
  }

  @Test
  fun `application context is available and has valid package name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull(context)
    assertEquals("com.aistudio.keepandgrow.vbglt", context.packageName)
  }
}
