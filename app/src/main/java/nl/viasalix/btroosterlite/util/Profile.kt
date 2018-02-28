package nl.viasalix.btroosterlite.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Profile(private var profileKey: String, context: Context) {
    private var code = ""
    private var type = ""
    private var location = ""
    private var name = ""
    private var cupPossible = false
    private var cupConfigured = false
    private var pToken = ""
    private var cKey = ""

    init {
        val sharedPreferences = getSharedPreferences(context)

        if (PreferenceManager.getDefaultSharedPreferences(context).getStringSet("profiles", setOf("")).contains(profileKey)) {
            code = sharedPreferences.getString("code", "12345")
            type = sharedPreferences.getString("type", "s")
            location = sharedPreferences.getString("location", "Goes")
            cupPossible = sharedPreferences.getBoolean("cupPossible", false)
            cupConfigured = sharedPreferences.getBoolean("cupConfigured", false)
            pToken = sharedPreferences.getString("preservationToken", "")
            cKey = sharedPreferences.getString("clientKey", "")
        } else {

        }
    }

    fun setCode(code: String, context: Context) {
        this.code = code

        getSharedPreferences(context).update("code", this.code)
    }

    fun setType(type: String, context: Context) {
        this.type = type

        getSharedPreferences(context).update("type", this.type)
    }

    fun setLocation(location: String, context: Context) {
        this.location = location

        getSharedPreferences(context).update("location", this.location)
    }

    fun setName(name: String, context: Context) {
        this.name = name

        getSharedPreferences(context).update("name", this.name)
    }

    fun setPreservationToken(pToken: String, context: Context) {
        this.pToken = pToken

        getSharedPreferences(context).update("preservationToken", this.pToken)
    }

    fun setClientKey(cKey: String, context: Context) {
        this.cKey = cKey

        getSharedPreferences(context).update("clientKey", this.pToken)
    }

    fun setCupPossible(cupPossible: Boolean, context: Context) {
        this.cupPossible = cupPossible

        getSharedPreferences(context).update("cupPossible", this.cupPossible)
    }

    fun setCupConfigured(cupConfigured: Boolean, context: Context) {
        this.cupConfigured = cupConfigured

        getSharedPreferences(context).update("cupPossible", this.cupConfigured)
    }

    private fun SharedPreferences.update(key: String, newVal: String) =
            this.edit().putString(key, newVal).apply()

    private fun SharedPreferences.update(key: String, newVal: Boolean) =
            this.edit().putBoolean(key, newVal).apply()

    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("profile_$profileKey", MODE_PRIVATE)
}