package com.example.gazelka.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.R

@Composable
fun CustomerBottomNavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_orders),
                    contentDescription = "Orders"
                )
            },
            label = { Text("Orders", fontSize = 10.sp, fontFamily = PoppinsFontFamily) },
            selected = currentRoute == "customerScheduledOrders",
            onClick = { navController.navigate("customerScheduledOrders") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9C80E),
                selectedTextColor = Color(0xFFF9C80E),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_history),
                    contentDescription = "History"
                )
            },
            label = { Text("History", fontSize = 10.sp, fontFamily = PoppinsFontFamily) },
            selected = currentRoute == "customerOrdersHistory",
            onClick = { navController.navigate("customerOrdersHistory") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9C80E),
                selectedTextColor = Color(0xFFF9C80E),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "New Order"
                )
            },
            label = { Text("New Order", fontSize = 10.sp, fontFamily = PoppinsFontFamily) },
            selected = currentRoute == "createOrder",
            onClick = { navController.navigate("createOrder") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9C80E),
                selectedTextColor = Color(0xFFF9C80E),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat),
                    contentDescription = "Chat"
                )
            },
            label = { Text("Chats", fontSize = 10.sp, fontFamily = PoppinsFontFamily) },
            selected = currentRoute == "customerChat",
            onClick = { navController.navigate("chats") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9C80E),
                selectedTextColor = Color(0xFFF9C80E),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", fontSize = 10.sp, fontFamily = PoppinsFontFamily) },
            selected = currentRoute == "customerSettings",
            onClick = { navController.navigate("customerSettings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9C80E),
                selectedTextColor = Color(0xFFF9C80E),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )
    }
}