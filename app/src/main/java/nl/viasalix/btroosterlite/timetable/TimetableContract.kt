package nl.viasalix.btroosterlite.timetable

import android.provider.BaseColumns

object TimetableContract {
    object Timetable : BaseColumns {
        const val TABLE_NAME = "timetable"
        const val COLUMN_NAME_IDENTIFIER = "identifier"
        const val COLUMN_NAME_TIMETABLE = "html"
    }
}