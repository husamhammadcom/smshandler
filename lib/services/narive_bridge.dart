import 'package:flutter/services.dart';

class NativeBridge {
  static const _ch = MethodChannel('com.husamhammd.smsotp/native');

  static Future<bool> scheduleSms(String phone, String text) async {
    final ok = await _ch.invokeMethod<bool>('scheduleSms', {
      'phone': phone,
      'text': text,
    });
    return ok ?? false;
  }
}
