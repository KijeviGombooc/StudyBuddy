package com.gmail.kijevigombooc.studybuddy

import StudyType
import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.core.animation.addListener
import com.gmail.kijevigombooc.studybuddy.database.DBHelper
import com.gmail.kijevigombooc.studybuddy.databinding.ActivityCardsBinding
import com.gmail.kijevigombooc.studybuddy.ui.CardMovement
import kotlin.concurrent.thread
import kotlin.random.Random

class CardsActivity : AppCompatActivity(), CardMovement.SwipeListener {

    companion object {
        const val KEY_SUBJECT_NAME = "KEY_SUBJECT_NAME"
        const val KEY_TOPIC_NAME = "KEY_TOPIC_NAME"
    }

    private lateinit var binding : ActivityCardsBinding
    private lateinit var totalCards : List<Pair<String, String>>
    //private var actualCards : MutableList<Pair<String ,String>> = arrayListOf()
    private var actualCardIndices : MutableList<Int> = arrayListOf()
    private lateinit var cardMovement : CardMovement
    private var iterativeLimit = -1
    private var frontCardIndex = -1
    private var backCardIndex = -1
    private lateinit var flipToAnim : Animator
    private lateinit var flipBackAnim : Animator
    private lateinit var growAnim : Animator
    private lateinit var subject : String
    private lateinit var topic : String
    private lateinit var studyType : StudyType

    @SuppressLint("ClickableViewAccessibility") //TODO: check what this is
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadIntents()
        loadStudyType()
        title = "$subject: $topic"

        cardMovement = CardMovement(getHorizontalThreshold(), this)
        binding.cvFirst.setOnTouchListener(cardMovement)

        binding.cvFirst.cameraDistance = 20000F

        loadAnimators()
        loadCards()
    }

    private fun loadStudyType(){
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val value = sharedPref.getInt(getString(R.string.shared_preference_key_study_type), -1)
        if(0 <= value && value < StudyType.values().size)
            studyType = StudyType.values()[value]
        else
            studyType = StudyType.NORMAL
    }

    private fun loadIntents(){
        var res = intent.getStringExtra(CardsActivity.KEY_SUBJECT_NAME)
        if(res == null)
            TODO("Subject can't be null, because that means a button had null text, which shouldn't impossible")
        subject = res

        res = intent.getStringExtra(CardsActivity.KEY_TOPIC_NAME)
        if(res == null)
            TODO("Subject can't be null, because that means a button had null text, which shouldn't impossible")
        topic = res
    }

    private fun loadAnimators(){
        flipToAnim = AnimatorInflater.loadAnimator(this, R.animator.flip_to_animator)
        flipToAnim.setTarget(binding.cvFirst)
        // For whatever reason I have to add listener directly to listener list, otherwise they aren't working
        flipToAnim.addListener() //I am calling addListener() to initialize the list itself
        flipToAnim.listeners.add(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                if(binding.tvFirstCardName.visibility == View.VISIBLE){
                    binding.tvFirstCardName.visibility = View.GONE
                    binding.tvFirstCardDesc.visibility = View.VISIBLE
                }
                else{
                    binding.tvFirstCardName.visibility = View.VISIBLE
                    binding.tvFirstCardDesc.visibility = View.GONE
                }
                flipBackAnim.start()
            }
        })

        flipBackAnim = AnimatorInflater.loadAnimator(this, R.animator.flip_back_animator)
        flipBackAnim.setTarget(binding.cvFirst)
        flipBackAnim.addListener()
        flipBackAnim.listeners.add(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
                binding.cvFirst.setOnTouchListener(cardMovement)
            }
        })

        growAnim = AnimatorInflater.loadAnimator(this, R.animator.grow_animator)
        growAnim.setTarget(binding.cvFirst)
    }

    private fun flipCard(){
        binding.cvFirst.setOnTouchListener(null)
        flipToAnim.start()
    }

    private fun loadCards(){
        binding.cvFirst.visibility = View.GONE
        binding.cvSecond.visibility = View.GONE
        binding.piTopics.visibility = View.VISIBLE
        thread {
            val db = DBHelper(this)
            val cards = db.getCardsOfTopic(subject, topic)
            totalCards = cards.shuffled()
            runOnUiThread {
                binding.cvFirst.visibility = View.VISIBLE
                binding.cvSecond.visibility = View.VISIBLE
                binding.piTopics.visibility = View.GONE
                initCards()
            }
        }
    }

    private fun setNextCards(){

        if(iterativeLimit == totalCards.size){
            if(actualCardIndices.size == 1){
                oneMoreCard()
                frontCardIndex = backCardIndex
                setFrontCardData()
                return
            }
            if(actualCardIndices.size <= 0){
                noMoreCards()
                return
            }
        }

        if(actualCardIndices.size == 1){
            iterativeLimit++
            var index = 0
            val currentLastIndex = actualCardIndices[actualCardIndices.size - 1]
            actualCardIndices = generateSequence {
                (index++).takeIf { it < iterativeLimit }
            }.toMutableList()

            do {
                actualCardIndices.shuffle()
            }while (currentLastIndex != actualCardIndices[0])
        }

        frontCardIndex = backCardIndex
        setFrontCardData()

        backCardIndex++
        if(backCardIndex >= actualCardIndices.size){
            backCardIndex = 0

            if(actualCardIndices.size > 1){
                val currentLastIndex = actualCardIndices[actualCardIndices.size - 1]
                do {
                    actualCardIndices.shuffle()
                }while (currentLastIndex == actualCardIndices[0])

            }
        }
        setBackCardData()
    }

    private fun setFrontCardData(){
        binding.tvFirstCardName.text = totalCards[actualCardIndices[frontCardIndex]].first
        binding.tvFirstCardDesc.text = totalCards[actualCardIndices[frontCardIndex]].second
        binding.tvFirstCardName.visibility = View.VISIBLE
        binding.tvFirstCardDesc.visibility = View.GONE
    }

    private fun setBackCardData(){
        binding.tvSecondCardName.text = totalCards[actualCardIndices[backCardIndex]].first
        binding.tvSecondCardDesc.text = totalCards[actualCardIndices[backCardIndex]].second
        binding.tvSecondCardName.visibility = View.VISIBLE
        binding.tvSecondCardDesc.visibility = View.GONE
    }

    private fun initCards(){

        if(studyType == StudyType.NORMAL)
            iterativeLimit = totalCards.size
        else
            iterativeLimit = 2

        var index = 0
        actualCardIndices = generateSequence {
            (index++).takeIf { it < iterativeLimit }
        }.toMutableList()

        frontCardIndex = 0
        backCardIndex = 1
        setFrontCardData()
        setBackCardData()
    }

    private fun oneMoreCard(){
        binding.tvSecondCardName.text = getString(R.string.no_more_cards_card_text)
        binding.tvSecondCardDesc.text = getString(R.string.no_more_cards_card_text)
    }

    private fun noMoreCards() {
        binding.tvFirstCardName.text = getString(R.string.no_more_cards_card_text)
        binding.tvFirstCardDesc.text = getString(R.string.no_more_cards_card_text)
    }

    override fun onSwipedLeft() {
        cardMovement.resetView()
        setNextCards()
        growAnim.start()
    }

    override fun onSwipedRight() {
        cardMovement.resetView()
        if(actualCardIndices.isNotEmpty()){
            if(frontCardIndex < backCardIndex) backCardIndex-- // index should be fixed to reflect removal changes
            actualCardIndices.removeAt(frontCardIndex)
        }
        setNextCards()
        growAnim.start()
    }

    override fun onSwipeCancelled() {
        cardMovement.resetView()
    }

    override fun onTapped() {
        flipCard()
    }

    private fun getHorizontalThreshold(): Float {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return 0.2f * displayMetrics.widthPixels.toFloat()
    }
}
