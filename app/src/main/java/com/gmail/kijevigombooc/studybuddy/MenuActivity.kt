package com.gmail.kijevigombooc.studybuddy

import StudyType
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gmail.kijevigombooc.studybuddy.adapter.SubjectAdapter
import com.gmail.kijevigombooc.studybuddy.database.DBHelper
import com.gmail.kijevigombooc.studybuddy.databinding.ActivityMenuBinding
import com.google.android.gms.common.util.IOUtils
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.jar.Manifest
import kotlin.concurrent.thread

class MenuActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FILE_OPEN = 123
        const val REQUEST_CODE_READ_PERMISSIONS = 1234
    }

    lateinit var binding : ActivityMenuBinding
    lateinit var permissionAsker : ActivityResultLauncher<Array<String>>
    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissions()

        setStudyTypeToggleButtonText()

        binding.btSubjects.setOnClickListener{
            var intent = Intent(this@MenuActivity, SubjectsActivity::class.java)
            startActivity(intent)
        }

        binding.btRefresh.setOnClickListener{
            val builder = AlertDialog.Builder(this@MenuActivity).apply {
                setTitle("Enter URL:")
                val input = EditText(this@MenuActivity).apply {
                    hint = "https://example.com/Subjects.json"
                }
                setView(input)
                setPositiveButton("Ok") { p0, p1 ->
                    updateSubjectsFromUrl(input.text.toString())
                }
                setNegativeButton("Cancel") { p0, p1 ->}
                show()
            }
        }

        binding.btLoadFromFile.setOnClickListener{
            val intent = Intent()
                .setType("application/octet-stream")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_FILE_OPEN)
        }

        binding.btToggleStudyType.setOnClickListener {
            toggleStudyType()
        }
    }

    private fun setStudyTypeToggleButtonText(){
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val value = sharedPref.getInt(getString(R.string.shared_preference_key_study_type), -1)
        if(value == StudyType.ITERATIVE.ordinal)
            binding.btToggleStudyType.text = "Study type: Iterative"
        else
            binding.btToggleStudyType.text = "Study type: Normal"
    }

    private fun toggleStudyType(){
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val value = sharedPref.getInt(getString(R.string.shared_preference_key_study_type), -1)
        var studyType : StudyType
        if(value == StudyType.ITERATIVE.ordinal)
            studyType = StudyType.NORMAL
        else
            studyType = StudyType.ITERATIVE
        with(sharedPref.edit()){
            putInt(getString(R.string.shared_preference_key_study_type), studyType.ordinal)
            apply()
        }
        setStudyTypeToggleButtonText()
    }

    private fun setupPermissions(){
        val requestPermissions = ActivityResultContracts.RequestMultiplePermissions()
        permissionAsker = registerForActivityResult(requestPermissions) {
            if(it.containsValue(false)){
            }
        }
    }

    private fun updateSubjectsFromUrl(url : String){
        setLoading(true)
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url, {
            thread{
                try {
                    val db = DBHelper(this)
                    db.updateSubjects(JSONObject(it))
                    showToast("Loaded data")
                }catch (ex : Exception){
                    showToast("Error loading data")
                }finally {
                    setLoading(false)
                }
            }
        },{
            showToast("Url unreachable")
            setLoading(false)
        })
        queue.add(stringRequest)
    }

    private fun setLoading(loading : Boolean){
        runOnUiThread {
            if(loading){
                binding.cpiLoading.visibility = View.VISIBLE
                binding.btSubjects.visibility = View.GONE
                binding.btRefresh.visibility = View.GONE
                binding.btLoadFromFile.visibility = View.GONE
                binding.btToggleStudyType.visibility = View.GONE
            }
            else{
                binding.cpiLoading.visibility = View.GONE
                binding.btSubjects.visibility = View.VISIBLE
                binding.btRefresh.visibility = View.VISIBLE
                binding.btLoadFromFile.visibility = View.VISIBLE
                binding.btToggleStudyType.visibility = View.VISIBLE
            }
        }
    }

    private fun showToast(text : String){
        runOnUiThread {
            Toast.makeText(this@MenuActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSubjectsFromFileUri(uri : Uri){
        thread{
            setLoading(true)
            var br : BufferedReader? = null
            try {
                br = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
                val jsonString = br.readText()
                val db = DBHelper(this)
                val jsonObject = JSONObject(jsonString).getJSONObject("Subjects")
                db.updateSubjects(jsonObject)
                showToast("Loaded data")
            }catch (ex : Exception){
                showToast("Error loading data")
            }
            finally {
                br?.close()
                setLoading(false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != RESULT_OK) {
            Toast.makeText(this, "Failed to load file", Toast.LENGTH_SHORT).show()
            return
        }

        if(requestCode == REQUEST_CODE_FILE_OPEN){
            val uri = data?.data
            if(uri == null){
                Toast.makeText(this, "Failed to load file", Toast.LENGTH_SHORT).show()
                return
            }
            updateSubjectsFromFileUri(uri)
        }
    }
}