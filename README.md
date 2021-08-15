# BiometricX

The easy way to use biometric authentication in your Flutter app.\
Supports Fingerprint, Face Recognition and Iris.\



## Starting

Published soon at...

```
$ flutter pub add biometricx
```

## Usage

Check biometric type available on the device.

```dart
BiometricType type = await BiometricX.type();
```

List of supported biometric types:

```dart
BiometricType.FACE
BiometricType.FINGERPRINT
BiometricType.IRIS
BiometricType.MULTIPLE
BiometricType.NONE
```

Check if device can use biometric.

```dart
bool isBiometricEnabled = await BiometricX.isEnabled();
```

Encrypt data using biometric authentication.

```dart
// Keep this messageKey to decrypt your message.
String? messageKey = await BiometricX.encrypt({
  biometricKey: 'salkuadrat',
  message: 'This is a very secret message',
});
```

Decrypt data using biometric authentication.

```dart
// Use the same biometricKey that is used to encrypt your message.
// Use messageKey that you get from [BiometricX.encrypt].
String? message = await BiometricX.decrypt({
  biometricKey: 'salkuadrat',
  messageKey: messageKey,
});
```

Showing custom message in your biometric prompt dialog.\
Method [encrypt] and [decrypt] have parameters that we can use to change biometric prompt dialog.

```dart
String? messageKey = await BiometricX.encrypt({
  biometricKey: 'salkuadrat',
  message: 'This is a very secret message',
  title: 'Biometric Encryption',
  subtitle: 'Enter biometric credentials to encrypt your message',
  description: 'Input Fingerprint or FaceID to ensure it\'s you!',
  negativeButtonText: 'CANCEL',
  confirmationRequired: false,
});
```

```dart
String? message = await BiometricX.decrypt({
  biometricKey: 'salkuadrat',
  messageKey: messageKey,
  title: 'Biometric Decryption',
  subtitle: 'Enter biometric credentials to decrypt your message',
  description: 'Input Fingerprint or FaceID to ensure it\'s you!',
  negativeButtonText: 'CANCEL',
  confirmationRequired: false,
});
```

## Example

[Example project](example).\
[Example APK]().