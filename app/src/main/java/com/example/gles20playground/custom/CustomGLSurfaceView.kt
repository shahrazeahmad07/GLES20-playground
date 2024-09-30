package com.example.gles20playground.custom

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class CustomGLSurfaceView @JvmOverloads constructor (context: Context, attributeSet: AttributeSet? = null): GLSurfaceView(context, attributeSet) {

    private var renderer : GLSurfaceRenderer? = null

    init {
        setEGLContextClientVersion(2)
        renderer = GLSurfaceRenderer()
        setRenderer(renderer)
//        renderMode = RENDERMODE_WHEN_DIRTY
    }
}