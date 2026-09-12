# VZC-ADAS Auto Android Prototype v2

This version automatically reads supported phone telemetry after the user grants permission:
- Accelerometer
- Gyroscope
- GPS speed/location
- Camera permission is requested so the app can be extended with traffic-light vision.

It continuously applies the project's heuristic risk formula to live telemetry.

Important: Android does not allow an app to silently access all device tools. The user must grant
permissions. This prototype does not record/upload sensor data and does not run as a guaranteed
safety system. It is an experimental research prototype.

Build in Android Studio:
Build > Build APK(s)
