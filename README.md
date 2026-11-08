# Dhikr - Digital Tasbeeh Counter & Daily Tracker

**Dhikr** is a free, privacy-first, offline-capable Android application built with Jetpack Compose and Material 3, designed to help Muslims track and maintain their daily Dhikr, complete Post-Prayer Tasbeeh, and find the Qibla direction with an elegant Islamic visual aesthetic.

---

## 🌟 Key Features

### 1. Primary Dhikr Counter & Quick Counter Mode
- **Large Circular Counting Surface**: High-contrast, responsive tap area with subtle press animations.
- **Quick Counter Mode (Free Counting)**: Count freely without selecting a specific Dhikr preset while maintaining full target customization.
- **Target Tracking**: Preset targets (33, 100, 500, 1000) and custom target entry.
- **Undo & Reset Safety**: Undo last increment without negative counts; confirmation dialog for resetting counts.
- **Active Session Persistence**: Counter state and active session survive application recreation, process death, and device restarts.
- **Completion Feedback**: Subtle visual glow and custom haptic/sound notifications on reaching targets.

### 2. After-Salah Sequential Tasbeeh Mode
- **Automated 3-Step Chain**:
  1. **SubhanAllah** (`سُبْحَانَ ٱللَّٰهِ`, 33 counts)
  2. **Alhamdulillah** (`ٱلْحَمْدُ لِلَّٰهِ`, 33 counts)
  3. **Allahu Akbar** (`ٱللَّٰهُ أَكْبَرُ`, 33 counts)
- **Automatic Step Advancement**: Reaching 33 counts triggers completion feedback and automatically transitions to the next Dhikr step.
- **Automatic Return to Quick Counter**: Upon completing the final 3rd step (Allahu Akbar), the session is recorded in history and the counter automatically returns to the default Quick Counter mode.
- **Visual Step Progress Bar**: Real-time 3-step indicator displaying live step counts (`12/33`) and completed step checkmarks (`✓`).
- **Smart Chain Undo**: Undoing at count `0` of a step seamlessly reverts to count `32/33` of the preceding step.

### 3. Built-in & Custom Dhikr Management
- **Built-in Presets**: Quick Counter, After-Salah Tasbeeh, SubhanAllah, Alhamdulillah, Allahu Akbar, Astaghfirullah, La ilaha illa Allah, SubhanAllahi wa bihamdihi.
- **Custom Dhikr Creation**: Add, edit, or delete custom Dhikr entries with transliteration, Arabic text, translation, description, and custom target count.
- **Local Offline Search**: Search presets and custom items by name, Arabic text, or translation.

### 4. Qibla Finder (100% Offline)
- **Precise On-Device Calculation**: Real-time calculation of Qibla bearing angle and Haversine distance to the Kaaba in Mecca (`21.422487° N, 39.826206° E`).
- **Interactive Compass Dial**: Custom canvas dial displaying rotating cardinal directions (N, E, S, W), degree ticks, and Qibla needle pointer.
- **Alignment Feedback**: Haptic vibration and visual glow when device aligns within 3° of Qibla direction.
- **Sensor & GPS Resilience**: Low-accuracy calibration warnings, GPS enable prompts, and fallback support for devices without magnetic sensors.
- **Zero Data Required**: Runs 100% offline using satellite GPS hardware and internal compass sensors.

### 5. Session History & Log
- **Date-grouped Log**: Historical completed sessions grouped into "Today", "Yesterday", and formatted date headers.
- **Detailed Inspection**: View session start time, last active time, achieved count, target, and status badge.
- **Record Deletion**: Delete individual historical records with explicit confirmation dialog.

### 6. Daily & Weekly Statistics
- **Analytics Dashboard**: Today's Dhikr total, completed sessions count, weekly total count, and average count per session.
- **Weekly Activity Chart**: Native Compose 7-day bar chart dynamically calculating daily counts and highlighting today's activity.

### 7. Customization & Preferences
- **Appearance**: Light theme, Dark theme, or Follow System theme.
- **Audio & Haptic Feedback**: Toggle vibration feedback, subtle sound clicks, and target completion alerts.
- **Data Privacy & Reset**: 100% offline local storage with double-confirmation full data wipe option.

---

## 🛠️ Technical Stack & Architecture

- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture (MVVM + Repository Pattern)
- **Database**: Room Persistence Library 2.6.1 (SQLite) with foreign key `SET_NULL` protection
- **Preferences**: Jetpack DataStore Preferences
- **Location & Sensors**: Hardware `SensorManager` (Rotation Vector / Accelerometer + Magnetometer) & `LocationManager`
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Dependency Injection**: Manual DI via `AppContainer` (`AppDataContainer`)

---

## 🔒 Privacy Commitment

- **Zero Data Collection**: No user tracking, no analytics SDKs, no cloud server connections.
- **100% Offline**: Requires zero network permissions (`android.permission.INTERNET` is omitted).
- **Local Control**: All data remains strictly on the user's physical device in an encrypted Room SQLite database.

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 11 or higher
- Android SDK 37 (Min SDK 26)

### Running Tests
Execute the unit test suite:
```bash
./gradlew test
```

Execute instrumentation UI tests on an emulator:
```bash
./gradlew connectedAndroidTest
```

### Generating Release APK
Build the test release APK:
```bash
./gradlew app:assembleRelease
```
The compiled APK will be located at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 📄 License & Attributions
- **Frameworks**: Android Open Source Project (AOSP), Jetpack Compose, Room, DataStore.
- **Icons**: Material Design Icons Extended.
