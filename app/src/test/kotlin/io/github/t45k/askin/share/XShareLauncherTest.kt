package io.github.t45k.askin.share

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class XShareLauncherTest {
    @Test
    fun launchStartsAndroidSharesheetWithSendIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        XShareLauncher.launch(context, "共有テキスト")

        val startedIntent = Shadows.shadowOf(context as Application).nextStartedActivity
        @Suppress("DEPRECATION")
        val sendIntent = startedIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        assertEquals(Intent.ACTION_CHOOSER, startedIntent.action)
        assertEquals(Intent.ACTION_SEND, sendIntent?.action)
        assertEquals("text/plain", sendIntent?.type)
        assertEquals("共有テキスト", sendIntent?.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue(startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}
