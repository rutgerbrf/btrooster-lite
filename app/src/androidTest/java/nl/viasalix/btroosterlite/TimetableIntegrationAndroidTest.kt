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
    fun setup() {
        context = InstrumentationRegistry.getContext()
    }

    @Test
    @SmallTest
    fun testURLBuilder() {
        val ttIntegration = TimetableIntegration(context!!, "Goes", "12345")
        val expected = "https://$AUTHORITY/RoosterEmbedServlet?code=12345&locatie=Goes&type=s&week=06"
        val actual = ttIntegration.buildURL(6)

        assertEquals(expected, actual)
    }
}