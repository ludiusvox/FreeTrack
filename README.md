# FreeTrack - 100% Free Custom Android App in Kotlin

Built with **Google AI Studio**. Runs completely locally on your phone for **$0/month forever**.

### Features
1. **Native GPS & Barometer Tracking**:
   - Hooks into `FusedLocationProviderClient` for accurate coordinates and speed.
   - Reads hardware barometer (`Sensor.TYPE_PRESSURE`) for precise barometric altitude without paid external elevation APIs.
2. **Local Database (Room)**:
   - `GpsPointEntity` stores latitude, longitude, barometric elevation, GPS elevation, pressure, accuracy, and speed.
   - `WeightEntryEntity` logs daily weight entries and computes rolling 7-day averages.
   - 100% offline SQLite storage. Zero cloud bills.
3. **UI Layouts**:
   - Live Map View using **OpenStreetMap (OsmDroid)** - zero Google Maps API keys required!
   - Elevation Profile Graph using **MPAndroidChart** with gradient fill and ascent/descent totals.
   - Daily weight logging screen with date picker and history.

---

### How to Build & Run on Your Phone (in 3 Minutes)
1. Download & Install [Android Studio](https://developer.android.com/studio) (100% Free).
2. Unzip this folder and open Android Studio: `File -> Open -> Select this unzipped folder`.
3. Enable **Developer Options & USB Debugging** on your Android phone:
   - Go to `Settings -> About Phone -> Tap "Build Number" 7 times`.
   - Go to `Settings -> System -> Developer Options -> Turn on "USB Debugging"`.
4. Plug your phone into your computer via USB cable.
5. Click the green **Play (Run 'app')** button in Android Studio.

Enjoy your $0/month lifetime tracker!
