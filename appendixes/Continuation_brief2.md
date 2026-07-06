# CatsCarsCoins — Project Blueprint Archive

This document summarizes the complete architectural and feature footprint of the **CatsCarsCoins** (`com.icodeforyou.catscarscoins`) Kotlin/Android codebase. It serves as a comprehensive transfer state snapshot for seamlessly continuing development in a fresh workspace session.

---

## 🏗️ Core Architecture & Tech Stack

The application is built around modern Android architecture principles using a **modular, feature-by-vertical offline-first** design framework:

* **UI Layer:** Jetpack Compose (Material 3), using a lean state-driven conditional component switcher for top-level navigation instead of heavy third-party routing graph libraries.
* **Dependency Injection:** Koin (using type-safe constructor-based DSL patterns like `viewModelOf`).
* **Concurrency:** Kotlin Coroutines and asynchronous state streams (`StateFlow`, `SharingStarted.WhileSubscribed`).
* **Local Persistence:** Room 3 utilizing an offline-first architecture pattern backed by dynamic structural database pooling mechanics.

---

## 📌 Milestones Completed (Phases 1–5)

### Phase 1: Core Database Foundations

* Established the central data engine layer wrapper (`CatsCarsDatabase`).
* Configured structural fallback and initialization states.

### Phase 2: Coins Polling Vertical

* Delivered real-time network tracking modules and periodic price updates.
* Exposed data streaming components via a dedicated `CoinRefresher` service abstraction contract.

### Phase 3: Cats Vertical

* Implemented custom search capabilities optimized by Full-Text Search (**Room FTS4**).
* Built localized asset loading pathways alongside high-performance text parsing blocks.

### Phase 4: Cars Vertical

* Integrated remote vehicle lookup indices driven by the public **NHTSA vPIC API**.
* Engineered fallback normalization patterns for nested model classes (e.g., mapping null fields to safe, structured defaults like `"Unknown Country"` for manufacturers).

### Phase 5: Database Viewer & Administrative Utilities

* **Schemaless Inspection (`SchemalessGrid`):** An interactive UI matrix that reads relational tables dynamically without hardcoded entity-schema compilation configurations.
* **Dynamic Data Clearing (`ResetAppUseCase`):** Designed an explicit, schema-blind administrative cleanup utility. It reads the system `sqlite_master` catalog at runtime and fires atomic `DELETE FROM` execution lines safely inside a low-level Room 3 write transaction wrapper to wipe all footprint data instantly.

---

## 📂 Project Directory Map

```
app/src/main/java/com/icodeforyou/catscarscoins/
├── MainActivity.kt               # App shell, splash management, status bars
├── db/
│   └── CatsCarsDatabase.kt       # Unified Room 3 Database instance
├── di/
│   └── DbViewerModule.kt         # Dependency graph mapping (Viewer/Reset dependencies)
├── ui/
│   └── AppNavGraph.kt            # Clean pure-Compose navigation switcher
│
# Feature Verticals
├── coins/
│   └── domain/CoinRefresher.kt   # Polling & asset synchronization contract
├── cats/
│   └── ui/CatsScreen.kt          # Searchable, FTS-backed image/data viewer
├── cars/
│   └── ui/CarsScreen.kt          # NHTSA manufacturer directory index
│
└── dbviewer/
    ├── data/
    │   ├── DbRow.kt              # Key-value map representation of a table row
    │   └── RoomDbViewerRepository.kt # Schema-blind cursor reflection reader
    ├── domain/
    │   ├── DbViewerRepository.kt # Repository abstraction layer
    │   └── ResetAppUseCase.kt    # Dynamic catalog wipe pipeline via Room 3
    └── ui/
        ├── DbViewerScreen.kt     # Dual-action system controller dashboard
        ├── DbViewerViewModel.kt  # Scope state coordinator & runtime dispatcher
        └── components/
            └── SchemalessGrid.kt # Generic grid viewport renderer

```

---

## 🔌 Current Navigation Contract Signature

The navigation switcher is anchored in `catscarscoins/ui/AppNavGraph.kt` and operates deterministically via a flat state tracking pattern without extra compilation arguments:

```kotlin
@Composable
fun AppNavGraph(
    currentScreen: String,
    onCatSelected: (String) -> Unit
) {
    when (currentScreen) {
        "coins"     -> CoinsScreen()
        "cats"      -> CatsScreen(onCatSelected = onCatSelected)
        "cars"      -> CarsScreen()
        "db_viewer" -> DbViewerScreen()
    }
}

```

---

## 🎯 Next Steps Checklist for a New Session

When booting up this project inside a brand-new chat context, here are the most logical avenues for building **Phase 6**:

1. **Cross-Vertical Interactivity:** Allow items to link together (e.g., "Purchasing a Cat/Car using Coins" logic).
2. **Unified Dashboard Tab:** Create a home viewport that displays aggregate counts and summary metrics fetched from all underlying feature models.
3. **User Personalization:** Implement user-specific bookmarks or a favorites sub-table matching user profile histories.

**Current Build Status:** `./gradlew assembleDebug` compiles 100% clean with zero warnings or errors.