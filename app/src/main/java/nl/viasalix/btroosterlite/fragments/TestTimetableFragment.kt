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

import android.app.Fragment
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.activities.SettingsActivity
import nl.viasalix.btroosterlite.timetable.TimetableIntegration

class TestTimetableFragment : Fragment() {
    private var currentView: View? = null
    private var webView: WebView? = null
    private var weekSpinner: Spinner? = null
    private var sharedPreferences: SharedPreferences? = null

    private var code: String? = null
    private var location: String? = null

    private var menuHasLoaded = false

    private var responseList = linkedMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_test_timetable, container, false)
        webView = currentView!!.findViewById(R.id.web_view)
        weekSpinner = currentView!!.findViewById(R.id.tt_week_spinner)

        return currentView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_mainactivity_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {
                loadTestTimetable(true)
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

    override fun onStart() {
        super.onStart()

        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        if ((activity as AppCompatActivity).supportActionBar != null)
            (activity as AppCompatActivity)
                    .supportActionBar!!
                    .setDisplayShowTitleEnabled(false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        code = sharedPreferences!!.getString("code", "12345")
        location = sharedPreferences!!.getString("location", "Goes")

        loadTestTimetable(true)
    }

    private fun online(): Boolean {
        val connectivityManager = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo?

        networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected
    }

    private fun loadTestTimetable(getIndexes: Boolean) {
        webView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (online())
            webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        else
            webView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (getIndexes)
            getIndexes()
        else
            getTestTimetable()
    }

    private fun getTestTimetable() {
        val weekChange = sharedPreferences!!.getInt("tt_weekChange", 0)

        val builder = Uri.Builder()
        builder.scheme(MainActivity.SCHEME)
                .encodedAuthority(MainActivity.AUTHORITY)
                .appendPath("ToetsroosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", location)
                .appendQueryParameter("type", "leerlingen")
                .appendQueryParameter("toetsweek", responseList.keys.toList()[weekChange])
        val url = builder.build().toString()

        webView!!.loadUrl(url)
    }

    private fun getIndexes() {
        if (online()) {
            val queue = Volley.newRequestQueue(activity)
            val builder = Uri.Builder()
            builder.scheme(MainActivity.SCHEME)
                    .encodedAuthority(MainActivity.AUTHORITY)
                    .appendPath("ToetsroosterEmbedServlet")
                    .appendQueryParameter("indexOphalen", "1")
                    .appendQueryParameter("locatie", location)
            val url = builder.build().toString()

            val stringRequest = StringRequest(Request.Method.GET, url,
                    { response ->
                        sharedPreferences!!.edit().putString("tt_indexes", response).apply()
                        Log.d("or", response)
                        handleIndexResponse(response)
                    }) { error -> Log.d("error", error.message) }

            queue.add(stringRequest)
        } else {
            val response = sharedPreferences!!.getString("tt_indexes", null)
            handleIndexResponse(response)
            getTestTimetable()
        }
    }

    private fun handleIndexResponse(response: String?) {
        if (response != null) {
            responseList = TimetableIntegration.handleIndexResponse(response)

            if (activity != null) {
                val adapter = ArrayAdapter(
                        activity,
                        android.R.layout.simple_spinner_dropdown_item,
                        responseList.values.toList())

                weekSpinner!!.adapter = adapter
                menuHasLoaded = true

                weekSpinner!!.setSelection(sharedPreferences!!.getInt("tt_weekChange", 1))

                weekSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                        sharedPreferences!!.edit().putInt("tt_weekChange", position).apply()

                        loadTestTimetable(false)
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>) {}
                }
            }
        }
    }
}// Required empty public constructor
