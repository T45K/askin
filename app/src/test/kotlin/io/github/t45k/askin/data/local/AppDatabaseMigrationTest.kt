package io.github.t45k.askin.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
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
        val helper = createHelper(version = 1, callback = createVersion1Callback())
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

    @Test
    fun migrate2To3CopiesMasterNamesToTrainingRecords() {
        val helper = createHelper(version = 2, callback = createVersion2Callback())
        helper.writableDatabase.apply {
            execSQL(
                "INSERT INTO categories (id, name, description, display_order, is_active) VALUES (1, '胸', '', 1, 1)",
            )
            execSQL(
                "INSERT INTO exercises (id, category_id, name, description, display_order, is_active) VALUES (1, 1, '腕立て伏せ', '', 2, 1)",
            )
            execSQL(
                """
                INSERT INTO training_records (id, date, exercise_id, reps, created_at, updated_at)
                VALUES (1, '2026-04-30', 1, 30, 1000, 2000)
                """.trimIndent(),
            )
            close()
        }

        helper.writableDatabase.use { database ->
            AppDatabase.MIGRATION_2_3.migrate(database)

            database.query(
                """
                SELECT date,
                       category_name,
                       exercise_name,
                       category_display_order,
                       exercise_display_order,
                       reps,
                       created_at,
                       updated_at
                FROM training_records
                """.trimIndent(),
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("2026-04-30", cursor.getString(0))
                assertEquals("胸", cursor.getString(1))
                assertEquals("腕立て伏せ", cursor.getString(2))
                assertEquals(1, cursor.getInt(3))
                assertEquals(2, cursor.getInt(4))
                assertEquals(30, cursor.getInt(5))
                assertEquals(1000, cursor.getLong(6))
                assertEquals(2000, cursor.getLong(7))
            }
        }
    }

    private fun createHelper(version: Int, callback: SupportSQLiteOpenHelper.Callback): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DATABASE)
                .callback(callback)
                .build(),
        )

    private fun createVersion1Callback(): SupportSQLiteOpenHelper.Callback = object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            createMastersV1(db)
            createTrainingRecordsV1(db)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private fun createVersion2Callback(): SupportSQLiteOpenHelper.Callback = object : SupportSQLiteOpenHelper.Callback(2) {
        override fun onCreate(db: SupportSQLiteDatabase) {
            createMastersV2(db)
            createTrainingRecordsV1(db)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    private fun createMastersV1(db: SupportSQLiteDatabase) {
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

    private fun createMastersV2(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
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
                description TEXT NOT NULL,
                display_order INTEGER NOT NULL,
                is_active INTEGER NOT NULL,
                FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_category_id ON exercises (category_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercises_category_id_name ON exercises (category_id, name)")
    }

    private fun createTrainingRecordsV1(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS training_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                exercise_id INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_training_records_exercise_id ON training_records (exercise_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_training_records_date_exercise_id ON training_records (date, exercise_id)")
    }

    companion object {
        private const val TEST_DATABASE = "migration-test"
    }
}
