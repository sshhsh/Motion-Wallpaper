package science.kiddd.motionwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import science.kiddd.motionwallpaper.ui.theme.MotionWallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionWallpaperTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ChooseVideoButton()
                }
            }
        }
    }
}

@Composable
fun ChooseVideoButton() {
    val context = LocalContext.current
    val videoResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let {
                    val dir = context.getDir(dirName, AppCompatActivity.MODE_PRIVATE)
                    for (f in dir.listFiles() ?: arrayOf()) {
                        f.delete()
                    }
                    val path = FFmpegKitConfig.getSafParameterForRead(context, it)
                    FFmpegKit.executeAsync("-i $path -vf scale=w=540:h=1080:force_original_aspect_ratio=increase,crop=540:1080 -qscale:v 2 ${dir.absolutePath}/out-%04d.jpg") {
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
            }
        }
    Button(content = {
        Text("Button")
    },
        onClick = {
            val intent = Intent().also {
                it.type = "video/*"
                it.action = Intent.ACTION_GET_CONTENT
            }
            videoResult.launch(intent)
        })
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MotionWallpaperTheme {
        ChooseVideoButton()
    }
}