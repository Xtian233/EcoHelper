import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.ecohelper.Escaneo
import com.example.ecohelper.ml.ModelUnquant
import com.example.ecohelper.ReturnInterpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ClassifyImageTf(context: Context?) {
    private val modelUnquant: ModelUnquant? = ModelUnquant.newInstance(context!!)
    private var returnInterpreter: ReturnInterpreter? = null

    fun setListenerInterpreter(returnInterpreter: ReturnInterpreter?) {
        this.returnInterpreter = returnInterpreter
    }

    fun classify(img: Bitmap?) {
        if (modelUnquant == null) {
            Log.e(TAG, "Model is not initialized")
            return }
        val inputSize = Escaneo.INPUT_SIZE
        val inputShape = intArrayOf(1, inputSize, inputSize, 3)
        val inputFeature0 = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32)
        val byteBuffer = convertBitmapToByteBuffer(img)
        if (byteBuffer == null) {
            Log.e(TAG, "ByteBuffer is null")
            return
        }
        inputFeature0.loadBuffer(byteBuffer)
        byteBuffer.clear()
        val outputs = modelUnquant.process(inputFeature0)
        val outputFeature = outputs.getOutputFeature0AsTensorBuffer()
        val confidence = outputFeature.floatArray
        val maxPos = getMaxConfidenceIndex(confidence)
        if (returnInterpreter != null) {
            returnInterpreter!!.classify(confidence, maxPos)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer? {
        if (bitmap == null) {
            return null
        }
        val inputSize = Escaneo.INPUT_SIZE
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(
            intValues,
            0,
            bitmap.getWidth(),
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight()
        )
        for (pixelValue in intValues) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF) * (1f / 255f))
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF) * (1f / 255f))
            byteBuffer.putFloat((pixelValue and 0xFF) * (1f / 255f))
        }
        return byteBuffer
    }

    private fun getMaxConfidenceIndex(confidence: FloatArray): Int {
        var maxIndex = 0
        var maxConfidence = confidence[0]
        for (i in 1 until confidence.size) {
            if (confidence[i] > maxConfidence) {
                maxConfidence = confidence[i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    companion object {
        private const val TAG = "ClassifyImageTf"
    }
}
