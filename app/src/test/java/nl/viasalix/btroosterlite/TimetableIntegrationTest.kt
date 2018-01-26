/*  BTRooster Lite: Roosterapp voor Calvijn College
 *  Copyright (C) 2017 Rutger Broekhoff <rutger broekhoff three at gmail dot com>
 *                 and Jochem Broekhoff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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