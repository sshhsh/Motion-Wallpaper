package science.kiddd.motionwallpaper

import android.content.Context
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlinx.coroutines.runBlocking

class WallPaper : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return MotionEngine(this)
    }

    inner class MotionEngine(private val context: Context) : WallpaperService.Engine() {
        private lateinit var myDrawer: MyDrawer

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                myDrawer.start(true)
            } else {
                myDrawer.pause()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val mode = runBlocking {
                getMode(context)
            }
            when (mode) {
                MODE.MOTION -> {
                    myDrawer = MotionDrawer(context, surfaceHolder)
                }
                MODE.SCROLL -> {
                    myDrawer = OffsetDrawer(context, surfaceHolder)
                }
            }
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            myDrawer.offset(xOffset)
        }

        override fun onDestroy() {
            myDrawer.destroy()
            super.onDestroy()
        }
    }
}