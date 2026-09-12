# Build the VZC ADAS APK on GitHub

1. Create a GitHub repository, for example `vzc-adas-android`.
2. Upload all files and folders from this project to the repository root. Make sure `.github/workflows/build-apk.yml` is included.
3. Open the repository's **Actions** tab.
4. Select **Build VZC ADAS APK**.
5. Click **Run workflow**.
6. Wait for the workflow to finish successfully.
7. Open the completed workflow run and scroll to **Artifacts**.
8. Download **VZC-ADAS-debug-apk**. It contains the real `app-debug.apk` built by Android's Gradle toolchain.

The workflow uses Java 17, Android API 35/build-tools 35.0.0, and Gradle 8.9.
