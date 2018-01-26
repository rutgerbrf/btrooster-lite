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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.singleton.Singleton

class CUPConfigFragment1 : Fragment(), Step {
    private var etSurname: EditText? = null
    private var tvErrorCode: TextView? = null
    private var ivError: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.cconf_step1, container, false)

    override fun onStart() {
        super.onStart()

        etSurname = activity!!.findViewById(R.id.et_surname)
        tvErrorCode = activity!!.findViewById(R.id.tv_error_code)
        ivError = activity!!.findViewById(R.id.iv_error)
    }

    override fun verifyStep(): VerificationError? =
            if (etSurname!!.text.length < 3 || etSurname!!.text.length > 7)
                VerificationError("INCORRECT_SIZE")
            else {
                // Correct ingevuld, laat de foutmelding (niet meer) zien (voor als de gebruiker terugkomt)
                tvErrorCode!!.visibility = View.INVISIBLE
                ivError!!.visibility = View.INVISIBLE

                // Zet de letters in de singleton voor gebruik in het volgende fragment
                Singleton.name = etSurname!!.text.toString()

                null
            }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {
        when (error.errorMessage) {
            "INCORRECT_SIZE" -> {
                // Foutmelding staat hardcoded in res/layout/cconf_step1.xml
                tvErrorCode!!.visibility = View.VISIBLE
                ivError!!.visibility = View.VISIBLE
            }
        }
    }
}
