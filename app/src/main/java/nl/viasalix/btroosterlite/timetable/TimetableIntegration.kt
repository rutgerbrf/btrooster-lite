package nl.viasalix.btroosterlite.timetable

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.WebView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.cup.CUPIntegration
import org.jetbrains.anko.doAsync
import java.util.regex.Pattern

class TimetableIntegration(private var context: Context,
                           private var location: String,
                           private var code: String) {

    private var sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
    private val queue = Volley.newRequestQueue(context)
    private val dbHelper = TimetableDbHelper(context)

    fun getIndexes(callback: (String, Boolean) -> Unit) {
        if (online(context)) {
            val builder = Uri.Builder()
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("api")
                    .appendPath("RoosterApiServlet")
                    .appendQueryParameter("actie", "weken")
                    .appendQueryParameter("locatie", location)
            val url = builder.build().toString()

            Log.d("url", url)

            val stringRequest = StringRequest(Request.Method.GET, url,
                    { response ->
                        sharedPreferences.edit().putString("t_indexes", response).apply()
                        Log.d("or", response)
                        callback(response, true)
                    }) { error -> Log.d("error", error.message) }
            queue.add(stringRequest)
        } else {
            val response = sharedPreferences.getString("t_indexes", null)
            callback(response, false)
        }
    }

    private fun handleResponse(response: String?): MutableMap<Int, String> {
        val indexes: MutableMap<Int, String> = mutableMapOf()

        if (response != null) {
            val responses = response.trim().split("\n")

            responses
                    .filter { it.isNotEmpty() }
                    .map { it.split("|") }
                    .forEach { responseWeek ->
                        indexes[responseWeek[0].toInt()] = ""
                    }
        }

        return indexes
    }

    fun downloadAvailableTimetables() {
        val onlyWifiPref = sharedPreferences.getBoolean("t_preload_only_wifi", true)

        if (online(context)) {
            if (wifiIsConnected(context) == onlyWifiPref) {
                getIndexes { it, _ ->
                    val response = handleResponse(it)

                    doAsync {
                        response.forEach {
                            downloadTimetable(it.key, {})
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * Functie die het rooster met de gegeven week downloadt en daarna (eventueel) de callback
     * uitvoert.
     *
     * @param week      week in het jaar van het rooster
     * @param callback  functie die wordt uitgevoerd als de stringRequest een response heeft
     *
     */
    fun downloadTimetable(week: Int, callback: (String) -> Unit) {
        /*
         * Identifier die wordt gebruikt als key in de database
         * Ziet er als volgt uit: "<code>|<week>"
         */

        val identifier = "$code|$week"

        // Check of de gebruiker online is
        if (online(context)) {
            val typeString = getType(code)

            // Maak de URL
            val builder = Uri.Builder()
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("RoosterEmbedServlet")
                    .appendQueryParameter("code", code)
                    .appendQueryParameter("locatie", location)
                    .appendQueryParameter("type", typeString)
                    .appendQueryParameter("week", week.toString())
            val url = builder.build().toString()


            /*
             *  Maak een nieuw StringRequest aan en override een aantal functies om de goede
             *  parameters mee te geven
             */

            val stringRequest = object : StringRequest(Request.Method.GET, url,
                    Response.Listener<String> {
                        Log.d("data (getTimetable, ttInteg", "data: $it")
                        Log.d("recexists", "${recordExists(identifier)}")

                        if (recordExists(identifier)) {
                            updateTimetable(identifier, it)
                            callback(it)
                        } else {
                            saveTimetableToDatabase(identifier, it)
                            callback(it)
                        }
                    },
                    Response.ErrorListener {

                    }) {
                override fun getBodyContentType() =
                        "application/x-www-form-urlencoded; charset=UTF-8"

                /**
                 *
                 * Maakt een Map<String, String> van headers in het volgende formaat:
                 * Client-Key=<sp/ci_clientKey>
                 * Bewaartoken=<sp/ci_preservationToken>
                 *
                 */

                override fun getHeaders(): Map<String, String> =
                        hashMapOf(
                                CUPIntegration.Params.ClientKey.param to
                                        sharedPreferences.getString(
                                                "ci_clientKey",
                                                ""),
                                CUPIntegration.Params.PreservationToken.param to
                                        sharedPreferences.getString(
                                                "ci_preservationToken",
                                                "")
                        )
            }

            queue.add(stringRequest)
        } else {
            // Als de gebruiker offline is wordt de volgende code uitgoevoerd

            // Check of het rooster al in de database staat
            if (recordExists(identifier)) {
                // Laad het rooster uit de database
                val data = loadTimetableFromDatabase(identifier)

                if (data.isNotEmpty()) {
                    // Voer de callback uit met de response als argument
                    callback(data)
                } else {
                    // Geef een foutmelding
                    callback(errorMessage)
                }
            } else {
                // Geef een foutmelding
                callback(errorMessage)
            }
        }
    }

    fun loadTimetable(week: Int, webView: WebView) {
        downloadTimetable(week, {
            loadDataInWebView(it, webView)
        })
    }

    fun loadDataInWebView(data: String, webView: WebView) {
        webView.loadData(data, "text/html; charset=UTF-8", null)
    }

    fun saveTimetableToDatabase(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER, identifier)
            put(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE, timetable)
        }

        Log.d("SAVING", "SAVING DATA: id: $identifier, tt: $timetable")

        val newRowId = db?.insert(TimetableContract.Timetable.TABLE_NAME, null, values)
    }

    fun updateTimetable(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE, timetable)
        }

        val selection = "${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} = ?"
        val selectionArgs = arrayOf(identifier)
        val count = db.update(
                TimetableContract.Timetable.TABLE_NAME,
                values,
                selection,
                selectionArgs)
    }

    fun loadTimetableFromDatabase(identifier: String): String {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE)

        val selection = "${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} = ?"
        val selectionArgs = arrayOf(identifier)

        val cursor = db.query(
                TimetableContract.Timetable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        )

        val timetables = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val timetable = getString(getColumnIndexOrThrow(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE))
                timetables.add(timetable)
            }
        }

        Log.d("test ltfd", "yo")
        Log.d("timetables", timetables.joinToString())

        if (timetables.isNotEmpty())
            return timetables[0]
        else
            return ""
    }

    fun deleteTimetable(identifier: String) {
        val db = dbHelper.writableDatabase

        val selection = "${TimetableContract.Timetable.COLUMN_NAME_TIMETABLE} LIKE ?" +
                "AND ${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} LIKE ?"
        val selectionArgs = arrayOf(identifier)
        db.delete(TimetableContract.Timetable.TABLE_NAME, selection, selectionArgs)
    }

    fun recordExists(identifier: String): Boolean {
        val db = dbHelper.readableDatabase

        val selection = "SELECT * FROM ${TimetableContract.Timetable.TABLE_NAME} WHERE ${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} = ?"
        val selectionArgs = arrayOf(identifier)
        val cursor = db.rawQuery(selection, selectionArgs)

        Log.d("cursor.count", cursor.count.toString())
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }

        cursor.close()
        return true
    }

    companion object {
        const val errorMessage =
                "Dit rooster kon niet geladen worden"

        fun getType(code: String?): String {
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

        fun online(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            return networkInfo != null && networkInfo.isConnected
        }

        fun wifiIsConnected(context: Context): Boolean {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifi = connMgr.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI)

            return wifi.isConnected
        }
    }
}