package io.github.t45k.askin.data.local

import android.content.Context
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun migrate1To2AddsMasterDescriptionColumns() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DATABASE)
                .callback(createVersion1Callback())
                .build(),
        )
        helper.writableDatabase.apply {
            execSQL("INSERT INTO categories (id, name, display_order, is_active) VALUES (1, '腕', 1, 1)")
            execSQL(
                """
                INSERT INTO exercises (id, category_id, name, display_order, is_active)
                VALUES (1, 1, '腕立て伏せ', 1, 1)
                """.trimIndent(),
            )
            close()
        }

        helper.writableDatabase.use { database ->
            AppDatabase.MIGRATION_1_2.migrate(database)

            database.query("SELECT description FROM categories WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("", cursor.getString(0))
            }
            database.query("SELECT description FROM exercises WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("", cursor.getString(0))
            }
        }
    }

    private fun createVersion1Callback(): SupportSQLiteOpenHelper.Callback = object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    display_order INTEGER NOT NULL,
                    is_active INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name ON categories (name)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    category_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    display_order INTEGER NOT NULL,
                    is_active INTEGER NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_category_id ON exercises (category_id)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercises_category_id_name ON exercises (category_id, name)")
        }

        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    companion object {
        private const val TEST_DATABASE = "migration-test"
    }
}