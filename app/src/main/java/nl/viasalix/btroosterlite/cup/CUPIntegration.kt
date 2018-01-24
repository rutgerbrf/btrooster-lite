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

package nl.viasalix.btroosterlite.cup

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
import nl.viasalix.btroosterlite.activities.MainActivity
import java.security.InvalidParameterException
import java.security.SecureRandom
import java.util.*
import kotlin.collections.HashMap

/**
 * CUP integratie voor het rooster
 * @see <a href="https://crooster.tk/doc/api.html">APIDOC</a>
 *
 * @param context   context die moet worden meegegeven voor de sharedPreferences
 */

class CUPIntegration(context: Context) {
    private var queue: RequestQueue = Volley.newRequestQueue(context)
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var availableNames: MutableMap<String, String> = HashMap()
    private val url = "https://" + MainActivity.AUTHORITY + "/api/CupApiServlet"
    private var namesCallback: (Map<String, String>) -> Unit = {}
    private var logInCallback: (String) -> Unit = {}

    val PRESTOK = "ci_preservationToken"
    val CLIENTKEY = "ci_clientKey"

    init {
        // Checkt of de clientKey en bewaartoken al bestaan, anders worden deze gemaakt
        if (sharedPreferences.getString(CLIENTKEY, "").isEmpty())
            sharedPreferences.edit().putString(CLIENTKEY, generateClientKey()).apply()

        if (sharedPreferences.getString(PRESTOK, "").isEmpty())
            getToken()
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

    /**
     * Functie om het bewaartoken op te halen
     *
     * @see <a href="https://crooster.tk/doc/api.html">APIDOC</a>
     */
    private fun getToken() {
        // Zorg dat de request eenmalig wordt verstuurd
        System.setProperty("http.keepAlive", "false")

        // Maak de stringRequest
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    handleResponse(response, ResponseType.PreservationToken)
                    Log.d("RESPONSE: GETTOKEN", response)
                },
                Response.ErrorListener {}) {
            override fun getBodyContentType() =
                    "application/x-www-form-urlencoded; charset=UTF-8"

            /**
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             *
             * @return  map van headers
             * @see     <a href="https://crooster.tk/doc/api.html">APIDOC</a>
             */

            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            CLIENTKEY,
                                            ""))
        }

        // Zorg ervoor dat het request maximaal één keer wordt verstuurd
        stringRequest.retryPolicy = DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        // Voeg het request toe aan de queue
        queue.add(stringRequest)
    }

    /**
     * Genereert een clientKey in de vorm van een String met een SecureRandom en
     * encodet deze met Base64 zodat deze als POST parameter kan worden gestuurd
     *
     * @return  clientKey
     */
    private fun generateClientKey(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        return Base64.encodeToString(
                bytes,
                Base64.DEFAULT
        ).take(32)
    }

    /**
     * Functie om op CUP namen te zoeken op (begin)letters van de achternaam
     *
     * @param letters   letters om mee te zoeken
     * @param callback  functie die wordt uitgevoerd na de response
     * @see             <a href="https://crooster.tk/doc/api.html">APIDOC</a>
     */
    fun searchNames(letters: String, callback: (Map<String, String>) -> Unit) {
        this.namesCallback = callback

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
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             * Bewaartoken=<sp/ci_preservationToken>
             *
             * @return  map van headers
             */

            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            CLIENTKEY,
                                            ""),
                            Params.PreservationToken.param to
                                    sharedPreferences.getString(
                                            PRESTOK,
                                            "")
                    )
        }

        Log.d("PRESTOK (SN)", sharedPreferences.getString(PRESTOK, "NOTHING"))

        queue.add(stringRequest)
    }

    /**
     * Functie om op CUP in te loggen
     *
     * @param name      naam waarmee wordt ingelogd
     * @param pinCode   pincode, nodig om mee in te loggen
     * @param callback  functie die wordt uitgevoerd na een response
     * @see             <a href="https://crooster.tk/doc/api.html">APIDOC</a>
     */
    fun logIn(name: String, pinCode: String, callback: (error: String) -> Unit) {
        this.logInCallback = callback

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
                            Params.UserName.param to name,
                            Params.PinCode.param to pinCode
                    )

            /**
             * Maakt een Map<String, String> van headers in het volgende formaat:
             * Client-Key=<sp/ci_clientKey>
             * Bewaartoken=<sp/ci_preservationToken>
             *
             * @return  map van headers
             */
            override fun getHeaders(): Map<String, String> =
                    hashMapOf(
                            Params.ClientKey.param to
                                    sharedPreferences.getString(
                                            CLIENTKEY,
                                            ""),
                            Params.PreservationToken.param to
                                    sharedPreferences.getString(
                                            PRESTOK,
                                            "")
                    )
        }

        Log.d("PTOK", sharedPreferences.getString(PRESTOK, ""))

        queue.add(stringRequest)
    }

    /**
     * Functie om te bepalen of een response een foutmelding is of niet.
     * Voert daarna de corresponderende functie uit.
     */
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

    /**
     * Functie om normaal antwoord te behandelen
     *
     * @param   response    response van API
     * @param   respType    type response: welke callback moet worden uitgevoerd
     */
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
                            .putString(PRESTOK, values[keys.indexOf(it)].trim())
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
            namesCallback(availableNames)
        else if (respType == ResponseType.LogIn)
            logInCallback(okRes)
    }

    /**
     * Functie die errors gehandelt
     *
     * @param error     foutmelding van de API
     * @param respType  type response: bepaalt welke callback moet worden gedraaid
     * @see             <a href="https://crooster.tk/doc/api.html">APIDOC</a>
     */
    private fun handleError(error: String, respType: ResponseType) {
        // Type error
        val key = error.split("|")[1]

        try {
            // Check de vorm van de exception

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
                /*
                 * Foutmelding in de vorm van:
                 * "ERROR: (<actie>): <foutmelding>"
                 */

                if (error.split("|")[2].isNotEmpty() &&
                        error.split("|")[3].isNotEmpty()) {
                    Log.e("ERROR",
                            e.message + "(" +
                                    error.split("|")[2] + "): \"" +
                                    error.split("|")[3] + "\"")

                    if (respType == ResponseType.LogIn)
                        logInCallback(error.split("|")[3])
                }
            } else {
                /*
                 * Als de foutmelding niet bekend is, spuw dan een error uit over de log
                 */
                Log.e("ERROR", e.message)
            }
        }
    }
}