package com.notai.app.util

import android.graphics.Bitmap
import com.notai.app.domain.model.WatermarkRegion
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaskGenerator @Inject constructor() {

    fun createMask(width: Int, height: Int, region: WatermarkRegion): Mat {
        val mask = Mat.zeros(height, width, CvType.CV_8UC1)

        val left = (width * region.x).toInt()
        val top = (height * region.y).toInt()
        val right = (left + width * region.w).toInt()
        val bottom = (top + height * region.h).toInt()

        val roi = mask.submat(top, bottom, left, right)
        roi.setTo(org.opencv.core.Scalar(255.0))
        roi.release()

        // Feather edges for smoother inpainting
        val blurred = Mat()
        Imgproc.GaussianBlur(mask, blurred, Size(5.0, 5.0), 0.0)
        mask.release()
        return blurred
    }

    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
}
