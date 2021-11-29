package com.gmail.kijevigombooc.studybuddy.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue
import kotlin.math.pow

class CardMovement(private val threshold: Float,
                   private val swipeListener: SwipeListener? = null)
    : View.OnTouchListener, Animator.AnimatorListener {

    private var startX = 0.0f
    private var startY = 0.0f
    private var origPosX = 0.0f
    private var origPosY = 0.0f
    private var origRot = 0.0f
    private var moving = false
    private var firstMove = true
    private var swipeDir : SwipeDir = SwipeDir.SWIPE_CANCELLED
    private lateinit var view : View
    private val moveThreshold = 10
    private val moveThresholdSquared = moveThreshold * moveThreshold

    enum class SwipeDir {
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_CANCELLED,
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when(motionEvent.action){
            MotionEvent.ACTION_DOWN -> {
                if(firstMove){
                    origPosX = view.x
                    origPosY = view.y
                    origRot = view.rotation
                    firstMove = false
                    this.view = view
                }
                moving = true
                startX = motionEvent.rawX
                startY = motionEvent.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                if(moving){

                    val currentX = motionEvent.rawX
                    val currentY = motionEvent.rawY

                    val distSquared = ((currentX - startX).pow(2) + (currentY - startY).pow(2))
                    if(distSquared > moveThresholdSquared){
                        view.x = origPosX + (currentX - startX)
                        view.y = origPosX + (currentY - startY)
                        view.rotation = -(currentX - startX) / threshold * 2
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                moving = false
                val endX = motionEvent.rawX
                val endY = motionEvent.rawY
                val dist = endX - startX

                if(dist.absoluteValue > threshold){
                    if(endX < startX){
                        swipeDir = SwipeDir.SWIPE_LEFT
                        moveViewToIn(view, -1000f, view.y, origRot, 0.1f) //TODO: move outside of screen
                    }
                    else{
                        swipeDir = SwipeDir.SWIPE_RIGHT
                        moveViewToIn(view, 1000f, view.y, origRot, 0.1f) //TODO: move outside of screen
                    }
                }
                else if((endX - startX).absoluteValue < moveThreshold && (endY - startY).absoluteValue < 5){
                    swipeListener?.onTapped()
                }
                else{
                    swipeDir = SwipeDir.SWIPE_CANCELLED
                    moveViewToIn(view, origPosX, origPosY, origRot, 0.2f)
                }
            }
        }
        return true
    }

    fun resetView(){
        view.x = origPosX
        view.y = origPosY
        view.rotation = origRot
    }

    private fun moveViewToIn(view : View, toX : Float, toY : Float, toRot : Float, inSeconds : Float){
        view.animate()
            .setDuration((inSeconds * 1000).toLong())
            .translationXBy(toX - view.x)
            .translationYBy(toY - view.y)
            .rotation(toRot)
            .setListener(this)
            .start()
    }

    interface SwipeListener{
        fun onSwipedLeft()
        fun onSwipedRight()
        fun onSwipeCancelled()
        fun onTapped()
    }

    override fun onAnimationStart(p0: Animator?) {
    }

    override fun onAnimationEnd(p0: Animator?) {
        when(swipeDir){
            SwipeDir.SWIPE_LEFT -> {
                swipeListener?.onSwipedLeft()
            }
            SwipeDir.SWIPE_RIGHT -> {
                swipeListener?.onSwipedRight()
            }
            SwipeDir.SWIPE_CANCELLED -> {
                swipeListener?.onSwipeCancelled()
            }
        }
        swipeDir = SwipeDir.SWIPE_CANCELLED
    }

    override fun onAnimationCancel(p0: Animator?) {
    }

    override fun onAnimationRepeat(p0: Animator?) {
    }
}