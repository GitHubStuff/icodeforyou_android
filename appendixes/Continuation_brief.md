# CatsCarsCoins — Continuation Brief

**Purpose:** Paste this into a new chat so the assistant resumes the CatsCarsCoins runbook exactly where we left off. It captures the working process, the locked stack, current state, parked issues, and the immediate next step.

**Last updated:** 2026-07-06 (evening session)
**Current git branch:** `24_4`
**Current position:** Phase 4 (Cars) — through **24.4.18** delivered. Cars vertical functionally complete. Next step is the **Phase 4 acceptance run** (with parked 6a and the placement-audit fixes folded in first).

---

## 1. What this project is

- **CatsCarsCoins** (`com.icodeforyou.catscarscoins`) — an Android reference blueprint app demonstrating Clean Architecture, TDD, and SOLID.
- Repo path: `/Users/denm4/AppDevelopment/icodeforyou_android/CatsCarsCoins` (inside the `icodeforyou_android` monorepo).
- Built **spec-first** using numbered runbook markdown docs as a living appendix. Runbook `.md` files live in `../appendixes/24/`.
- Three feature verticals: **Cats** (The Cat API), **Cars** (NHTSA vPIC), **Coins** (Coinbase BTC-USD). Plus Preferences/Settings and an app shell.

---

## 2. WORKING PROCESS (strict — follow exactly)

1. I say **"next"** → deliver **ONE** numbered step: `24.<phase>.<n>.md` + the complete source file(s), presented as downloadables from the outputs directory.
2. **Complete files only, never diffs or snippets.** One file per step (multi-file only when they must land together to compile/resolve — state this explicitly).
3. **Lists / steps / trees, NOT paragraphs.** Every file-placement instruction shows a directory **tree fragment**. No "Next step" sections inside the `.md` files.
4. **No comments on terminal/gradle command lines** (a `# comment` once broke my zsh). Put expectations in prose; keep commands copy-paste-safe.
5. **All terminal commands in copy-able fenced code boxes.**
6. **TDD red → green.** Red gate = compile failure on one unresolved symbol. Uncertain new-API usages are "adjudication points": I paste the compiler/device error, the correction folds in at the ORIGIN step (`24.x.y-correctionN.md`, no fix-of-fix chains).
7. **Test placement rule (critical — I have been burned ~5×, it makes me angry):**
   - `androidx.test.*` / `@RunWith(AndroidJUnit4::class)` / `MigrationTestHelper` / turbine-on-real-Room → **`src/androidTest`** (instrumented, runs on device).
   - Pure JVM logic → **`src/test`**.
   - Every test file header states `Test sources.` or `androidTest sources.`; the runbook's "File delivered" line has the full path.
8. **When tests fail inexplicably: source-parity check EVERY involved file FIRST.** Byte-verify what's actually on disk before theorizing. (This session: caught `CarsRemoteSource` living in `cars/domain`, caught the missing `CatApi.BASE_URL`-mirror, caught three misplaced test files — parity checks before delivery are now standard for mirror steps.)
9. **File headers:** short feature-relative path only, never the full `app/src/...` path. House style:
   ```
   // cars/di/CarsModule.kt
   // CatsCarsCoins — spec 24.4.14. Complete file.
   // Change from 24.x.y: <what changed>.   (reissues only)
   ```
   Test files append `Test sources.` / `androidTest sources.` to the spec line.
10. A full source snapshot can be uploaded as `app.zip` — when present, the assistant extracts it and does its own parity reads instead of asking for pastes.

---

## 3. LOCKED STACK (do not re-litigate)

- Kotlin **2.2.10** / AGP **9.2.1** / compileSdk **37** / minSdk **31**.
- **Koin** runtime DSL (no codegen). `viewModelOf(::X)`; parameterized VMs use `viewModel { parameters -> X(parameters.get(), get()) }` + `parametersOf(...)` at the call site.
- **Nav3** + `NavigationSuiteScaffold`. App-shell keys in `nav/AppNavKeys.kt` (sealed `AppNavKey`); feature keys live in the feature's own `nav/` package (e.g. `cats/nav/CatsNavKeys.kt` holds `CatDetailKey(catId: String)`).
- **Room 3.0.0** — `androidx.room3` coordinates AND packages, `BundledSQLiteDriver`, external-content FTS4 via `@Fts4(contentEntity = ...)`. Schemas tracked flat: `app/schemas/<FQN>/{1,2,3}.json`. Migrations are `AutoMigration` (new-table additions).
- **KSP 2.3.9** (not Kotlin-matched).
- **Coil 3.4.0** (pinned) — `coil3.compose.AsyncImage`.
- **Retrofit 3.0.0** + first-party kotlinx-serialization converter — `retrofit2.converter.kotlinx.serialization.asConverterFactory` (dotted). **OkHttp 5.4.0** BOM.
- **coroutines = "1.11.0"** — ONE version ref for core + test. The app APK ships `kotlinx-coroutines-android` explicitly (fixed a device-skew `NoSuchMethodError`).
- **material-icons-extended** adopted (`Icons.Default.CurrencyBitcoin`, `Icons.Default.Pets`, now also `Icons.Default.DirectionsCar`).
- Money stored as **`Long` cents**, never Double.
- Coinbase BTC-USD polling with `distinctUntilChanged`, FIFO 500-row cap.
- Custom `NotifierHost` with `LaunchedEffect(id)`-keyed auto-dismiss.
- Package-by-feature, single `:app` module.

### vPIC facts (verified live 2026-07-06 — do not re-verify unless the API misbehaves)
- Base URL `https://vpic.nhtsa.dot.gov/api/`, endpoint `vehicles/GetAllManufacturers?format=json&page=N`.
- **Keyless** — `carsModule` has NO interceptor and uses the shared OkHttpClient as-is (deliberate contrast with `catsModule`).
- Paged at 100 rows. Envelope: `Count` / `Message` / `SearchCriteria` / `Results[]`.
- Row fields consumed: `Mfr_ID` (Int), `Mfr_Name` (String), `Country` (String, sometimes null). Ignored: `Mfr_CommonName`, `VehicleTypes[]`, others (`ignoreUnknownKeys`).

### Shared constants / helpers in place
- `ui/StateFlowDefaults.kt` → `SUBSCRIPTION_STOP_TIMEOUT_MS = 5000L`.
- `notifier/Notifier.kt` → `COIN_TOAST_MILLIS = 1250`, `READ_ONLY_TOAST_MILLIS = 750`; `Notifier` (replace-on-collision, monotonic ids); `NotifierHost`.
- `ui/components/AppButton.kt` (§17: required `useHaptics` + `fontSize`, buzz in onClick only). Accessors: `hapticsEnabled()`, `buttonFontSize()` from `preferences/PreferenceAccessors.kt`.
- `ui/components/SwipeableRefreshableList.kt` (generic list capstone — but see §6c: the FILE currently sits in the wrong directory).
- `coins/ui/UsdDisplay.kt` → `toUsdDisplay()`.
- Shared test fakes (contract's package, `src/test`): `FakePreferencesRepository`, `FakeCoinsRepository`, `FakeCatsRepository`, `FakeCarsRepository`. `testsupport/MainDispatcherRule`.

---

## 4. AppPreferences — CURRENT BASELINE (user-evolved)

`preferences/domain/AppPreferences.kt` companion:
```
POLLING_INTERVAL_MIN_SECONDS = 5
POLLING_INTERVAL_MAX_SECONDS = 30
POLLING_INTERVAL_DEFAULT = 10          // user evolved this (was MIN)
POLLING_INTERVAL_RANGE = 5..30
DEFAULTS = AppPreferences(
    themeMode = DARK,
    pollingIntervalSeconds = POLLING_INTERVAL_DEFAULT,  // = 10
    pollingPaused = true,
    hapticsEnabled = true,
)
```
**Default = 10 (not 5)** — this caused the engine-test saga in §6a.

---

## 5. PHASE STATUS

### Phase 0–2 — COMPLETE
App shell (`NavigationSuiteScaffold`, splash orchestration), full Preferences slice, full Coins slice (Room, Retrofit polling, `CoinPollingEngine`, `NotifierHost`, `CoinToastPresenter`).

### Phase 3 (Cats) — COMPLETE (24.3.1 – 24.3.39)
Domain (`Cat`, non-null `Breed`), Room v2 (`CatEntity` + `CatFtsEntity`, `AutoMigration(1,2)`), wire (`CatDto`, `CatApi` in `cats/data/remote/`, x-api-key interceptor, `CAT_API_KEY` working), `CatsViewModel` (debounce 300ms), `CatsScreen` on the capstone, `CatDetailKey`/`CatDetailViewModel`/`CatDetailScreen` (parameterized Koin), `SwipeableRefreshableList<T>` capstone.

### Phase 4 (Cars) — CODE COMPLETE (24.4.1 – 24.4.18); acceptance run pending
Mirrors Cats. Source: NHTSA vPIC `GetAllManufacturers`.
- `24.4.1` `Manufacturer` — Int id (Mfr_ID), name, country. FTS fields: name + country.
- `24.4.2` `CarsRepository` contract (`cars/domain`) — `manufacturers(query)`, `refresh()` (upsert, `FETCH_PAGE = 1`), `clearAll()`.
- `24.4.3` `ManufacturerEntity` + `ManufacturerFtsEntity` (tables `manufacturers` / `manufacturers_fts`).
- `24.4.4` `CarDao` — `observeAll` / `observeMatching` (`ORDER BY name ASC, id ASC`), `@Upsert`, `deleteAll`.
- `24.4.5` `CatsCarsDatabase` v3, `AutoMigration(2,3)`, `3.json` committed.
- `24.4.6` `DatabaseMigrationTest` `migrate2To3` (androidTest).
- `24.4.7` `CarsRemoteSource` contract — lives in **`cars/domain`** (domain port, alongside the repository contract).
- `24.4.8` `RoomCarsRepositoryTest` — 9 tests. ⚠ Landed in the WRONG source set; see §6c.
- `24.4.9` `RoomCarsRepository` (green) — `toFtsMatch()` byte-identical to cats (rule-of-two; a third FTS feature promotes it to shared `data/FtsQuery.kt`).
- `24.4.10` `CarDtoTest` (RED, 8 JVM tests) — decodes literal vPIC JSON; pins `toDomainOrNull` (null id → null, null/blank name → null, null/blank country → `""`).
- `24.4.11` `CarDto.kt` (GREEN) — `ManufacturersResponseDto` (declares only `Results`) + `CarDto` (all-nullable, `@SerialName`-pinned) + mapper, `NO_COUNTRY` const.
- `24.4.12` + `correction1` — `CarApi`: static `GET vehicles/GetAllManufacturers?format=json`, `@Query("page")`, suspend; companion `BASE_URL` (correction folded: house pattern is the interface owns its base URL, mirroring `CatApi.BASE_URL`).
- `24.4.13` + `correction1` — `CarApiRemoteSource`: `fetchPage(page)` = envelope → `mapNotNull { it.toDomainOrNull() }` (correction folded: import `cars.domain.CarsRemoteSource`).
- `24.4.14` — `carsModule` (own Retrofit on shared OkHttpClient + shared Json, NO interceptor) + `DatabaseModule` reissue (`CarDao` single) + `AppModules` reissue (`carsModule` after `catsModule`).
- `24.4.15` `FakeCarsRepository` + `CarsViewModelTest` (RED, 10 JVM tests — the 9 Cats mirrors + country-field narrowing). Fake filters over BOTH indexed fields (name, country).
- `24.4.16` `CarsViewModel` (GREEN) — byte-parallel to `CatsViewModel`: debounce 300ms → `flatMapLatest` → `stateIn`; `refreshFailures` signaled never presented; `isRefreshing` in try/finally; `CancellationException` rethrown.
- `24.4.17` `CarsScreen` (on the capstone; text-only rows, no tap — manufacturer detail spec-optional, skipped; dual empty states; 750 ms failure toast) + `CarsModule` reissue (`viewModelOf(::CarsViewModel)`).
- `24.4.18` `AppNavKeys` reissue (`CarsKey` between Cats and Coins) + `MainActivity` reissue (rail Main / Cats / Cars / Coins / Settings, `Icons.Default.DirectionsCar`, `entry<CarsKey> { CarsScreen() }`). Diff-verified: only the intended delta.

### Phase 5 — NOT STARTED
- Database Viewer with `SchemalessGrid` capstone (§19 refresh via `CoinRefresher`, read-only toasts, 2×2 control box).
- Reset App: `ResetAppUseCase` (`resetToDefaults` + each repo `clearAll()`), `SoundPool` playing custom-synthesized `db_wipe.ogg` (no license).

---

## 6. PARKED ISSUES (return before/at the acceptance run — do NOT lose these)

### 6a. Engine test — root cause found, fix delivered, NOT yet confirmed applied on disk
- **Symptom:** 5 of 11 `CoinPollingEngineTest` tests fail.
- **Root cause (proven via a 5-generation probe bisect):** tests derived their polling interval from `AppPreferences.DEFAULTS`; `POLLING_INTERVAL_DEFAULT` evolved to **10** (was MIN=5), breaking the timing math; the interval-change test set 10 when it was already 10 → swallowed by `distinctUntilChanged`. **Engine and environment are correct — test hygiene (tests must own their timing inputs).**
- **Fix = `24.2.22-correction4.md`** → reissued `CoinPollingEngineTest.kt` seeding `TEST_INTERVAL_SECONDS = 5` explicitly and reverting an earlier wrong `+1ms` boundary workaround.
- **When resumed:** place the file, delete any `SchedulerProbeTest.kt` scratch file, run `./gradlew :app:testDebugUnitTest`, expect all JVM green.

### 6b. `@OptIn(ExperimentalCoroutinesApi)` warning sweep
- Pure noise from the coroutines 1.11 bump. Warnings only. Standalone housekeeping step, never smuggled into a fix.

### 6c. PLACEMENT AUDIT (found 2026-07-06 from the app.zip snapshot — apply if not already done)
Three strays poison `:app:testDebugUnitTest` compilation; the fourth is cosmetic-but-wrong:
1. `RoomCarsRepositoryTest.kt` (androidTest content, 24.4.8) sits in **`src/test`** and is ABSENT from `src/androidTest`:
```
mkdir -p app/src/androidTest/java/com/icodeforyou/catscarscoins/cars/data
```
```
git mv app/src/test/java/com/icodeforyou/catscarscoins/cars/data/RoomCarsRepositoryTest.kt app/src/androidTest/java/com/icodeforyou/catscarscoins/cars/data/RoomCarsRepositoryTest.kt
```
2. Duplicate `DatabaseMigrationTest.kt` in `src/test` (correct copy already in `src/androidTest`):
```
git rm app/src/test/java/com/icodeforyou/catscarscoins/db/DatabaseMigrationTest.kt
```
3. Duplicate of main's `CoinbaseApi.kt` in `src/test` (`coins/data/remote/`):
```
git rm app/src/test/java/com/icodeforyou/catscarscoins/coins/data/remote/CoinbaseApi.kt
```
4. `SwipeableRefreshableList.kt` declares package `ui.components` but sits at `cats/ui/componets/` (typo dir, wrong feature — compiles because Kotlin doesn't enforce package/dir match):
```
git mv app/src/main/java/com/icodeforyou/catscarscoins/cats/ui/componets/SwipeableRefreshableList.kt app/src/main/java/com/icodeforyou/catscarscoins/ui/components/SwipeableRefreshableList.kt
```

---

## 7. TEST COUNTS (expected, after 6a + 6c land)

- **JVM `src/test`:** **94** = prior 76 + 8 `CarDtoTest` + 10 `CarsViewModelTest`. (Until 6a: 5 `CoinPollingEngineTest` reds. Until 6c: JVM compile is noise.)
- **Instrumented `src/androidTest`:** **25** = 5 RoomCoins + 2 migration (1→2, 2→3) + 9 RoomCats + 9 RoomCars. (Until 6c item 1 lands, the androidTest set holds only 16 — the 9 RoomCars never ran instrumented.)

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
```
./gradlew installDebug
```

---

## 8. IMMEDIATE NEXT STEP — Phase 4 acceptance run

1. Apply §6c (all four moves/deletes), then §6a (correction-4, delete probe scratch).
2. `./gradlew :app:testDebugUnitTest` → expect **94 green, 0 red**.
3. `./gradlew :app:connectedDebugAndroidTest` → expect **25 green** (RoomCars finally runs on device).
4. `./gradlew installDebug` → device checklist: rail Main / Cats / Cars / Coins / Settings; Cars empty state → Refresh pulls ~100 vPIC rows → search narrows by name ("tes") and country ("germany") → pull-to-refresh spins → airplane-mode Refresh shows the 750 ms toast; Cats and Coins regress-checked.
5. Commit on `24_4`, then 6b (warning sweep) as standalone housekeeping if desired.
6. Then **Phase 5**: Database Viewer (`SchemalessGrid` capstone), then Reset App (`ResetAppUseCase` + `SoundPool` + `db_wipe.ogg`).

---

## 9. HOW TO RESTART

Paste this whole file into a new chat with a message like:

> "Here's the continuation brief for CatsCarsCoins. Please read it and confirm you're ready to resume. When I say 'next', run the Phase 4 acceptance sequence (§8) following the working process exactly."

Then say **next**.
