import 'package:biometricx/biometricx.dart';
import 'package:flutter/material.dart';
import 'package:flutter_styled_toast/flutter_styled_toast.dart';
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

  /* @override
  void initState() {
    super.initState();
    WidgetsBinding.instance?.addPostFrameCallback((_) async { 
      await _encryptWithMessageKey();
    });
  }

  Future<void> _encryptWithMessageKey() async {
    final result = await BiometricX.encrypt(
      biometricKey: app.biometricKey,
      messageKey: 'username',
      message: 'This is a secret message',
    );

    if (result.isSuccess) {
      print(result.data);
    }
  } */

  Future<void> _requestRead(int index) async {
    final message = messages.item(index);
    final messageKey = message['message_key'] as String;

    final result = await BiometricX.decrypt(
      biometricKey: app.biometricKey,
      messageKey: messageKey,
      title: 'Biometric Permission',
      subtitle: 'Enter biometric credentials to read this message',
    );

    if (result.isSuccess) {
      app.read(result.data!);
      return;
    }

    if (result.isFailed) {
      showToast(
        result.errorMsg,
        context: context,
        animation: StyledToastAnimation.fade,
        position: StyledToastPosition.center,
      );
    }
  }

  Future<void> _requestDelete(int index) async {
    final message = messages.item(index);
    final messageKey = message['message_key'] as String;

    final result = await BiometricX.decrypt(
      biometricKey: app.biometricKey,
      messageKey: messageKey,
      title: 'Biometric Permission',
      subtitle: 'Enter biometric credentials to delete this message',
    );

    if (result.isSuccess) {
      bool isDeleted = await messages.delete(index);

      if (isDeleted) {
        showToast(
          'Message is deleted.',
          context: context,
          animation: StyledToastAnimation.fade,
          position: StyledToastPosition.center,
        );
      }
      return;
    }

    if (result.isFailed) {
      showToast(
        result.errorMsg,
        context: context,
        animation: StyledToastAnimation.fade,
        position: StyledToastPosition.center,
      );
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
