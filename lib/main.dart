import 'dart:io';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

class AppGlobals {
  static String? fcmToken;
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  await _requestPermissions();
  final fcm = await FirebaseMessaging.instance.getToken();
  AppGlobals.fcmToken = fcm;
  FirebaseMessaging.instance.onTokenRefresh.listen((t) {
    AppGlobals.fcmToken = t;
  });

  print(fcm);

  runApp(MyApp(initialToken: AppGlobals.fcmToken));
}

Future<void> prewarmNative() async {
  const ch = MethodChannel('com.husamhammd.smsotp/native');
  try {
    await ch.invokeMethod('prewarm');
  } catch (_) {}
}

Future<void> _requestPermissions() async {
  if (Platform.isAndroid) {
    // طلب صلاحية إرسال SMS
    final sms = await Permission.sms.status;
    if (!sms.isGranted) {
      await Permission.sms.request();
    }

    // طلب صلاحية حالة الهاتف
    final state = await Permission.phone.status;

    if (!state.isGranted) {
      await Permission.phone.request();
    }
    // طلب صلاحية الإشعارات
    final notification = await Permission.notification.status;

    if (!notification.isGranted) {
      await Permission.notification.request();
    }
  }
}

class MyApp extends StatelessWidget {
  const MyApp({super.key, this.initialToken});
  final String? initialToken;
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SMS OTP HANDLER',
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(title: Text("SMS OTP HANDLER")),
        body: buildAppBody(initialToken),
      ),
    );
  }
}

Widget buildAppBody(String? token) {
  String? token0 = token;
  return Padding(
    padding: const EdgeInsets.all(16),
    child: Column(
      children: [
        Flexible(
          child: Card(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SizedBox(height: 100),
                  const Icon(Icons.vpn_key),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'FCM Token',
                          style: TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 6),
                        SelectableText(token!),
                        IconButton(
                          tooltip: 'Copy',
                          onPressed: token0 == null
                              ? null
                              : () async {
                                  await Clipboard.setData(
                                    ClipboardData(text: token0),
                                  );
                                },
                          icon: const Icon(Icons.copy),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),

        Text("by Husam hammad", style: TextStyle(fontWeight: FontWeight.bold)),
        Text("husam-hammad.com"),
      ],
    ),
  );
}
