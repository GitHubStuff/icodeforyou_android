# CatsCarsCoins — Continuation Brief

**Purpose:** Paste this into a new chat so the assistant resumes the CatsCarsCoins runbook exactly where we left off. It captures the working process, the locked stack, current state, parked issues, and the immediate next step.

**Last updated:** 2026-07-06
**Current git branch:** `24_4`
**Current position:** Phase 4 (Cars) — through **24.4.9** delivered. Next step is **24.4.10** (CarDto wire layer).

---

## 1. What this project is

- **CatsCarsCoins** (`com.icodeforyou.catscarscoins`) — an Android reference blueprint app demonstrating Clean Architecture, TDD, and SOLID.
- Repo path: `/Users/denm4/AppDevelopment/icodeforyou_android/CatsCarsCoins` (inside the `icodeforyou_android` monorepo).
- Built **spec-first** using numbered runbook markdown docs as a living appendix. Runbook `.md` files live in `../appendixes/24/`.
- Three feature verticals: **Cats** (The Cat API), **Cars** (NHTSA vPIC), **Coins** (Coinbase BTC-USD). Plus Preferences/Settings and an app shell.

---

## 2. WORKING PROCESS (strict — follow exactly)

1. I say **"next"** → deliver **ONE** numbered step: `24.<phase>.<n>.md` + the complete source file(s), presented as downloadables from the outputs directory.
2. **Complete files only, never diffs or snippets.** One file per step (multi-file only when they must land together to compile — state this explicitly).
3. **Lists / steps / trees, NOT paragraphs.** Every file-placement instruction shows a directory **tree fragment**. No "Next step" sections inside the `.md` files.
4. **No comments on terminal/gradle command lines** (a `# comment` once broke my zsh). Put expectations in prose; keep commands copy-paste-safe.
5. **All terminal commands in copy-able fenced code boxes.**
6. **TDD red → green.** Red gate = compile failure on one unresolved symbol. Uncertain new-API usages are "adjudication points": I paste the compiler/device error, the correction folds in at the ORIGIN step (no fix-of-fix chains).
7. **Test placement rule (critical — I have been burned ~4×, it makes me angry):**
   - `androidx.test.*` / `@RunWith(AndroidJUnit4::class)` / `MigrationTestHelper` / turbine-on-real-Room → **`src/androidTest`** (instrumented, runs on device).
   - Pure JVM logic → **`src/test`**.
   - Every test file header states `Test sources.` or `androidTest sources.`; the runbook's "File delivered" line has the full path.
8. **When tests fail inexplicably: source-parity check EVERY involved file FIRST.** (Hard lesson — see §6 saga. Byte-verify what's actually on disk before theorizing.)

---

## 3. LOCKED STACK (do not re-litigate)

- Kotlin **2.2.10** / AGP **9.2.1** / compileSdk **37** / minSdk **31**.
- **Koin** runtime DSL (no codegen). `viewModelOf(::X)`; parameterized VMs use `viewModel { parameters -> X(parameters.get(), get()) }` + `parametersOf(...)` at the call site.
- **Nav3** + `NavigationSuiteScaffold`. App-shell keys in `nav/AppNavKeys.kt` (sealed `AppNavKey`); feature keys live in the feature's own `nav/` package (e.g. `cats/nav/CatsNavKeys.kt` holds the parameterized `CatDetailKey(catId: String)`).
- **Room 3.0.0** — `androidx.room3` coordinates AND packages, `BundledSQLiteDriver`, external-content FTS4 via `@Fts4(contentEntity = ...)`. Schemas tracked flat: `app/schemas/<FQN>/{1,2,3}.json`. Migrations are `AutoMigration` (new-table additions).
- **KSP 2.3.9** (not Kotlin-matched).
- **Coil 3.4.0** (pinned) — `coil3.compose.AsyncImage`.
- **Retrofit 3.0.0** + first-party kotlinx-serialization converter — package `retrofit2.converter.kotlinx.serialization.asConverterFactory` (dotted). **OkHttp 5.4.0** BOM.
- **coroutines = "1.11.0"** — ONE version ref for core + test. The app APK must ship `kotlinx-coroutines-android` explicitly (fixed a device-skew `NoSuchMethodError`).
- **material-icons-extended** adopted (`Icons.Default.CurrencyBitcoin`, `Icons.Default.Pets`; accessor `libs.androidx.material.icons.extended`).
- Money stored as **`Long` cents**, never Double.
- Coinbase BTC-USD polling with `distinctUntilChanged`, FIFO 500-row cap.
- Custom `NotifierHost` with `LaunchedEffect(id)`-keyed auto-dismiss.
- Package-by-feature, single `:app` module.

### Shared constants / helpers already in place
- `ui/StateFlowDefaults.kt` → `SUBSCRIPTION_STOP_TIMEOUT_MS = 5000L`.
- `notifier/` → `COIN_TOAST_MILLIS = 1250`, `READ_ONLY_TOAST_MILLIS = 750`.
- `ui/components/AppButton.kt` (§17: required `useHaptics` + `fontSize`, buzz in onClick only).
- `ui/components/SwipeableRefreshableList.kt` (generic list capstone — see §5).
- `coins/ui/UsdDisplay.kt` → `toUsdDisplay()`.
- Shared test fakes: `FakePreferencesRepository`, `FakeCoinsRepository`, `FakeCatsRepository` (all in the faked contract's package, `src/test`). `testsupport/MainDispatcherRule`.

---

## 4. AppPreferences — CURRENT BASELINE (user-evolved)

`preferences/domain/AppPreferences.kt` companion:
```
POLLING_INTERVAL_MIN_SECONDS = 5
POLLING_INTERVAL_MAX_SECONDS = 30
POLLING_INTERVAL_DEFAULT = 10          // <-- user evolved this (was MIN)
POLLING_INTERVAL_RANGE = 5..30
DEFAULTS = AppPreferences(
    themeMode = DARK,
    pollingIntervalSeconds = POLLING_INTERVAL_DEFAULT,  // = 10
    pollingPaused = true,
    hapticsEnabled = true,
)
```
**This default = 10 (not 5) is the important fact** — it caused the engine-test saga in §6.

---

## 5. PHASE STATUS

### Phase 0–2 — COMPLETE
App shell (`NavigationSuiteScaffold`, splash orchestration), full Preferences slice, full Coins slice (Room, Retrofit polling, `CoinPollingEngine`, `NotifierHost`, `CoinToastPresenter`).

### Phase 3 (Cats) — COMPLETE (24.3.1 – 24.3.39)
- Domain: `Cat` (String id, imageUrl, non-null `Breed`), `CatsRepository`.
- Room v2: `CatEntity` (String PK) + `CatFtsEntity` (`@Fts4`), `CatDao`, `AutoMigration(1,2)`.
- Wire: `CatDto`, `CatApi` (`has_breeds=1`), `CatApiRemoteSource`, `catsModule` (x-api-key interceptor = `BuildConfig.CAT_API_KEY`).
- **CAT_API_KEY** provisioned in `local.properties` and **working**. Blank-key build warning in `build.gradle.kts`.
- `CatsViewModel` (debounce 300ms → flatMapLatest → stateIn; `refreshFailures`; `isRefreshing`).
- `CatsScreen` (search, Coil rows, tappable → `onCatSelected`, on the capstone).
- `CatDetailKey(catId)` + `CatDetailViewModel` (selects by id from cached `cats("")`, no refetch) + `CatDetailScreen` (parameterized `koinViewModel { parametersOf(catId) }`).
- `MainActivity` wires Cats rail entry + `CatDetailKey` push/pop.
- **`SwipeableRefreshableList<T>`** capstone (24.3.35): generic, Material3 `PullToRefreshBox`, unopinionated slots (`itemContent` owns dividers; `emptyContent` slot; empty renders as fill-viewport item so pull works empty). Both `CatsScreen` (isRefreshing from VM) and `CoinsScreen` (isRefreshing = false, since `CoinRefresher` is instant) refactored onto it.

### Phase 4 (Cars) — IN PROGRESS (24.4.1 – 24.4.9 delivered)
Mirrors Cats. Source: NHTSA **vPIC** `GetAllManufacturers`.
- `24.4.1` `Manufacturer` domain — **Int** id (Mfr_ID), name, country. FTS fields: name + country.
- `24.4.2` `CarsRepository` contract — `manufacturers(query)`, `refresh()` (upsert by id, `FETCH_PAGE = 1`), `clearAll()`.
- `24.4.3` `ManufacturerEntity` + `ManufacturerFtsEntity` (Int PK, tables `manufacturers` / `manufacturers_fts`, mappers `toDomain`/`toEntity`).
- `24.4.4` `CarDao` — `observeAll` / `observeMatching` (both `ORDER BY name ASC, id ASC`), `@Upsert`, `deleteAll`.
- `24.4.5` `CatsCarsDatabase` **v3** — manufacturer entities registered, `carDao()`, `AutoMigration(2,3)`. **`3.json` committed on branch `24_4`; build succeeded.**
- `24.4.6` `DatabaseMigrationTest` — added `migrate2To3` test (cat survives, schema validates 3.json, manufacturers writable Tesla row, manufacturers_fts exists). androidTest.
- `24.4.7` `CarsRemoteSource` — `fetchPage(page): List<Manufacturer>`.
- `24.4.8` `RoomCarsRepositoryTest` (androidTest) — 9 tests (Aston/BMW/Tesla seed; "tes"→Tesla prefix, "germany"→country field, "bmw" case-insensitive, "united kingdom" multi-word AND). Red gate: unresolved `RoomCarsRepository`.
- `24.4.9` `RoomCarsRepository` (green) — three members + `toFtsMatch()` (byte-identical to cats; rule-of-two OK; a third FTS feature promotes it to shared `data/FtsQuery.kt`). Gate: **25 instrumented green** (5 RoomCoins + 2 migration + 9 RoomCats + 9 RoomCars).

### Phase 5 — NOT STARTED
- Database Viewer with `SchemalessGrid` capstone (§19 refresh via `CoinRefresher`, read-only toasts, 2×2 control box).
- Reset App: `ResetAppUseCase` (`resetToDefaults` + each repo `clearAll()`), `SoundPool` playing custom-synthesized `db_wipe.ogg` (no license).

---

## 6. PARKED ISSUES (return later — do NOT lose these)

### 6a. Engine test — root cause found, fix ready, NOT yet applied on disk
- **Symptom:** 5 of 11 `CoinPollingEngineTest` tests fail.
- **Root cause (proven via a 5-generation probe bisect):** the tests derived their polling interval from `AppPreferences.DEFAULTS`; the user evolved `POLLING_INTERVAL_DEFAULT` to **10** (was MIN=5), so the tests' timing math broke. The interval-change test set 10 when the value was already 10 → non-change swallowed by `distinctUntilChanged`. **Engine and environment are both correct — it was test hygiene (tests must own their timing inputs).**
- **Fix = `24.2.22-correction4.md`** → reissued `CoinPollingEngineTest.kt` that seeds `TEST_INTERVAL_SECONDS = 5` explicitly and reverts an earlier wrong `+1ms` boundary workaround.
- **Status:** user hit a `permission denied` (pasted a file path as a command) and said "move on." **Correction-4 is delivered but not confirmed applied.** When resumed: place the file, delete any `SchedulerProbeTest.kt` scratch file, run `./gradlew :app:testDebugUnitTest`, expect all JVM green.

### 6b. `@OptIn(ExperimentalCoroutinesApi)` warning sweep
- Pure noise across the test files (from the coroutines 1.11 bump). Warnings only, no failures. Do as a standalone housekeeping step, never smuggled into a fix.

---

## 7. TEST COUNTS (expected)

- **JVM `src/test`:** **76 total** (once correction-4 is applied). Currently 5 `CoinPollingEngineTest` reds until 6a lands.
- **Instrumented `src/androidTest`:** **25** = 5 RoomCoins + 2 migration (1→2, 2→3) + 9 RoomCats + 9 RoomCars.

### Useful commands
```
./gradlew :app:testDebugUnitTest
```
```
./gradlew :app:connectedDebugAndroidTest
```
```
./gradlew assembleDebug
```

---

## 8. IMMEDIATE NEXT STEP — 24.4.10 (CarDto wire layer)

1. **Web-verify the current vPIC `GetAllManufacturers` JSON shape first** (do not trust memory). Confirm the `Results[]` fields — expected `Mfr_ID` (Int), `Mfr_Name` (String), `Country` (String, sometimes null). Base URL `https://vpic.nhtsa.dot.gov/api/`, endpoint `vehicles/GetAllManufacturers?format=json&page=`, **no API key** (vPIC is keyless — so `carsModule` has NO interceptor, unlike catsModule).
2. Then `CarDto` + mapper (`toDomainOrNull`, blank-country default) with a JVM test that decodes literal JSON (parallel to `CatDtoTest`).
3. Then `CarApi` (static `GET vehicles/GetAllManufacturers?format=json`, `@Query("page")`), `CarApiRemoteSource` (`mapNotNull`).
4. Then `carsModule` (own Retrofit on the shared OkHttp client + shared Json, no interceptor), `databaseModule` += `carDao()` single, `appModules` += `carsModule`.
5. Then `CarsViewModel` (debounce + isRefreshing, parallel to Cats), `CarsScreen` on `SwipeableRefreshableList`, `CarsKey` rail entry (rail order: **Main / Cats / Cars / Coins / Settings**), optional manufacturer detail.
6. Then Phase 4 acceptance run. Then Phase 5.

---

## 9. HOW TO RESTART

Paste this whole file into a new chat with a message like:

> "Here's the continuation brief for CatsCarsCoins. Please read it and confirm you're ready to resume. When I say 'next', deliver step 24.4.10 following the working process exactly."

Then say **next**.
