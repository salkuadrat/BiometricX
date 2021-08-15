import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../states/states.dart';

class MessageItem extends StatelessWidget {
  final int index;
  final void Function()? onRequestRead;
  final void Function()? onRequestDelete;

  const MessageItem(
    this.index, {
    Key? key,
    this.onRequestRead,
    this.onRequestDelete,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Consumer<MessagesState>(
      builder: (_, messages, __) {
        final message = messages.item(index);
        final dt = DateTime.fromMillisecondsSinceEpoch(message['timestamp']);
        final dateStr = DateFormat('dd MMM yyyy HH:mm:ss').format(dt);

        return GestureDetector(
          onTap: onRequestRead,
          child: Container(
            margin: EdgeInsets.only(
              top: index == 0 ? 18 : 0,
              bottom: 14,
              left: 15,
              right: 15,
            ),
            child: Card(
              child: ListTile(
                title: Text('Secret message...'),
                subtitle: Text(dateStr),
                trailing: IconButton(
                  onPressed: onRequestDelete,
                  icon: Icon(Icons.delete_outline),
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
