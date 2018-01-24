package nl.viasalix.btroosterlite.cup.cupconfig

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

    override fun verifyStep(): VerificationError? {
        return if (etSurname!!.text.length < 3 || etSurname!!.text.length > 7)
            VerificationError("INCORRECT_SIZE")
        else {
            tvErrorCode!!.visibility = View.INVISIBLE
            ivError!!.visibility = View.INVISIBLE

            Singleton.name = etSurname!!.text.toString()
            Log.d("surname: ", etSurname!!.text.toString())

            null
        }
    }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {
        when (error.errorMessage) {
            "INCORRECT_SIZE" -> {
                tvErrorCode!!.visibility = View.VISIBLE
                ivError!!.visibility = View.VISIBLE
            }
        }
    }
}