package br.unasp.reviveparts.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Mask: (XX)XXXXX-XXXX
        val trimmed = if (text.text.length >= 11) text.text.substring(0..10) else text.text
        val out = StringBuilder()
        for (i in trimmed.indices) {
            if (i == 0) out.append("(")
            if (i == 2) out.append(")")
            if (i == 7) out.append("-")
            out.append(trimmed[i])
        }

        val phoneOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0
                if (offset <= 2) return offset + 1
                if (offset <= 7) return offset + 2
                if (offset <= 11) return offset + 3
                return out.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset <= 3) return (offset - 1).coerceAtLeast(0)
                if (offset <= 9) return (offset - 2).coerceAtLeast(0)
                if (offset <= 14) return (offset - 3).coerceAtLeast(0)
                return trimmed.length
            }
        }

        return TransformedText(AnnotatedString(out.toString()), phoneOffsetMapping)
    }
}

class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Mask: XXX.XXX.XXX-XX
        val trimmed = if (text.text.length >= 11) text.text.substring(0..10) else text.text
        val out = StringBuilder()
        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 2 || i == 5) out.append(".")
            if (i == 8) out.append("-")
        }

        val cpfOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 6) return offset + 1
                if (offset <= 9) return offset + 2
                if (offset <= 11) return offset + 3
                return out.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return (offset - 1).coerceAtLeast(0)
                if (offset <= 11) return (offset - 2).coerceAtLeast(0)
                if (offset <= 14) return (offset - 3).coerceAtLeast(0)
                return trimmed.length
            }
        }

        return TransformedText(AnnotatedString(out.toString()), cpfOffsetMapping)
    }
}
