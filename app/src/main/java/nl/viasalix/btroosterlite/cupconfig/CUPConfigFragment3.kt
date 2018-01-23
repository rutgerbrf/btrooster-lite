package nl.viasalix.btroosterlite.cupconfig

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.singleton.Singleton
import nl.viasalix.btroosterlite.R
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
        var test = defaultSharedPreferences.getStringSet("cc_name", mutableSetOf())
        test.first()

        Singleton.cupIntegration!!.logIn(test.first(), etPinCode!!.text.toString(), {
            Log.d("it", it)

            if (it == "" || it == "Ok") {
                tvErrorCode!!.visibility = View.INVISIBLE
                tvErrorCode!!.visibility = View.INVISIBLE
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