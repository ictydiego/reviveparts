package br.unasp.reviveparts.ui.components

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class StlGlView(context: Context, private val assetPath: String) : GLSurfaceView(context) {
    private val renderer: StlRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = StlRenderer(context, assetPath)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    private var lastX = 0f
    private var lastY = 0f

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { lastX = e.x; lastY = e.y }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - lastX
                val dy = e.y - lastY
                renderer.rotY += dx * 0.5f
                renderer.rotX += dy * 0.5f
                lastX = e.x; lastY = e.y
            }
        }
        return true
    }
}

class StlRenderer(private val context: Context, private val assetPath: String) : GLSurfaceView.Renderer {
    @Volatile var rotX = 20f
    @Volatile var rotY = 0f

    private var program = 0
    private var aPos = 0
    private var aNorm = 0
    private var uMvp = 0
    private var uModel = 0
    private var uLightDir = 0
    private var uColor = 0

    private var vBuf: FloatBuffer? = null
    private var nBuf: FloatBuffer? = null
    private var triCount = 0
    private var meshCenter = floatArrayOf(0f, 0f, 0f)
    private var meshScale = 1f

    private val proj = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)
    private val tmp = FloatArray(16)
    private var autoSpin = 0f

    private val vertexShader = """
        uniform mat4 uMvp;
        uniform mat4 uModel;
        attribute vec3 aPos;
        attribute vec3 aNorm;
        varying vec3 vNorm;
        varying vec3 vWorld;
        void main() {
            gl_Position = uMvp * vec4(aPos, 1.0);
            vNorm = mat3(uModel) * aNorm;
            vWorld = (uModel * vec4(aPos, 1.0)).xyz;
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        varying vec3 vNorm;
        varying vec3 vWorld;
        uniform vec3 uLightDir;
        uniform vec3 uColor;
        void main() {
            vec3 n = normalize(vNorm);
            float d = max(dot(n, normalize(uLightDir)), 0.0);
            vec3 ambient = uColor * 0.25;
            vec3 diffuse = uColor * d;
            vec3 rim = vec3(1.0, 0.84, 0.04) * pow(1.0 - max(dot(n, vec3(0.0, 0.0, 1.0)), 0.0), 2.0) * 0.4;
            gl_FragColor = vec4(ambient + diffuse + rim, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        program = link(vertexShader, fragmentShader)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aNorm = GLES20.glGetAttribLocation(program, "aNorm")
        uMvp = GLES20.glGetUniformLocation(program, "uMvp")
        uModel = GLES20.glGetUniformLocation(program, "uModel")
        uLightDir = GLES20.glGetUniformLocation(program, "uLightDir")
        uColor = GLES20.glGetUniformLocation(program, "uColor")

        try {
            context.assets.open(assetPath).use { input ->
                val mesh = if (assetPath.endsWith(".3mf", ignoreCase = true))
                    ThreeMfLoader.parse(input)
                else
                    StlLoader.parseBinary(input)
                triCount = mesh.triangleCount
                meshCenter = mesh.center
                meshScale = mesh.scale
                vBuf = ByteBuffer.allocateDirect(mesh.vertices.size * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(mesh.vertices); position(0) }
                nBuf = ByteBuffer.allocateDirect(mesh.normals.size * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(mesh.normals); position(0) }
            }
        } catch (e: Exception) { android.util.Log.e("StlRenderer", "load fail", e) }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.perspectiveM(proj, 0, 35f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (vBuf == null) return

        autoSpin += 0.3f
        Matrix.setLookAtM(view, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)

        Matrix.setIdentityM(model, 0)
        Matrix.rotateM(model, 0, rotX, 1f, 0f, 0f)
        Matrix.rotateM(model, 0, rotY + autoSpin, 0f, 1f, 0f)
        Matrix.scaleM(model, 0, meshScale, meshScale, meshScale)
        Matrix.translateM(model, 0, -meshCenter[0], -meshCenter[1], -meshCenter[2])

        Matrix.multiplyMM(tmp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, proj, 0, tmp, 0)

        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(uModel, 1, false, model, 0)
        GLES20.glUniform3f(uLightDir, 0.5f, 0.8f, 1.0f)
        GLES20.glUniform3f(uColor, 1.0f, 0.84f, 0.04f)

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, vBuf)
        GLES20.glEnableVertexAttribArray(aNorm)
        GLES20.glVertexAttribPointer(aNorm, 3, GLES20.GL_FLOAT, false, 0, nBuf)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triCount * 3)
        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aNorm)
    }

    private fun compile(type: Int, src: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)
        val status = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            android.util.Log.e("StlRenderer", "shader: ${GLES20.glGetShaderInfoLog(s)}")
            GLES20.glDeleteShader(s)
            return 0
        }
        return s
    }

    private fun link(vs: String, fs: String): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v)
        GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)
        return p
    }
}
