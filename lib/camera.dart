import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter/services.dart';
import 'dart:math' as math;


typedef void Callback(List<dynamic> list, int h, int w);

class Camera extends StatefulWidget {
  final List<CameraDescription> cameras;
  Camera(this.cameras);

  @override
  _CameraState createState() => new _CameraState();
}



class _CameraState extends State<Camera> {
  CameraController controller;
  bool isDetecting = false;
  static const platform = const MethodChannel('samples.flutter.io/battery');

  void motiondetect(Uint8List bytes, int width, int height) async {
    if(width < height){
      var x = width;
      width = height;
      height = x;
    }
    Map<String, Object> map = {
      "data": bytes,
      "width": width,
      "height": height
    };
    final int  result  = await platform.invokeMethod('getBatteryLevel', map);
    print("getBatteryLevel" +  result.toString());
  }


  @override
  void initState() {
    super.initState();

    if (widget.cameras == null || widget.cameras.length < 1) {
      print('No camera is found');
    } else {
      controller = new CameraController(
        widget.cameras[0],
        ResolutionPreset.high,
      );
      controller.initialize().then((_) {
        if (!mounted) {
          return;
        }
        setState(() {});
        controller.startImageStream((CameraImage img) {
          img.planes.forEach((element) {
            motiondetect(element.bytes, img.width, img.height);
          });

        });
      });
    }
  }

  @override
  void dispose() {
    controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (controller == null || !controller.value.isInitialized) {
      return Container();
    }

    var tmp = MediaQuery.of(context).size;
    var screenH = math.max(tmp.height, tmp.width);
    var screenW = math.min(tmp.height, tmp.width);
    tmp = controller.value.previewSize;
    var previewH = math.max(tmp.height, tmp.width);
    var previewW = math.min(tmp.height, tmp.width);
    var screenRatio = screenH / screenW;
    var previewRatio = previewH / previewW;

//    return new ListView(
//      children: [
//        new Container(
//          child: CameraPreview(controller),
//            width: 300,
//            height: 400,
//          ),
//        new Image.asset(
//          'assets/cover.jpg',
//          width: screenW,
//          height: screenW * 3 / 4,
//          fit: BoxFit.cover,
//        ),
//      ],
//    );
    return OverflowBox(
      maxHeight:
      screenRatio > previewRatio ? screenH : screenW / previewW * previewH,
      maxWidth:
      screenRatio > previewRatio ? screenH / previewH * previewW : screenW,
      child: CameraPreview(controller),
    );
  }
}