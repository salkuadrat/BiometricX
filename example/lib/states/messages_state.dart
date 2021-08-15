import 'package:flutter/foundation.dart';
import 'package:get_storage/get_storage.dart';

class MessagesState extends ChangeNotifier {

  List _messages = [];

  List get messages => _messages;
  int get count => _messages.length;
  bool get hasData => _messages.isNotEmpty;
  bool get isEmpty => _messages.isEmpty;

  item(int index) => _messages[index];

  MessagesState() {
    init();
  }

  Future<void> init() async {
    await _restore();
  }

  Future<void> add(String messageKey) async {
    final message = {
      'message_key': messageKey,
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    };

    _messages = [..._messages, message];
    await _save();
    notifyListeners();
  }

  Future<bool> delete(index) async {
    if (index < count) {
      _messages.removeAt(index);
      await _save();
      notifyListeners();
      return true;
    }
    return false;
  }

  Future<void> _save() async {
    final box = GetStorage();
    await box.write('messages', messages);
  }

  Future<void> _restore() async {
    final box = GetStorage();

    if (box.hasData('messages')) {
      final messages = box.read('messages');

      if (messages is List) {
        _messages = messages.toList();
        notifyListeners();
      }
    }
  }
}
