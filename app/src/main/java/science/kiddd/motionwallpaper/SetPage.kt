package science.kiddd.motionwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SetPage(file: File?, files: SnapshotStateList<File>, navController: NavController) {
    val context = LocalContext.current
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (toolbar) = createRefs()
        val scope = rememberCoroutineScope()
        val state = remember {
            val it = mutableStateOf<Bitmap?>(null)
            scope.launch {
                it.value = getCover(context, file)
            }
            it
        }
        val bitMap = state.value
        bitMap?.let {
            Image(
                bitmap = it.asImageBitmap(), "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Row(
            modifier = Modifier.background(Color(0x88888888), RoundedCornerShape(20.dp))
                .height(40.dp)
                .constrainAs(toolbar) {
                    bottom.linkTo(parent.bottom, 20.dp)
                    start.linkTo(parent.start, 20.dp)
                    end.linkTo(parent.end, 20.dp)
                    width = Dimension.fillToConstraints
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ModeButton()
            IconButton(onClick = {
                files.remove(file)
                file?.delete()
                navController.popBackStack()
            }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Finish")
            }
            IconButton(onClick = {
                file?.let { preparePictureCache(context, it) }
            }) {
                Icon(Icons.Rounded.Check, contentDescription = "Finish")
            }
        }

    }
}

@Composable
fun ModeButton() {
    val expanded = remember { mutableStateOf(false) }
    val selectedIndex = remember { mutableStateOf(0) }
    IconButton(onClick = {
        expanded.value = !expanded.value
    }) {
        Icon(Icons.Rounded.Menu, contentDescription = "Finish")
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            listOf("Motion", "Scroll").forEachIndexed { index, mode ->
                DropdownMenuItem(onClick = {
                    selectedIndex.value = index
                    expanded.value = false
                }) {
                    Text(text = mode)
                }
            }
        }
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