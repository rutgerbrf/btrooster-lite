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

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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

    init {
        if (sharedPreferences.getString("ci_clientKey", "").isBlank()) {
            getToken(true)
        } else {
            getToken(false)
        }

        createSession()
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

    private fun getToken(genClientKey: Boolean) {
        if (genClientKey)
            sharedPreferences.edit().putString("ci_clientKey", generateClientKey()).apply()

        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response)
                    Log.d("RESPONSE", response)
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

    private fun createSession() {
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response)
                    Log.v("RESP", response)
                },
                Response.ErrorListener {

                }) {
            override fun getBodyContentType() =
                    "application/x-www-form-urlencoded; charset=UTF-8"

            /**
             *
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientkey>,
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

        queue.add(stringRequest)
    }

    fun searchNames(letters: String) {
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response)
                    Log.v("RESP", response)
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

        queue.add(stringRequest)
    }

    private fun logIn(userName: String, pinCode: String) {
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response)
                    Log.v("RESP", response)
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

        queue.add(stringRequest)
    }

    private fun handleResponse(response: String) {
        if (response.isNotEmpty()) {
            if (response.startsWith("ERR"))
                handleError(response.split("\n")[0].trim())
            else {
                Log.d("handleNormalResponse", response)
                handleNormalResponse(response)
            }
        }
    }

    private fun handleNormalResponse(response: String) {
        val keys: MutableList<String> = ArrayList()
        val values: MutableList<String> = ArrayList()

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
                ResponseHeaders.PreservationToken.header ->
                    sharedPreferences.edit()
                            .putString("ci_preservationToken", values[keys.indexOf(it)].trim())
                            .apply()
                ResponseHeaders.Ok.header -> {
                }
            // Neemt aan dat de response een lijst van namen is
                else -> availableNames.put(it, values[keys.indexOf(it)])
            }
        }

        Log.d("availableNames", availableNames.toString())
    }

    private fun handleError(error: String) {
        val key = error.split("|")[1]

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
            if (e.message == ErrorCode.ClientKeyInvalidLength.code) {
                if (error.split("|")[2].isNotEmpty()) {
                    Log.e("ERROR",
                            e.message +
                                    error.split("|")[2])
                }
            } else if (e.message == ErrorCode.Text.code) {
                if (error.split("|")[2].isNotEmpty() &&
                        error.split("|")[3].isNotEmpty()) {
                    Log.e("ERROR",
                            e.message +
                                    error.split("|")[2] +
                                    error.split("|")[3])
                }
            } else {
                Log.e("ERROR", e.message)
            }
        }
    }

    interface CUPIntegrationListener {
        fun ciCallback(view: View, result: String)
    }
}