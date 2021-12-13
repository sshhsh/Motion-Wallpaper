package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MotionDrawer(private val context: Context, private val holder: SurfaceHolder) : SensorEventListener {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var picture: VideoPicture = VideoPicture(context)
    private var renderJob = Job()
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + renderJob)
    private var yAngle = 0f
    private var xAngle = 0f
    private val xRange = 40f
    private val yRange = 40f
    private val frameRate = 60
    private val edgeWidth = 0.1f
    private val xSensitivity = 1f
    private val ySensitivity = 1f

    override fun onSensorChanged(event: SensorEvent) {
        updateAngle(event.values[0], event.values[1])
        if (job == null || job?.isCompleted == true) {
            job = scope.launch {
                val canvas = holder.lockHardwareCanvas()
                val bitmap = picture.getPicture(-yAngle/yRange)
                bitmap?.let {
                    drawBitMapOnCanvas(it, canvas, -xAngle/xRange, edgeWidth)
                }
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun updateAngle(x: Float, y: Float) {
        xAngle += x * xSensitivity
        xAngle = if (xAngle > xRange) xRange else if (xAngle < -xRange) -xRange else xAngle
        yAngle += y * ySensitivity
        yAngle = if (yAngle > yRange) yRange else if (yAngle < -yRange) -yRange else yAngle
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun start(clear: Boolean) {
        if (clear) {
            yAngle = 0f
            xAngle = 0f
        }
        if (picture.isOutDated()) {
            Log.d("picture", "outdated")
            picture = VideoPicture(context)
        }
        sensorManager.registerListener(this, sensor, 1000000 / frameRate)
    }

    fun pause() {
        sensorManager.unregisterListener(this)
    }

    fun destroy() {
        sensorManager.unregisterListener(this)
        renderJob.cancel()
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