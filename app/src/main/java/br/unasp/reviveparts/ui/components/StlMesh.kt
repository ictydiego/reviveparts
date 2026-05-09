package br.unasp.reviveparts.ui.components

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class StlMesh(
    val vertices: FloatArray,
    val normals: FloatArray,
    val triangleCount: Int,
    val center: FloatArray,
    val scale: Float
)

object StlLoader {
    fun parseBinary(input: InputStream): StlMesh {
        val bytes = input.readBytes()
        return parse(bytes)
    }

    fun parse(bytes: ByteArray): StlMesh {
        if (looksAscii(bytes)) return parseAscii(String(bytes, Charsets.US_ASCII))
        return parseBinaryBytes(bytes)
    }

    private fun looksAscii(bytes: ByteArray): Boolean {
        if (bytes.size < 84) return true
        val header = String(bytes, 0, 5, Charsets.US_ASCII).lowercase()
        if (header != "solid") return false
        val triCount = ByteBuffer.wrap(bytes, 80, 4).order(ByteOrder.LITTLE_ENDIAN).int
        val expected = 84L + 50L * triCount.toLong().coerceAtLeast(0)
        if (triCount in 0..50_000_000 && expected == bytes.size.toLong()) return false
        return true
    }

    private fun parseBinaryBytes(bytes: ByteArray): StlMesh {
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buf.position(80)
        val triCount = buf.int
        require(triCount in 0..50_000_000) { "STL binário com triCount inválido: $triCount" }
        val v = FloatArray(triCount * 9)
        val n = FloatArray(triCount * 9)
        var vi = 0
        var ni = 0
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        for (t in 0 until triCount) {
            val nx = buf.float; val ny = buf.float; val nz = buf.float
            for (k in 0 until 3) {
                val x = buf.float; val y = buf.float; val z = buf.float
                v[vi++] = x; v[vi++] = y; v[vi++] = z
                n[ni++] = nx; n[ni++] = ny; n[ni++] = nz
                if (x < minX) minX = x; if (y < minY) minY = y; if (z < minZ) minZ = z
                if (x > maxX) maxX = x; if (y > maxY) maxY = y; if (z > maxZ) maxZ = z
            }
            buf.short
        }
        return finish(v, n, triCount, minX, minY, minZ, maxX, maxY, maxZ)
    }

    private fun parseAscii(text: String): StlMesh {
        val verts = ArrayList<Float>(8192)
        val norms = ArrayList<Float>(8192)
        var curN = floatArrayOf(0f, 0f, 0f)
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("facet normal", ignoreCase = true) -> {
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 5) {
                        curN = floatArrayOf(parts[2].toFloat(), parts[3].toFloat(), parts[4].toFloat())
                    }
                }
                line.startsWith("vertex", ignoreCase = true) -> {
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 4) {
                        verts.add(parts[1].toFloat()); verts.add(parts[2].toFloat()); verts.add(parts[3].toFloat())
                        norms.add(curN[0]); norms.add(curN[1]); norms.add(curN[2])
                    }
                }
            }
        }
        val triCount = verts.size / 9
        val v = FloatArray(verts.size); for (i in v.indices) v[i] = verts[i]
        val n = FloatArray(norms.size); for (i in n.indices) n[i] = norms[i]

        // recompute normals if any are zero
        for (t in 0 until triCount) {
            val b = t * 9
            if (n[b] == 0f && n[b + 1] == 0f && n[b + 2] == 0f) {
                val ax = v[b]; val ay = v[b + 1]; val az = v[b + 2]
                val bx = v[b + 3]; val by = v[b + 4]; val bz = v[b + 5]
                val cx = v[b + 6]; val cy = v[b + 7]; val cz = v[b + 8]
                val ux = bx - ax; val uy = by - ay; val uz = bz - az
                val wx = cx - ax; val wy = cy - ay; val wz = cz - az
                var nx = uy * wz - uz * wy
                var ny = uz * wx - ux * wz
                var nz = ux * wy - uy * wx
                val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
                if (len > 0f) { nx /= len; ny /= len; nz /= len }
                for (k in 0 until 3) {
                    n[b + k * 3] = nx; n[b + k * 3 + 1] = ny; n[b + k * 3 + 2] = nz
                }
            }
        }

        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        var i = 0
        while (i < v.size) {
            val x = v[i]; val y = v[i + 1]; val z = v[i + 2]
            if (x < minX) minX = x; if (y < minY) minY = y; if (z < minZ) minZ = z
            if (x > maxX) maxX = x; if (y > maxY) maxY = y; if (z > maxZ) maxZ = z
            i += 3
        }
        return finish(v, n, triCount, minX, minY, minZ, maxX, maxY, maxZ)
    }

    private fun finish(v: FloatArray, n: FloatArray, triCount: Int,
                       minX: Float, minY: Float, minZ: Float,
                       maxX: Float, maxY: Float, maxZ: Float): StlMesh {
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val cz = (minZ + maxZ) / 2f
        val span = maxOf(maxX - minX, maxY - minY, maxZ - minZ)
        val scale = if (span > 0f) 2f / span else 1f
        return StlMesh(v, n, triCount, floatArrayOf(cx, cy, cz), scale)
    }
}
