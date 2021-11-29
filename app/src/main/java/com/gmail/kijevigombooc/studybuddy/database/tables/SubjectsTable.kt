package com.gmail.kijevigombooc.studybuddy.database.tables

import android.provider.BaseColumns
import com.gmail.kijevigombooc.studybuddy.database.CreateableTable

object SubjectsTable : CreateableTable {
    const val TABLE_NAME = "subjects"
    const val COLUMN_NAME_NAME = "name"

    override fun createTableCommand(): String {
        return """CREATE TABLE $TABLE_NAME (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                $COLUMN_NAME_NAME TEXT UNIQUE
                )"""
    }
}