# travel-guide
Using Travel guide app user can access the set of information that is considered a prerequisite to having a hassle-free journey. The information that Travelbriefing.org provides includes the following: health &amp; vaccines, weather, visa requirements, currency, electric sockets and language etc for travellers around the world.

### Table of Contents
  - [Setup for Mac](#setup-for-mac)
    - [Homebrew](#homebrew)
    - [Android Studio](https://developer.android.com/studio) or [Visual Studio Code](https://code.visualstudio.com)
    - [Xcode](#Xcode)
    - [Flutter](https://flutter.dev/docs/get-started/install/macos)
    - [Build and Run the Application](#build-and-run-the-application)

Extract the zip to your development workspace like the example below: cd ~/workspace

unzip downloaded zip. Add flutter to your PATH variable in your .bash_profile export PATH=$PATH:~/workspace/flutter/bin

Note: Make sure to refresh your environment variables using the command source ~.bash_profile.

Run flutter doctor. This will tell you what else is missing from your environment and provide instructions to resolve those issues. Download Dart SDK(https://dart.dev/get-dart)

Build and Run the Application Checkout this project from GitHub to your workspace git clone https://github.com/bskalyaan/Travel-Info.git

Navigate to the project directory cd Travel-Info/

###Run the app
   flutter run

   #Build APK or IPA
   flutter build apk --release -t lib/main.dart
   flutter build ios --release -t lib/main.dart

   #Dart Analyzer
   To check linters added analysis_options.yaml file

   #Unit Tests
   flutter test
   flutter test test/yourtestfile.dart

   #Golden tests for one particular file
   flutter test --update-goldens <path_to_test_file>
   To run all
   flutter test

   #Integration Tests
   Add dependency flutter_driver in pubspec.yaml file. Follow flutter reference:
   https://flutter.dev/docs/cookbook/testing/integration/introduction

   #Test Coverage
   Run: flutter test --coverage which generates lcov.info path_to_test_file
   Run: genhtml coverage/lcov.info --output-directory coverage to generate html file in readable format.
   If genhtml is not installed run brew install genhtml

