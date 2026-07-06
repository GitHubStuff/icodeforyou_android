// db/DatabaseMigrationTest.kt
// CatsCarsCoins — spec 24.4.6. Complete file. androidTest sources.
// Correction folded in — Room 3 MigrationTestHelper API (adjudicated by
// the compiler): the constructor takes the database *file*; createDatabase
// and runMigrationsAndValidate are suspend, take only the version (the
// file is fixed at construction), and there is no validateDroppedTables
// flag. Test body now runs under runTest.
// Second correction (adjudicated on-device): the FTS MATCH probe is out of
// migration scope — Room installs the external-content sync triggers in
// its OPEN path, not in migration DDL, so the helper's raw connection has
// the tables but no triggers. FTS sync behavior is proven by
// RoomCatsRepositoryTest through a properly opened Room; this test now
// asserts what migration owns: schema, data survival, tables writable,
// the virtual table present.
// 24.4.6: a second test covers 2 -> 3 (manufacturers + manufacturers_fts),
// same scope discipline.
package com.icodeforyou.catscarscoins.db

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test.db"

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath(TEST_DB),
        driver = BundledSQLiteDriver(),
        databaseClass = CatsCarsDatabase::class,
    )

    @Test
    fun migrate1To2_preservesCoinsAndCreatesCatsTables() = runTest {
        helper.createDatabase(1).apply {
            execSQL(
                "INSERT INTO coins (amount_cents, recorded_at) VALUES (434317, 1000)",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(2)

        migrated.prepare("SELECT amount_cents, recorded_at FROM coins").use { statement ->
            assertTrue(statement.step())
            assertEquals(434317L, statement.getLong(0))
            assertEquals(1000L, statement.getLong(1))
            assertTrue(!statement.step())
        }

        migrated.prepare("SELECT COUNT(*) FROM cats").use { statement ->
            assertTrue(statement.step())
            assertEquals(0L, statement.getLong(0))
        }

        migrated.execSQL(
            "INSERT INTO cats (id, image_url, breed_id, breed_name, origin, temperament, life_span, description) " +
                    "VALUES ('abc', 'https://x/y.jpg', 'beng', 'Bengal', 'United States', 'Alert, Agile', '12 - 15', 'Bengals are a lot of fun')",
        )

        migrated.prepare("SELECT breed_name FROM cats WHERE id = 'abc'").use { statement ->
            assertTrue(statement.step())
            assertEquals("Bengal", statement.getText(0))
        }

        migrated.prepare("SELECT COUNT(*) FROM cats_fts").use { statement ->
            assertTrue(statement.step())
        }

        migrated.close()
    }

    @Test
    fun migrate2To3_preservesCatsAndCreatesManufacturerTables() = runTest {
        helper.createDatabase(2).apply {
            execSQL(
                "INSERT INTO cats (id, image_url, breed_id, breed_name, origin, temperament, life_span, description) " +
                        "VALUES ('abc', 'https://x/y.jpg', 'beng', 'Bengal', 'United States', 'Alert', '12 - 15', 'Fun')",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(3)

        migrated.prepare("SELECT breed_name FROM cats WHERE id = 'abc'").use { statement ->
            assertTrue(statement.step())
            assertEquals("Bengal", statement.getText(0))
        }

        migrated.prepare("SELECT COUNT(*) FROM manufacturers").use { statement ->
            assertTrue(statement.step())
            assertEquals(0L, statement.getLong(0))
        }

        migrated.execSQL(
            "INSERT INTO manufacturers (id, name, country) " +
                    "VALUES (988, 'Tesla, Inc.', 'UNITED STATES (USA)')",
        )

        migrated.prepare("SELECT name FROM manufacturers WHERE id = 988").use { statement ->
            assertTrue(statement.step())
            assertEquals("Tesla, Inc.", statement.getText(0))
        }

        migrated.prepare("SELECT COUNT(*) FROM manufacturers_fts").use { statement ->
            assertTrue(statement.step())
        }

        migrated.close()
    }
}