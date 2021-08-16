import 'dart:async';
import 'package:flutter/services.dart';
import 'biometric_result.dart';

export 'biometric_result.dart';

/// BiometricX flutter plugin to connect with native android BiometricX.
class BiometricX {
  static const MethodChannel _channel = const MethodChannel('biometricx');

  /// Get the [BiometricType] available on the device.
  ///
  /// [BiometricType.FACE]
  /// [BiometricType.FINGERPRINT]
  /// [BiometricType.IRIS]
  /// [BiometricType.MULTIPLE]
  /// [BiometricType.NONE]
  /// [BiometricType.NO_HARDWARE]
  /// [BiometricType.UNAVAILABLE]
  /// [BiometricType.UNSUPPORTED]
  static Future<BiometricType> get type async {
    try {
      final String? type = await _channel.invokeMethod('type');

      print('Biometric type: $type');

      switch (type) {
        case 'FACE':
          return BiometricType.FACE;
        case 'FINGERPRINT':
          return BiometricType.FINGERPRINT;
        case 'IRIS':
          return BiometricType.IRIS;
        case 'MULTIPLE':
          return BiometricType.MULTIPLE;
        case 'NONE':
          return BiometricType.NONE;
        case 'NO_HARDWARE':
          return BiometricType.NO_HARDWARE;
        case 'UNAVAILABLE':
          return BiometricType.UNAVAILABLE;
        default:
          return BiometricType.UNSUPPORTED;
      }
    } on Exception {}

    return BiometricType.UNSUPPORTED;
  }

  /// Returns true if device can use biometric.
  static Future<bool> get isEnabled async {
    try {
      final bmType = await type;

      return bmType == BiometricType.FACE ||
          bmType == BiometricType.FINGERPRINT ||
          bmType == BiometricType.IRIS ||
          bmType == BiometricType.MULTIPLE;
    } on Exception {
      return false;
    }
  }

  /// Encrypt message using biometric.
  static Future<BiometricResult> encrypt({
    required String biometricKey,
    required String message,
    String messageKey = '',
    String title = 'Biometric Authentication',
    String subtitle = 'Enter biometric credentials to proceed',
    String description = '',
    String negativeButtonText = 'CANCEL',
    bool confirmationRequired = false,
    bool deviceCredentialAllowed = false,
  }) async {
    try {
      final resultKey = await _channel.invokeMethod('encrypt', {
        'biometric_key': biometricKey,
        'message_key': messageKey,
        'message': message,
        'title': title,
        'subtitle': subtitle,
        'description': description,
        'negative_button_text': negativeButtonText,
        'confirmation_required': confirmationRequired,
        'device_credential_allowed': deviceCredentialAllowed,
      });

      if (resultKey is String) {
        return BiometricResult(
          status: BiometricStatus.SUCCESS,
          data: resultKey,
        );
      } else {
        final bmType = await type;

        return BiometricResult(
          status: BiometricStatus.FAILED,
          type: bmType,
        );
      }
    } on Exception {
      return BiometricResult(status: BiometricStatus.CANCELED);
    }
  }

  /// Decrypt message using biometric.
  static Future<BiometricResult> decrypt({
    required String biometricKey,
    required String messageKey,
    String title = 'Biometric Authentication',
    String subtitle = 'Enter biometric credentials to proceed',
    String description = '',
    String negativeButtonText = 'CANCEL',
    bool confirmationRequired = false,
    bool deviceCredentialAllowed = false,
  }) async {
    try {
      final String? message = await _channel.invokeMethod('decrypt', {
        'biometric_key': biometricKey,
        'message_key': messageKey,
        'title': title,
        'subtitle': subtitle,
        'description': description,
        'negative_button_text': negativeButtonText,
        'confirmation_required': confirmationRequired,
        'device_credential_allowed': deviceCredentialAllowed,
      });

      if (message is String) {
        return BiometricResult(
          status: BiometricStatus.SUCCESS,
          data: message,
        );
      } else {
        final bmType = await type;

        return BiometricResult(
          status: BiometricStatus.FAILED,
          type: bmType,
        );
      }
    } on Exception {
      return BiometricResult(status: BiometricStatus.CANCELED);
    }
  }
}
