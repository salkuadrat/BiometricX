/// Biometric Type
enum BiometricType {
  FACE,
  FINGERPRINT,
  IRIS,
  MULTIPLE,
  NONE,
  NO_HARDWARE,
  UNAVAILABLE,
  UNSUPPORTED,
}

/// BiometricResult Status
enum BiometricStatus {
  SUCCESS,
  FAILED,
  CANCELED,
}

/// Biometric Result
class BiometricResult {
  final BiometricStatus status;
  final BiometricType? type;
  final String? data;

  bool get hasData => data is String;
  bool get isSuccess => status == BiometricStatus.SUCCESS;
  bool get isFailed => status == BiometricStatus.FAILED;
  bool get isCanceled => status == BiometricStatus.CANCELED;

  String get errorMsg {
    if (isSuccess || isCanceled) {
      return '';
    }

    if (isFailed) {
      if (type == BiometricType.NO_HARDWARE)
        return 'There is no biometric hardware.';
      if (type == BiometricType.UNAVAILABLE)
        return 'The hardware is unavailable.';
      if (type == BiometricType.NONE)
        return 'No biometrics enrolled in your device.';
      if (type == BiometricType.UNSUPPORTED)
        return 'Biometric is not supported in your device.';
    }
    return '';
  }

  BiometricResult({
    required this.status,
    this.type,
    this.data,
  });
}
