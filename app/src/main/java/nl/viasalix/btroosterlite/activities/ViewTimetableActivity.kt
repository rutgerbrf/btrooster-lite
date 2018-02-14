package nl.viasalix.btroosterlite.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import nl.viasalix.btroosterlite.R
import nl.viasalix.btroosterlite.fragments.TimetableFragment
import nl.viasalix.btroosterlite.timetable.TimetableIntegration
import nl.viasalix.btroosterlite.timetable.TimetableIntegration.Companion.getType
import nl.viasalix.btroosterlite.util.Util
import nl.viasalix.btroosterlite.util.Util.Companion.currentWeekOfYear
import org.jetbrains.anko.defaultSharedPreferences

class ViewTimetableActivity : AppCompatActivity() {
    var classSpinner: Spinner? = null
    var locationSpinner: Spinner? = null
    var weekSpinner: Spinner? = null
    var etCode: EditText? = null
    var btnView: Button? = null
    var webView: WebView? = null
    var saveCheckBox: CheckBox? = null
    var locationValuesArray: Array<String> = arrayOf()
    var availableWeeks: LinkedHashMap<Int, String> = linkedMapOf()

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
        weekSpinner = findViewById(R.id.vt_week)
        etCode = findViewById(R.id.vt_code)
        btnView = findViewById(R.id.vt_btn_view)
        webView = findViewById(R.id.vt_webview)
        saveCheckBox = findViewById(R.id.vt_save_timetable)

        classSpinner!!.isEnabled = false
        classSpinner!!.isClickable = false

        locationValuesArray = resources.getStringArray(R.array.locatieValues)

        locationSpinner!!.setSelection(locationValuesArray.indexOf(defaultSharedPreferences.getString("location", "Goes")))

        etCode!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                processCodeInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val ttIntegration = TimetableIntegration(this,
                defaultSharedPreferences.getString("location", "Goes"),
                defaultSharedPreferences.getString("code", "12345"))

        TimetableFragment.getIndexes(this, ttIntegration, {
            TimetableFragment.handleIndexResponse(
                    this,
                    ttIntegration,
                    weekSpinner!!,
                    it,
                    false,
                    {},
                    false,
                    { availableWeeks = it }
            )
        }, {})

        btnView!!.setOnClickListener { viewTimetable() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return true
            }
        }

        return false
    }

    private fun processCodeInput() {
        val type = getType(etCode!!.text.toString())

        if (type == "unknown") {
            classSpinner!!.isEnabled = true
            classSpinner!!.isClickable = true
        } else {
            classSpinner!!.isEnabled = false
            classSpinner!!.isClickable = false
            classSpinner!!.setSelection(when (type) {
                "s" -> 2
                "t" -> 3
                else -> 2
            })
        }
    }

    private fun viewTimetable() =
            TimetableIntegration(this,
                    locationValuesArray[locationSpinner!!.selectedItemPosition],
                    etCode!!.text.toString()
            ).downloadTimetable(Util.getKeyByIndex(availableWeeks, weekSpinner!!.selectedItemPosition)!!,
                    { data ->
                        webView!!.loadData(data,
                                "text/html; charset=UTF-8",
                                null)
                    },
                    saveCheckBox!!.isChecked,
                    if (getType(etCode!!.text.toString()) == "unknown") {
                        when (classSpinner!!.selectedItemPosition) {
                            0 -> "c"
                            1 -> "r"
                            else -> ""
                        }
                    } else {
                        ""
                    })
}
