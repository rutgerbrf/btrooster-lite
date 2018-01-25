package nl.viasalix.btroosterlite

import android.content.Context
import com.nhaarman.mockito_kotlin.mock
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.timetable.TimetableIntegration
import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.handleIndexResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class TimetableIntegrationTest {
    @Test
    fun testIndexResponse() {
        val expected: LinkedHashMap<Int, String> = linkedMapOf(
                15 to "Vorige Week",
                16 to "Deze Week",
                17 to "Volgende Week")

        val testString = "15|Vorige Week\n16|Deze Week\n17|Volgende Week\n"
        val actual = handleIndexResponse(testString)

        assertEquals(expected, actual)
    }

    @Test
    fun testURLBuilder() {
        val context: Context = mock()
        val ttInteg = TimetableIntegration(context, "Goes", "12345")
        val expected = "https://${MainActivity.AUTHORITY}/RoosterEmbedServlet?week=06&code=12345&locatie=Goes"
        val actual = ttInteg.buildURL(6)

        assertEquals(expected, actual)
    }
}