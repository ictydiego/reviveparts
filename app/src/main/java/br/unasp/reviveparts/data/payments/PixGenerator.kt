package br.unasp.reviveparts.data.payments

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object PixGenerator {
    fun pixCopyPaste(orderId: Long, totalCents: Long): String {
        val amount = "%.2f".format(totalCents / 100.0)
        return "00020126360014br.gov.bcb.pix0114REVIVEPARTS$orderId" +
                "5204000053039865406$amount" +
                "5802BR5910RevivePart6009SAOPAULO62070503***6304ABCD"
    }

    fun qr(content: String, size: Int = 512): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) for (y in 0 until size)
            bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        return bmp
    }
}
