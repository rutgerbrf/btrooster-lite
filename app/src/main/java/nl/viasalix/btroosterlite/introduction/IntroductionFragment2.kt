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

package nl.viasalix.btroosterlite.introduction

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.cup.cupconfig.CUPConfigActivity
import nl.viasalix.btroosterlite.fragments.TimetableFragment
import nl.viasalix.btroosterlite.timetable.TimetableIntegration
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.yesButton

class IntroductionFragment2 : Fragment(), Step {
    private var etCode: EditText? = null
    private var spLocation: Spinner? = null
    private var ivError: ImageView? = null
    private var tvErrorCode: TextView? = null
    private var sharedPreferences: SharedPreferences? = null
    private var rbG13: RadioButton? = null
    private var rbG46: RadioButton? = null
    private var rbTeacher: RadioButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.int_step2, container, false)

    override fun onStart() {
        super.onStart()
        etCode = activity!!.findViewById(R.id.et_code)
        spLocation = activity!!.findViewById(R.id.sp_location)
        ivError = activity!!.findViewById(R.id.iv_error)
        tvErrorCode = activity!!.findViewById(R.id.tv_error_code_s2)
        sharedPreferences = defaultSharedPreferences
        rbG13 = activity!!.findViewById(R.id.rbG13)
        rbG46 = activity!!.findViewById(R.id.rbG46)
        rbTeacher = activity!!.findViewById(R.id.rbTeacher)

        rbG13!!.setOnClickListener {
            rbG46!!.isChecked = false
            rbTeacher!!.isChecked = false
        }

        rbG46!!.setOnClickListener {
            rbG13!!.isChecked = false
            rbTeacher!!.isChecked = false
        }

        rbTeacher!!.setOnClickListener {
            rbG13!!.isChecked = false
            rbG46!!.isChecked = false
        }
    }

    override fun verifyStep(): VerificationError? {
        if (etCode!!.text.toString().isEmpty())
            return VerificationError("NO_CODE")
        else if (!rbG13!!.isChecked && !rbG46!!.isChecked)
            return VerificationError("NO_GRADE")
        else {
            if (TimetableIntegration.getType(etCode!!.text.toString()) != "unknown") {
                val location: String = TimetableFragment.locatiesURL[spLocation!!.selectedItemPosition]

                ivError!!.visibility = View.INVISIBLE
                tvErrorCode!!.visibility = View.INVISIBLE

                sharedPreferences!!.edit()
                        .putString("code", etCode!!.text.toString())
                        .putString("location", location)
                        .apply()

                sharedPreferences!!.edit()
                        .putBoolean("firstLaunch", false)
                        .apply()

                if (rbG46!!.isChecked && location == "Goes") {
                    Log.d("isChecked", rbG46!!.isChecked.toString())
                    Log.d("location", location)

                    alert(getString(R.string.alert_cupintegration_text),
                            getString(R.string.alert_cupintegration_title)) {
                        yesButton {
                            sharedPreferences!!.edit()
                                    .putBoolean("cupPossible", true)
                                    .putBoolean("cupCancelled", false)
                                    .apply()

                            startActivity(Intent(activity, CUPConfigActivity::class.java))
                            activity!!.finish()
                        }
                        noButton {
                            sharedPreferences!!.edit()
                                    .putBoolean("cupPossible", true)
                                    .putBoolean("cupCancelled", true)
                                    .apply()

                            startActivity(Intent(activity, MainActivity::class.java))
                            activity!!.finish()
                        }
                    }.show()
                } else {
                    sharedPreferences!!.edit()
                            .putBoolean("cupPossible", false)
                            .putBoolean("cupCancelled", false)
                            .apply()

                    startActivity(Intent(activity, MainActivity::class.java))
                    activity!!.finish()
                }
            } else {
                return VerificationError("NOT_ACCEPTED")
            }
        }

        return null
    }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {
        when (error.errorMessage) {
            "NO_CODE" -> {
                ivError!!.visibility = View.VISIBLE
                tvErrorCode!!.visibility = View.VISIBLE
                tvErrorCode!!.text = getString(R.string.error_nocode)
            }
            "NO_GRADE" -> {
                ivError!!.visibility = View.VISIBLE
                tvErrorCode!!.visibility = View.VISIBLE
                tvErrorCode!!.text = getString(R.string.error_nograde)
            }
            "NOT_ACCEPTED" -> {
                ivError!!.visibility = View.VISIBLE
                tvErrorCode!!.visibility = View.VISIBLE
                tvErrorCode!!.text = getString(R.string.int_error_code_not_accepted)
            }
        }
    }
}