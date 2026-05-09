package br.unasp.reviveparts.ui.components

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.zip.ZipInputStream

object ThreeMfLoader {
    fun parse(input: InputStream): StlMesh {
        var modelXml: ByteArray? = null
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.endsWith("3dmodel.model", ignoreCase = true)) {
                    modelXml = zip.readBytes()
                    break
                }
                entry = zip.nextEntry
            }
        }
        val xml = modelXml ?: error("3MF without 3dmodel.model")

        val verts = ArrayList<Float>(4096)
        val tris = ArrayList<Int>(4096)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(xml.inputStream(), null)
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "vertex" -> {
                        verts.add(parser.getAttributeValue(null, "x").toFloat())
                        verts.add(parser.getAttributeValue(null, "y").toFloat())
                        verts.add(parser.getAttributeValue(null, "z").toFloat())
                    }
                    "triangle" -> {
                        tris.add(parser.getAttributeValue(null, "v1").toInt())
                        tris.add(parser.getAttributeValue(null, "v2").toInt())
                        tris.add(parser.getAttributeValue(null, "v3").toInt())
                    }
                }
            }
            event = parser.next()
        }

        val triCount = tris.size / 3
        val v = FloatArray(triCount * 9)
        val n = FloatArray(triCount * 9)
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

        for (t in 0 until triCount) {
            val i0 = tris[t * 3] * 3
            val i1 = tris[t * 3 + 1] * 3
            val i2 = tris[t * 3 + 2] * 3
            val ax = verts[i0]; val ay = verts[i0 + 1]; val az = verts[i0 + 2]
            val bx = verts[i1]; val by = verts[i1 + 1]; val bz = verts[i1 + 2]
            val cx = verts[i2]; val cy = verts[i2 + 1]; val cz = verts[i2 + 2]
            val ux = bx - ax; val uy = by - ay; val uz = bz - az
            val wx = cx - ax; val wy = cy - ay; val wz = cz - az
            var nx = uy * wz - uz * wy
            var ny = uz * wx - ux * wz
            var nz = ux * wy - uy * wx
            val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
            if (len > 0f) { nx /= len; ny /= len; nz /= len }
            val base = t * 9
            v[base] = ax; v[base + 1] = ay; v[base + 2] = az
            v[base + 3] = bx; v[base + 4] = by; v[base + 5] = bz
            v[base + 6] = cx; v[base + 7] = cy; v[base + 8] = cz
            for (k in 0 until 3) {
                n[base + k * 3] = nx; n[base + k * 3 + 1] = ny; n[base + k * 3 + 2] = nz
            }
            for ((x, y, z) in listOf(Triple(ax, ay, az), Triple(bx, by, bz), Triple(cx, cy, cz))) {
                if (x < minX) minX = x; if (y < minY) minY = y; if (z < minZ) minZ = z
                if (x > maxX) maxX = x; if (y > maxY) maxY = y; if (z > maxZ) maxZ = z
            }
        }
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val cz = (minZ + maxZ) / 2f
        val span = maxOf(maxX - minX, maxY - minY, maxZ - minZ)
        val scale = if (span > 0f) 2f / span else 1f
        return StlMesh(v, n, triCount, floatArrayOf(cx, cy, cz), scale)
    }
}
