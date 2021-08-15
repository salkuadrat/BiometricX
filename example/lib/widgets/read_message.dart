import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../states/states.dart';

class ReadMessage extends StatelessWidget {
  final String message;

  const ReadMessage(this.message, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: Center(
            child: Padding(
              padding: EdgeInsets.all(18),
              child: Text(
                message,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ),
        Consumer<AppState>(
          builder: (_, app, __) => Container(
            padding: EdgeInsets.symmetric(
              vertical: 25,
              horizontal: 20,
            ),
            child: ElevatedButton(
              onPressed: app.showList,
              child: Text(
                'Close Message',
                style: TextStyle(fontSize: 15),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
