package science.kiddd.motionwallpaper

import android.content.Context
import android.view.SurfaceHolder

class OffsetDrawer(context: Context, private val holder: SurfaceHolder) : MyDrawer {
    private var picture: VideoPicture = VideoPicture(context)

    override fun start(clear: Boolean) {
    }

    override fun pause() {
    }

    override fun destroy() {
    }

    override fun offset(offset: Float) {
        val bitmap = picture.getPicture(offset * 2 - 1) ?: return
        val canvas = holder.lockHardwareCanvas()
        drawBitMapOnCanvas(bitmap, canvas, 0f, 0f)
        holder.unlockCanvasAndPost(canvas)
    }
}