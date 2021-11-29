package com.gmail.kijevigombooc.studybuddy.database.tables

import android.provider.BaseColumns
import com.gmail.kijevigombooc.studybuddy.database.CreateableTable

object TopicsTable : CreateableTable {
    const val TABLE_NAME = "topics"
    const val COLUMN_NAME_NAME = "name"
    const val COLUMN_NAME_SUBJECT_ROW_ID = "subject_row_id"

    override fun createTableCommand(): String {
        return """CREATE TABLE $TABLE_NAME (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                $COLUMN_NAME_NAME TEXT,
                $COLUMN_NAME_SUBJECT_ROW_ID INTEGER,
                FOREIGN KEY($COLUMN_NAME_SUBJECT_ROW_ID)
                REFERENCES ${SubjectsTable.TABLE_NAME}(${BaseColumns._ID}),
                UNIQUE($COLUMN_NAME_SUBJECT_ROW_ID, $COLUMN_NAME_NAME)
                )"""
    }
}