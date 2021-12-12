package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WallPaper : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return MotionEngine(this)
    }

    inner class MotionEngine(private val context: Context) : WallpaperService.Engine(), SensorEventListener {
        private lateinit var sensorManager: SensorManager
        private lateinit var sensor: Sensor
        private lateinit var holder: SurfaceHolder
        private lateinit var picture: VideoPicture
        private var viewModelJob = Job()
        private var job: Job? = null
        private val scope = CoroutineScope(Dispatchers.IO + viewModelJob)
        private var yAngle = 0f
        private var xAngle = 0f
        val xRange = 40f
        val yRange = 40f

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                yAngle = 0f
                xAngle = 0f
                if (picture.isOutDated()) {
                    Log.d("picture", "outdated")
                    picture = VideoPicture(context)
                }

                sensorManager.registerListener(this, sensor, 20000)
            } else {
                sensorManager.unregisterListener(this)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            this.holder = surfaceHolder!!
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            picture = VideoPicture(context)
        }

        override fun onDestroy() {
            sensorManager.unregisterListener(this)
            super.onDestroy()
        }

        override fun onSensorChanged(event: SensorEvent) {
            updateAngle(event.values[0], event.values[1])
            if (job == null || job?.isCompleted == true) {
                job = scope.launch {
                    val canvas = holder.lockHardwareCanvas()
                    val bitmap = picture.getPicture(-yAngle/yRange)
                    bitmap?.let {
                        drawBitMapOnCanvas(it, canvas, -xAngle/xRange)
                    }
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun updateAngle(x: Float, y: Float) {
            xAngle += x
            xAngle = if (xAngle > xRange) xRange else if (xAngle < -xRange) -xRange else xAngle
            yAngle += y
            yAngle = if (yAngle > yRange) yRange else if (yAngle < -yRange) -yRange else yAngle
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
}

fun drawBitMapOnCanvas(bitmap: Bitmap, canvas: Canvas, bias: Float, edge: Float = 0.1f) {
    val rect = Rect()
    canvas.getClipBounds(rect)
    val w = bitmap.width
    val h = (bitmap.height * (1 - edge)).toInt()
    val biasH = (bitmap.height * edge / 2 * (bias + 1)).toInt()
    val bitmapRect = if (w * rect.height() > h * rect.width()) {
        val newWidth = rect.width() * h / rect.height()
        Rect((w - newWidth)/2, biasH, (w + newWidth)/2, h + biasH)
    } else {
        val newHeight = rect.height() * w / rect.width()
        Rect(0, (h - newHeight)/2 + biasH, w, (h + newHeight)/2 + biasH)
    }
    canvas.drawBitmap(bitmap, bitmapRect, rect, null)
}