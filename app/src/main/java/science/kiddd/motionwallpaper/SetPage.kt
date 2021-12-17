package science.kiddd.motionwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

@Composable
fun SetPage(file: File?) {
    val context = LocalContext.current
    Button(onClick = {
        file?.let { preparePictureCache(context, it) }
    }) {
        Text(text = "Button")
    }
}

fun preparePictureCache(context: Context, file: File) {
    val dir = context.getDir(
            cachePictureDirName,
            AppCompatActivity.MODE_PRIVATE
    )
    for (f in dir.listFiles() ?: arrayOf()) {
        f.delete()
    }
    FFmpegKit.executeAsync("-i ${file.absolutePath} -vf scale=w=540:h=1080:force_original_aspect_ratio=increase,crop=540:1080 -qscale:v 2 ${dir.absolutePath}/out-%04d.jpg") {
        val intent = Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, WallPaper::class.java)
        )
        context.startActivity(intent)
    }
}