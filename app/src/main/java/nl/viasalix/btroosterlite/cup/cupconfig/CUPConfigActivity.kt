package nl.viasalix.btroosterlite.cup.cupconfig

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.stepstone.stepper.StepperLayout
import nl.viasalix.btroosterlite.R

class CUPConfigActivity : AppCompatActivity() {
    private var stepperLayout: StepperLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        stepperLayout = findViewById(R.id.stepperLayout)
        stepperLayout!!.adapter = CUPConfigAdapter(supportFragmentManager, this)
    }
}
