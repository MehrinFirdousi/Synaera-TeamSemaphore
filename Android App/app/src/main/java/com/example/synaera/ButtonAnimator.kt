package com.example.synaera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

class ButtonAnimator {
    var i = 0
    val al: ArrayList<Int> = ArrayList()
    val al2: ArrayList<Int> = ArrayList()
    var currentAnimator: AnimatorSet? = null
    var settingPopupVisibilityDuration = 0

    fun animateStroke(circleView: CircleView) {
        val random: Int
        if (al.isNotEmpty()) {
            random = al[i++]
            if (i >= al.size) {
                for (j in al.indices.reversed()) {
                    al2.add(al[j])
                }
                al.clear()
                i = 0
            }
        } else {
            random = al2[i++]
            if (i >= al2.size) {
                for (j in al2.indices.reversed()) {
                    al.add(al2[j])
                }
                al2.clear()
                i = 0
            }
        }
        circleView.animateRadius(circleView.getmMaxRadius(), random.toFloat())
    }
    fun resetAnimation(circleView: CircleView) {
        i = 0
        al.clear()
        al2.clear()
        al.add(25)
        al.add(30)
        al.add(35)
        al.add(40)
//        al.add(45)
//        al.add(50);
//        al.add(55);
//        al.add(60);
        circleView.endAnimation()
    }

    private fun dpToPx(resources: Resources, valueInDp: Float): Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics).toInt()
    }

    fun startAnimationOfSquare(resources: Resources, circleView: CircleView, recordButton: CardView) {
        settingPopupVisibilityDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime)
        currentAnimator?.cancel()
        val finalBounds = Rect()
        val globalOffset = Point()
        circleView.getGlobalVisibleRect(finalBounds, globalOffset)
        recordButton.let {
            TransitionManager.beginDelayedTransition(
                it, TransitionSet()
                    .addTransition(ChangeBounds()).setDuration(settingPopupVisibilityDuration.toLong())
            )
        }
        val params = recordButton.layoutParams
        params.height = dpToPx(resources, 40F)
        params.width = dpToPx(resources, 40F)
        recordButton.layoutParams = params
        val set = AnimatorSet()
        set.play(ObjectAnimator.ofFloat(recordButton, "radius", dpToPx(resources, 8F).toFloat()))
        set.duration = settingPopupVisibilityDuration.toLong()
        set.interpolator = DecelerateInterpolator()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finishAnimation()
            }

            override fun onAnimationCancel(animation: Animator) {
                finishAnimation()
            }

            private fun finishAnimation() {
                currentAnimator = null
            }
        })
        set.start()
        currentAnimator = set
    }

    public fun stopAnimationOfSquare(resources: Resources, recordButton: CardView) {
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
        }
        recordButton.let {
            TransitionManager.beginDelayedTransition(
                it, TransitionSet()
                    .addTransition(ChangeBounds()).setDuration(settingPopupVisibilityDuration.toLong())
            )
        }
        val params = recordButton.layoutParams
        params.width = dpToPx(resources, 70F)
        params.height = dpToPx(resources, 70F)
        recordButton.layoutParams = params
        val set1 = AnimatorSet()
        set1.play(
            ObjectAnimator.ofFloat(
                recordButton,
                "radius",
                dpToPx(resources, 40F).toFloat()
            )
        ) //radius = height/2 to make it round
        set1.duration = settingPopupVisibilityDuration.toLong()
        set1.interpolator = DecelerateInterpolator()
        set1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finishAnimation()
            }

            override fun onAnimationCancel(animation: Animator) {
                finishAnimation()
            }

            private fun finishAnimation() {
                currentAnimator = null
            }
        })
        set1.start()
        currentAnimator = set1
    }
}