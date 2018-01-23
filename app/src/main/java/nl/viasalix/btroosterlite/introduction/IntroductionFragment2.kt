package nl.viasalix.btroosterlite.introduction

import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.activities.MainActivity
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.fragments.TimetableFragment
import nl.viasalix.btroosterlite.cupconfig.CUPConfigActivity
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

        rbG13!!.setOnClickListener {
            rbG46!!.isChecked = false
        }

        rbG46!!.setOnClickListener {
            rbG13!!.isChecked = false
        }
    }

    override fun verifyStep(): VerificationError? {
        if (etCode!!.text.toString().isEmpty())
            return VerificationError("NO_CODE")
        else if (!rbG13!!.isChecked && !rbG46!!.isChecked)
            return VerificationError("NO_GRADE")
        else {
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

                alert("Door in te loggen op CUP kun je je geselecteerde lessen zien in je rooster. Wil je dit doen?",
                        "CUP integratie") {
                    yesButton {
                        sharedPreferences!!.edit()
                                .putBoolean("cupPossible", true)
                                .putBoolean("cupConfigured", false)
                                .putBoolean("cupCancelled", false)
                                .apply()

                        startActivity(Intent(activity, CUPConfigActivity::class.java))
                        activity!!.finish()
                    }
                    noButton {
                        sharedPreferences!!.edit()
                                .putBoolean("cupPossible", true)
                                .putBoolean("cupConfigured", false)
                                .putBoolean("cupCancelled", true)
                                .apply()

                        startActivity(Intent(activity, MainActivity::class.java))
                        activity!!.finish()
                    }
                }.show()
            } else {
                sharedPreferences!!.edit()
                        .putBoolean("cupPossible", false)
                        .putBoolean("cupConfigured", false)
                        .putBoolean("cupCancelled", false)
                        .apply()

                startActivity(Intent(activity, MainActivity::class.java))
                activity!!.finish()
            }
        }

        return null
    }

    override fun onSelected() {

    }

    override fun onError(error: VerificationError) {
        when (error.errorMessage) {
            "NO_CODE" -> {
                ivError!!.visibility = View.VISIBLE
                tvErrorCode!!.visibility = View.VISIBLE
                tvErrorCode!!.text = "Code niet ingevuld"
            }
            "NO_GRADE" -> {
                ivError!!.visibility = View.VISIBLE
                tvErrorCode!!.visibility = View.VISIBLE
                tvErrorCode!!.text = "Geen klas geselecteerd"
            }
        }
    }
}