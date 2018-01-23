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

import android.content.Intent
import android.graphics.Color
import android.app.Fragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.cupconfig.CUPIntegration
import nl.viasalix.btroosterlite.fragments.CUPFragment
import nl.viasalix.btroosterlite.fragments.TestTimetableFragment
import nl.viasalix.btroosterlite.fragments.TimetableFragment
import nl.viasalix.btroosterlite.introduction.IntroductionActivity
import nl.viasalix.btroosterlite.singleton.Singleton

class MainActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<AHBottomNavigation>(R.id.bottom_navigation)

        val itemTimetable = AHBottomNavigationItem("Rooster", R.drawable.ic_border_all_black_24dp, R.color.colorBottomNavigationPrimary)
        val itemCup = AHBottomNavigationItem("CUP", R.drawable.ic_event_black_24dp, R.color.colorBottomNavigationPrimary)
        val itemTestTimetable = AHBottomNavigationItem("Toetsrooster", R.drawable.ic_chrome_reader_mode_black_24dp, R.color.colorBottomNavigationPrimary)
        bottomNavigation.addItem(itemTimetable)
        bottomNavigation.addItem(itemCup)
        bottomNavigation.addItem(itemTestTimetable)
        bottomNavigation.defaultBackgroundColor = Color.parseColor("#FEFEFE")
        bottomNavigation.isBehaviorTranslationEnabled = false
        bottomNavigation.accentColor = Color.parseColor("#F63D2B")
        bottomNavigation.inactiveColor = Color.parseColor("#747474")
        bottomNavigation.isForceTint = true
        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigation.currentItem = 0

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstLaunch", true)) {
            val intent = Intent(this, IntroductionActivity::class.java)
            startActivity(intent)
            finish()
        }

        tabSelected(bottomNavigation.currentItem, false)

        bottomNavigation.currentItem = when (currentFragment) {
            is TimetableFragment -> 0
            is CUPFragment -> 1
            is TestTimetableFragment -> 2
            else -> 0
        }

        bottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            tabSelected(position, wasSelected)
            true
        }

        Singleton.cupIntegration = CUPIntegration(this)
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


    private fun launchTimetableFragment() {
        currentFragment = null
        currentFragment = TimetableFragment()
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun launchCUPFragment() {
        currentFragment = null
        currentFragment = CUPFragment()
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun launchTestTimetableFragment() {
        currentFragment = null
        currentFragment = TestTimetableFragment()
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, currentFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    companion object {
        val AUTHORITY = "1d1d1-dot-btrfrontend.appspot.com"
    }
}
