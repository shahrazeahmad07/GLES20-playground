package com.example.gles20playground.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object MyGLUtils {
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == GLES20.GL_FALSE) {
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Error creating program.")
        }

        return program
    }

    private fun loadShader(type: Int, shaderSource: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderSource)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error compiling shader.")
        }

        return shader
    }

    fun checkError(msg: String) {
        var error = GLES20.glGetError()
        while (error != GLES20.GL_NO_ERROR) {
            Log.e("TAG", "checkError: $msg $error", Throwable(msg))
            error = GLES20.glGetError()
        }
    }

    fun floatArrayToFloatBuffer(floatArray: FloatArray) : FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(floatArray.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(floatArray)
                position(0)
            }
        }
        return buffer
    }

    fun createTextBitmap(text: String): Bitmap {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 120f
        paint.isAntiAlias = true
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)
        // You may need to flip the canvas if text is upside down
        canvas.scale(1f, -1f, bounds.width() / 2f, bounds.height() / 2f)
        canvas.drawText(text, -bounds.left.toFloat(), -bounds.top.toFloat(), paint)
        return bitmap
    }

    fun createTextureDataHandleFromBitmap(bitmap: Bitmap): Int {
        val handles = IntArray(1)
        GLES20.glGenTextures(1, handles, 0)
        val textureId = handles[0]
        //! binding texture to set properties
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        // unbinding this texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureId
    }
}