package io.github.t45k.askin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun exerciseDao(): ExerciseDao

    abstract fun trainingRecordDao(): TrainingRecordDao

    companion object {
        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "askin.db",
        ).build()
    }
}
