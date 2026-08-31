package com.fitflow.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.di.ViewModelFactory
import com.fitflow.app.ui.exercises.ExerciseLibraryScreen
import com.fitflow.app.ui.exercises.ExerciseLibraryViewModel
import com.fitflow.app.ui.food.FoodScreen
import com.fitflow.app.ui.food.FoodViewModel
import com.fitflow.app.ui.history.HistoryScreen
import com.fitflow.app.ui.history.HistoryViewModel
import com.fitflow.app.ui.home.HomeScreen
import com.fitflow.app.ui.home.HomeViewModel
import com.fitflow.app.ui.schedule.ScheduleScreen
import com.fitflow.app.ui.schedule.ScheduleViewModel
import com.fitflow.app.ui.templates.TemplateDetailEditScreen
import com.fitflow.app.ui.templates.TemplateEditViewModel
import com.fitflow.app.ui.templates.TemplatesScreen
import com.fitflow.app.ui.templates.TemplatesViewModel

@Composable
fun FitFlowNavGraph(
    navController: NavHostController,
    repository: FitFlowRepository,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(innerPadding),
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { fadeIn(animationSpec = tween(250)) },
        popExitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        // Home Screen
        composable(route = Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSchedule = {
                    navController.navigate(Screen.Schedule.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Food & Nutrition Screen
        composable(route = Screen.Food.route) {
            val foodViewModel: FoodViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            FoodScreen(viewModel = foodViewModel)
        }

        // Templates Screen
        composable(route = Screen.Templates.route) {
            val templatesViewModel: TemplatesViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            TemplatesScreen(
                viewModel = templatesViewModel,
                onCreateTemplate = {
                    navController.navigate(Screen.TemplateEdit.createRoute())
                },
                onEditTemplate = { templateId ->
                    navController.navigate(Screen.TemplateEdit.createRoute(templateId))
                }
            )
        }

        // Template Detail / Edit Screen
        composable(
            route = Screen.TemplateEdit.route,
            arguments = listOf(
                navArgument("templateId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
            val editViewModel: TemplateEditViewModel = viewModel(
                factory = ViewModelFactory(repository, templateId)
            )
            TemplateDetailEditScreen(
                viewModel = editViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Schedule Screen
        composable(route = Screen.Schedule.route) {
            val scheduleViewModel: ScheduleViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            ScheduleScreen(viewModel = scheduleViewModel)
        }

        // Exercise Library Screen
        composable(route = Screen.ExerciseLibrary.route) {
            val libraryViewModel: ExerciseLibraryViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            ExerciseLibraryScreen(viewModel = libraryViewModel)
        }

        // History & Stats Screen
        composable(route = Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel(
                factory = ViewModelFactory(repository)
            )
            HistoryScreen(viewModel = historyViewModel)
        }
    }
}
