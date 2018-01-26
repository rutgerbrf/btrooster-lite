/*  BTRooster Lite: Roosterapp voor Calvijn College
 *  Copyright (C) 2017 Rutger Broekhoff <rutger broekhoff three at gmail dot com>
 *                 and Jochem Broekhoff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.viasalix.btroosterlite.timetable

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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