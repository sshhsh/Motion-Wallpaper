package science.kiddd.motionwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            selectVideo()
        }
    }

    private fun selectVideo() {
        val intent = Intent().also {
            it.type = "video/*"
            it.action = Intent.ACTION_GET_CONTENT
        }
        videoResult.launch(intent)
    }

    var videoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                val dir = getDir(dirName, MODE_PRIVATE)
                for (f in dir.listFiles() ?: arrayOf()) {
                    f.delete()
                }
                val path = FFmpegKitConfig.getSafParameterForRead(this, it)
                FFmpegKit.executeAsync("-i $path -vf scale=w=540:h=1080:force_original_aspect_ratio=increase,crop=540:1080 -qscale:v 2 ${dir.absolutePath}/out-%04d.jpg") {
                    val intent = Intent(
                        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                    )
                    intent.putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this, WallPaper::class.java)
                    )
                    startActivity(intent)
                }
            }
        }
    }
}

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