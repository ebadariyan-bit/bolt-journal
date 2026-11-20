package ir.boltjournal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

val DarkEmerald = Color(0xFF1B5E20)
val Cream = Color(0xFFFDFBF7)
val Gold = Color(0xFFC5A059)

// رنگ‌های آیزنهاور
val EisenhowerRed = Color(0xFFE53935)    // مهم و فوری
val EisenhowerGreen = Color(0xFF43A047)  // مهم و غیرفوری
val EisenhowerOrange = Color(0xFFFB8C00) // غیرمهم و فوری
val EisenhowerGray = Color(0xFF757575)   // غیرمهم و غیرفوری

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
    var newTaskPriority by remember { mutableStateOf("GRAY") }
    var newTaskRecurrence by remember { mutableStateOf("NONE") }
    var newTaskDateString by remember { mutableStateOf("") } // فقط برای آینده‌نگار
    var newTaskReminderDays by remember { mutableStateOf("0") }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بولت ژورنال", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkEmerald)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    newTaskTitle = ""
                    newTaskPriority = "GRAY"
                    newTaskRecurrence = "NONE"
                    newTaskDateString = ""
                    newTaskReminderDays = "0"
                    showDialog = true 
                },
                containerColor = Gold,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "افزودن") }
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
                            selectedIconColor = DarkEmerald, selectedTextColor = DarkEmerald,
                            indicatorColor = Gold.copy(alpha = 0.3f), unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItem) {
            0 -> DailyLogScreen(paddingValues, tasks, taskDao)
            1 -> WeeklyLogScreen(paddingValues, tasks, taskDao)
            2 -> MonthlyLogScreen(paddingValues, tasks, taskDao)
            3 -> FutureLogScreen(paddingValues, tasks, taskDao)
            else -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("مجموعه‌نگار به زودی...", fontSize = 18.sp, color = DarkEmerald)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("افزودن کار", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = {
                Column(modifier = Modifier.heightIn(max = 500.dp)) {
                    OutlinedTextField(value = newTaskTitle, onValueChange = { newTaskTitle = it }, label = { Text("عنوان") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("اولویت (آیزنهاور):", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("RED" to EisenhowerRed, "GREEN" to EisenhowerGreen, "ORANGE" to EisenhowerOrange, "GRAY" to EisenhowerGray).forEach { (p, c) ->
                            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(c).border(2.dp, if (newTaskPriority == p) Color.Black else Color.Transparent, CircleShape).clickable { newTaskPriority = p })
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("تکرار در:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = newTaskRecurrence == "NONE", onClick = { newTaskRecurrence = "NONE" }, label = { Text("بدون تکرار") })
                        FilterChip(selected = newTaskRecurrence == "WEEKLY", onClick = { newTaskRecurrence = "WEEKLY" }, label = { Text("هفتگی") })
                        FilterChip(selected = newTaskRecurrence == "MONTHLY", onClick = { newTaskRecurrence = "MONTHLY" }, label = { Text("ماهانه") })
                    }
                    
                    if (selectedItem == 3) { // فقط برای آینده‌نگار
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newTaskDateString, onValueChange = { newTaskDateString = it }, label = { Text("تاریخ آینده (1405/12/25)") }, singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newTaskReminderDays, onValueChange = { newTaskReminderDays = it }, label = { Text("یادآور چند روز قبل (0=بدون یادآور)") }, singleLine = true)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        scope.launch {
                            val recVal = if (newTaskRecurrence == "WEEKLY") PersianDate.getDayOfWeekName() else if (newTaskRecurrence == "MONTHLY") PersianDate.getDayOfMonthNumber() else ""
                            taskDao.insertTask(TaskEntity(
                                title = newTaskTitle, 
                                priority = newTaskPriority,
                                recurrenceType = newTaskRecurrence,
                                recurrenceValue = recVal,
                                dateString = newTaskDateString,
                                reminderDays = newTaskReminderDays.toIntOrNull() ?: 0
                            ))
                            showDialog = false
                        }
                    }
                }) { Text("ذخیره", color = DarkEmerald) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("لغو", color = Color.Gray) } }
        )
    }
}

@Composable
fun DailyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao) {
    val scope = rememberCoroutineScope()
    val currentDate = PersianDate.getCurrentPersianDate()
    val todayDayOfWeek = PersianDate.getDayOfWeekName()
    val todayDayOfMonth = PersianDate.getDayOfMonthNumber()
    val occasion = PersianDate.getOccasion()

    // فیلتر کردن کارهای امروز (تاریخ دقیق، هفتگی امروز، ماهانه امروز، یا آینده‌نگاری که رسیده)
    val todayTasks = tasks.filter {
        it.dateString == currentDate || 
        (it.recurrenceType == "WEEKLY" && it.recurrenceValue == todayDayOfWeek) ||
        (it.recurrenceType == "MONTHLY" && it.recurrenceValue == todayDayOfMonth) ||
        (it.dateString.isNotEmpty() && it.dateString == currentDate)
    }
    
    // مرتب سازی بر اساس آیزنهاور
    val priorityOrder = mapOf("RED" to 1, "GREEN" to 2, "ORANGE" to 3, "GRAY" to 4)
    val sortedTasks = todayTasks.sortedBy { priorityOrder[it.priority] ?: 5 }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("امروز", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(currentDate, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (occasion.isNotEmpty()) {
                        Text("مناسبت: $occasion", color = Gold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (sortedTasks.isEmpty()) {
            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("هیچ کاری ثبت نشده است.", color = Color.Gray) } }
        } else {
            items(sortedTasks) { task ->
                CompactTaskItem(task, taskDao, scope, onPostpone = {
                    scope.launch {
                        // موکول کردن به فردا (با تغییر تاریخ)
                        taskDao.updateTask(task.copy(dateString = "فردا")) // برای سادگی فعلا متن فردا ذخیره می‌شود
                    }
                })
            }
        }
    }
}

@Composable
fun WeeklyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao) {
    val scope = rememberCoroutineScope()
    val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        weekDays.forEach { day ->
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(day, fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
                        val dayTasks = tasks.filter { it.recurrenceType == "WEEKLY" && it.recurrenceValue == day }
                        if (dayTasks.isEmpty()) Text("خالی", fontSize = 12.sp, color = Color.Gray)
                        else dayTasks.forEach { task -> CompactTaskItem(task, taskDao, scope) }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao) {
    val scope = rememberCoroutineScope()
    val monthGrid = remember { PersianDate.getCurrentMonthGrid() }
    val monthYear = remember { PersianDate.getCurrentMonthYear() }
    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        item {
            Text(monthYear, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkEmerald, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = TextAlign.Center)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                weekDays.forEach { Text(it, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp) }
            }
        }
        item {
            // گرید تقویم به صورت فشرده
            Column {
                monthGrid.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        week.forEach { dayInfo ->
                            if (dayInfo == null) Box(modifier = Modifier.size(36.dp))
                            else {
                                val hasTasks = tasks.any { it.recurrenceType == "MONTHLY" && it.recurrenceValue == dayInfo.first.toString() }
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (dayInfo.first.toString() == PersianDate.getDayOfMonthNumber()) DarkEmerald else Color.Transparent), contentAlignment = Alignment.Center) {
                                    Text(dayInfo.first.toString(), color = if (dayInfo.first.toString() == PersianDate.getDayOfMonthNumber()) Color.White else Color.Black, fontSize = 14.sp)
                                    if (hasTasks) Box(modifier = Modifier.size(4.dp).background(Gold, CircleShape).align(Alignment.BottomCenter).padding(bottom = 2.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun FutureLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao) {
    val scope = rememberCoroutineScope()
    val futureTasks = tasks.filter { it.dateString.isNotEmpty() && it.dateString != PersianDate.getCurrentPersianDate() }.sortedBy { it.dateString }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        if (futureTasks.isEmpty()) {
            item { Text("برنامه‌ای برای آینده ثبت نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
        } else {
            val grouped = futureTasks.groupBy { it.dateString }
            grouped.forEach { (date, dateTasks) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(date, fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
                            dateTasks.forEach { task -> CompactTaskItem(task, taskDao, scope) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTaskItem(task: TaskEntity, taskDao: TaskDao, scope: kotlinx.coroutines.CoroutineScope, onPostpone: () -> Unit = {}) {
    val color = when(task.priority) {
        "RED" -> EisenhowerRed
        "GREEN" -> EisenhowerGreen
        "ORANGE" -> EisenhowerOrange
        else -> EisenhowerGray
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape)) // نشانگر رنگ آیزنهاور
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title, fontSize = 14.sp, color = if (task.isCompleted) Color.Gray else Color.Black,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onPostpone, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.ArrowForward, "موکول", tint = Color.Blue, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = { scope.launch { taskDao.updateTask(task.copy(isCompleted = !task.isCompleted)) } }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Check, "انجام", tint = if (task.isCompleted) Color.Green else Color.LightGray, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun BoltJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = DarkEmerald, background = Cream, surface = Cream, tertiary = Gold), content = content)
}
