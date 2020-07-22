package com.fluttercamera

import android.os.Bundle
import android.util.Log
import com.turing.os.init.SdkInitializer
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.turingapi.turingstory.BookRecognizer

class MainActivity: FlutterActivity() {
    companion object {
        const val FLUTTER_LOG_CHANNEL = "android_log"
    }
    private var bookRecognizer: BookRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    var methodChannel: MethodChannel? = null;
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
//        GeneratedPluginRegistrant.registerWith(flutterEngine)
//        FlutterPluginTestNewPlugin.registerWith(flutterEngine, "samples.flutter.io/battery")

        initmoveDetect(0.0f, false)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "samples.flutter.io/battery")
        methodChannel?.setMethodCallHandler { call, result ->
            if (call.method.equals("getBatteryLevel")) {
                var data: ByteArray? = call.argument("data")
                var width: Int? = call.argument("width")
                var height: Int? = call.argument("height")

                if(data != null && data.size > 0){
                    Log.e("yhp", "width:" + width + "height:" + height + "================" + data.size)
                    moveDetect(data, data!!.size, width!!, height!!, 0, 0)
                }else{
                    Log.e("yhp", "================")
                }

//                Log.e("yhp", "================")
//                val batteryLevel: Int = 100
//                if (batteryLevel != -1) {
//                    result.success(batteryLevel)
//                } else {
//                    result.error("UNAVAILABLE", "Battery level not available.", null)
//                }
            } else {
                result.notImplemented()
            }
        }
    }

    fun initmoveDetect(angle: Float, _isMiorr: Boolean){
        bookRecognizer = BookRecognizer()
        imageAngle = angle
        isMiorr = _isMiorr
    }
    var isMiorr: Boolean = true
    var isIdle: Boolean = true
    var imageAngle: Float = 0.0f
    fun moveDetect(yuvData: ByteArray?, length: Int?, width: Int, height: Int, ocrFlag: Int, fingerFlag: Int){
        if(bookRecognizer == null) return
        val moveValue = bookRecognizer!!.moveDetect(yuvData, width, height, ocrFlag, fingerFlag)
        if (moveValue != null) {
            Log.e("BookRecognizer", "========================" + isIdle)
            if (isIdle) {
                isIdle = false
                saveImage(moveValue, ocrFlag == 1, width, height)
            }
        }
    }
    private fun saveImage(data: ByteArray?,  isOcr: Boolean, width: Int, height: Int){
        //第七步：将翻页检测的结果保存为图片
        var timeStamp: Long = System.currentTimeMillis()
        var filePath = SdkInitializer.getFilePath() + "_" + timeStamp +"_book_image.jpg"
        BitmapUtil.saveYuvTpJpg(imageAngle, filePath!!, data!!, isOcr, isMiorr, width, height, object : BitmapUtil.OnSaveImgListener {
            override fun onFiled(meaasge: String) {
                isIdle = true
                Log.e("BookRecognizer", "saveImage failed: " + meaasge)
            }

            override fun onSuccess(filePath: String?) {
                Log.e("BookRecognizer", "saveImage successs: " + filePath)
                isIdle = true
            }

        })
    }

}

