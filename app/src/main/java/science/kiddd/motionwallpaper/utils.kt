package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
    inputStream.use { input ->
        val outputStream = FileOutputStream(outputFile)
        outputStream.use { output ->
            val buffer = ByteArray(4 * 1024) // buffer size
            while (true) {
                val byteCount = input.read(buffer)
                if (byteCount < 0) break
                output.write(buffer, 0, byteCount)
            }
            output.flush()
        }
    }
}

const val coverCacheDirName = "cover_cache"
fun getCover(context: Context, file: File?): Bitmap? {
    val dir = context.getDir(coverCacheDirName, AppCompatActivity.MODE_PRIVATE)
    if (file == null) {
        return null
    }
    val coverFile = File(dir, file.name)
    if (coverFile.exists()) {
        return BitmapFactory.decodeFile(coverFile.absolutePath)
    }
    val metadata = MediaMetadataRetriever()
    metadata.setDataSource(file.absolutePath)
    val res = metadata.getFrameAtTime(0)
    metadata.release()
    res?.let {
        saveBitmap(it, coverFile)
    }
    return res
}

fun saveBitmap(bitmap: Bitmap, file: File) {
    val out = FileOutputStream(file)
    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
        out.flush()
        out.close()
    }
}

fun drawBitMapOnCanvas(bitmap: Bitmap, canvas: Canvas, bias: Float, edge: Float = 0.1f) {
    val rect = Rect()
    canvas.getClipBounds(rect)
    val w = bitmap.width
    val h = (bitmap.height * (1 - edge)).toInt()
    val biasH = (bitmap.height * edge / 2 * (bias + 1)).toInt()
    val bitmapRect = if (w * rect.height() > h * rect.width()) {
        val newWidth = rect.width() * h / rect.height()
        Rect((w - newWidth) / 2, biasH, (w + newWidth) / 2, h + biasH)
    } else {
        val newHeight = rect.height() * w / rect.width()
        Rect(0, (h - newHeight) / 2 + biasH, w, (h + newHeight) / 2 + biasH)
    }
    canvas.drawBitmap(bitmap, bitmapRect, rect, null)
}
