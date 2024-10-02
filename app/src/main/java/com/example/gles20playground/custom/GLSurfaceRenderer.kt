package com.example.gles20playground.custom

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.gles20playground.util.MyGLUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceRenderer : GLSurfaceView.Renderer {
    private var coordsPerPositionVertex = 0

    private var coordsPerTextureVertex = 0

    private var positionCoordinatesBuffer: FloatBuffer? = null
    private var textureCoordinatesBuffer: FloatBuffer? = null

    private var programHandle = 0
    private var positionCoordHandle = 0
    private var textureCoordHandle = 0
    private var textureDataUniformHandle = 0
    private var textureDataHandle = 0
    private var textureWidth = 0
    private var textureHeight = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        val vertexSource = """
    attribute vec4 a_position;
    attribute vec2 a_texCoord;
    varying vec2 v_texCoord;
    void main() {
        gl_Position = a_position;
        v_texCoord = a_texCoord;
    }
""".trimIndent()
        val fragmentSource = """
    precision mediump float;
    uniform sampler2D u_texture;
    varying vec2 v_texCoord;
    void main() {
        gl_FragColor = texture2D(u_texture, v_texCoord);
    }
""".trimIndent()

        val positionCoordinates = floatArrayOf(
            -1f, 1f, 0f, // top left
            -1f, -1f, 0f, // bottom left
            1f, 1f, 0f, // top right
            1f, -1f, 0f, // bottom right
        )
        coordsPerPositionVertex = 3
        val textureCoordinates = floatArrayOf(
            0f, 1f,   // top left       0f, 1f,     1.0f, 1.0f,
            0f, 0f,   // bottom left    0f, 0f,     1.0f, 0.0f,
            1f, 1f,   // top right      1f, 1f,     0.0f, 1.0f,
            1f, 0f,   // bottom right   1f, 0f,     0.0f, 0.0f,
        )
        coordsPerTextureVertex = 2

        //! developing buffer resources for coordinates
        positionCoordinatesBuffer = MyGLUtils.floatArrayToFloatBuffer(positionCoordinates)

        textureCoordinatesBuffer = MyGLUtils.floatArrayToFloatBuffer(textureCoordinates)

        //! creating program from the shaders and linking them:
        programHandle = MyGLUtils.createProgram(vertexSource, fragmentSource)

        val textBitmap = MyGLUtils.createTextBitmap("Hello World!")
        textureDataHandle = MyGLUtils.createTextureDataHandleFromBitmap(textBitmap)
        //! no need of that bitmap anymore
        textureWidth = textBitmap.width
        textureHeight = textBitmap.height
        textBitmap.recycle()
        positionCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_position")
        textureCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_texCoord")
        textureDataUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_texture")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val surfaceAspect = width.toFloat() / height.toFloat()
        val textureAspect = textureWidth.toFloat() / textureHeight.toFloat()

        var scaleX = 1f
        var scaleY = 1f
        if (surfaceAspect > textureAspect) {
            scaleX = textureAspect / surfaceAspect
        } else {
            scaleY = surfaceAspect / textureAspect
        }

        val adjustedPositionCoordinates = floatArrayOf(
            -scaleX, scaleY, 0f, // top left
            -scaleX, -scaleY, 0f, // bottom left
            scaleX, scaleY, 0f, // top right
            scaleX, -scaleY, 0f, // bottom right
        )
        positionCoordinatesBuffer = MyGLUtils.floatArrayToFloatBuffer(adjustedPositionCoordinates)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND)
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glUseProgram(programHandle)
        GLES20.glEnableVertexAttribArray(positionCoordHandle)
        GLES20.glVertexAttribPointer(positionCoordHandle, coordsPerPositionVertex, GLES20.GL_FLOAT, false, coordsPerPositionVertex * Float.SIZE_BYTES, positionCoordinatesBuffer)
        GLES20.glEnableVertexAttribArray(textureCoordHandle)
        GLES20.glVertexAttribPointer(textureCoordHandle, coordsPerTextureVertex, GLES20.GL_FLOAT, false, coordsPerTextureVertex * Float.SIZE_BYTES, textureCoordinatesBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle)
        GLES20.glUniform1i(textureDataUniformHandle, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisable(GLES20.GL_BLEND)

        GLES20.glDisableVertexAttribArray(positionCoordHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)
        GLES20.glUseProgram(0)
    }
}