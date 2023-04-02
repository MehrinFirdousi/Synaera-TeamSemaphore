package com.example.synaera

import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath

class CustomCornerTreatment : CornerTreatment() {

        override fun getCornerPath(
        shapePath: ShapePath,
        angle: Float,
        interpolation: Float,
        radius: Float
    ) {
        val interpolatedRadius = radius * interpolation
        shapePath.reset(0f, -radius * interpolation, 270f,270 -angle)
        shapePath.addArc(
            0f,
            -2*interpolatedRadius,
            2*interpolatedRadius,
            0f,
            180f,
            - angle)
//        super.getCornerPath(shapePath, angle, interpolation, radius)
    }
}