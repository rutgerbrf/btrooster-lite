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

package nl.viasalix.btroosterlite.util

import android.content.Context
import android.net.ConnectivityManager

class Util {
    companion object {
        fun online(context: Context): Boolean {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo

            var isAvailable = false
            if (networkInfo != null && networkInfo.isConnected)
                isAvailable = true
            return isAvailable
        }

        fun <K, V> getIndexByKey(map: LinkedHashMap<K, V>, key: K): Int? {
            var i = 0

            map.forEach {
                if (it.key == key)
                    return i
                i++
            }

            return null
        }

        fun <K, V> getKeyByIndex(map: LinkedHashMap<K, V>, index: Int): K? {
            var i = 0

            map.forEach {
                if (i == index)
                    return it.key
                i++
            }

            return null
        }
    }
}