package nl.viasalix.btroosterlite.cupconfig

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R
import org.jetbrains.anko.support.v4.defaultSharedPreferences

class CUPConfigFragment3 : Fragment(), Step {
    var etPinCode: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.cconf_step3, container, false)

    override fun onStart() {
        super.onStart()

        etPinCode = activity!!.findViewById(R.id.et_pincode)
    }

    override fun verifyStep(): VerificationError? {
        var test = defaultSharedPreferences.getStringSet("cc_name", mutableSetOf())
        test.first()

        CUPConfig.integration!!.logIn(test.first(), etPinCode!!.text.toString())

        return null
    }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {

    }
}