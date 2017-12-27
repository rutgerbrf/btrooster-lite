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

package nl.viasalix.btroosterlite;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

public class MainActivity extends AppCompatActivity {
    public static final String AUTHORITY = "1-dot-0-dot-btrfrontend.appspot.com";
    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AHBottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item_timetable = new AHBottomNavigationItem("Rooster", R.drawable.ic_border_all_black_24dp, R.color.colorBottomNavigationPrimary);
        AHBottomNavigationItem item_cup = new AHBottomNavigationItem("CUP", R.drawable.ic_event_black_24dp, R.color.colorBottomNavigationPrimary);
        AHBottomNavigationItem item_test_timetable = new AHBottomNavigationItem("Toetsrooster", R.drawable.ic_chrome_reader_mode_black_24dp, R.color.colorBottomNavigationPrimary);
        bottomNavigation.addItem(item_timetable);
        bottomNavigation.addItem(item_cup);
        bottomNavigation.addItem(item_test_timetable);
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));
        bottomNavigation.setForceTint(true);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setCurrentItem(0);

        tabSelected(bottomNavigation.getCurrentItem(), false);

        if (currentFragment instanceof TimetableFragment) {
            bottomNavigation.setCurrentItem(0);
        } else if (currentFragment instanceof CUPFragment) {
            bottomNavigation.setCurrentItem(1);
        } else if (currentFragment instanceof TestTimetableFragment) {
            bottomNavigation.setCurrentItem(2);
        }

        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            tabSelected(position, wasSelected);

            return true;
        });
    }

    private void tabSelected(int position, boolean wasSelected) {
        if (!wasSelected) {
            switch (position) {
                case 0:
                    launchTimetableFragment();
                    break;
                case 1:
                    launchCUPFragment();
                    break;
                case 2:
                    launchTestTimetableFragment();
                    break;
                default:
                    launchTimetableFragment();
            }
        }
    }


    private void launchTimetableFragment() {
        currentFragment = null;
        currentFragment = new TimetableFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void launchCUPFragment() {
        currentFragment = null;
        currentFragment = new CUPFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void launchTestTimetableFragment() {
        currentFragment = null;
        currentFragment = new TestTimetableFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
