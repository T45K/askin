package io.github.t45k.askin.data.local.seed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.t45k.askin.data.local.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InitialMasterSeederTest {
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
    fun seedIfNeededInsertsInitialCategoriesAndExercises() = runTest {
        InitialMasterSeeder(database).seedIfNeeded()

        val categories = database.categoryDao().getActiveCategories()
        val exercises = database.exerciseDao().getActiveExercises()

        assertEquals(listOf("腕", "胸", "腹筋", "背中", "下半身", "全身"), categories.map { it.name })
        assertEquals(19, exercises.size)
        assertTrue(exercises.any { it.name == "腕立て伏せ" && it.categoryId == categories.first { category -> category.name == "胸" }.id })
        assertTrue(exercises.any { it.name == "スクワット" && it.categoryId == categories.first { category -> category.name == "下半身" }.id })
    }

    @Test
    fun seedIfNeededIsIdempotent() = runTest {
        val seeder = InitialMasterSeeder(database)

        seeder.seedIfNeeded()
        seeder.seedIfNeeded()

        assertEquals(6, database.categoryDao().count())
        assertEquals(19, database.exerciseDao().count())
    }
}
