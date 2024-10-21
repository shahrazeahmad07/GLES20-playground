package com.example.gles20playground.util

import android.opengl.GLES20
import java.nio.FloatBuffer

class FilterGLUtil {
    private var perPositionCoords = 0
    private var programHandle = 0
    private var positionCoordBuffer: FloatBuffer? = null
    private var positionHandle = 0
    private var colorHandle = 0
    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.44f, 0.26f, 0.08f, 0.7f)

    fun onSurfaceCreated() {
        val vertexShaderSource = """
            attribute vec4 a_position;
            void main() {
                gl_Position = a_position;
            }
        """.trimIndent()

        val fragmentShaderSource = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()

        programHandle = MyGLUtils.createProgram(vertexShaderSource, fragmentShaderSource)


        val positionCoords = floatArrayOf(
            -1f, 1f, 0f, // top left
            -1f, -1f, 0f, // bottom left
            1f, 1f, 0f, // top right
            1f, -1f, 0f // bottom right
        )
        perPositionCoords = 3
        positionCoordBuffer = MyGLUtils.floatArrayToFloatBuffer(positionCoords)

        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_position")
        MyGLUtils.checkError("position attrib location")
        colorHandle = GLES20.glGetUniformLocation(programHandle, "vColor")
        MyGLUtils.checkError("color attrib location")
    }

    fun drawFilter() {
        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND)
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glUseProgram(programHandle)
        MyGLUtils.checkError("use program")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, perPositionCoords, GLES20.GL_FLOAT, false, perPositionCoords * Float.SIZE_BYTES, positionCoordBuffer)
        MyGLUtils.checkError("vertex attrib pointer")

        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        MyGLUtils.checkError("passing color")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        MyGLUtils.checkError("draw arrays")
        GLES20.glDisable(GLES20.GL_BLEND)


        GLES20.glDisableVertexAttribArray(0)
        GLES20.glUseProgram(0)
    }
}