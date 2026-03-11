package mtg.app.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomNavigationBar(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item.route == currentRoute,
 onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(),
            )
        }
    }
}

private enum class BottomNavItem(
    val route: Route,
    val icon: ImageVector,
    val label: String,
) {
    MAP(Route.Map, Icons.Filled.Map, "Map"),
    TRADE(Route.Trade, Icons.Filled.Storefront, "Trade"),
    MESSAGES(Route.Messages, Icons.Filled.ChatBubbleOutline, "Messages"),
    SETTINGS(Route.Settings, Icons.Filled.Settings, "Settings"),
}
