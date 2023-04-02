package com.example.synaera
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CustomShape(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var color = Color.YELLOW
    private var size = 500f
    private var insideColor = ContextCompat.getColor(context, R.color.white)
    private var borderWidth = 2f
    private val mouthPath = Path()
    private val background = Color.TRANSPARENT

    private fun drawCurvedShape(canvas: Canvas) {

        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL
        setBackgroundColor(background);

        // 1
        mouthPath.moveTo(0f, size)
        // 2
        mouthPath.quadTo(size * 0f, size * 0.5f, size, size * 0.5f)
        mouthPath.lineTo(canvas.width-size,size * 0.5f)
        mouthPath.quadTo(canvas.width - 0.1f, size * 0.45f, canvas.width * 1f, 0f)
        mouthPath.lineTo(canvas.width * 1f,canvas.height * 1f)
        mouthPath.lineTo(0f,canvas.height * 1f)
        mouthPath.lineTo(0f,size)

        // 4
        paint.color = insideColor
        paint.strokeWidth = borderWidth
        paint.style = Paint.Style.FILL


        // 5
        canvas.drawPath(mouthPath, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas != null)
            drawCurvedShape(canvas)
    }
}