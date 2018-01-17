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

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner

import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.annimon.stream.Stream
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

import java.util.ArrayList
import java.util.Calendar
import java.util.Stack
import java.util.regex.Pattern

import android.content.Context.CONNECTIVITY_SERVICE
import android.provider.CalendarContract
import java.time.Year

class TimetableFragment : Fragment() {
    private var weekSpinner: Spinner? = null
    // Opslag 'roostercode', location en type
    private var code: String? = ""
    private var location: String? = ""
    private var type = ""
    // Variabelen om beschikbare weken weer te geven
    private var availableWeeks: MutableList<String> = ArrayList()
    private var availableWeeksNames: MutableList<String> = ArrayList()
    // Initialiseer benodigde variabelen
    private var sharedPreferences: SharedPreferences? = null
    private var currentView: View? = null
    private var webView: WebView? = null
    private var webViewPL: WebView? = null
    private var pagesToLoad = Stack<String>()
    private var mListener: OnFragmentInteractionListener? = null

    private val currentWeekOfYear: Int
        get() = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_mainactivity_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun online(): Boolean {
        val connectivityManager = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var networkInfo: NetworkInfo? = null
        if (connectivityManager != null) {
            networkInfo = connectivityManager.activeNetworkInfo
        }

        return networkInfo != null && networkInfo.isConnected
    }

    private fun mobileIsConnected(): Boolean {
        val connMgr = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mobile = connMgr.getNetworkInfo(
                ConnectivityManager.TYPE_MOBILE)

        return mobile.isConnected
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {
                loadTimetable(true)
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(intent)
            }
            R.id.action_opensource -> {
                val ossIntent = Intent(activity, OssLicensesMenuActivity::class.java)
                ossIntent.putExtra("title", "Open-source licenties")
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
        webViewPL = currentView!!.findViewById(R.id.web_view_preload)
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

        if (loadSharedPreferences() != 1)
            loadTimetable(true)
    }

    private fun loadSharedPreferences(): Int {
        if (!sharedPreferences!!.contains("code")) {
            showCodeDialog()
            return 1
        }

        if (!sharedPreferences!!.contains("location")) {
            showLocationDialog()
            return 1
        }

        code = sharedPreferences!!.getString("code", "12345")

        location = sharedPreferences!!.getString("location", locaties[0])
        type = getType(code)

        return 0
    }

    private fun getType(code: String?): String {
        val docentPatternInput = "([A-Za-z]){3}"
        val leerlingPatternInput = "([0-9]){5}"

        val docentPattern = Pattern.compile(docentPatternInput)
        val leerlingPattern = Pattern.compile(leerlingPatternInput)

        if (!docentPattern.matcher(code!!).matches() && !leerlingPattern.matcher(code).matches()) {
            return "c"
        } else if (docentPattern.matcher(code).matches()) {
            return "t"
        } else if (leerlingPattern.matcher(code).matches()) {
            return "s"
        }

        return "none"
    }

    private fun showCodeDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Code")

        val layout = LinearLayout(activity)
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)

        layout.layoutParams = layoutParams
        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.layoutParams = layoutParams

        val margin_left_right_bottom = getPixelValue(activity, 24)
        val margin_top = getPixelValue(activity, 20)
        layoutParams.setMargins(margin_left_right_bottom,
                margin_top,
                margin_left_right_bottom,
                margin_left_right_bottom)

        layout.addView(input)

        builder.setView(layout)

        builder.setPositiveButton("OK") { dialog, which ->
            sharedPreferences!!.edit().putString("code", input.text.toString()).apply()
            onStart()
        }.setNegativeButton("Annuleren") { dialog, which ->
                    activity.finish()
                    dialog.cancel()
                }

        builder.show()
    }

    private fun showLocationDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Locatie")
        val items = locaties

        builder.setSingleChoiceItems(items, -1) { dialog, which -> }

        builder.setPositiveButton("OK") { dialog, which ->
            val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
            sharedPreferences!!.edit().putString("location", locatiesURL[selectedPosition]).apply()
            Log.d("selected", Integer.toString(selectedPosition))
            onStart()
        }

        builder.setNegativeButton("Annuleren") { dialog, which ->
            activity.finish()
            dialog.cancel()
        }

        builder.show()
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

    private fun loadTimetable(getIndexes: Boolean) {
        webView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (online()) {
            webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        } else {
            webView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }

        if (getIndexes) {
            getIndexes()
        } else {
            getTimetable(
                    sharedPreferences!!.getString("t_week",
                            Integer.toString(currentWeekOfYear)))
            if (sharedPreferences!!.getBoolean("t_preload", false)) {
                val onlyWifiPref = sharedPreferences!!.getBoolean(
                        "t_preload_only_wifi",
                        true)

                if (online()) {
                    if (mobileIsConnected() != onlyWifiPref) {
                        preloadTimetables()
                    }
                }
            } else {
                webViewPL!!.destroy()
            }
        }
    }

    private fun preloadTimetables() {
        pagesToLoad.empty()

        Stream.of(availableWeeks).forEach { it ->
            pagesToLoad.push(
                    buildTimetableURL(it)
            )
        }

        class PreloadClient : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (!pagesToLoad.isEmpty())
                    view.loadUrl(pagesToLoad.pop())
            }
        }

        val client = PreloadClient()
        webViewPL!!.webViewClient = client

        webViewPL!!.loadUrl(pagesToLoad.pop())
    }

    private fun buildTimetableURL(week: String?): String {
        val typeString = getType(code)

        val builder = Uri.Builder()
        builder.scheme("https")
                .authority(MainActivity.AUTHORITY)
                .appendPath("RoosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", location)
                .appendQueryParameter("type", typeString)
                .appendQueryParameter("week", week)
        val url = builder.build().toString()

        Log.v("URL built", url)

        return url
    }

    private fun getTimetable(week: String?) {
        webView!!.loadUrl(buildTimetableURL(week))
    }

    private fun getIndexes() {
        if (online()) {
            val queue = Volley.newRequestQueue(activity)
            val builder = Uri.Builder()
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("RoosterEmbedServlet")
                    .appendQueryParameter("indexOphalen", "1")
                    .appendQueryParameter("locatie", location)
            val url = builder.build().toString()

            val stringRequest = StringRequest(Request.Method.GET, url,
                    { response ->
                        sharedPreferences!!.edit().putString("t_indexes", response).apply()
                        Log.d("or", response)
                        handleResponse(response)
                    }) { error -> Log.d("error", error.message) }
            queue.add(stringRequest)
        } else {
            val response = sharedPreferences!!.getString("t_indexes", null)
            handleResponse(response)
            getTimetable(
                    sharedPreferences!!.getString(
                            "t_week",
                            Integer.toString(currentWeekOfYear))
            )
        }
    }

    private fun handleResponse(response: String?) {
        if (response != null) {
            availableWeeks.clear()
            availableWeeksNames.clear()

            var i = 0

            Log.d("response", response)
            val responses = response.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (responseString in responses) {
                if (responseString.isNotEmpty()) {
                    val responseStringSplit = responseString.split("\\|".toRegex(), 2).toTypedArray()

                    availableWeeks.add(i, responseStringSplit[0])
                    availableWeeksNames.add(i, responseStringSplit[1])
                    ++i
                }
            }

            val adapter = ArrayAdapter(
                    activity,
                    android.R.layout.simple_spinner_dropdown_item,
                    availableWeeksNames)

            weekSpinner!!.adapter = adapter

            weekSpinner!!.setSelection(
                    availableWeeks.indexOf(
                            sharedPreferences!!.getString(
                                    "t_week",
                                    Integer.toString(currentWeekOfYear))))

            weekSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                    sharedPreferences!!.edit().putString("t_week", availableWeeks[position]).apply()

                    loadTimetable(false)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
        }
    }

    internal interface OnFragmentInteractionListener

    companion object {
        // Locaties
        var locaties = arrayOf("Goes Klein Frankrijk", "Goes Noordhoeklaan", "Goes Stationspark", "Krabbendijke Appelstraat", "Krabbendijke Kerkpolder", "Middelburg", "Tholen")
        // Locaties die in de request URL moeten te komen staan
        var locatiesURL = arrayOf("Goes", "GoesNoordhoeklaan", "GoesStationspark", "KrabbendijkeAppelstraat", "KrabbendijkeKerkpolder", "Middelburg", "Tholen")

        private fun getPixelValue(context: Context, dimenId: Int): Int {
            val resources = context.resources
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dimenId.toFloat(),
                    resources.displayMetrics).toInt()
        }
    }
}// Lege constructor is nodig om een fragment te kunnen gebruiken
