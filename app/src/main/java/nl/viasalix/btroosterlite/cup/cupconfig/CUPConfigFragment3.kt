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

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.singleton.Singleton
import org.jetbrains.anko.support.v4.defaultSharedPreferences

class CUPConfigFragment3 : Fragment(), Step {
    private var etPinCode: EditText? = null
    private var tvErrorCode: TextView? = null
    private var ivError: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.cconf_step3, container, false)

    override fun onStart() {
        super.onStart()

        etPinCode = activity!!.findViewById(R.id.et_pincode)
        tvErrorCode = activity!!.findViewById(R.id.tv_error_code_s3)
        ivError = activity!!.findViewById(R.id.iv_error_s3)
    }

    override fun verifyStep(): VerificationError? {
        val name = defaultSharedPreferences.getString("cc_name", "")

        Singleton.cupIntegration!!.logIn(name, etPinCode!!.text.toString(), {
            Log.d("it", it)

            if (it == "" || it == "Ok") {
                tvErrorCode!!.visibility = View.INVISIBLE
                tvErrorCode!!.visibility = View.INVISIBLE

                defaultSharedPreferences.edit()
                        .putBoolean("cupConfigured", true)
                        .apply()

                startActivity(Intent(activity!!, MainActivity::class.java))
            } else
                onError(VerificationError(it))
        })

        return null
    }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {
        tvErrorCode!!.text = error.errorMessage

        tvErrorCode!!.visibility = View.VISIBLE
        ivError!!.visibility = View.VISIBLE
    }
}