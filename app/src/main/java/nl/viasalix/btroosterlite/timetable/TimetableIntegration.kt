package nl.viasalix.btroosterlite.timetable

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.WebView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.cup.CUPIntegration
import nl.viasalix.btroosterlite.util.Util.Companion.online
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

    /**
     * Functie die de URL om het rooster op te halen bouwt
     *
     * @param week  week in het jaar van het rooster
     * @return      URL om het rooster op te halen
     */
    fun buildURL(week: Int): String {
        // Bepaalt of de gebruiker een docent, klas of leerling is
        val typeString = getType(code)

        val builder = Uri.Builder()
        builder.scheme("https")
                .authority(MainActivity.AUTHORITY)
                .appendPath("RoosterEmbedServlet")
                .appendQueryParameter("code", code)
                .appendQueryParameter("locatie", location)
                .appendQueryParameter("type", typeString)
                .appendQueryParameter("week", String.format("%02d", week))
        return builder.build().toString()
    }

    fun downloadAvailableTimetables() {
        val onlyWifiPref = sharedPreferences.getBoolean("t_preload_only_wifi", true)

        if (online(context)) {
            if (wifiIsConnected(context) == onlyWifiPref) {
                getIndexes { it, _ ->
                    val response = handleIndexResponse(it)

                    doAsync {
                        response.forEach {
                            Log.d("TimetableIntegration", "Downloading timetable for week ${it.key}")
                            downloadTimetable(it.key, {})
                        }
                    }
                }
            }
        }
    }

    /**
     * Functie die het rooster met de gegeven week downloadt en daarna (eventueel) de callback
     * uitvoert.
     *
     * @param week      week in het jaar van het rooster
     * @param callback  functie die wordt uitgevoerd als de stringRequest een response heeft
     */
    private fun downloadTimetable(week: Int, callback: (String) -> Unit) {
        /*
         * Identifier die wordt gebruikt als key in de database
         * Ziet er als volgt uit: "<code>|<week>"
         */

        val identifier = "$code|$week"

        // Check of de gebruiker online is
        if (online(context)) {
            /*
             *  Maak een nieuw StringRequest aan en override een aantal functies om de goede
             *  parameters mee te geven
             */

            val stringRequest = object : StringRequest(
                    Request.Method.GET, buildURL(week),
                    Response.Listener<String> {
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

    private fun loadDataInWebView(data: String, webView: WebView) {
        webView.loadData(data, "text/html; charset=UTF-8", null)
    }

    fun saveTimetableToDatabase(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER, identifier)
            put(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE, timetable)
        }

        db?.insert(TimetableContract.Timetable.TABLE_NAME, null, values)
    }

    fun updateTimetable(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE, timetable)
        }

        val selection = "${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} LIKE ?"
        val selectionArgs = arrayOf(identifier)
        val count = db.update(
                TimetableContract.Timetable.TABLE_NAME,
                values,
                selection,
                selectionArgs)
    }

    private fun loadTimetableFromDatabase(identifier: String): String {
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

        return if (timetables.isNotEmpty())
            timetables[0]
        else
            ""
    }

    private fun deleteTimetable(identifier: String) {
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

        fun getType(code: String): String {
            val docentPatternInput = "([A-Za-z]){3}"
            val leerlingPatternInput = "([0-9]){5}"

            val docentPattern = Pattern.compile(docentPatternInput)
            val leerlingPattern = Pattern.compile(leerlingPatternInput)

            if (!docentPattern.matcher(code).matches() &&
                    !leerlingPattern.matcher(code).matches()) {
                return "c"
            } else if (docentPattern.matcher(code).matches()) {
                return "t"
            } else if (leerlingPattern.matcher(code).matches()) {
                return "s"
            }

            return "none"
        }

        fun wifiIsConnected(context: Context): Boolean {
            val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE)
                    as WifiManager

            if (manager.isWifiEnabled) {
                val wifiInfo = manager.connectionInfo

                if (wifiInfo.networkId == -1)
                    return false

                return true
            } else
                return false
        }

        fun handleIndexResponse(response: String?): LinkedHashMap<Int, String> {
            val indexes: LinkedHashMap<Int, String> = linkedMapOf()

            if (response != null) {
                val responses = response.trim().split("\n")

                responses
                        .filter { it.isNotEmpty() }
                        .map { it.split("|") }
                        .forEach { responseWeek ->
                            indexes[responseWeek[0].toInt()] = responseWeek[1]
                        }
            }

            return indexes
        }
    }
}