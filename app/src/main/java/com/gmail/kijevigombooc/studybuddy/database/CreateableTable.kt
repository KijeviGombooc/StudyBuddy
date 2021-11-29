package com.gmail.kijevigombooc.studybuddy.database

import android.provider.BaseColumns

interface CreateableTable : BaseColumns {
    fun createTableCommand() : String
}