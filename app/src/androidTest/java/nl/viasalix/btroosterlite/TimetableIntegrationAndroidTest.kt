package nl.viasalix.btroosterlite

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import nl.viasalix.btroosterlite.activities.MainActivity.Companion.AUTHORITY
import nl.viasalix.btroosterlite.timetable.TimetableIntegration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimetableIntegrationAndroidTest {
    private var context: Context? = null

    @Before
    fun setup() { context = InstrumentationRegistry.getContext() }

    @Test
    @SmallTest
    fun testURLBuilder() {
        val ttIntegration = TimetableIntegration(context!!, "Goes", "12345")
        val expected = "https://$AUTHORITY/RoosterEmbedServlet?code=12345&locatie=Goes&type=s&week=06"
        val actual = ttIntegration.buildURL(6)

        assertEquals(expected, actual)
    }
}