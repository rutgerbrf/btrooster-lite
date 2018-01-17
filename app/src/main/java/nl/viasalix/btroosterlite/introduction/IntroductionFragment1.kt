package nl.viasalix.btroosterlite.introduction

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import nl.viasalix.btroosterlite.R

class IntroductionFragment1 : Fragment(), Step {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.step1, container, false)

    override fun verifyStep(): VerificationError? = null

    override fun onSelected() {

    }

    override fun onError(error: VerificationError) {

    }
}