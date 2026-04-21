package com.example.gazelka

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gazelka.pages.LoginScreen
import androidx.compose.ui.Modifier
import com.example.gazelka.pages.CreateOrderScreen
import com.example.gazelka.pages.CustomerScheduledOrders
import com.example.gazelka.pages.CustomerSettingsScreen

import com.example.gazelka.pages.CustomerOrdersHistory
import com.example.gazelka.pages.DriverScheduledOrders
import com.example.gazelka.pages.DriverSettingsScreen
import com.example.gazelka.pages.DriverOrdersHistory
import com.example.gazelka.pages.AvailableOrders
import com.example.gazelka.pages.CustomerOrderDetailsScreen
import com.example.gazelka.pages.DriverOrderDetailsScreen
import com.example.gazelka.pages.EditOrderScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.gazelka.pages.AccountSettingsScreen
import com.example.gazelka.pages.ApplicationSettingsScreen
import com.example.gazelka.pages.AuthLandingScreen
import com.example.gazelka.pages.InfoSettingsScreen
import com.example.gazelka.pages.WelcomeScreen
import com.example.gazelka.pages.ChatScreen
import com.example.gazelka.pages.RegBasicInfoScreen
import com.example.gazelka.pages.RegCarInfoScreen
import com.example.gazelka.pages.RegPhoneInfoScreen
import com.example.gazelka.pages.RoleChRegScreen
import com.example.gazelka.pages.ChatsScreen
import com.example.gazelka.pages.NotificationsSettingsScreenCustomer
import com.example.gazelka.pages.NotificationsSettingsScreenDriver
import com.example.gazelka.pages.EditCarProfileScreen
import com.example.gazelka.pages.EditUserProfileScreen
import com.example.gazelka.pages.EmailConfirmationScreen
import com.example.gazelka.pages.LoginPasswordRecoveryScreen
import com.example.gazelka.pages.PasswordRecoveryScreen

@Composable
fun GazelkaNavigation(modifier: Modifier, authViewModel: AuthViewModel, regViewModel: RegistrationViewModel, startIntent: Intent?) {
        val navController = rememberNavController()

        LaunchedEffect(startIntent) {
                val type = startIntent?.getStringExtra("push_type")
                val orderId = startIntent?.getStringExtra("orderId")?.toIntOrNull()

                val role =  authViewModel._userData.value?.role

                when (type) {
                        "chat" -> orderId?.let {
                                navController.navigate("chat/$it")
                        }

                        "order" -> orderId?.let {
                                if(role == "customer"){
                                        navController.navigate("CustomerOrderDetails/$it")
                                }else{
                                        navController.navigate("DriverOrderDetails/$it")
                                }
                        }
                }
        }

        NavHost(
                navController = navController,
                startDestination = "welcome",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                builder = {
                        composable("welcome"){
                                WelcomeScreen(modifier, navController, authViewModel)
                        }

                        composable("login"){
                                LoginScreen(modifier, navController, authViewModel)
                        }

                        composable("createOrder") {
                                CreateOrderScreen(modifier, navController, authViewModel)
                        }

                        composable("customerScheduledOrders") {
                                CustomerScheduledOrders(modifier, navController, authViewModel)
                        }

                        composable("customerOrdersHistory") {
                                CustomerOrdersHistory(modifier, navController, authViewModel)
                        }

                        composable("customerSettings") {
                                CustomerSettingsScreen(modifier, navController, authViewModel)
                        }
                        composable("driverScheduledOrders") {
                                DriverScheduledOrders(modifier, navController, authViewModel)
                        }

                        composable("driverOrdersHistory") {
                                DriverOrdersHistory(modifier, navController, authViewModel)
                        }

                        composable("driverSettings") {
                                DriverSettingsScreen(modifier, navController, authViewModel)
                        }
                        composable("availableOrders") {
                                AvailableOrders(modifier, navController, authViewModel)
                        }

                        composable(
                                route = "CustomerOrderDetails/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
                        ) { backStackEntry ->
                                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                                CustomerOrderDetailsScreen(
                                        modifier = modifier,
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        orderId = orderId
                                )
                        }

                        composable(
                                route = "DriverOrderDetails/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
                        ) { backStackEntry ->
                                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                                DriverOrderDetailsScreen(
                                        modifier = modifier,
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        orderId = orderId
                                )
                        }

                        composable("accountSettings") {
                                AccountSettingsScreen(modifier, navController, authViewModel)
                        }

                        composable("applicationSettings") {
                                ApplicationSettingsScreen(modifier, navController, authViewModel)
                        }

                        composable("infoSettings") {
                                InfoSettingsScreen(modifier, navController, authViewModel)
                        }

                        composable("editProfile") {
                                EditUserProfileScreen(modifier, navController, authViewModel)
                        }

                        composable("chats") {
                                ChatsScreen(modifier, navController, authViewModel)
                        }

                        composable(
                                route = "chat/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
                        ) { backStackEntry ->
                                val orderId = backStackEntry.arguments?.getInt("orderId") ?: return@composable
                                ChatScreen(
                                        modifier = Modifier,
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        orderId = orderId
                                )
                        }

                        composable(
                                route = "editOrder/{orderJson}",
                                arguments = listOf(navArgument("orderJson") { type = NavType.StringType })
                        ) { backStackEntry ->
                                val orderJson = backStackEntry.arguments?.getString("orderJson")
                                EditOrderScreen(
                                        modifier = modifier,
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        orderJson = orderJson
                                )
                        }

                        composable("authLandingScreen") {
                                AuthLandingScreen(modifier, navController, authViewModel)
                        }

                        composable("roleChReg") {
                                RoleChRegScreen(modifier, navController, authViewModel, regViewModel)
                        }

                        composable(route = "basicInfoReg") {
                                RegBasicInfoScreen(modifier, navController, authViewModel, regViewModel)
                        }

                        composable("phoneInfoReg") {
                                RegPhoneInfoScreen(modifier, navController, authViewModel, regViewModel)
                        }

                        composable("carInfoReg"){
                                RegCarInfoScreen(modifier, navController, authViewModel, regViewModel)
                        }

                        composable("editCar") {
                                EditCarProfileScreen(modifier, navController, authViewModel)
                        }

                        composable("notificationsCustomer") {
                                NotificationsSettingsScreenCustomer(modifier, navController, authViewModel)
                        }

                        composable("notificationsDriver") {
                                NotificationsSettingsScreenDriver(modifier, navController, authViewModel)
                        }


                        composable(
                                route = "emailConfirmation/{email}",
                                arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                                val email = backStackEntry.arguments?.getString("email")
                                EmailConfirmationScreen(
                                        modifier = modifier,
                                        navController = navController,
                                        authViewModel = authViewModel,
                                        email = email
                                )
                        }
                        composable("passwordRecovery") {
                                PasswordRecoveryScreen(modifier, navController, authViewModel)
                        }
                        composable("loginPasswordRecovery") {
                                LoginPasswordRecoveryScreen(modifier, navController, authViewModel)
                        }


                })
}
