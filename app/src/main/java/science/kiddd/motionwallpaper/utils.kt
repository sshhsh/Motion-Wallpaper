package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
