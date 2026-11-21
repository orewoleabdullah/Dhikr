---
name: dhikr-app-architecture
description: Comprehensive technical specification, architecture standards, custom Jetpack Compose Canvas bead rendering rules, state management, and domain logic for the Dhikr Islamic companion app.
---

# Dhikr App Engineering Specification & Architecture Skill

This document defines the definitive technical blueprint, architecture patterns, custom Jetpack Compose rendering rules, and domain specifications for the **Dhikr** Android application. All features and refactoring must adhere strictly to the standards outlined herein.

---

## 1. Architectural Principles & Layering

The application follows strict **Clean Architecture** principles combined with **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**:

```
┌────────────────────────────────────────────────────────┐
│                      Presentation                      │
│     (Jetpack Compose UI + ViewModels + StateFlow)      │
└──────────────────────────┬─────────────────────────────┘
                           │ communicates via
                           ▼
┌────────────────────────────────────────────────────────┐
│                         Domain                         │
│   (Models, Use Cases, Repositories, Qibla Calculator)  │
└──────────────────────────┬─────────────────────────────┘
                           │ implemented by
                           ▼
┌────────────────────────────────────────────────────────┐
│                          Data                          │
│    (Room Database, DataStore Preferences, Repos)       │
└────────────────────────────────────────────────────────┘
```

### Layer Rules:
1. **Domain Layer**: Pure Kotlin with zero Android framework dependencies. Contains domain models (`DhikrItem`, `DhikrPreset`), repositories (`DhikrRepository`, `SessionRepository`), and domain math (`QiblaCalculator`).
2. **Data Layer**: Implements repository interfaces. Manages persistence via Room SQLite database (`DhikrDatabase`, DAOs, entities) and Jetpack DataStore Preferences (`UserPreferencesRepository`).
3. **Presentation Layer**: Jetpack Compose UIs. ViewModels manage state, exposing immutable `StateFlow<UiState>` and one-shot `SharedFlow<UiEvent>` for haptic and audio feedback.

---

## 2. Core Feature Specifications

### A. Circular Tasbeeh Bead Tracker (`TasbeehCircularCounter`)
- **Canvas Rendering**: Built using a custom Jetpack Compose `Canvas` with hardware-accelerated drawing.
- **Arc Bead Distribution**: Beads are distributed evenly across a **300° arc** (starting past bottom-left at ~120° and ending before bottom-right at ~60°), leaving a precise 60° gap at the bottom center.
- **Imam Bead & Tassel**: Rendered strictly at the 6 o'clock position (90° / `π/2`) with realistic drop shadows, specular highlights, and hanging tassel lines.
- **Universal Target Scaling**: 
  - For targets `≤ 33`, each bead represents 1 count.
  - For targets `> 33` (`100`, `500`, `1000`, or custom), the 33 beads fill up proportionally (`progress = currentCount / target`) as counts accumulate.
- **Visual Feedback**: Active beads glow in primary/secondary theme colors; inactive beads render with translucent outline styling.

### B. After-Salah Sequential Tasbeeh Mode (`AfterSalahChain`)
- **Automated 3-Step State Machine**:
  1. `SubhanAllah` (33 counts)
  2. `Alhamdulillah` (33 counts)
  3. `Allahu Akbar` (33 counts)
- **Automatic Transitions**: Reaching 33 counts on step 1 or 2 triggers completion haptic feedback and automatically advances to the next step. Upon completing step 3, the session is recorded in history and the app returns to Quick Counter mode.
- **Smart Chain Undo**: Undoing at count `0` of a step seamlessly reverts to count `32/33` of the preceding step.

### C. Qibla Finder (`QiblaCalculator` & `QiblaScreen`)
- **Geodetic Math**: Calculates true Qibla bearing angle and Haversine distance to the Kaaba in Mecca (`21.422487° N, 39.826206° E`).
- **Sensor Fusion**: Combines hardware rotation vector / accelerometer and magnetometer sensors via `SensorManager` to drive a rotating compass needle dial.

### D. Statistics & Weekly Activity Chart
- **Analytics Engine**: Computes today's count, completed sessions, weekly total, all-time total, and average count per session.
- **Weekly Bar Chart**: Native Compose 7-day bar chart with animated height fractions and today highlighting.

---

## 3. UI & Design System Guidelines

- **Material 3 & Jetpack Compose**: Consistent surface tones, tonal elevations, and dynamic theming (Light/Dark).
- **Zero Emojis Policy**: Production UI strings must be free of emojis (e.g. `⚡`, `🕌`, `🎉`, `🌟`), utilizing clean typography, badge components, and Material Design icons (`Icons.Default.*`).
- **Accessibility**: All interactive targets must provide semantic `contentDescription` and touch feedback.
- **Typography**: Custom Arabic text styling (`ArabicTextStyle`) for Quranic and Arabic Dhikr entries.

---

## 4. Testing & Verification Standard

- **Unit Tests (`src/test`)**: All ViewModels (`CounterViewModelTest`, `PresetsViewModelTest`, `SettingsViewModelTest`, `CounterViewModelChainTest`) and calculators (`QiblaCalculatorTest`) must maintain green test execution.
- **Instrumentation Tests (`src/androidTest`)**: Room database tests (`DhikrDatabaseTest`) and Compose UI tests (`CounterScreenTest`, `PresetsScreenTest`).
- **Build Verification**: Run `./gradlew app:assembleDebug` to guarantee zero compilation warnings or errors.
