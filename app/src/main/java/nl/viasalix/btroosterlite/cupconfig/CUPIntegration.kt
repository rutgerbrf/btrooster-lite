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

package nl.viasalix.btroosterlite.cupconfig

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import nl.viasalix.btroosterlite.MainActivity
import java.security.InvalidParameterException
import java.security.SecureRandom
import java.util.*
import kotlin.collections.HashMap

/**
 *
 * CUP integratie voor het rooster
 * APIDOC: https://crooster.tk/doc/api.html
 *
 */

class CUPIntegration(context: Context) {
    private var queue: RequestQueue = Volley.newRequestQueue(context)
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var availableNames: MutableMap<String, String> = HashMap()
    private val url = "https://" + MainActivity.AUTHORITY + "/CupServlet"
    private var namesCallbackListener: (Map<String, String>) -> Unit = {}
    private var logInCallbackListener: (String) -> Unit = {}

    init {
        if (sharedPreferences.getString("ci_preservationToken", "").isBlank()) {
            if (sharedPreferences.getString("ci_clientKey", "").isBlank()) {
                getToken(true)
            } else {
                getToken(false)
            }
        } else {
            if (sharedPreferences.getString("ci_clientKey", "").isBlank()) {
                getToken(true)
            } else {
                getToken(false)
            }
        }
    }

    enum class ErrorCode(val code: String) {
        ClientKeyNull("CLIENT_KEY_NULL"),
        ClientKeyInvalidLength("CLIENT_KEY_ONGELDDIGE_LENGTE"),
        PreservationTokenNull("BEWAARTOKEN_NULL"),
        PreservationTokenInvalidOrExpired("BEWAARTOKEN_ONGELDIG_OF_VERLOPEN"),
        LoginAgain("OPNIEUW_INLOGGEN"),
        NotImplemented("NIET_GEIMPLEMENTEERD"),
        ActionUnknown("ACTIE_ONBEKEND"),
        ActionRequiredFirst("VERPLICHT_EERST_ACTIE"),
        ActionRequiredFirstIncorrect("VERPLICHT_ACTIE_INCORRECT"),
        Text("TEKST"),
        SearchLettersNull("ZOEKLETTERS_NULL"),
        SearchLettersTooShort("ZOEKLETTERS_TE_KORT"),
        AlreadyLoggedIn("AL_INGELOGD"),
        UserNameNull("GEBRUIKERSNAAM_NULL"),
        PinCodeNull("PINCODE_NULL")
    }

    enum class ResponseHeaders(val header: String) {
        PreservationToken("BEWAARTOKEN"),
        Ok("OK")
    }

    enum class Actions(val action: String) {
        SearchNames("zoekNamen"),
        LogIn("logIn")
    }

    enum class Params(val param: String) {
        SearchLetters("zoekletters"),
        UserName("gebruikersnaam"),
        PinCode("pincode"),
        ClientKey("Client-Key"),
        PreservationToken("Bewaartoken")
    }

    enum class ResponseType {
        SearchNames,
        LogIn,
        PreservationToken
    }

    private fun getToken(genClientKey: Boolean) {
        if (genClientKey)
            sharedPreferences.edit().putString("ci_clientKey", generateClientKey()).apply()

        Log.d("GETTOKEN", "YE BOI")
        System.setProperty("http.keepAlive", "false")

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response, ResponseType.PreservationToken)
                    Log.d("RESPONSE: GETTOKEN", response)
                },
                Response.ErrorListener {

                }) {
            override fun getBodyContentType() =
                    "application/x-www-form-urlencoded; charset=UTF-8"

            /**
             *
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             *
             */

            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            "ci_clientKey",
                                            ""))
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(stringRequest)
    }

    private fun generateClientKey(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        return Base64.encodeToString(
                bytes,
                Base64.DEFAULT
        ).take(32)
    }

    fun searchNames(letters: String, callbackListener: (Map<String, String>) -> Unit) {
        this.namesCallbackListener = callbackListener

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response, ResponseType.SearchNames)
                    Log.v("RESP: SEARCHNAMES", response)
                },
                Response.ErrorListener {

                }) {
            override fun getBodyContentType() =
                    "application/x-www-form-urlencoded; charset=UTF-8"

            override fun getParams(): MutableMap<String, String> =
                    mutableMapOf(
                            "actie" to Actions.SearchNames.action,
                            Params.SearchLetters.param to letters
                    )

            /**
             *
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             * Bewaartoken=<sp/ci_preservationToken>
             *
             */

            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            "ci_clientKey",
                                            ""),
                            Params.PreservationToken.param to
                                    sharedPreferences.getString(
                                            "ci_preservationToken",
                                            "")
                    )
        }

        Log.d("PRESTOK (SN)", sharedPreferences.getString("ci_preservationToken", "NOTHING"))

        queue.add(stringRequest)
    }

    fun logIn(userName: String, pinCode: String, callbackListener: (error: String) -> Unit) {
        this.logInCallbackListener = callbackListener

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response, ResponseType.LogIn)
                    Log.v("RESP: LOGIN", response)
                },
                Response.ErrorListener {

                }) {
            override fun getBodyContentType() =
                    "application/x-www-form-urlencoded; charset=UTF-8"

            override fun getParams(): MutableMap<String, String> =
                    mutableMapOf(
                            "actie" to Actions.LogIn.action,
                            Params.UserName.param to userName,
                            Params.PinCode.param to pinCode
                    )

            /**
             *
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             * Bewaartoken=<sp/ci_preservationToken>
             *
             */

            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            "ci_clientKey",
                                            ""),
                            Params.PreservationToken.param to
                                    sharedPreferences.getString(
                                            "ci_preservationToken",
                                            "")
                    )
        }

        Log.d("PTOK", sharedPreferences.getString("ci_preservationToken", ""))

        queue.add(stringRequest)
    }

    private fun handleResponse(response: String, respType: ResponseType) {
        if (response.isNotEmpty()) {
            if (response.startsWith("ERR"))
                handleError(response.split("\n")[0].trim(), respType)
            else {
                Log.d("handleNormalResponse", response)
                handleNormalResponse(response, respType)
            }
        }
    }

    private fun handleNormalResponse(response: String, respType: ResponseType) {
        val keys: MutableList<String> = ArrayList()
        val values: MutableList<String> = ArrayList()
        var okRes = ""

        response.split("\n").forEach {
            if (it.split("|").size > 1) {
                keys.add(it.split("|")[0])
                Log.d("adding key", it.split("|")[0])
                values.add(it.split("|")[1])
                Log.d("adding value", it.split("|")[1])
            }
        }

        keys.forEach {
            when (it) {
                ResponseHeaders.PreservationToken.header -> {
                    sharedPreferences.edit()
                            .putString("ci_preservationToken", values[keys.indexOf(it)].trim())
                            .apply()
                    Log.d("BEWAARTOKEN", values[keys.indexOf(it)].trim())
                }
                ResponseHeaders.Ok.header -> {
                    okRes = "Ok"
                }
            // Neemt aan dat de response een lijst van namen is
                else -> {
                    if (respType == ResponseType.SearchNames)
                        availableNames.put(it, values[keys.indexOf(it)])
                }
            }
        }

        if (respType == ResponseType.SearchNames)
            namesCallbackListener(availableNames)
        else if (respType == ResponseType.LogIn)
            logInCallbackListener(okRes)
    }

    private fun handleError(error: String, respType: ResponseType) {
        val key = error.split("|")[1]

        Log.d("handleError key", key)
        Log.d("handleError [0]", error.split("|")[0])

        try {
            when (key) {
                ErrorCode.ClientKeyNull.code ->
                    throw NullPointerException("ClientKeyNull")
                ErrorCode.ClientKeyInvalidLength.code ->
                    throw InvalidParameterException("ClientKeyInvalidLength")
                ErrorCode.PreservationTokenNull.code ->
                    throw NullPointerException("PreservationTokenNull")
                ErrorCode.PreservationTokenInvalidOrExpired.code ->
                    throw InvalidParameterException("PreservationTokenInvalidOrExpired")
                ErrorCode.LoginAgain.code ->
                    throw Exception("LoginAgain")
                ErrorCode.NotImplemented.code ->
                    throw Exception("NotImplemented")
                ErrorCode.ActionUnknown.code ->
                    throw Exception("ActionUnknown")
                ErrorCode.ActionRequiredFirst.code ->
                    throw Exception("ActionRequiredFirst")
                ErrorCode.ActionRequiredFirstIncorrect.code ->
                    throw Exception("ActionRequiredFirstIncorrect")
                ErrorCode.Text.code ->
                    throw Exception("Text")
                ErrorCode.SearchLettersNull.code ->
                    throw NullPointerException("SearchLettersNull")
                ErrorCode.SearchLettersTooShort.code ->
                    throw Exception("SearchLettersTooShort")
                ErrorCode.AlreadyLoggedIn.code ->
                    throw Exception("AlreadyLoggedIn")
                ErrorCode.UserNameNull.code ->
                    throw NullPointerException("UserNameNull")
                ErrorCode.PinCodeNull.code ->
                    throw NullPointerException("PinCodeNull")
            }
        } catch (e: Exception) {
            if (e.message == "ClientKeyInvalidLength") {
                if (error.split("|")[2].isNotEmpty()) {
                    Log.e("ERROR",
                            e.message +
                                    error.split("|")[2])
                }
            } else if (e.message == "Text") {
                if (error.split("|")[2].isNotEmpty() &&
                        error.split("|")[3].isNotEmpty()) {
                    Log.e("ERROR",
                            e.message + "(" +
                                    error.split("|")[2] + "): \"" +
                                    error.split("|")[3] + "\"")

                    if (respType == ResponseType.LogIn)
                        logInCallbackListener(error.split("|")[3])
                }
            } else {
                Log.e("ERROR", e.message)
            }
        }
    }
}