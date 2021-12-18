package science.kiddd.motionwallpaper

import android.R.attr
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.R.attr.bitmap


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

fun getCover(file: File?): Bitmap? {
    if (file == null) {
        return null
    }
    val metadata = MediaMetadataRetriever()
    metadata.setDataSource(file.absolutePath)
    val res = metadata.getFrameAtTime(0)
    metadata.release()
    return res
}

fun saveBitmap(bitmap: Bitmap, file: File) {
    val out = FileOutputStream(file)
    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
        out.flush()
        out.close()
    }
}
