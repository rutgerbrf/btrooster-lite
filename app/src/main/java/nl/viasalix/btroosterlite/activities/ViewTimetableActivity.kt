package nl.viasalix.btroosterlite.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.getType
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.sdk25.coroutines.onClick

class ViewTimetableActivity : AppCompatActivity() {
    var classSpinner: Spinner? = null
    var locationSpinner: Spinner? = null
    var etCode: EditText? = null
    var btnView: Button? = null
    var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_timetable)
    }

    override fun onStart() {
        super.onStart()

        val toolbar = findViewById<Toolbar>(R.id.vt_toolbar)
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        classSpinner = findViewById(R.id.vt_class_room)
        locationSpinner = findViewById(R.id.vt_location)
        etCode = findViewById(R.id.vt_code)
        btnView = findViewById(R.id.vt_btn_view)
        webView = findViewById(R.id.vt_webview)

        classSpinner!!.isEnabled = false
        classSpinner!!.isClickable = false

        val locationValuesArray = resources.getStringArray(R.array.locatieValues)

        Log.d("lva", locationValuesArray.joinToString())

        locationSpinner!!.setSelection(locationValuesArray.indexOf(defaultSharedPreferences.getString("location", "Goes")))

        etCode!!.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                processCodeInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnView!!.setOnClickListener { viewTimetable() }
    }
    
    override public boolean onOptionsItemSelected(MenuItem item) {
        when (item.id) {
            android.R.id.home => {
                startActivity(Intent(this, MainActivity::class))
                finish()
            }
        }
    }

    private fun processCodeInput() {
        if (getType(etCode!!.text.toString()) == "unknown") {
            classSpinner!!.isEnabled = true
            classSpinner!!.isClickable = true
        } else {
            classSpinner!!.isEnabled = false
            classSpinner!!.isClickable = false
        }
    }

    private fun viewTimetable() {

    }
}
