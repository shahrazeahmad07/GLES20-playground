package com.example.gles20playground.custom

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.example.gles20playground.util.MyGLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceRenderer : GLSurfaceView.Renderer {

    private val vertexShaderSource = """
    uniform mat4 uMatrix;
    attribute vec4 a_position;
    attribute vec2 a_texCoord;
    varying vec2 v_texCoord;

    void main() {
        gl_Position = uMatrix * a_position;
        v_texCoord = a_texCoord;
    }

""".trimIndent()

    private val fragmentShaderSource = """
    precision mediump float;
    uniform sampler2D u_texture;
    varying vec2 v_texCoord;
    void main() {
        gl_FragColor = texture2D(u_texture, v_texCoord);
    }
""".trimIndent()

    private var programHandle = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureUniformHandle = 0
    private var textureHandle = 0

    private val vertexData = floatArrayOf(
        // X, Y, Z,  U,  V
        -1f, -1f, 0f,  0f, 0f,   // Bottom-left
        1f, -1f, 0f,  1f, 0f,   // Bottom-right
        -1f,  1f, 0f,  0f, 1f,   // Top-left
        1f,  1f, 0f,  1f, 1f    // Top-right
    )

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textBitmap: Bitmap

    // Create a rotation matrix (identity matrix)
    private val rotationMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        // Compile shaders and link program
        programHandle = MyGLUtils.createProgram(vertexShaderSource, fragmentShaderSource)

        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_position")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_texCoord")
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_texture")

        // Create a buffer for vertex data
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)

        // Generate texture for text
        textBitmap = createTextBitmap("Hello World")
        textureHandle = loadTexture(textBitmap)

        // Initialize in onSurfaceCreated()
        Matrix.setIdentityM(rotationMatrix, 0)
        // Rotate 180 degrees along the X-axis and Y-axis
        Matrix.rotateM(rotationMatrix, 0, 180f, 1f, 0f, 0f)
//        Matrix.rotateM(rotationMatrix, 0, 180f, 0f, 1f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Calculate the aspect ratio of the bitmap and surface
        val bitmapAspectRatio = textBitmap.width.toFloat() / textBitmap.height
        val surfaceAspectRatio = width.toFloat() / height

        // Adjust the quad’s vertex data to maintain the aspect ratio
        val scaleX: Float
        val scaleY: Float

        if (bitmapAspectRatio > surfaceAspectRatio) {
            // If the bitmap is wider, adjust the height
            scaleX = 1f
            scaleY = surfaceAspectRatio / bitmapAspectRatio
        } else {
            // If the bitmap is taller, adjust the width
            scaleX = bitmapAspectRatio / surfaceAspectRatio
            scaleY = 1f
        }

        // Set the new vertex data for proper scaling
        val vertexData = floatArrayOf(
            // X, Y, Z, U, V
            -scaleX, -scaleY, 0f, 0f, 0f,   // Bottom-left
            scaleX, -scaleY, 0f, 1f, 0f,   // Bottom-right
            -scaleX,  scaleY, 0f, 0f, 1f,   // Top-left
            scaleX,  scaleY, 0f, 1f, 1f    // Top-right
        )

        // Update vertex buffer with new data
        vertexBuffer.put(vertexData).position(0)
    }


    override fun onDrawFrame(gl: GL10?) {


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Use the shader program
        GLES20.glUseProgram(programHandle)

        // Enable vertex position
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 20, vertexBuffer)

        // Enable texture coordinates
        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 20, vertexBuffer)

        // Pass the matrix to the shader
        val uMatrixLocation = GLES20.glGetUniformLocation(programHandle, "uMatrix")
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, rotationMatrix, 0)


        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        GLES20.glUniform1i(textureUniformHandle, 0)

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glUseProgram(0)
    }


    private fun createTextBitmap(text: String): Bitmap {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, -bounds.left.toFloat(), -bounds.top.toFloat(), paint)
        return bitmap
    }

    private fun loadTexture(bitmap: Bitmap): Int {
        val textureHandles = IntArray(1)
        GLES20.glGenTextures(1, textureHandles, 0)

        if (textureHandles[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        return textureHandles[0]
    }
}