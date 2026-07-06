// coins/data/RoomCoinsRepository.kt
// CatsCarsCoins — spec 24.2.11. Complete file.
package com.icodeforyou.catscarscoins.coins.data

import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.coins.domain.CoinsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [CoinsRepository]. Constructor-injected [CoinDao] (Koin
 * provides it from databaseModule) — which is exactly what lets the
 * 24.2.10 tests hand it a DAO from an in-memory database.
 *
 * Per the 24.2.2 contract: storage only, no dedup (that rule lives in the
 * polling pipeline), and every insert is followed by the FIFO trim so
 * [Coin.ROW_CAP] is a persistence invariant, never a caller courtesy.
 */
class RoomCoinsRepository(
    private val coinDao: CoinDao,
) : CoinsRepository {

    override val coins: Flow<List<Coin>> = coinDao.observeAll()
        .map { entities -> entities.map { entity -> entity.toDomain() } }

    override suspend fun record(amountCents: Long, recordedAtEpochMillis: Long) {
        coinDao.insert(
            CoinEntity(
                amountCents = amountCents,
                recordedAt = recordedAtEpochMillis,
            ),
        )
        coinDao.trimToLimit(Coin.ROW_CAP)
    }

    override suspend fun clearAll() {
        coinDao.deleteAll()
    }
}