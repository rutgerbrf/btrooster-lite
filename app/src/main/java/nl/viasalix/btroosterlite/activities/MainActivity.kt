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

package nl.viasalix.btroosterlite.activities

import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.cup.CUPIntegration
import nl.viasalix.btroosterlite.fragments.CUPFragment
import nl.viasalix.btroosterlite.fragments.TestTimetableFragment
import nl.viasalix.btroosterlite.fragments.TimetableFragment
import nl.viasalix.btroosterlite.introduction.IntroductionActivity
import nl.viasalix.btroosterlite.singleton.Singleton
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null
    private var bottomNavigation: AHBottomNavigation? = null

    private var backPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        val tabColors: IntArray = intArrayOf(
                ContextCompat.getColor(this, R.color.colorBottomNavigationPrimary),
                ContextCompat.getColor(this, R.color.colorBottomNavigationPrimary),
                ContextCompat.getColor(this, R.color.colorBottomNavigationPrimary))

        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.bottom_navigation)
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors)

        bottomNavigation!!.defaultBackgroundColor = Color.parseColor("#FEFEFE")
        bottomNavigation!!.isBehaviorTranslationEnabled = false
        bottomNavigation!!.accentColor = Color.parseColor("#F63D2B")
        bottomNavigation!!.inactiveColor = Color.parseColor("#747474")
        bottomNavigation!!.isForceTint = true
        bottomNavigation!!.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        currentFragment = null

        if (PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("firstLaunch", true)) {
            startActivity(Intent(this, IntroductionActivity::class.java))
            finish()
        }

        bottomNavigation!!.setOnTabSelectedListener { position, wasSelected ->
            tabSelected(position, wasSelected)
            true
        }

        Singleton.cupIntegration = CUPIntegration(this)
    }

    override fun onResume() {
        super.onResume()

        tabSelected(bottomNavigation!!.currentItem, false)
    }

    override fun onBackPressed() {
        // Interval is 2000 seconden
        if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            snackbar(findViewById(android.R.id.content), resources.getString(R.string.tap_again_exit))
        }

        backPressed = System.currentTimeMillis()
    }

    private fun tabSelected(position: Int, wasSelected: Boolean) {
        if (!wasSelected) {
            when (position) {
                0 -> launchTimetableFragment()
                1 -> launchCUPFragment()
                2 -> launchTestTimetableFragment()
                else -> launchTimetableFragment()
            }
        }
    }

    fun launchTimetableFragment() {
        currentFragment = null
        currentFragment = TimetableFragment()

        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.commit()

        bottomNavigation!!.setCurrentItem(0, false)
    }

    private fun launchCUPFragment() {
        currentFragment = null
        currentFragment = CUPFragment()

        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.commit()

        bottomNavigation!!.setCurrentItem(1, false)
    }

    private fun launchTestTimetableFragment() {
        currentFragment = null
        currentFragment = TestTimetableFragment()

        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.commit()

        bottomNavigation!!.setCurrentItem(2, false)
    }

    companion object {
        const val AUTHORITY = "1d2d0-dot-btrfrontend.appspot.com"
        const val SCHEME = "https"

        // In-house testing
//        const val SCHEME = "http"
//        const val AUTHORITY = "192.168.178.73:8080"
    }
}
