import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:travel_guide/features/screens/travel_guide_screen.dart';

void main() async {
  await dotenv.load(fileName: ".env");
  runApp(TravelGuide());
}

class TravelGuide extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: TravelGuideScreen(),
    );
  }
}
