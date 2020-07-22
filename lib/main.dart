import 'dart:async';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import 'camera.dart';


List<CameraDescription> cameras;

Future<Null> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown
  ]);

  try {
    cameras = await availableCameras();
  } on CameraException catch (e) {
    print('Error: $e.code\nError Message: $e.message');
  }
  runApp(new MyApp());
}

Future requestPermission(BuildContext context) async {
  // 申请权限
  Map<PermissionGroup, PermissionStatus> permissions =
  await PermissionHandler().requestPermissions([PermissionGroup.storage, PermissionGroup.camera]);
  if (permissions == PermissionStatus.granted) {

  } else {

  }
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    requestPermission(context);
    return MaterialApp(
      title: 'tflite real-time detection',
      theme: ThemeData(
        brightness: Brightness.dark,
      ),
      home: Camera(cameras),
    );
  }
}