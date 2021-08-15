import 'package:biometricx/biometricx.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:provider/provider.dart';

import '../states/states.dart';
import 'message_item.dart';

class MessageList extends StatefulWidget {
  const MessageList({Key? key}) : super(key: key);

  @override
  _MessageListState createState() => _MessageListState();
}

class _MessageListState extends State<MessageList> {
  AppState get app => context.read<AppState>();
  MessagesState get messages => context.read<MessagesState>();

  Future<void> _requestRead(int index) async {
    final message = messages.item(index);
    final messageKey = message['message_key'] as String;

    final decryptedMessage = await Biometricx.decrypt(
      biometricKey: app.biometricKey,
      messageKey: messageKey,
      title: 'Biometric Permission',
      subtitle: 'Enter biometric credentials to read this message',
    );

    if (decryptedMessage is String) {
      app.read(decryptedMessage);
    }
  }

  Future<void> _requestDelete(int index) async {
    final message = messages.item(index);
    final messageKey = message['message_key'] as String;

    final decryptedMessage = await Biometricx.decrypt(
      biometricKey: app.biometricKey,
      messageKey: messageKey,
      title: 'Biometric Permission',
      subtitle: 'Enter biometric credentials to delete this message',
    );

    if (decryptedMessage is String) {
      bool isDeleted = await messages.delete(index);

      if (isDeleted) {
        Fluttertoast.showToast(
          msg: 'Message is deleted.',
          toastLength: Toast.LENGTH_SHORT,
          gravity: ToastGravity.CENTER,
          timeInSecForIosWeb: 1,
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<MessagesState>(
      builder: (_, messages, __) {
        return ListView.builder(
          itemCount: messages.count,
          itemBuilder: (_, index) => MessageItem(
            index,
            onRequestRead: () => _requestRead(index),
            onRequestDelete: () => _requestDelete(index),
          ),
        );
      },
    );
  }
}
