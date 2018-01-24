package nl.viasalix.btroosterlite.cup.cupconfig

import android.support.v4.app.Fragment
import android.os.Bundle
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
import nl.viasalix.btroosterlite.singleton.Singleton
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.cup.CUPIntegration
import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.online
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

        if (online(activity!!)) {
            if (defaultSharedPreferences.getString("ci_preservationToken", "").isEmpty()) {
                Singleton.cupIntegration = CUPIntegration(activity!!)
            }
        } else {
            alert("Je bent niet verbonden met het internet. Opnieuw proberen?",
                    "Opnieuw proberen?") {
                yesButton {
                    searchNames()
                }

                noButton {
                    activity!!.finish()
                }
            }

            Singleton.cupIntegration!!.searchNames(Singleton.name, {
                nameMap = it.toMutableMap()

                it.forEach {
                    val rb = RadioButton(activity)
                    rb.text = it.value

                    rgName!!.addView(rb)
                }
            })
        }
    }

    override fun verifyStep(): VerificationError? {
        if (rgName!!.checkedRadioButtonId == -1)
            return VerificationError("NO_SELECTION")
        else {
            val selectedText: String = (rgName!!.findViewById(rgName!!.checkedRadioButtonId) as RadioButton).text.toString()

            tvErrorCode!!.visibility = View.INVISIBLE
            ivError!!.visibility = View.INVISIBLE

            var key = ""
            nameMap.forEach {
                if (it.value == selectedText)
                    key = it.key
            }

            val nameKeyVal: Set<String> = setOf(
                    key,
                    selectedText
            )

            defaultSharedPreferences.edit().putStringSet("cc_name", nameKeyVal).apply()
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