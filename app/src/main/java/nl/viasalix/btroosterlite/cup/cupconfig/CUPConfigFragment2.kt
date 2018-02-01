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

package nl.viasalix.btroosterlite.cup.cupconfig

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.cup.CUPIntegration
import nl.viasalix.btroosterlite.singleton.Singleton
import nl.viasalix.btroosterlite.util.Util.Companion.online
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.yesButton

class CUPConfigFragment2 : Fragment(), Step {
    private var rgName: RadioGroup? = null
    private var nameMap: MutableMap<String, String> = HashMap()
    private var tvErrorCode: TextView? = null
    private var ivError: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.cconf_step2, container, false)

    override fun onStart() {
        super.onStart()

        rgName = activity!!.findViewById(R.id.rg_name)
        tvErrorCode = activity!!.findViewById(R.id.tv_error_code_s2)
        ivError = activity!!.findViewById(R.id.iv_error_s2)
    }

    private fun searchNames() {
        rgName!!.removeAllViewsInLayout()
        rgName!!.clearCheck()
        rgName!!.isSelected = false
        rgName!!.check(-1)

        nameMap.clear()

        if (activity != null) {
            if (online(activity!!.applicationContext)) {
                if (defaultSharedPreferences.getString("ci_preservationToken", "").isEmpty()) {
                    Singleton.cupIntegration = CUPIntegration(activity!!)
                }

                Singleton.cupIntegration!!.searchNames(Singleton.name, {
                    nameMap = it.toMutableMap()

                    it.forEach {
                        val rb = RadioButton(activity)
                        rb.text = it.value

                        rgName!!.addView(rb)
                    }
                })
            } else {
                alertNotConnected()
            }
        }
    }

    private fun alertNotConnected() {
        alert(context!!.getString(R.string.alert_notconnected_text),
                context!!.getString(R.string.alert_notconnected_title)) {
            yesButton {
                searchNames()
            }

            noButton {
                activity!!.finish()
            }
        }.show()
    }


    override fun verifyStep(): VerificationError? {
        if (rgName!!.checkedRadioButtonId == -1)
            return VerificationError("NO_SELECTION")
        else {
            val selectedText: String = (rgName!!.findViewById(rgName!!.checkedRadioButtonId) as RadioButton).text.toString()

            tvErrorCode!!.visibility = View.INVISIBLE
            ivError!!.visibility = View.INVISIBLE

            nameMap.forEach {
                if (it.value == selectedText)
                    defaultSharedPreferences.edit().putString("cc_name", it.key).apply()
            }
        }

        return null
    }

    override fun onSelected() {
        searchNames()
    }

    override fun onError(error: VerificationError) {
        Log.d("ERROR", error.errorMessage)

        when (error.errorMessage) {
            "NO_SELECTION" -> {
                tvErrorCode!!.visibility = View.VISIBLE
                ivError!!.visibility = View.VISIBLE
            }
        }
    }
}