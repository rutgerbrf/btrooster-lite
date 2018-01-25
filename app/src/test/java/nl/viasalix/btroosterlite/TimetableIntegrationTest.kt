package nl.viasalix.btroosterlite

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
    fun testGetType() {
        val actualClass = TimetableIntegration.getType("go1a")
        val actualStudent = TimetableIntegration.getType("12345")
        val actualTeacher = TimetableIntegration.getType("abc")

        assertEquals(actualClass, "c")
        assertEquals(actualStudent, "s")
        assertEquals(actualTeacher, "t")
    }
}