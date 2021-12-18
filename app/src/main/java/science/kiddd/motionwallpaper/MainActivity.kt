package science.kiddd.motionwallpaper

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
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()
    MotionWallpaperTheme {
        NavHost(navController = navController, startDestination = "start") {
            composable("start") { StartPage(videolist, navController) }
            composable("set/{path}") { backStackEntry ->
                val path = backStackEntry.arguments?.getString("path")
                val dir = context.getDir(videoDirName, AppCompatActivity.MODE_PRIVATE)
                val file = path?.let { File(dir, path) }
                SetPage(file, videolist, navController)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartPage(videolist: SnapshotStateList<File>, navController: NavController) {
    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
                floatingActionButton = {
                    ChooseVideoButton(videolist)
                }
        ) {
            // Screen content
            Column {
                LazyVerticalGrid(
                        cells = GridCells.Adaptive(minSize = 128.dp)
                ) {
                    items(videolist.size) { i ->
                        VideoCard(videolist[i], navController)
                    }
                }
            }
        }
    }
}

const val videoDirName = "video"

@Composable
fun VideoCard(file: File, navController: NavController) {
    val scope = rememberCoroutineScope()
    val state = remember {
        val it = mutableStateOf<Bitmap?>(null)
        scope.launch {
            it.value = getCover(file)
        }
        it
    }
    val bitMap = state.value
    bitMap?.let {
        Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                        .aspectRatio(1f, true)
                        .clickable {
                            navController.navigate("set/${file.name}")
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
