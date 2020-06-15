English |  <a href="README.ko.md">한국어</a>

## APK Quick Install
This is a service that detects the creation or change of the apk file of a specific path on the PC, sends it to the Android device, and requests installation. In one word, As soon as you build the apk in Android Studio, <strong>"Do you want to install it?"</strong> appears on the screen of your phone.

![fin](https://user-images.githubusercontent.com/43294688/84275058-92be0b00-ab6b-11ea-91ec-10b020ed0fb4.gif)

### Motivation
This service is useless in most situations, but it is good when you have the following restrictions:

* <strong>USB connection between PC and device is not possible (so wireless adb cannot be used)</strong>
* <strong>AVD not available</strong>

There is only one way left in this situation. Whenever you need to run your app, build the apk file, upload to Google Drive, download form phone and install it. It is APKQI that automates this annoying process.

### Usage
 _Before you use this application, The server must be running on your PC. More information: <a href="https://github.com/wirekang/apk-quick-install-server">APKQI Server</a>_ 
 
1. <a href="https://play.google.com/store/apps/details?id=com.wirekang.apkqi">Download android app from Google Play Store</a>.
2. Start application on your android device.
3. Eneter IP Address of your computer and port
4. Run Service.
