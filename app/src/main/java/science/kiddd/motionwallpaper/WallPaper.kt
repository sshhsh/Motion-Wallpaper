package science.kiddd.motionwallpaper

import android.content.Context
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class WallPaper : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return MotionEngine(this)
    }

    inner class MotionEngine(private val context: Context) : WallpaperService.Engine() {
        private lateinit var motionDrawer: MotionDrawer

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                motionDrawer.start(true)
            } else {
                motionDrawer.pause()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            motionDrawer = MotionDrawer(context, surfaceHolder)
        }

        override fun onDestroy() {
            motionDrawer.destroy()
            super.onDestroy()
        }

    }
}