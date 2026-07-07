// MainActivity.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Change from 24.4.18: DbViewer wired — SettingsScreen's Database Tools
// card pushes DbViewerKey (entry<DbViewerKey> renders DbViewerScreen);
// detail is pushed on top of Settings, so system back pops to Settings.
// The rail is unchanged (Main / Cats / Cars / Coins / Settings) — the
// DbViewer is reached through Settings by design. No shell changes.
// Base: user's splash/system-bar orchestration (themed system bars via
// enableEdgeToEdge per state, background painted behind the transparent
// notch, splash logo entries), LocalActivity-provided Activity.
package com.icodeforyou.catscarscoins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.icodeforyou.catscarscoins.cars.ui.CarsScreen
import com.icodeforyou.catscarscoins.cats.nav.CatDetailKey
import com.icodeforyou.catscarscoins.cats.ui.CatDetailScreen
import com.icodeforyou.catscarscoins.cats.ui.CatsScreen
import com.icodeforyou.catscarscoins.coins.ui.CoinsScreen
import com.icodeforyou.catscarscoins.dbviewer.nav.DbViewerKey
import com.icodeforyou.catscarscoins.dbviewer.ui.DbViewerScreen
import com.icodeforyou.catscarscoins.nav.AppNavKey
import com.icodeforyou.catscarscoins.nav.CarsKey
import com.icodeforyou.catscarscoins.nav.CatsKey
import com.icodeforyou.catscarscoins.nav.CoinsKey
import com.icodeforyou.catscarscoins.nav.MainKey
import com.icodeforyou.catscarscoins.nav.SettingsKey
import com.icodeforyou.catscarscoins.nav.SplashKey
import com.icodeforyou.catscarscoins.notifier.NotifierHost
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import com.icodeforyou.catscarscoins.preferences.hapticsEnabled
import com.icodeforyou.catscarscoins.preferences.ui.SettingsScreen
import com.icodeforyou.catscarscoins.preferences.ui.ThemeViewModel
import com.icodeforyou.catscarscoins.ui.theme.AppTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** How long the in-app splash entry holds before advancing to Main. */
private val SPLASH_HOLD: Duration = 4000.milliseconds

/** A rail/bar destination: key + chrome presentation. */
private data class TopLevelDestination(
    val key: AppNavKey,
    val label: String,
    val icon: ImageVector,
)

/**
 * The navigation chrome's contents, in display order. Adding a destination
 * here is the only chrome change a new feature needs.
 */
private val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(key = MainKey, label = "Main", icon = Icons.Default.Home),
    TopLevelDestination(key = CatsKey, label = "Cats", icon = Icons.Default.Pets),
    TopLevelDestination(key = CarsKey, label = "Cars", icon = Icons.Default.DirectionsCar),
    TopLevelDestination(key = CoinsKey, label = "Coins", icon = Icons.Default.CurrencyBitcoin),
    TopLevelDestination(key = SettingsKey, label = "Settings", icon = Icons.Default.Settings),
)

/**
 * Single Activity (spec §5). Owns the spec 0.5 tree:
 * AppTheme { NavigationSuiteScaffold { NotifierHost { NavDisplay } } }.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = koinViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

            // Resolve the current target theme mathematically
            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Pass the resolved theme down to the shell so it can orchestrate the system bars
                    AppShell(isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

/**
 * The one and only app shell. NavDisplay owns every destination (single
 * source of truth); navigation chrome visibility is derived data —
 * [NavigationSuiteType.None] while Splash is current, the adaptive
 * bar/rail/drawer default otherwise. No destination is ever rendered
 * outside NavDisplay, so the spec 0.5 tree never changes shape.
 */
@Composable
private fun AppShell(isDarkTheme: Boolean) {
    val backStack = rememberNavBackStack(SplashKey)
    val currentKey: NavKey? = backStack.lastOrNull()
    val isSplash = currentKey == SplashKey
    val activity = requireNotNull(LocalActivity.current as? ComponentActivity) {
        "AppShell must be hosted in a ComponentActivity"
    }
    val haptics = LocalHapticFeedback.current

    // Dynamically update the edge-to-edge styling.
    // If the Splash screen is active, FORCE dark mode so the white icons sit perfectly on the
    // black background with zero scrim. Otherwise, respect the user's actual theme choice.
    DisposableEffect(isSplash, isDarkTheme) {
        val style = if (isSplash || isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        }

        activity.enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style,
        )

        onDispose {}
    }

    val layoutType = if (isSplash) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfoV2(),
        )
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                item(
                    selected = currentKey == destination.key,
                    onClick = {
                        // Buzz only when the tap actually navigates —
                        // re-tapping the current tab stays silent. §17
                        // semantics: preference read at tap time, Confirm
                        // type (same as AppButton).
                        if (currentKey != destination.key) {
                            if (hapticsEnabled()) {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                            backStack.clear()
                            backStack.add(destination.key)
                        }
                    },
                    icon = {
                        Icon(destination.icon, contentDescription = destination.label)
                    },
                    label = { Text(destination.label) },
                )
            }
        },
    ) {
        val safeAreaModifier = if (isSplash) {
            Modifier
        } else {
            Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Paint the background FIRST to cover the activity window behind the transparent notch
                .background(MaterialTheme.colorScheme.background)
                .then(safeAreaModifier),
        ) {
            NotifierHost(enabled = !isSplash) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    // Specifying entryDecorators replaces the defaults, so
                    // the saveable-state default is restated, then the
                    // ViewModel store decorator is added: each back-stack
                    // entry owns its own ViewModel store. Without it every
                    // entry shares the Activity store, so the parameterized
                    // CatDetailViewModel was created once (first catId)
                    // and reused for every later detail push. (Scene setup
                    // is built into NavDisplay in this Nav3 line — its
                    // decorator was removed from the public API.)
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<SplashKey> {
                            SplashScreenEntry(
                                onFinished = {
                                    backStack.clear()
                                    backStack.add(MainKey)
                                },
                            )
                        }
                        entry<MainKey> {
                            MainScreenEntry()
                        }
                        entry<CatsKey> {
                            CatsScreen(
                                onCatSelected = { catId ->
                                    backStack.add(CatDetailKey(catId))
                                },
                            )
                        }
                        entry<CatDetailKey> { key ->
                            CatDetailScreen(
                                catId = key.catId,
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<CarsKey> {
                            CarsScreen()
                        }
                        entry<CoinsKey> {
                            CoinsScreen()
                        }
                        entry<SettingsKey> {
                            SettingsScreen(
                                onDatabaseToolsSelected = {
                                    backStack.add(DbViewerKey)
                                },
                            )
                        }
                        entry<DbViewerKey> {
                            DbViewerScreen()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SplashScreenEntry(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_HOLD)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Splash Logo",
        )
    }
}

@Composable
private fun MainScreenEntry() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.5f),
    ) {
        Image(
            painter = painterResource(id = R.drawable.hplogo),
            contentDescription = "Main Logo",
        )
    }
}