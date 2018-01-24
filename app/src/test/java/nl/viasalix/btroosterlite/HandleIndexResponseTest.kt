package nl.viasalix.btroosterlite

import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.handleIndexResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleIndexResponseTest {
    @Test
    fun checkHandleIndexResponse() {
        val expected: LinkedHashMap<Int, String> = linkedMapOf(
                15 to "Vorige Week",
                16 to "Deze Week",
                17 to "Volgende Week")

        val testString = "15|Vorige Week\n16|Deze Week\n17|Volgende Week\n"
        val actual = handleIndexResponse(testString)

        assertEquals(expected, actual)
    }
}