// dbviewer/domain/FakeResetAppUseCase.kt
// CatsCarsCoins — spec 24.5.14. Complete file. Test sources.
package com.icodeforyou.catscarscoins.dbviewer.domain

/**
 * Canonical in-memory fake of [ResetAppUseCase] for unit tests. Test
 * source set, contract's package — the established Fake pattern.
 * Counts invocations; scenario failures can be added when the §16
 * rewrite gives the contract failure semantics.
 */
class FakeResetAppUseCase : ResetAppUseCase {

    var executeCount = 0

    override suspend fun execute() {
        executeCount++
    }
}