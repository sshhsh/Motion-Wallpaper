package science.kiddd.motionwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.launch
import science.kiddd.motionwallpaper.ui.theme.MotionWallpaperTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val videolist = remember {
        val dir = context.getDir(videoDirName, AppCompatActivity.MODE_PRIVATE)
        val list = (dir.listFiles() ?: arrayOf()).toList()
        mutableStateListOf<File>().also {
            it.addAll(list)
        }
    }
    MotionWallpaperTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            Scaffold(
                floatingActionButton = {
                    ChooseVideoButton(videolist)
                }
            ) {
                // Screen content
                Column {
                    VideoList(videolist)
                }
            }
        }
    }
}

const val videoDirName = "video"

@Composable
fun VideoList(files: SnapshotStateList<File>) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(files.size) { i ->
            VideoCard(files[i], files)
        }
    }
}

@Composable
fun VideoCard(file: File, files: SnapshotStateList<File>) {
    val scope = rememberCoroutineScope()
    val state = remember {
        val it = mutableStateOf<Bitmap?>(null)
        scope.launch {
            val metadata = MediaMetadataRetriever()
            metadata.setDataSource(file.absolutePath)
            val res = metadata.getFrameAtTime(0)
            metadata.release()
            it.value = res
        }
        it
    }
    val bitMap = state.value
    bitMap?.let {
        val context = LocalContext.current
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.aspectRatio(1f, true)
                .clickable {
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
                },
        )
    }
}

@Composable
fun ChooseVideoButton(videolist: SnapshotStateList<File>) {
    val context = LocalContext.current
    val videoResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.data ?: return@rememberLauncherForActivityResult
                val input = context.contentResolver.openInputStream(uri)
                    ?: return@rememberLauncherForActivityResult
                val videoDir = context.getDir(videoDirName, AppCompatActivity.MODE_PRIVATE)
                val newFile = File(videoDir, Date().time.toString())
                copyStreamToFile(input, newFile)
                videolist.add(newFile)
            }
        }
    FloatingActionButton(content = {
        Icon(Icons.Rounded.Add, contentDescription = "Add video")
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
    App()
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