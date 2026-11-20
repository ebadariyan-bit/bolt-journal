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

val EisenhowerRed = Color(0xFFE53935)
val EisenhowerGreen = Color(0xFF43A047)
val EisenhowerOrange = Color(0xFFFB8C00)
val EisenhowerGray = Color(0xFF757575)

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

    val weekDaysList = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    var selectedWeekDay by remember { mutableStateOf(weekDaysList[0]) }
    var selectedMonthDay by remember { mutableStateOf(PersianDate.getDayOfMonthNumber()) }

    var showDialog by remember { mutableStateOf(false) }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("GRAY") }
    var newTaskDateString by remember { mutableStateOf("") }
    var newTaskReminderDays by remember { mutableStateOf("0") }
    
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    
    val scope = rememberCoroutineScope()

    val handleEditTask: (TaskEntity) -> Unit = { task ->
        taskToEdit = task
        newTaskTitle = task.title
        newTaskPriority = task.priority
        newTaskDateString = task.dateString
        newTaskReminderDays = task.reminderDays.toString()
        showDialog = true
    }

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
                    taskToEdit = null 
                    newTaskTitle = ""
                    newTaskPriority = "GRAY"
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
            0 -> DailyLogScreen(paddingValues, tasks, taskDao, handleEditTask)
            1 -> WeeklyLogScreen(paddingValues, tasks, taskDao, weekDaysList, selectedWeekDay, { selectedWeekDay = it }, handleEditTask)
            2 -> MonthlyLogScreen(paddingValues, tasks, taskDao, selectedMonthDay, { selectedMonthDay = it }, handleEditTask)
            3 -> FutureLogScreen(paddingValues, tasks, taskDao, handleEditTask)
            else -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("مجموعه‌نگار به زودی...", fontSize = 18.sp, color = DarkEmerald)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (taskToEdit == null) "افزودن کار جدید" else "ویرایش کار", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = {
                Column(modifier = Modifier.heightIn(max = 500.dp)) {
                    OutlinedTextField(value = newTaskTitle, onValueChange = { newTaskTitle = it }, label = { Text("عنوان کار") }, singleLine = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (selectedItem == 0) {
                        Text("اولویت (آیزنهاور):", fontSize = 12.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("RED" to EisenhowerRed, "GREEN" to EisenhowerGreen, "ORANGE" to EisenhowerOrange, "GRAY" to EisenhowerGray).forEach { (p, c) ->
                                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(c).border(2.dp, if (newTaskPriority == p) Color.Black else Color.Transparent, CircleShape).clickable { newTaskPriority = p })
                            }
                        }
                    } 
                    else if (selectedItem == 1 && taskToEdit == null) {
                        Text("این کار به صورت خودکار هر هفته در روز «$selectedWeekDay» تکرار می‌شود.", fontSize = 12.sp, color = DarkEmerald)
                    } else if (selectedItem == 2 && taskToEdit == null) {
                        Text("این کار به صورت خودکار هر ماه در روز «$selectedMonthDay» تکرار می‌شود.", fontSize = 12.sp, color = DarkEmerald)
                    } 
                    else if (selectedItem == 3) {
                        OutlinedTextField(value = newTaskDateString, onValueChange = { newTaskDateString = it }, label = { Text("تاریخ آینده (مثلا 1405/12/25)") }, singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newTaskReminderDays, onValueChange = { newTaskReminderDays = it }, label = { Text("یادآور چند روز قبل (0=بدون)") }, singleLine = true)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        scope.launch {
                            if (taskToEdit == null) {
                                val recType = when(selectedItem) { 1 -> "WEEKLY"; 2 -> "MONTHLY"; else -> "NONE" }
                                val recValue = when(selectedItem) { 1 -> selectedWeekDay; 2 -> selectedMonthDay; else -> "" }
                                
                                taskDao.insertTask(TaskEntity(
                                    title = newTaskTitle, 
                                    priority = newTaskPriority,
                                    recurrenceType = recType,
                                    recurrenceValue = recValue,
                                    dateString = newTaskDateString,
                                    reminderDays = newTaskReminderDays.toIntOrNull() ?: 0
                                ))
                            } else {
                                taskDao.updateTask(taskToEdit!!.copy(
                                    title = newTaskTitle,
                                    priority = newTaskPriority,
                                    dateString = newTaskDateString,
                                    reminderDays = newTaskReminderDays.toIntOrNull() ?: 0
                                ))
                            }
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
fun DailyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao, onEditTask: (TaskEntity) -> Unit) {
    val scope = rememberCoroutineScope()
    val currentDate = PersianDate.getCurrentPersianDate()
    val todayDayOfWeek = PersianDate.getDayOfWeekName()
    val todayDayOfMonth = PersianDate.getDayOfMonthNumber()
    val occasion = PersianDate.getOccasion()

    val todayTasks = tasks.filter {
        it.recurrenceType == "NONE" || 
        (it.recurrenceType == "WEEKLY" && it.recurrenceValue == todayDayOfWeek) ||
        (it.recurrenceType == "MONTHLY" && it.recurrenceValue == todayDayOfMonth)
    }
    
    val priorityOrder = mapOf("RED" to 1, "GREEN" to 2, "ORANGE" to 3, "GRAY" to 4)
    val sortedTasks = todayTasks.sortedBy { priorityOrder[it.priority] ?: 5 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("امروز", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(currentDate, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (occasion.isNotEmpty()) {
                        Text("مناسبت: $occasion", color = Gold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        if (sortedTasks.isEmpty()) {
            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("هیچ کاری ثبت نشده است.", color = Color.Gray) } }
        } else {
            items(sortedTasks) { task ->
                CompactTaskItem(
                    task = task, 
                    taskDao = taskDao, 
                    scope = scope, 
                    showPriority = true, 
                    onEdit = { onEditTask(task) },
                    onPriorityChange = { newColor -> 
                        scope.launch { taskDao.updateTask(task.copy(priority = newColor)) }
                    }
                )
            }
        }
    }
}

@Composable
fun WeeklyLogScreen(
    paddingValues: PaddingValues, 
    tasks: List<TaskEntity>, 
    taskDao: TaskDao,
    weekDaysList: List<String>,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    onEditTask: (TaskEntity) -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp)
    ) {
        weekDaysList.forEach { day ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDay == day) Cream else Color.White
                    ),
                    border = if (selectedDay == day) androidx.compose.foundation.BorderStroke(2.dp, DarkEmerald) else null,
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { onDaySelected(day) }
                            .padding(8.dp)
                    ) {
                        Text(day, fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
                        
                        val dayTasks = tasks.filter { it.recurrenceType == "WEEKLY" && it.recurrenceValue == day }
                        if (dayTasks.isEmpty()) {
                            Text("خالی", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        } else {
                            dayTasks.forEach { task -> 
                                CompactTaskItem(task, taskDao, scope, showPriority = false, onEdit = { onEditTask(task) }) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyLogScreen(
    paddingValues: PaddingValues, 
    tasks: List<TaskEntity>, 
    taskDao: TaskDao,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    onEditTask: (TaskEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    val monthGrid = remember { PersianDate.getCurrentMonthGrid() }
    val monthYear = remember { PersianDate.getCurrentMonthYear() }
    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp)
    ) {
        item {
            Text(monthYear, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkEmerald, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = TextAlign.Center)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                weekDays.forEach { Text(it, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp) }
            }
        }
        item {
            Column {
                monthGrid.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        week.forEach { dayInfo ->
                            if (dayInfo == null) {
                                Box(modifier = Modifier.size(40.dp))
                            } else {
                                val dayStr = dayInfo.first.toString()
                                val isSelected = dayStr == selectedDay
                                val hasTasks = tasks.any { it.recurrenceType == "MONTHLY" && it.recurrenceValue == dayStr }
                                
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) DarkEmerald else Color.Transparent)
                                        .border(if (isSelected) 0.dp else 1.dp, if (isSelected) Color.Transparent else Color.LightGray.copy(alpha=0.5f), CircleShape)
                                        .clickable { onDaySelected(dayStr) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(dayStr, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    if (hasTasks) {
                                        Box(modifier = Modifier.size(5.dp).background(Gold, CircleShape).align(Alignment.BottomCenter).padding(bottom = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
            Text("کارهای روز $selectedDay ماه", fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
        }
        
        val monthDayTasks = tasks.filter { it.recurrenceType == "MONTHLY" && it.recurrenceValue == selectedDay }
        if (monthDayTasks.isEmpty()) {
            item { Text("هیچ کاری ثبت نشده است.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        } else {
            items(monthDayTasks) { task ->
                CompactTaskItem(task, taskDao, scope, showPriority = false, onEdit = { onEditTask(task) })
            }
        }
    }
}

@Composable
fun FutureLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao, onEditTask: (TaskEntity) -> Unit) {
    val scope = rememberCoroutineScope()
    val futureTasks = tasks.filter { it.dateString.isNotEmpty() }.sortedBy { it.dateString }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp)
    ) {
        if (futureTasks.isEmpty()) {
            item { Text("برنامه‌ای برای آینده ثبت نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
        } else {
            val grouped = futureTasks.groupBy { it.dateString }
            grouped.forEach { (date, dateTasks) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Cream), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(date, fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
                            dateTasks.forEach { task -> 
                                CompactTaskItem(task, taskDao, scope, showPriority = false, onEdit = { onEditTask(task) }) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTaskItem(
    task: TaskEntity, 
    taskDao: TaskDao, 
    scope: kotlinx.coroutines.CoroutineScope, 
    showPriority: Boolean, 
    onEdit: () -> Unit,
    onPriorityChange: (String) -> Unit = {} // قابلیت تغییر رنگ با کلیک
) {
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
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showPriority) {
                // نقطه رنگی قابل کلیک برای تغییر سریع اولویت
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { 
                            val nextColor = when(task.priority) {
                                "RED" -> "GREEN"
                                "GREEN" -> "ORANGE"
                                "ORANGE" -> "GRAY"
                                else -> "RED"
                            }
                            onPriorityChange(nextColor) 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = task.title, fontSize = 14.sp, color = if (task.isCompleted) Color.Gray else Color.Black,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) { 
                Icon(Icons.Default.Edit, "ویرایش", tint = Color.Blue, modifier = Modifier.size(16.dp)) 
            }
            
            IconButton(onClick = { scope.launch { taskDao.updateTask(task.copy(isCompleted = !task.isCompleted)) } }, modifier = Modifier.size(30.dp)) { 
                Icon(Icons.Default.Check, "انجام", tint = if (task.isCompleted) Color.Green else Color.LightGray, modifier = Modifier.size(18.dp)) 
            }
            
            IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(30.dp)) { 
                Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(18.dp)) 
            }
        }
    }
}

@Composable
fun BoltJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = DarkEmerald, background = Cream, surface = Cream, tertiary = Gold), content = content)
}
