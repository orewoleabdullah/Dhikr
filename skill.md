---
name: dhikr-app-architecture
description: Technical guidance, architecture patterns, Jetpack Compose UI components, custom Canvas bead rendering, and state management for the Dhikr Islamic companion app.
---

# Dhikr App Development & Architecture Skill

This skill defines the technical architecture, design patterns, custom Compose rendering, and state management rules for the **Dhikr** Android application.

## 1. Architecture & Layers

- **Clean Architecture (MVVM)**:
  - **Domain Layer**: Pure Kotlin business models (`DhikrItem`, `DhikrPreset`, `AfterSalahChain`), calculation engines (`QiblaCalculator`), and repository interfaces (`DhikrRepository`, `SessionRepository`).
  - **Data Layer**: Room SQLite persistence (`DhikrDatabase`, DAOs, entities), offline repositories (`OfflineDhikrRepository`, `OfflineSessionRepository`), and Jetpack DataStore preferences (`UserPreferencesRepository`).
  - **Presentation Layer**: Jetpack Compose UIs driven by `ViewModel` classes exposing immutable `StateFlow<UiState>` and one-shot `SharedFlow<UiEvent>`.
  - **Dependency Injection**: Manual DI via `AppContainer` (`AppDataContainer`).

---

## 2. Core Features & Domain Logic

### Primary & Quick Counter
- **Circular Tasbeeh Bead Tracker**: Custom Composable (`TasbeehCircularCounter`) rendering an interactive circular track with animated 3D beads (rubbies) and a traditional Imam bead with a tassel at the 6 o'clock position (90°).
- **Proportional Bead Scaling**: Supports targets of 33, 100, 500, 1000, or custom values. For targets > 33, the 33 beads fill up proportionally (`progress = currentCount / target`).
- **Persistence & Feedback**: Active sessions and counts persist across process death via Room and DataStore. Integrated haptic vibration and sound clicks via `FeedbackManager`.

### After-Salah Sequential Tasbeeh Mode
- **3-Step Automated Chain**: SubhanAllah (33) ➔ Alhamdulillah (33) ➔ Allahu Akbar (33).
- **Automatic Transitions**: Reaching the target for each step triggers completion haptics and advances to the next step, automatically returning to Quick Counter upon completing the 3rd step.

### Qibla Compass
- **100% Offline Calculation**: Computes Qibla bearing angle and Haversine distance to the Kaaba (`21.422487° N, 39.826206° E`).
- **Sensor Integration**: Uses device `SensorManager` (Rotation Vector / Accelerometer + Magnetometer) for real-time compass orientation.

---

## 3. UI Guidelines & Design System

- **Jetpack Compose & Material 3**:
  - Consistent styling with dynamic color support, light/dark theme toggle, and zero emojis in production UI text.
  - Typography: Custom Arabic typography style (`ArabicTextStyle`) for Quranic/Arabic Dhikr text.
- **Custom Canvas Drawing (`TasbeehCircularCounter`)**:
  - Arc-based bead positioning spanning ~300° to leave a bottom gap for the Imam bead.
  - Drop shadows and specular highlights for realistic 3D bead rendering.

---

## 4. Testing Strategy

- **Unit Tests (`src/test`)**:
  - ViewModel unit tests (`CounterViewModelTest`, `PresetsViewModelTest`, `SettingsViewModelTest`, `CounterViewModelChainTest`).
  - Domain calculation unit tests (`QiblaCalculatorTest`).
- **Instrumentation Tests (`src/androidTest`)**:
  - Room database tests (`DhikrDatabaseTest`).
  - Compose UI tests (`CounterScreenTest`, `PresetsScreenTest`).
