package nl.viasalix.btroosterlite.introduction

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.stepstone.stepper.StepperLayout
import nl.viasalix.btroosterlite.R

class IntroductionActivity : AppCompatActivity() {
    private var stepperLayout: StepperLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        stepperLayout = findViewById(R.id.stepperLayout)
        stepperLayout!!.adapter = IntroductionAdapter(supportFragmentManager, this)
    }
}
