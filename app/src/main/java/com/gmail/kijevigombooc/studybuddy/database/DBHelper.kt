package com.gmail.kijevigombooc.studybuddy.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gmail.kijevigombooc.studybuddy.database.tables.CardsTable
import com.gmail.kijevigombooc.studybuddy.database.tables.SubjectsTable
import com.gmail.kijevigombooc.studybuddy.database.tables.TopicsTable
import org.json.JSONObject
import java.lang.Exception
import kotlin.concurrent.thread

object DBInfo{
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "StudyBuddy.db"
}

class DBHelper(val context : Context) : SQLiteOpenHelper(context, DBInfo.DATABASE_NAME, null, DBInfo.DATABASE_VERSION){


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SubjectsTable.createTableCommand())
        db?.execSQL(TopicsTable.createTableCommand())
        db?.execSQL(CardsTable.createTableCommand())
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    fun getSubjects(): List<String> {

        val cursor = readableDatabase.query(SubjectsTable.TABLE_NAME, arrayOf(SubjectsTable.COLUMN_NAME_NAME), null, null, null, null, null)
        cursor.moveToFirst()
        val columnIndexName = cursor.getColumnIndex(SubjectsTable.COLUMN_NAME_NAME)
        val subjects = mutableListOf<String>()
        while(!cursor.isAfterLast){
            subjects.add(cursor.getString(columnIndexName))
            cursor.moveToNext()
        }
        readableDatabase.close()
        return subjects
    }

    fun getTopicsOfSubject(subject : String): List<String> {
        val cursor = readableDatabase.query(
            "${SubjectsTable.TABLE_NAME}, ${TopicsTable.TABLE_NAME}",
            arrayOf("${TopicsTable.TABLE_NAME}.${TopicsTable.COLUMN_NAME_NAME}"),
            """${TopicsTable.TABLE_NAME}.${TopicsTable.COLUMN_NAME_SUBJECT_ROW_ID} = ${SubjectsTable.TABLE_NAME}.${BaseColumns._ID}
                AND ${SubjectsTable.TABLE_NAME}.${SubjectsTable.COLUMN_NAME_NAME} = ?
            """, arrayOf(subject),null, null, null)

        cursor.moveToFirst()
        val columnIndexName = cursor.getColumnIndex(TopicsTable.COLUMN_NAME_NAME)
        val topics = mutableListOf<String>()
        while(!cursor.isAfterLast){
            topics.add(cursor.getString(columnIndexName))
            cursor.moveToNext()
        }
        readableDatabase.close()
        return topics
    }

    fun getCardsOfTopic(subject : String, topic : String): List<Pair<String, String>> {
        val cursor = readableDatabase.query(
            "${SubjectsTable.TABLE_NAME}, ${TopicsTable.TABLE_NAME}, ${CardsTable.TABLE_NAME}",
            arrayOf("${CardsTable.TABLE_NAME}.${CardsTable.COLUMN_NAME_NAME}", "${CardsTable.TABLE_NAME}.${CardsTable.COLUMN_NAME_DESC}"),
                """${TopicsTable.TABLE_NAME}.${TopicsTable.COLUMN_NAME_SUBJECT_ROW_ID} = ${SubjectsTable.TABLE_NAME}.${BaseColumns._ID}
                AND ${SubjectsTable.TABLE_NAME}.${SubjectsTable.COLUMN_NAME_NAME} = ?
                AND ${CardsTable.TABLE_NAME}.${CardsTable.COLUMN_NAME_TOPIC_ROW_ID} = ${TopicsTable.TABLE_NAME}.${BaseColumns._ID}
                AND ${TopicsTable.TABLE_NAME}.${TopicsTable.COLUMN_NAME_NAME} = ?
            """, arrayOf(subject, topic),null, null, null)

        cursor.moveToFirst()
        val columnIndexName = cursor.getColumnIndex(CardsTable.COLUMN_NAME_NAME)
        val columnIndexDesc = cursor.getColumnIndex(CardsTable.COLUMN_NAME_DESC)
        val cards = mutableListOf<Pair<String, String>>()
        while(!cursor.isAfterLast){
            cards.add(Pair(cursor.getString(columnIndexName), cursor.getString(columnIndexDesc)))
            cursor.moveToNext()
        }
        readableDatabase.close()
        return cards
    }

    fun deleteSubject(subject : String){
        writableDatabase.delete(
            CardsTable.TABLE_NAME,
        """
            ${CardsTable.COLUMN_NAME_TOPIC_ROW_ID} IN
                (SELECT ${TopicsTable.TABLE_NAME}.${BaseColumns._ID} FROM ${TopicsTable.TABLE_NAME} WHERE
                ${TopicsTable.COLUMN_NAME_SUBJECT_ROW_ID} IN
                    (SELECT ${SubjectsTable.TABLE_NAME}.${BaseColumns._ID} FROM ${SubjectsTable.TABLE_NAME} WHERE
                    ${SubjectsTable.TABLE_NAME}.${SubjectsTable.COLUMN_NAME_NAME} = ?)
                )
            """, arrayOf(subject))

        writableDatabase.delete(
            TopicsTable.TABLE_NAME,
        """
            ${TopicsTable.COLUMN_NAME_SUBJECT_ROW_ID} IN
                (SELECT ${SubjectsTable.TABLE_NAME}.${BaseColumns._ID} FROM ${SubjectsTable.TABLE_NAME} WHERE
                ${SubjectsTable.TABLE_NAME}.${SubjectsTable.COLUMN_NAME_NAME} = ?)
            """, arrayOf(subject))

        writableDatabase.delete(
            SubjectsTable.TABLE_NAME,
            "${SubjectsTable.TABLE_NAME}.${SubjectsTable.COLUMN_NAME_NAME} = ?", arrayOf(subject))

        writableDatabase.close()
    }

    fun updateSubjects(subjects : JSONObject){
        for(i in 0 until subjects.names().length()){
            val subject = subjects.names()[i].toString()
            deleteSubject(subject)
            val cvsSubject = ContentValues().apply {
                put(SubjectsTable.COLUMN_NAME_NAME, subject)
            }
            val subjectID = writableDatabase.insert(SubjectsTable.TABLE_NAME, null, cvsSubject)
            val topics = subjects.getJSONObject(subject)

            for(i in 0 until topics.names().length()){
                val topic = topics.names()[i].toString()
                val cvsTopic = ContentValues().apply {
                    put(TopicsTable.COLUMN_NAME_NAME, topic)
                    put(TopicsTable.COLUMN_NAME_SUBJECT_ROW_ID, subjectID)
                }
                val topicID = writableDatabase.insert(TopicsTable.TABLE_NAME, null, cvsTopic)
                val cards = topics.getJSONObject(topic)

                for(i in 0 until cards.names().length()){
                    val cardName = cards.names()[i].toString()
                    val cardDesc = cards.getString(cardName)
                    val cvsCard = ContentValues().apply {
                        put(CardsTable.COLUMN_NAME_NAME, cardName)
                        put(CardsTable.COLUMN_NAME_DESC, cardDesc)
                        put(CardsTable.COLUMN_NAME_TOPIC_ROW_ID, topicID)
                    }
                    writableDatabase.insert(CardsTable.TABLE_NAME, null, cvsCard)
                }
            }
        }
    }
}