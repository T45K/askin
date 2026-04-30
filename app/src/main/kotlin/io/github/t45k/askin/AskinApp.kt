package io.github.t45k.askin

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.t45k.askin.data.repository.TrainingRecordRepository
import io.github.t45k.askin.domain.usecase.GenerateShareTextUseCase
import io.github.t45k.askin.share.XShareLauncher
import io.github.t45k.askin.ui.history.HistoryScreen
import io.github.t45k.askin.ui.history.HistoryViewModel
import io.github.t45k.askin.ui.master.CategoryEditScreen
import io.github.t45k.askin.ui.master.ExerciseEditScreen
import io.github.t45k.askin.ui.master.MasterScreen
import io.github.t45k.askin.ui.master.MasterViewModel
import io.github.t45k.askin.ui.record.RecordEditScreen
import io.github.t45k.askin.ui.record.RecordEditViewModel
import io.github.t45k.askin.ui.settings.SettingsScreen
import io.github.t45k.askin.ui.today.TodayScreen
import io.github.t45k.askin.ui.today.TodayViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val ScreenTransitionDurationMillis = 120
private const val ScreenTransitionOffsetDivisor = 4

@Composable
fun AskinApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as AskinApplication
    val coroutineScope = rememberCoroutineScope()
    val shareTextUseCase = remember(application.database) {
        GenerateShareTextUseCase(TrainingRecordRepository(application.database.trainingRecordDao()))
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val masterViewModel: MasterViewModel = viewModel(factory = MasterViewModel.factory(application.database))
    val masterUiState by masterViewModel.uiState.collectAsStateWithLifecycle()
    val todayViewModel: TodayViewModel = viewModel(factory = TodayViewModel.factory(application.database))
    val todayUiState by todayViewModel.uiState.collectAsStateWithLifecycle()
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(application.database))
    val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
    fun shareDate(dateText: String) {
        coroutineScope.launch {
            val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())
            XShareLauncher.launch(context, shareTextUseCase(date))
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF8A65),
            secondary = Color(0xFF81C784),
            background = Color(0xFFFFF8EC),
            surface = Color.White,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "today",
                            onClick = { navController.navigate("today") },
                            label = { Text("今日") },
                            icon = { Text("💪") },
                        )
                        NavigationBarItem(
                            selected = currentRoute == "history",
                            onClick = { navController.navigate("history") },
                            label = { Text("履歴") },
                            icon = { Text("📅") },
                        )
                        NavigationBarItem(
                            selected = currentRoute == "master",
                            onClick = { navController.navigate("master") },
                            label = { Text("マスタ") },
                            icon = { Text("⚙️") },
                        )
                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = { navController.navigate("settings") },
                            label = { Text("設定") },
                            icon = { Text("ℹ️") },
                        )
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "today",
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / ScreenTransitionOffsetDivisor },
                            animationSpec = tween(
                                durationMillis = ScreenTransitionDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / ScreenTransitionOffsetDivisor },
                            animationSpec = tween(
                                durationMillis = ScreenTransitionDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / ScreenTransitionOffsetDivisor },
                            animationSpec = tween(
                                durationMillis = ScreenTransitionDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / ScreenTransitionOffsetDivisor },
                            animationSpec = tween(
                                durationMillis = ScreenTransitionDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    },
                ) {
                    composable("today") {
                        TodayScreen(
                            uiState = todayUiState,
                            onAddRecordClick = { navController.navigate("record/$it") },
                            onShareClick = ::shareDate,
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            uiState = historyUiState,
                            onDateSelected = historyViewModel::selectDate,
                            onAddRecordClick = { navController.navigate("record/$it") },
                            onShareClick = ::shareDate,
                        )
                    }
                    composable("master") {
                        MasterScreen(
                            uiState = masterUiState,
                            onAddCategoryClick = { navController.navigate("category/new") },
                            onEditCategoryClick = { navController.navigate("category/$it") },
                            onAddExerciseClick = { navController.navigate("exercise/new/$it") },
                            onEditExerciseClick = { navController.navigate("exercise/$it") },
                        )
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                    composable(
                        route = "record/{date}",
                        arguments = listOf(navArgument("date") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val dateText = backStackEntry.arguments?.getString("date").orEmpty()
                        val initialDate = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())
                        val recordViewModel: RecordEditViewModel = viewModel(
                            key = "record-$dateText",
                            factory = RecordEditViewModel.factory(application.database, initialDate),
                        )
                        val recordUiState by recordViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(recordUiState.isSaved) {
                            if (recordUiState.isSaved) {
                                recordViewModel.consumeSaved()
                                navController.popBackStack("today", inclusive = false)
                            }
                        }

                        RecordEditScreen(
                            uiState = recordUiState,
                            onDateChange = recordViewModel::updateDate,
                            onExerciseSelected = recordViewModel::selectExercise,
                            onRepsChange = recordViewModel::updateReps,
                            onSaveClick = recordViewModel::save,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable("category/new") {
                        CategoryEditScreen(
                            category = null,
                            onSave = { name, description, displayOrder ->
                                masterViewModel.addCategory(name, description, displayOrder)
                                navController.popBackStack()
                            },
                            onDeactivate = null,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = "category/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                        val category = masterUiState.categories.firstOrNull { it.category.id == categoryId }?.category
                        CategoryEditScreen(
                            category = category,
                            onSave = { name, description, displayOrder ->
                                masterViewModel.updateCategory(categoryId, name, description, displayOrder)
                                navController.popBackStack()
                            },
                            onDeactivate = {
                                masterViewModel.deactivateCategory(categoryId)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = "exercise/new/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
                        ExerciseEditScreen(
                            exercise = null,
                            categories = masterUiState.categories.map { it.category },
                            initialCategoryId = categoryId,
                            onSave = { name, description, selectedCategoryId, displayOrder ->
                                masterViewModel.addExercise(name, description, selectedCategoryId, displayOrder)
                                navController.popBackStack()
                            },
                            onDeactivate = null,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = "exercise/{exerciseId}",
                        arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
                        val exercise = masterUiState.categories
                            .flatMap { it.exercises }
                            .firstOrNull { it.id == exerciseId }
                        ExerciseEditScreen(
                            exercise = exercise,
                            categories = masterUiState.categories.map { it.category },
                            initialCategoryId = exercise?.categoryId,
                            onSave = { name, description, categoryId, displayOrder ->
                                masterViewModel.updateExercise(exerciseId, name, description, categoryId, displayOrder)
                                navController.popBackStack()
                            },
                            onDeactivate = {
                                masterViewModel.deactivateExercise(exerciseId)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
