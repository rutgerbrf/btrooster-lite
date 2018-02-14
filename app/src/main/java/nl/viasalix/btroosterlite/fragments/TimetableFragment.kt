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

package nl.viasalix.btroosterlite.fragments

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.activities.SettingsActivity
import nl.viasalix.btroosterlite.activities.ViewTimetableActivity
import nl.viasalix.btroosterlite.timetable.TimetableIntegration
import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.getType
import nl.viasalix.btroosterlite.util.Util.Companion.currentWeekOfYear
import nl.viasalix.btroosterlite.util.Util.Companion.getIndexByKey
import nl.viasalix.btroosterlite.util.Util.Companion.getKeyByIndex
import org.jetbrains.anko.alert
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Weeks
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

class TimetableFragment : Fragment() {
    private var weekSpinner: Spinner? = null
    // Opslag code, location en type
    private var code: String? = ""
    private var location: String? = ""
    private var type = ""
    // Initialiseer benodigde variabelen
    private var sharedPreferences: SharedPreferences? = null
    private var currentView: View? = null
    private var webView: WebView? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var ttIntegration: TimetableIntegration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_mainactivity_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {
                loadTimetable()
                getIndexes(activity,
                        ttIntegration!!,
                        { handleIndexResponse(activity,
                                ttIntegration!!,
                                weekSpinner!!,
                                it,
                                true,
                                {})
                        },
                        { getTimetable(defaultSharedPreferences.getInt(
                                "t_week",
                                currentWeekOfYear))
                        })
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(intent)
            }
            R.id.action_opensource -> {
                val ossIntent = Intent(activity, OssLicensesMenuActivity::class.java)
                ossIntent.putExtra("title", getString(R.string.opensource_licences))
                activity.startActivity(ossIntent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_timetable, container, false)
        webView = currentView!!.findViewById(R.id.web_view)
        weekSpinner = currentView!!.findViewById(R.id.week_spinner)

        return currentView
    }

    override fun onStart() {
        super.onStart()

        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        if ((activity as AppCompatActivity).supportActionBar != null)
            (activity as AppCompatActivity)
                    .supportActionBar!!
                    .setDisplayShowTitleEnabled(false)

        webView!!.settings.builtInZoomControls = true
        webView!!.settings.displayZoomControls = false

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        loadSharedPreferences()

        ttIntegration = TimetableIntegration(activity!!, location!!, code!!)

        activity.findViewById<FloatingActionButton>(R.id.t_loadOtherTimetable)?.setOnClickListener {
            startActivity(Intent(activity!!, ViewTimetableActivity::class.java))
        }

        if (loadSharedPreferences() != 1) {
            if (activity != null) {
                getIndexes(activity,
                        ttIntegration!!,
                        {
                            handleIndexResponse(activity,
                                    ttIntegration!!,
                                    weekSpinner!!,
                                    it,
                                    true,
                                    { loadTimetable() })
                        },
                        {
                            getTimetable(defaultSharedPreferences.getInt(
                                    "t_week",
                                    currentWeekOfYear))
                        })
                loadTimetable()
            }
        }
    }

    private fun loadSharedPreferences(): Int {
        if (!sharedPreferences!!.contains("code")) {
            showCodeAlert()
            return 1
        }

        if (!sharedPreferences!!.contains("location")) {
            showLocationAlert()
            return 1
        }

        code = sharedPreferences!!.getString("code", "12345")

        location = sharedPreferences!!.getString("location", locaties[0])
        type = getType(code!!)

        try {
            defaultSharedPreferences.getInt("t_week", currentWeekOfYear)
        } catch (e: ClassCastException) {
            defaultSharedPreferences.edit().putInt("t_week", currentWeekOfYear).apply()
        }
        return 0
    }

    private fun showCodeAlert() {
        alert(getString(R.string.alert_nocode_text),
                getString(R.string.warning)) {

            yesButton {
                onStart()
            }

            noButton {
                activity!!.finish()
            }
        }
    }

    private fun showLocationAlert() {
        alert(getString(R.string.alert_nolocation_text),
                getString(R.string.warning)) {
            yesButton {
                onStart()
            }

            noButton {
                activity!!.finish()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun loadTimetable() {
        getTimetable(sharedPreferences!!.getInt("t_week",
                currentWeekOfYear))
        if (sharedPreferences!!.getBoolean("t_preload", false)) {
            ttIntegration!!.downloadAvailableTimetables()
        }
    }

    private fun getTimetable(week: Int) {
        ttIntegration!!.loadTimetable(week, webView!!)
    }

    internal interface OnFragmentInteractionListener

    companion object {
        // Locaties
        var locaties = arrayOf("Goes Klein Frankrijk", "Goes Noordhoeklaan", "Goes Stationspark", "Krabbendijke Appelstraat", "Krabbendijke Kerkpolder", "Middelburg", "Tholen")
        // Locaties die in de request URL moeten te komen staan
        var locatiesURL = arrayOf("Goes", "GoesNoordhoeklaan", "GoesStationspark", "KrabbendijkeAppelstraat", "KrabbendijkeKerkpolder", "Middelburg", "Tholen")

        fun parseAvailableWeeks(resources: Resources, availableWeeks: LinkedHashMap<Int, String>): List<String> {
            val weekNames = resources.getStringArray(R.array.in_weeks)
            val currentWeek = DateTime.now(DateTimeZone.getDefault())
                    .withDayOfWeek(1)
                    .withHourOfDay(0)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0)
            val dateParser: DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy")

            val result = arrayListOf<String>()

            availableWeeks.values.forEach { week ->
                val date = dateParser.parseDateTime(week)
                val dif = Weeks.weeksBetween(currentWeek, date).weeks

                when (dif) {
                    -1 -> result.add(weekNames[1])
                    0 -> result.add(weekNames[2])
                    1 -> result.add(weekNames[3])

                    else -> {
                        if (dif < 0) {
                            result.add(dif.absoluteValue.toString() + " " + weekNames[0])
                        } else if (dif > 1) {
                            result.add(weekNames[4].replace("|", dif.absoluteValue.toString()))
                        }
                    }
                }
            }

            return result
        }

        fun getIndexes(activity: Activity, ttIntegration: TimetableIntegration, callback: (String?) -> Unit, loadTimetableCallback: () -> Unit) {
            ttIntegration.getIndexes { it, wasOnline ->
                if (wasOnline) {
                    callback(it)

                    try {
                        if (activity != null) {
                            loadTimetableCallback()
                        }
                    } catch (e: NullPointerException) {
                        Log.d("ERROR", e.message)
                    }
                } else {
                    callback(it)
                }
            }
        }

        fun handleIndexResponse(activity: Activity, ttIntegration: TimetableIntegration, weekSpinner: Spinner, response: String?, deleteUnusedTimetables: Boolean = false, callback: () -> Unit, editSharedPreferences: Boolean = true, availableWeeksCallback: (LinkedHashMap<Int, String>) -> Unit = {}) {
            if (response != null) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

                val availableWeeks = TimetableIntegration.handleIndexResponse<Int, String>(response)
                val parsedWeekNames = parseAvailableWeeks(activity.resources, availableWeeks)

                availableWeeksCallback(availableWeeks)

                if (deleteUnusedTimetables)
                    ttIntegration.deleteUnusedTimetables(availableWeeks.keys.toList())

                val adapter = ArrayAdapter(
                        activity,
                        android.R.layout.simple_spinner_dropdown_item,
                        parsedWeekNames)

                weekSpinner.adapter = adapter

                weekSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                        if (editSharedPreferences) {
                            val week = getKeyByIndex(availableWeeks, position)

                            if (week != null)
                                sharedPreferences?.edit()?.putInt("t_week", week)?.apply()
                            else
                                sharedPreferences?.edit()?.putInt("t_week", currentWeekOfYear)?.apply()
                        }

                        callback()
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>) {}
                }

                val indexToSet =
                        getIndexByKey(availableWeeks,
                                sharedPreferences.getInt(
                                        "t_week",
                                        currentWeekOfYear))

                if (indexToSet != null)
                    weekSpinner.setSelection(indexToSet)
                else
                    weekSpinner.setSelection(2)
            }
        }
    }
}