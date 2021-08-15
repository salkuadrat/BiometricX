import 'package:biometricx/biometricx.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:provider/provider.dart';

import '../states/states.dart';

class WriteMessage extends StatefulWidget {
  const WriteMessage({Key? key}) : super(key: key);

  @override
  _WriteMessageState createState() => _WriteMessageState();
}

class _WriteMessageState extends State<WriteMessage> {
  AppState get app => context.read<AppState>();
  MessagesState get messages => context.read<MessagesState>();

  final controller = TextEditingController();

  Future<void> _saveMessage() async {
    print(controller.text);

    final result = await BiometricX.encrypt(
      biometricKey: app.biometricKey,
      message: controller.text,
      title: 'Biometric Permission',
      subtitle: 'Enter biometric credentials to save this message',
    );

    if (result.isSuccess) {
      await messages.add(result.data!);

      Fluttertoast.showToast(
        msg: 'Your message is saved',
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.CENTER,
        timeInSecForIosWeb: 1,
      );

      app.showList();
      return;
    }

    if (result.isFailed) {
      Fluttertoast.showToast(
        msg: result.errorMsg,
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.CENTER,
        timeInSecForIosWeb: 1,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: Center(
            child: TextField(
              controller: controller,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 20),
              textCapitalization: TextCapitalization.sentences,
              decoration: InputDecoration(
                border: InputBorder.none,
                focusedBorder: InputBorder.none,
                enabledBorder: InputBorder.none,
                errorBorder: InputBorder.none,
                disabledBorder: InputBorder.none,
                contentPadding: EdgeInsets.symmetric(
                  vertical: 15,
                  horizontal: 18,
                ),
                hintText: 'Write your secret message',
                hintStyle: TextStyle(fontSize: 20),
              ),
            ),
          ),
        ),
        Container(
          padding: EdgeInsets.symmetric(
            vertical: 25,
            horizontal: 20,
          ),
          child: ElevatedButton(
            onPressed: _saveMessage,
            child: Text(
              'Save Message',
              style: TextStyle(fontSize: 15),
            ),
          ),
        ),
      ],
    );
  }
}
