package science.kiddd.motionwallpaper

import android.content.Context
import android.graphics.Bitmap
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

class MotionDrawer(private val context: Context, private val holder: SurfaceHolder) :
    SensorEventListener, MyDrawer {
    private var sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

    private var lastBitmap: Bitmap? = null
    private var lastBias = Float.MIN_VALUE
    private val minBias = 1f / 10 * edgeWidth

    override fun onSensorChanged(event: SensorEvent) {
        updateAngle(event.values[0], event.values[1])
        if (job == null || job?.isCompleted == true) {
            job = scope.launch {
                val bitmap = picture.getPicture(-yAngle / yRange)
                val newBitmap = bitmap ?: lastBitmap
                val newBias = (-xAngle / xRange).let {
                    if (kotlin.math.abs(it - lastBias) > minBias) it else lastBias
                }
                if (kotlin.math.abs(newBias - lastBias) > minBias || bitmap !== null) {
                    newBitmap?.let {
                        val canvas = holder.lockHardwareCanvas()
                        drawBitMapOnCanvas(it, canvas, newBias, edgeWidth)
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
                lastBias = newBias
                lastBitmap = newBitmap
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

    override fun start(clear: Boolean) {
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

    override fun pause() {
        sensorManager.unregisterListener(this)
    }

    override fun destroy() {
        sensorManager.unregisterListener(this)
        renderJob.cancel()
    }

    override fun offset(offset: Float) {
    }
}
