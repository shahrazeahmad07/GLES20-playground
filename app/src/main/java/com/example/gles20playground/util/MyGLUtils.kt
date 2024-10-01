package com.example.gles20playground.util

import android.opengl.GLES20
import android.util.Log

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
}