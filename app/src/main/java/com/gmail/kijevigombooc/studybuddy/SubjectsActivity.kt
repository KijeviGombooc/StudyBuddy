package com.gmail.kijevigombooc.studybuddy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gmail.kijevigombooc.studybuddy.adapter.SubjectAdapter
import com.gmail.kijevigombooc.studybuddy.adapter.SubjectOnScrollListener
import com.gmail.kijevigombooc.studybuddy.database.DBHelper
import com.gmail.kijevigombooc.studybuddy.databinding.ActivitySubjectsBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.concurrent.thread

class SubjectsActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySubjectsBinding
    private val subjectAdapter = SubjectAdapter(this::onSubjectClicked)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSubjects.adapter = subjectAdapter
        val subjectOnScrollListener = object : SubjectOnScrollListener() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val subjectName = subjectAdapter.removeSubject(pos)
                var undo = false
                Snackbar.make(binding.rvSubjects, "Removed $subjectName", Snackbar.LENGTH_LONG)
                    .setAction("Undo"
                    ) {
                        undo = true
                        runOnUiThread {
                            subjectAdapter.addSubject(subjectName, pos)
                        }
                    }.addCallback(object : Snackbar.Callback(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if(!undo){
                                val db = DBHelper(this@SubjectsActivity)
                                thread {
                                    db.deleteSubject(subjectName)
                                }
                            }
                        }
                    }).show()
            }

        }
        ItemTouchHelper(subjectOnScrollListener).attachToRecyclerView(binding.rvSubjects)

        loadSubjects()
    }

    private fun loadSubjects(){
        binding.rvSubjects.visibility = View.GONE
        binding.piSubjects.visibility = View.VISIBLE
        thread {
            val db = DBHelper(this)
            val subjects = db.getSubjects()
            runOnUiThread {
                subjectAdapter.subjects = subjects as MutableList<String>
                binding.rvSubjects.visibility = View.VISIBLE
                binding.piSubjects.visibility = View.GONE
            }
        }

    }

    private fun onSubjectClicked(subject : String){
        val intent = Intent(this, TopicsActivity::class.java)
        intent.putExtra(TopicsActivity.KEY_SUBJECT_NAME, subject)
        startActivity(intent)

    }
}