package com.example.gles20playground.custom

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceRenderer : GLSurfaceView.Renderer {
    private val squareVerticesArray = floatArrayOf(
        -0.5f, -0.5f, // bottom left 0
        0.5f, -0.5f, // bottom right 1
        0.5f, 0.5f, // top right 2
        -0.5f, 0.5f, // top left 3
    )

    private val indexArrayOrDrawOrder = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )

    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var squareVertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null

    private var program = 0
    private val vertexShader = """
    attribute vec4 position;
    void main()
    {
        gl_Position = position;
    }
""".trimIndent()

    private val fragmentShader = """
    precision mediump float;
    uniform vec4 color;
    void main()
    {
        gl_FragColor = color;
    }
""".trimIndent()


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        squareVertexBuffer = // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(squareVerticesArray.size * Float.SIZE_BYTES).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(squareVerticesArray)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }

        indexBuffer = ByteBuffer.allocateDirect(indexArrayOrDrawOrder.size * Short.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indexArrayOrDrawOrder)
                position(0)
            }
        }

        program = GLES20.glCreateProgram()
        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        GLES20.glAttachShader(program, vs)
        GLES20.glAttachShader(program, fs)
        GLES20.glLinkProgram(program)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        GLES20.glUseProgram(program)
        val vertexPositionArray = GLES20.glGetAttribLocation(program, "position")
        GLES20.glVertexAttribPointer(vertexPositionArray, 2, GLES20.GL_FLOAT, false, 2 * Float.SIZE_BYTES, squareVertexBuffer)
        GLES20.glEnableVertexAttribArray(vertexPositionArray)

        val colorPointer = GLES20.glGetUniformLocation(program, "color")
        GLES20.glUniform4fv(colorPointer, 1, color, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(vertexPositionArray)

    }

    private fun compileShader(type: Int, source: String): Int {
        val id = GLES20.glCreateShader(type)
        GLES20.glShaderSource(id, source)
        GLES20.glCompileShader(id)
        val result = IntArray(1)
        GLES20.glGetShaderiv(id, GLES20.GL_COMPILE_STATUS, result, 0)
        if (result[0] == GLES20.GL_FALSE) {
            Log.i("TAG", "compileShader: Failed to compile shader ${GLES20.glGetShaderInfoLog(id)}")
            GLES20.glDeleteShader(id)
            return 0
        }
        return id
    }
}