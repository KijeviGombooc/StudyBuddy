package com.gmail.kijevigombooc.studybuddy.database.tables

import android.provider.BaseColumns
import com.gmail.kijevigombooc.studybuddy.database.CreateableTable

object CardsTable : CreateableTable {
    const val TABLE_NAME = "cards"
    const val COLUMN_NAME_NAME = "name"
    const val COLUMN_NAME_DESC = "desc"
    const val COLUMN_NAME_TOPIC_ROW_ID = "topic_row_id"

    override fun createTableCommand(): String {
        return """CREATE TABLE $TABLE_NAME (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                $COLUMN_NAME_NAME TEXT,
                $COLUMN_NAME_DESC TEXT,
                $COLUMN_NAME_TOPIC_ROW_ID INTEGER,
                FOREIGN KEY($COLUMN_NAME_TOPIC_ROW_ID)
                REFERENCES ${TopicsTable.TABLE_NAME}(${BaseColumns._ID}),
                UNIQUE($COLUMN_NAME_TOPIC_ROW_ID, $COLUMN_NAME_NAME)
                )"""
    }

}