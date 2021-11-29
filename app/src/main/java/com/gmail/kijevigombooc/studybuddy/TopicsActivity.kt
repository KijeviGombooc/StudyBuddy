package com.gmail.kijevigombooc.studybuddy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.gmail.kijevigombooc.studybuddy.adapter.SubjectAdapter
import com.gmail.kijevigombooc.studybuddy.adapter.TopicAdapter
import com.gmail.kijevigombooc.studybuddy.database.DBHelper
import com.gmail.kijevigombooc.studybuddy.databinding.ActivityTopicsBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlin.concurrent.thread

class TopicsActivity : AppCompatActivity() {

    companion object {
        const val KEY_SUBJECT_NAME = "KEY_SUBJECT_NAME"
    }

    private lateinit var binding : ActivityTopicsBinding
    private lateinit var subject : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val res = intent.getStringExtra(KEY_SUBJECT_NAME)
        if(res == null)
            TODO("Subject can't be null, because that means a button had null text, which shouldn't impossible")
        subject = res
        title = subject

        loadTopics()
    }

    private fun loadTopics(){
        binding.rvTopics.visibility = View.GONE
        binding.piTopics.visibility = View.VISIBLE
        thread {
            val db = DBHelper(this)
            val topics = db.getTopicsOfSubject(subject)
            runOnUiThread {
                binding.rvTopics.adapter = TopicAdapter(topics, this::onTopicClicked)
                binding.rvTopics.visibility = View.VISIBLE
                binding.piTopics.visibility = View.GONE
            }
        }

    }

    private fun onTopicClicked(topic : String) {
        val intent = Intent(this, CardsActivity::class.java)
        intent.putExtra(CardsActivity.KEY_SUBJECT_NAME, subject)
        intent.putExtra(CardsActivity.KEY_TOPIC_NAME, topic)
        startActivity(intent)
    }
}