package io.github.t45k.askin.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun migrationFrom1To2CopiesMasterNamesToTrainingRecords() {
        val db = database.openHelper.writableDatabase
        recreateVersion1TrainingRecords(db)
        db.execSQL("INSERT INTO categories (id, name, display_order, is_active) VALUES (1, '胸', 1, 1)")
        db.execSQL("INSERT INTO exercises (id, category_id, name, display_order, is_active) VALUES (1, 1, '腕立て伏せ', 2, 1)")
        db.execSQL(
            """
            INSERT INTO training_records (id, date, exercise_id, reps, created_at, updated_at)
            VALUES (1, '2026-04-30', 1, 30, 1000, 2000)
            """.trimIndent(),
        )

        AppDatabase.MIGRATION_1_2.migrate(db)

        db.query(
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

    private fun recreateVersion1TrainingRecords(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE training_records")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `training_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` TEXT NOT NULL,
                `exercise_id` INTEGER NOT NULL,
                `reps` INTEGER NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_records_exercise_id` ON `training_records` (`exercise_id`)")
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_training_records_date_exercise_id`
            ON `training_records` (`date`, `exercise_id`)
            """.trimIndent(),
        )
    }
}