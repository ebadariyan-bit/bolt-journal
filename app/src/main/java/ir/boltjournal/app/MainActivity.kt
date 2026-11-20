package ir.boltjournal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate

val DarkEmerald = Color(0xFF1B5E20)
val Cream = Color(0xFFFDFBF7)
val Gold = Color(0xFFC5A059)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
    val navIcons = listOf("📝", "📅", "🗓️", "🔭", "📚")

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val taskDao = db.taskDao()
    val tasks by taskDao.getAllTasks().collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بولت ژورنال", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkEmerald)
            )
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Gold,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن کار جدید")
                }
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedItem == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkEmerald),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "امروز",
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = PersianDate.getCurrentPersianDate(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // متن تستی: تاریخ میلادی که برنامه می‌خواند را نشان می‌دهد
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تاریخ سیستم: ${LocalDate.now()}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "امروز هیچ کاری ثبت نشده است.\nبرای افزودن، روی دکمه + بزنید.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (task.isCompleted) Color.Gray else DarkEmerald,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Row {
                                        IconButton(onClick = {
                                            scope.launch {
                                                taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "انجام شد",
                                                tint = if (task.isCompleted) Color.Green else Color.LightGray
                                            )
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                taskDao.deleteTask(task)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = Color.Red.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "صفحه ${navItems[selectedItem]} به زودی...",
                        fontSize = 18.sp,
                        color = DarkEmerald,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("افزودن کار جدید", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("عنوان کار") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            scope.launch {
                                taskDao.insertTask(TaskEntity(title = newTaskTitle))
                                newTaskTitle = ""
                                showDialog = false
                            }
                        }
                    }
                ) {
                    Text("ذخیره", color = DarkEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("لغو", color = Color.Gray)
                }
            }
        )
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
