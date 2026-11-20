package ir.boltjournal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp

// رنگ‌های اصلی برنامه
val DarkEmerald = Color(0xFF1B5E20) // سبز یشمی تیره
val Cream = Color(0xFFFDFBF7)       // کرم
val Gold = Color(0xFFC5A059)        // طلایی ملایم

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // اجبار برنامه به راست‌چین بودن
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                BoltJournalTheme {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val navItems = listOf("روزنگار", "هفته‌نگار", "ماه‌نگار", "آینده‌نگار", "مجموعه‌نگار")
    val navIcons = listOf("📝", "📅", "🗓️", "🔭", "📚") // فعلاً از آیکون‌های ساده استفاده می‌کنیم

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("بولت ژورنال", color = Color.White, fontWeight = FontWeight.Bold) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkEmerald)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Cream) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Text(navIcons[index], fontSize = 20.sp) },
                        label = { Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkEmerald,
                            selectedTextColor = DarkEmerald,
                            indicatorColor = Gold.copy(alpha = 0.3f),
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // این متن فعلاً نمایش داده می‌شود تا وقتی صفحات واقعی ساخته شوند
            Text(
                text = "صفحه ${navItems[selectedItem]} به زودی ساخته می‌شود...",
                fontSize = 18.sp,
                color = DarkEmerald,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BoltJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = DarkEmerald,
            background = Cream,
            surface = Cream,
            tertiary = Gold
        ),
        content = content
    )
}