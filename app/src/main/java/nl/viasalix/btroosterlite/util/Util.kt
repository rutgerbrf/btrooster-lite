package nl.viasalix.btroosterlite.util

import android.content.Context
import android.net.ConnectivityManager

class Util {
    companion object {
        fun online(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            return networkInfo != null && networkInfo.isConnected
        }
    }
}