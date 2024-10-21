package com.example.gles20playground.custom

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.gles20playground.util.FilterGLUtil
import com.example.gles20playground.util.TextGLUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceRenderer : GLSurfaceView.Renderer {
    private val filterGLUtil = FilterGLUtil()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        filterGLUtil.onSurfaceCreated()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 1.0f)

        filterGLUtil.drawFilter()
    }
}