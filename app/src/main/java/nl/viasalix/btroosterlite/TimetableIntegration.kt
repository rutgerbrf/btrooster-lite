package nl.viasalix.btroosterlite

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
import nl.viasalix.btroosterlite.cupconfig.CUPIntegration
import java.util.regex.Pattern

class TimetableIntegration(private var context: Context,
                           private var location: String,
                           private var code: String) {

    private var sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
    private val queue = Volley.newRequestQueue(context)
    private val dbHelper = TimetableDbHelper(context)

    fun getIndexes(callback: (String) -> Unit) {
        if (online(context)) {
            val builder = Uri.Builder()
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("RoosterEmbedServlet")
                    .appendQueryParameter("indexOphalen", "1")
                    .appendQueryParameter("locatie", location)
            val url = builder.build().toString()

            val stringRequest = StringRequest(Request.Method.GET, url,
                    { response ->
                        sharedPreferences.edit().putString("t_indexes", response).apply()
                        Log.d("or", response)
                        callback(response)
                    }) { error -> Log.d("error", error.message) }
            queue.add(stringRequest)
        } else {
            val response = sharedPreferences.getString("t_indexes", null)
            callback(response)
        }
    }

    fun downloadAvailableTimetables() {

    }

    fun loadTimetable(week: Int, webView: WebView) {
        if (online(context)) {
            val typeString = getType(code)

            val builder = Uri.Builder()
            builder.scheme("https")
                    .authority(MainActivity.AUTHORITY)
                    .appendPath("RoosterEmbedServlet")
                    .appendQueryParameter("code", code)
                    .appendQueryParameter("locatie", location)
                    .appendQueryParameter("type", typeString)
                    .appendQueryParameter("week", week.toString())
            val url = builder.build().toString()

            val stringRequest = object : StringRequest(Request.Method.GET, url,
                    Response.Listener<String> {
                        if (recordExists("$code|$week")) {
                            updateTimetable("$code|$week", it)
                            webView.loadData(it, "text/html; charset=UTF-8", null)
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

        }
    }

    fun saveTimetableToDatabase(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER, identifier)
            put(TimetableContract.Timetable.COLUMN_NAME_TIMETABLE, timetable)
        }

        val newRowId = db?.insert(TimetableContract.Timetable.TABLE_NAME, null, values)
    }

    fun updateTimetable(identifier: String, timetable: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER, identifier)
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

    fun loadTimetableFromDatabase(identifier: String) {
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

        Log.d("TTS: TTI", timetables.joinToString())
    }

    fun deleteTimetable(identifier: String) {
        val db = dbHelper.writableDatabase

        val selection = "${TimetableContract.Timetable.COLUMN_NAME_TIMETABLE} LIKE ?" +
                "AND ${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} LIKE ?"
        val selectionArgs = arrayOf(identifier)
        db.delete(TimetableContract.Timetable.TABLE_NAME, selection, selectionArgs)
    }

    fun recordExists(identifier: String): Boolean {
        val db = dbHelper.writableDatabase


        val cursor = db.rawQuery(
                "SELECT * FROM ${TimetableContract.Timetable.TABLE_NAME}" +
                        "WHERE ${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} = ?",
                arrayOf(identifier))

        if (cursor != null)
            return true

        return false
    }

    companion object {
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

        fun mobileIsConnected(context: Context): Boolean {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobile = connMgr.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE)

            return mobile.isConnected
        }
    }
}