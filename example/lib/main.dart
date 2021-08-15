import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get_storage/get_storage.dart';
import 'package:provider/provider.dart';

import 'states/states.dart';
import 'widgets/widgets.dart';

main() async {
  await GetStorage.init();
  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider(create: (_) => AppState()),
      ChangeNotifierProvider(create: (_) => MessagesState()),
    ],
    child: App(),
  ));
}

class App extends StatelessWidget {
  const App({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.light,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
      ),
      themeMode: ThemeMode.dark,
      home: Home(),
    );
  }
}

class Home extends StatelessWidget {
  const Home({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (_, app, __) => WillPopScope(
        onWillPop: () {
          if (app.isWrite || app.isRead) {
            app.showList();
            return Future.value(false);
          }

          return Future.value(true);
        },
        child: Scaffold(
          appBar: AppBar(
            title: Text('BiometricX'),
            centerTitle: true,
          ),
          body: Builder(
            builder: (_) {
              if (app.isWrite) return WriteMessage();
              if (app.isRead) return ReadMessage(app.currentMessage);
              return MessageList();
            },
          ),
          floatingActionButtonLocation:
              FloatingActionButtonLocation.centerFloat,
          floatingActionButton: app.isList
              ? FloatingActionButton(
                  child: Icon(Icons.add_rounded),
                  onPressed: app.write,
                )
              : null,
        ),
      ),
    );
  }
}
