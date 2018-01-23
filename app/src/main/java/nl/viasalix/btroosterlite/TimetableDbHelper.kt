package nl.viasalix.btroosterlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

private const val SQL_CREATE_TIMETABLES =
        "CREATE TABLE ${TimetableContract.Timetable.TABLE_NAME} (" +
                "${TimetableContract.Timetable.COLUMN_NAME_IDENTIFIER} VARCHAR(20) PRIMARY KEY," +
                "${TimetableContract.Timetable.COLUMN_NAME_TIMETABLE} TEXT)"

private const val SQL_DELETE_TIMETABLES = "DROP TABLE IF EXISTS ${TimetableContract.Timetable.TABLE_NAME}"

class TimetableDbHelper(context: Context) : SQLiteOpenHelper(context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TIMETABLES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_TIMETABLES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // Versie verhogen wanneer schema veranderd
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "BTRoosterTTS.db"
    }
}

object TimetableContract {
    object Timetable : BaseColumns {
        const val TABLE_NAME = "timetable"
        const val COLUMN_NAME_IDENTIFIER = "testyouboi"
        const val COLUMN_NAME_TIMETABLE = "html"
    }
}