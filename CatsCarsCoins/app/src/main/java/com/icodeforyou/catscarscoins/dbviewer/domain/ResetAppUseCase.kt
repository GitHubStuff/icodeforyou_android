// dbviewer/domain/ResetAppUseCase.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Change from 24.5.9-correction1: concrete Room-coupled class → domain
// contract. The implementation relocated to data/RoomResetAppUseCase —
// its androidx.room3 import never belonged in domain. Interface seam
// enables JVM fakes (the prior concrete final class could not be faked,
// which left DbViewerViewModelTest an empty husk).
package com.icodeforyou.catscarscoins.dbviewer.domain

/**
 * Domain contract for the app-wide destructive reset (spec §16 "Reset
 * App" / Settings "Nuke Database"). Implementations wipe persisted
 * state; presentation (haptics, sound, styling) belongs to callers.
 *
 * Current implementation truncates database tables only — the full §16
 * contract (preferences resetToDefaults + each repository clearAll,
 * Room-invalidation-friendly, failure signaling) lands in the queued
 * rewrite.
 */
interface ResetAppUseCase {

    /** Performs the wipe. Suspends until the reset completes. */
    suspend fun execute()
}