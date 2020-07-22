package com.fluttercamera


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.text.TextUtils
import android.util.Log

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


object BitmapUtil {
    private val TAG = "BitmapUtil"
    var WIDTH = 640
    var HEIGHT = 480

    var MIU_WIDTH = 320
    var MIU_HEIGHT = 240

    interface OnSaveImgListener {
        fun onSuccess(filePath: String?)
        fun onFiled(meaasge: String)
    }

    fun saveYuvTpJpg(angel: Float, path: String, data: ByteArray, ocr: Boolean, isMiorr: Boolean,width: Int, height: Int,listener: OnSaveImgListener) {
        Thread(Runnable {
            var srcpath: String? = null
            val bitmap = yuvToRGB(data, ocr, width, height)
            Log.d(TAG, "bitmapWidth: " + bitmap.width + "    bitmapHeight: " + bitmap.height)
            val imageFile = File(path)
//            if (Build.BRAND.contains("3Q") || Build.BRAND.contains("NV6001") || Build.MODEL.contains("NV6001")) {
//                srcpath = BitmapUtil.saveToInternalStorage(imageFile, bitmap)
//            } else {
//                val rotate = BitmapUtil.rotateBitmap(angel, bitmap)
//                srcpath = BitmapUtil.saveToInternalStorage(imageFile, rotate)
//            }
            if(isMiorr){
                var mirror = rotaingImageView(angel.toInt(), bitmap, -1F, 1F)
                srcpath = BitmapUtil.saveToInternalStorage(imageFile, mirror)
            }else{
                val rotate = BitmapUtil.rotateBitmap(angel, bitmap)
                srcpath = BitmapUtil.saveToInternalStorage(imageFile, rotate)
            }
            if (TextUtils.isEmpty(srcpath)) {
                listener.onFiled("Picture save failed ！")
            } else {
                listener.onSuccess(srcpath)
            }
        }).start()
    }

    fun saveYuvTpJpg(angel: Float, path: String, data: ByteArray, ocr: Boolean, listener: OnSaveImgListener) {
        saveYuvTpJpg(angel, path, data, ocr, false, WIDTH, HEIGHT, listener)
    }

    /**
     * 将yuv格式的byte数组转化成RGB的bitmap
     */
    fun yuvToRGB(data: ByteArray, ocr: Boolean, _width: Int, _height: Int): Bitmap {
        var width = _width
        var height = _height
        var quality = 80
        if (!ocr) {
            width = width.div(2)
            height = height.div(2)
        }
        val yuvimage = YuvImage(data, ImageFormat.NV21, width, height, null)
        val baos = ByteArrayOutputStream()
        yuvimage.compressToJpeg(Rect(0, 0, width, height), quality, baos)
        return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().size)
    }

    fun bitMapScale(bitmap: Bitmap?, scale: Float): Bitmap {
        var bitmap = bitmap
        val matrix = Matrix()
        matrix.postScale(scale, scale) //长和宽放大缩小的比例
        val resizeBmp = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        bitmap = null
        return resizeBmp
    }

    /**
     * 保存Yuv数据到Jpg文件中
     * 暂时没有清理机制，考虑如果将来图片过大，可能需要清理一下。
     *
     * @param data
     * @param destPath
     */
    fun saveYuvToJpgFile(data: ByteArray, width: Int, height: Int, destPath: String) {
        val file = File(destPath)
        if (file.exists()) {
            file.delete()
        }
        var created = false
        var fos: FileOutputStream? = null
        try {
            created = file.createNewFile()
            fos = FileOutputStream(file)
            val yuvimage = YuvImage(data, ImageFormat.NV21, width, height, null)
            yuvimage.compressToJpeg(Rect(0, 0, width, height), 80, fos)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.flush()
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * 将argb_8888的图像转化为w*h*4的字节数组，算法使用
     */
    fun bitmap2byte(bitmap: Bitmap): ByteArray {
        val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(byteBuffer)
        return byteBuffer.array()
    }

    /**
     * 将图片按照固定比例进行压缩
     */
    fun resizeBitmapWithConstantWHRatio(bmp: Bitmap?, mWidth: Int, mHeight: Int): Bitmap? {
        if (bmp != null) {
            val width = bmp.width.toFloat() //728
            val height = bmp.height.toFloat() //480
            Log.d(TAG, "----原图片的宽度:" + bmp.width + ", 高度:" + bmp.height) //720/480 = 1.5

            var scale = 1.0f
            val scaleX = mWidth.toFloat() / width
            val scaleY = mHeight.toFloat() / height
            if (scaleX < scaleY && (scaleX > 0 || scaleY > 0)) {
                scale = scaleX
            }
            if (scaleY <= scaleX && (scaleX > 0 || scaleY > 0)) {
                scale = scaleY
            }

            Log.d(TAG, "-----scaleX:$scale , scaleY:$scale")
            return resizeBitmapByScale(bmp, scale)
        }
        return null
    }

    fun resizeBitmapByScale(bitmap: Bitmap, scale: Float): Bitmap {
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val width = bitmap.width.toFloat() //728
        val height = bitmap.height.toFloat() //480
        return Bitmap.createBitmap(bitmap, 0, 0, width.toInt(), height.toInt(), matrix, true)
    }

    /***将bitmap写进指定路径 */
    fun saveToInternalStorage(imageFile: File, bitmapImage: Bitmap): String {
        var path = ""
        if (!imageFile.exists()) {
            try {
                imageFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            path = imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bitmapImage.recycle()
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return path //得到.jpg的全路径名
    }

    /**
     * bitmap转化为byte[]数组，网络传输使用
     */
    fun bitmap2Bytes(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        return baos.toByteArray()
    }

    /**
     * sx, sy -1, 1   左右翻转   1, -1  上下翻转
     */
    fun rotaingImageView(angle: Int, srcBitmap: Bitmap, sx: Float, sy: Float): Bitmap {
        val matrix = Matrix()  //使用矩阵 完成图像变换
        if (sx != 0f || sy != 0f) {
            matrix.postScale(sx, sy)  //重点代码，记住就ok
        }

        val w = srcBitmap.width
        val h = srcBitmap.height
        val cacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cacheBitmap)  //使用canvas在bitmap上面画像素

        matrix.postRotate(angle.toFloat())
        val retBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, matrix, true)
        canvas.drawBitmap(retBitmap, Rect(0, 0, w, h), Rect(0, 0, w, h), null)
        return retBitmap
    }

    fun rotateBitmap(angle: Float, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true)
    }

}
