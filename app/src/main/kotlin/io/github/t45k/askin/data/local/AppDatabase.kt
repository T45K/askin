package io.github.t45k.askin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.t45k.askin.data.local.dao.CategoryDao
import io.github.t45k.askin.data.local.dao.ExerciseDao
import io.github.t45k.askin.data.local.dao.TrainingRecordDao
import io.github.t45k.askin.data.local.entity.CategoryEntity
import io.github.t45k.askin.data.local.entity.ExerciseEntity
import io.github.t45k.askin.data.local.entity.TrainingRecordEntity

@Database(
    entities = [
        CategoryEntity::class,
        ExerciseEntity::class,
        TrainingRecordEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun exerciseDao(): ExerciseDao

    abstract fun trainingRecordDao(): TrainingRecordDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exercises ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `training_records_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `category_name` TEXT NOT NULL,
                        `exercise_name` TEXT NOT NULL,
                        `category_display_order` INTEGER NOT NULL,
                        `exercise_display_order` INTEGER NOT NULL,
                        `reps` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `training_records_new` (
                        `id`,
                        `date`,
                        `category_name`,
                        `exercise_name`,
                        `category_display_order`,
                        `exercise_display_order`,
                        `reps`,
                        `created_at`,
                        `updated_at`
                    )
                    SELECT tr.`id`,
                           tr.`date`,
                           c.`name`,
                           e.`name`,
                           c.`display_order`,
                           e.`display_order`,
                           tr.`reps`,
                           tr.`created_at`,
                           tr.`updated_at`
                    FROM `training_records` AS tr
                    INNER JOIN `exercises` AS e ON e.`id` = tr.`exercise_id`
                    INNER JOIN `categories` AS c ON c.`id` = e.`category_id`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `training_records`")
                db.execSQL("ALTER TABLE `training_records_new` RENAME TO `training_records`")
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_training_records_date_category_name_exercise_name`
                    ON `training_records` (`date`, `category_name`, `exercise_name`)
                    """.trimIndent(),
                )
            }
        }

        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "askin.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
}
