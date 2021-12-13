package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.util.*

const val dirName = "pics"

class VideoPicture(private val context: Context) {
    private val length: Int
    private val cache = mutableMapOf<Int, Bitmap?>()
    private val pictures: MutableList<File>
    private val time: Date
    private var lastIndex = -1

    init {
        val dir = context.getDir(dirName, Context.MODE_PRIVATE)
        pictures = dir.listFiles()?.toMutableList() ?: mutableListOf()
        pictures.sortWith { o1, o2 -> o1.name.compareTo(o2.name) }
        length = pictures.size
        time = Date()
    }

    fun getPicture(bias: Float): Bitmap? {
        val indexF = (bias + 1) * length / 2
        var index = indexF.toInt()
        index = if (index >= length) length - 1 else if (index < 0) 0 else index
        if ( lastIndex == index ) {
            return null
        }
        lastIndex = index
        Log.d("aaa", index.toString())
        if (cache.containsKey(index)) {
            return cache[index]
        }
        cache[index] = BitmapFactory.decodeFile(pictures[index].absolutePath)
        return cache[index]
    }

    fun isOutDated(): Boolean {
        val dir = context.getDir(dirName, Context.MODE_PRIVATE)
        val files = dir.listFiles()
        if (files == null || files.isEmpty()) {
            return true
        }
        return files[0].lastModified() > time.time
    }
}