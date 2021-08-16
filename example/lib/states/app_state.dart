import 'package:flutter/foundation.dart';

enum AppScreenState {
  LIST,
  WRITE,
  READ,
}

class AppState extends ChangeNotifier {
  final String _biometricKey = 'salkuadrat';

  AppScreenState _screenState = AppScreenState.LIST;
  String _currentMessage = '';

  AppScreenState get screenState => _screenState;
  String get currentMessage => _currentMessage;
  String get biometricKey => _biometricKey;

  bool get isList => screenState == AppScreenState.LIST;
  bool get isWrite => screenState == AppScreenState.WRITE;
  bool get isRead => screenState == AppScreenState.READ;

  set screenState(AppScreenState value) {
    _screenState = value;
    notifyListeners();
  }

  void showList() {
    _screenState = AppScreenState.LIST;
    notifyListeners();
  }

  void write() {
    _screenState = AppScreenState.WRITE;
    notifyListeners();
  }

  void read(String message) {
    _currentMessage = message;
    _screenState = AppScreenState.READ;
    notifyListeners();
  }
}
