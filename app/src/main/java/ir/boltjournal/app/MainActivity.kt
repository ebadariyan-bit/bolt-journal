package ir.boltjournal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.cos
import kotlin.math.sin

val DarkEmerald = Color(0xFF1B5E20)
val Cream = Color(0xFFFDFBF7)
val Gold = Color(0xFFC5A059)

val EisenhowerRed = Color(0xFFE53935)
val EisenhowerGreen = Color(0xFF43A047)
val EisenhowerOrange = Color(0xFFFB8C00)
val EisenhowerGray = Color(0xFF757575)

val WarningBg = Color(0xFFFFF3E0)
val WarningBorder = Color(0xFFFFB74D)
val WarningText = Color(0xFFE65100)

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
    val collectionDao = db.collectionDao()
    
    val tasks by taskDao.getAllTasks().collectAsState(initial = emptyList())
    val collections by collectionDao.getAllItems().collectAsState(initial = emptyList())

    val weekDaysList = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    var selectedWeekDay by remember { mutableStateOf(weekDaysList[0]) }
    var selectedMonthDay by remember { mutableStateOf(PersianDate.getDayOfMonthNumber()) }
    
    val defaultCategories = listOf("کتاب‌ها", "فیلم‌ها", "فهرست خرید", "لوازم سفر", "مخاطبین", "یادداشت‌های آزاد")
    var selectedCategory by remember { mutableStateOf(defaultCategories[0]) }
    val allCategories = (defaultCategories + collections.map { it.category }).distinct()

    var showDialog by remember { mutableStateOf(false) }
    var showPostponeDialog by remember { mutableStateOf(false) }
    var taskToPostpone by remember { mutableStateOf<TaskEntity?>(null) }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("GRAY") }
    var newTaskDateString by remember { mutableStateOf("") }
    var newTaskReminderDays by remember { mutableStateOf("0") }
    var newCategoryName by remember { mutableStateOf("") }
    
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var collectionItemToEdit by remember { mutableStateOf<CollectionItemEntity?>(null) }
    
    val scope = rememberCoroutineScope()
    val todayNumeric = PersianDate.getCurrentDateNumeric()
    val tomorrowNumeric = PersianDate.getTomorrowDateNumeric()

    // انتقال خودکار کارهای انجام نشده به روز بعد
    LaunchedEffect(Unit) {
        val unfinishedPastTasks = tasks.filter { 
            it.recurrenceType == "NONE" && !it.isCompleted && it.dateString.isNotEmpty() && 
            !it.dateString.startsWith("SKIPPED") && it.dateString != "NEXT_WEEK" && it.dateString < todayNumeric 
        }
        unfinishedPastTasks.forEach { task ->
            taskDao.updateTask(task.copy(dateString = todayNumeric))
        }
    }

    val handleEditTask: (TaskEntity) -> Unit = { task ->
        taskToEdit = task
        newTaskTitle = task.title
        newTaskPriority = task.priority
        newTaskDateString = task.dateString
        newTaskReminderDays = task.reminderDays.toString()
        showDialog = true
    }
    
    val handleEditCollectionItem: (CollectionItemEntity) -> Unit = { item ->
        collectionItemToEdit = item
        newTaskTitle = item.title
        newTaskDesc = item.description
        showDialog = true
    }

    val handlePostpone: (TaskEntity) -> Unit = { task ->
        taskToPostpone = task
        showPostponeDialog = true
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
                    collectionItemToEdit = null
                    newTaskTitle = ""
                    newTaskDesc = ""
                    newTaskPriority = "GRAY"
                    newTaskDateString = if (selectedItem == 3) "" else todayNumeric
                    newTaskReminderDays = "0"
                    newCategoryName = ""
                    if (selectedCategory.isEmpty()) selectedCategory = defaultCategories[0]
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
            0 -> DailyLogScreen(paddingValues, tasks, taskDao, handleEditTask, handlePostpone)
            1 -> WeeklyLogScreen(paddingValues, tasks, taskDao, weekDaysList, selectedWeekDay, { selectedWeekDay = it }, handleEditTask)
            2 -> MonthlyLogScreen(paddingValues, tasks, taskDao, selectedMonthDay, { selectedMonthDay = it }, handleEditTask)
            3 -> FutureLogScreen(paddingValues, tasks, taskDao, handleEditTask)
            4 -> CollectionScreen(paddingValues, collections, collectionDao, allCategories, selectedCategory, { selectedCategory = it }, handleEditCollectionItem)
        }
    }

    // دیالوگ ویرایش/افزودن
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (taskToEdit != null || collectionItemToEdit != null) "ویرایش" else "افزودن", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = {
                Column(modifier = Modifier.heightIn(max = 500.dp)) {
                    OutlinedTextField(value = newTaskTitle, onValueChange = { newTaskTitle = it }, label = { Text("عنوان") }, singleLine = true)
                    
                    if (selectedItem == 4) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = newTaskDesc, onValueChange = { newTaskDesc = it }, label = { Text("توضیحات (اختیاری)") }, singleLine = true)
                        if (collectionItemToEdit == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("مجموعه جدید (اختیاری)") }, placeholder = { Text("اگر خالی باشد، به $selectedCategory اضافه می‌شود") }, singleLine = true)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (selectedItem == 0) {
                            Text("اولویت:", fontSize = 12.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                listOf("RED" to EisenhowerRed, "GREEN" to EisenhowerGreen, "ORANGE" to EisenhowerOrange, "GRAY" to EisenhowerGray).forEach { (p, c) ->
                                    Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(c).border(2.dp, if (newTaskPriority == p) Color.Black else Color.Transparent, CircleShape).clickable { newTaskPriority = p })
                                }
                            }
                        } else if (selectedItem == 3) {
                            OutlinedTextField(value = newTaskDateString, onValueChange = { newTaskDateString = it }, label = { Text("تاریخ (مثلا 1405/12/25)") }, singleLine = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newTaskReminderDays, onValueChange = { newTaskReminderDays = it }, label = { Text("یادآور چند روز قبل") }, singleLine = true)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        scope.launch {
                            if (selectedItem == 4) {
                                val targetCat = if (newCategoryName.isNotBlank()) newCategoryName else selectedCategory
                                if (collectionItemToEdit == null) {
                                    collectionDao.insertItem(CollectionItemEntity(category = targetCat, title = newTaskTitle, description = newTaskDesc))
                                    if (newCategoryName.isNotBlank()) selectedCategory = newCategoryName
                                } else {
                                    collectionDao.updateItem(collectionItemToEdit!!.copy(title = newTaskTitle, description = newTaskDesc))
                                }
                            } else {
                                if (taskToEdit == null) {
                                    val recType = when(selectedItem) { 1 -> "WEEKLY"; 2 -> "MONTHLY"; else -> "NONE" }
                                    val recValue = when(selectedItem) { 1 -> selectedWeekDay; 2 -> selectedMonthDay; else -> "" }
                                    taskDao.insertTask(TaskEntity(title = newTaskTitle, priority = newTaskPriority, recurrenceType = recType, recurrenceValue = recValue, dateString = if(selectedItem==3) newTaskDateString else todayNumeric, reminderDays = newTaskReminderDays.toIntOrNull() ?: 0))
                                } else {
                                    if (taskToEdit!!.recurrenceType != "NONE") {
                                        taskDao.insertTask(TaskEntity(title = newTaskTitle, priority = newTaskPriority, recurrenceType = "NONE", dateString = todayNumeric))
                                        taskDao.updateTask(taskToEdit!!.copy(dateString = "SKIPPED_$todayNumeric"))
                                    } else {
                                        taskDao.updateTask(taskToEdit!!.copy(title = newTaskTitle, priority = newTaskPriority, dateString = newTaskDateString, reminderDays = newTaskReminderDays.toIntOrNull() ?: 0))
                                    }
                                }
                            }
                            showDialog = false
                        }
                    }
                }) { Text("ذخیره", color = DarkEmerald) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("لغو", color = Color.Gray) } }
        )
    }

    // دیالوگ موکول کردن کار
    if (showPostponeDialog && taskToPostpone != null) {
        AlertDialog(
            onDismissRequest = { showPostponeDialog = false },
            title = { Text("موکول کردن", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = { Text("این کار به کدام روز موکول شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (taskToPostpone!!.recurrenceType != "NONE") {
                            taskDao.insertTask(TaskEntity(title = taskToPostpone!!.title, priority = taskToPostpone!!.priority, recurrenceType = "NONE", dateString = tomorrowNumeric))
                            taskDao.updateTask(taskToPostpone!!.copy(dateString = "SKIPPED_$todayNumeric"))
                        } else {
                            taskDao.updateTask(taskToPostpone!!.copy(dateString = tomorrowNumeric))
                        }
                        showPostponeDialog = false
                    }
                }) { Text("فردا", color = DarkEmerald) }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (taskToPostpone!!.recurrenceType != "NONE") {
                            taskDao.insertTask(TaskEntity(title = taskToPostpone!!.title, priority = taskToPostpone!!.priority, recurrenceType = "NONE", dateString = "NEXT_WEEK"))
                            taskDao.updateTask(taskToPostpone!!.copy(dateString = "SKIPPED_$todayNumeric"))
                        } else {
                            taskDao.updateTask(taskToPostpone!!.copy(dateString = "NEXT_WEEK"))
                        }
                        showPostponeDialog = false
                    }
                }) { Text("هفته بعد", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun DailyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao, onEditTask: (TaskEntity) -> Unit, onPostpone: (TaskEntity) -> Unit) {
    val scope = rememberCoroutineScope()
    val currentDate = PersianDate.getCurrentPersianDate()
    val todayNumeric = PersianDate.getCurrentDateNumeric()
    val todayDayOfWeek = PersianDate.getDayOfWeekName()
    val todayDayOfMonth = PersianDate.getDayOfMonthNumber()
    val occasion = PersianDate.getOccasion()

    val todayTasks = tasks.filter {
        val isSkippedToday = it.dateString == "SKIPPED_$todayNumeric"
        !isSkippedToday && (
            (it.recurrenceType == "NONE" && (it.dateString.isEmpty() || it.dateString == todayNumeric)) || 
            (it.recurrenceType == "WEEKLY" && it.recurrenceValue == todayDayOfWeek) ||
            (it.recurrenceType == "MONTHLY" && it.recurrenceValue == todayDayOfMonth)
        )
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("میلادی: ${PersianDate.getMiladiDate()} | قمری: ${PersianDate.getQamariDate()}", color = Color.LightGray, fontSize = 11.sp)
                    Text("سن (تولد 1362/09/01): ${PersianDate.getAge()}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    
                    if (occasion.isNotEmpty()) {
                        Text("مناسبت: $occasion", color = WarningBorder, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        if (todayDayOfMonth == "29") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "هشدار", tint = WarningText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("یادآوری: امروز ۲۹م ماه است. اگر ماه ۳۰ یا ۳۱ روزه است، اقدامات روزهای بعد را فراموش نکنید.", color = WarningText, fontSize = 12.sp, lineHeight = 16.sp)
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
                    onPostpone = { onPostpone(task) },
                    onPriorityChange = { newColor -> scope.launch { taskDao.updateTask(task.copy(priority = newColor)) } },
                    showTick = false // تیک در روزنگار حذف شد
                )
            }
        }
    }
}

@Composable
fun WeeklyLogScreen(
    paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao,
    weekDaysList: List<String>, selectedDay: String, onDaySelected: (String) -> Unit, onEditTask: (TaskEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val rightDays = listOf("شنبه", "یکشنبه", "دوشنبه")
    val leftDays = listOf("سه‌شنبه", "چهارشنبه", "پنجشنبه")
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0..2) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayGridCard(rightDays[i], selectedDay, onDaySelected, tasks, Modifier.weight(1f))
                    DayGridCard(leftDays[i], selectedDay, onDaySelected, tasks, Modifier.weight(1f))
                }
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DayGridCard("جمعه", selectedDay, onDaySelected, tasks, Modifier.fillMaxWidth(0.6f))
            }
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
            Text("کارهای روز $selectedDay", fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
        }
        
        val dayTasks = tasks.filter { it.recurrenceType == "WEEKLY" && it.recurrenceValue == selectedDay }
        if (dayTasks.isEmpty()) {
            item { Text("هیچ کاری ثبت نشده است.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        } else {
            items(dayTasks) { task -> 
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(task.title, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        IconButton(onClick = { onEditTask(task) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Blue, modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun DayGridCard(day: String, selectedDay: String, onDaySelected: (String) -> Unit, tasks: List<TaskEntity>, modifier: Modifier = Modifier) {
    val dayTasks = tasks.filter { it.recurrenceType == "WEEKLY" && it.recurrenceValue == day }
    Card(
        modifier = modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (selectedDay == day) Cream else Color.White),
        border = if (selectedDay == day) androidx.compose.foundation.BorderStroke(2.dp, DarkEmerald) else null,
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.clickable { onDaySelected(day) }.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day, fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 14.sp)
            
            if (dayTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.Center) {
                    repeat(dayTasks.size) {
                        Box(modifier = Modifier.padding(horizontal = 1.dp).size(6.dp).background(EisenhowerOrange, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyLogScreen(
    paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao,
    selectedDay: String, onDaySelected: (String) -> Unit, onEditTask: (TaskEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    val days = (1..31).map { it.toString() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column {
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        week.forEach { dayStr ->
                            val isSelected = dayStr == selectedDay
                            val taskCount = tasks.count { it.recurrenceType == "MONTHLY" && it.recurrenceValue == dayStr }
                            
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (isSelected) DarkEmerald else Color.Transparent)
                                    .border(if (isSelected) 0.dp else 1.dp, if (isSelected) Color.Transparent else Color.LightGray.copy(alpha=0.5f), CircleShape)
                                    .clickable { onDaySelected(dayStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dayStr, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                
                                // رسم دایره‌های نارنجی به صورت حلقه‌ای دور عدد
                                if (taskCount > 0) {
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        val radius = size.minDimension / 2f
                                        val dotRadius = 2.5.dp.toPx()
                                        val angleStep = (2 * Math.PI / taskCount).toFloat()
                                        val startAngle = (-Math.PI / 2).toFloat()
                                        
                                        for (i in 0 until taskCount) {
                                            val angle = startAngle + i * angleStep
                                            // محاسبه موقعیت نقطه روی محیط دایره
                                            val x = center.x + (radius - dotRadius - 2.dp.toPx()) * cos(angle)
                                            val y = center.y + (radius - dotRadius - 2.dp.toPx()) * sin(angle)
                                            drawCircle(color = EisenhowerOrange, radius = dotRadius, center = Offset(x, y))
                                        }
                                    }
                                }
                            }
                        }
                        if (week.size < 7) {
                            repeat(7 - week.size) { Box(modifier = Modifier.size(44.dp)) }
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
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(6.dp)) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(task.title, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        IconButton(onClick = { onEditTask(task) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Blue, modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun FutureLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao, onEditTask: (TaskEntity) -> Unit) {
    val scope = rememberCoroutineScope()
    val todayNumeric = PersianDate.getCurrentDateNumeric()
    
    // فقط کارهایی که تاریخشان بزرگ‌تر از امروز است
    val futureTasks = tasks.filter { it.dateString.isNotEmpty() && !it.dateString.startsWith("SKIPPED") && it.dateString > todayNumeric }.sortedBy { it.dateString }

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
                            dateTasks.forEach { task -> 
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    
                                    // قالب جدید: تاریخ - عنوان (یادآور)
                                    val reminderText = if (task.reminderDays > 0) " (یادآور از ${task.reminderDays} روز قبل فعال است)" else ""
                                    Text("$date - ${task.title}$reminderText", modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.Black)
                                    
                                    IconButton(onClick = { onEditTask(task) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Blue, modifier = Modifier.size(16.dp)) }
                                    IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionScreen(
    paddingValues: PaddingValues, collections: List<CollectionItemEntity>, collectionDao: CollectionDao, 
    allCategories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit, onEditItem: (CollectionItemEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allCategories) { category ->
            val isExpanded = selectedCategory == category
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isExpanded) DarkEmerald else Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.clickable { onCategorySelected(if (isExpanded) "" else category) }.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(category, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isExpanded) Color.White else DarkEmerald)
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = if (isExpanded) Color.White else DarkEmerald)
                    }
                    
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val filteredItems = collections.filter { it.category == category }
                        if (filteredItems.isEmpty()) {
                            Text("این مجموعه خالی است.", color = Color.LightGray, fontSize = 12.sp)
                        } else {
                            filteredItems.forEach { item ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(6.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        
                                        Checkbox(
                                            checked = item.isCompleted,
                                            onCheckedChange = { scope.launch { collectionDao.updateItem(item.copy(isCompleted = it)) } },
                                            colors = CheckboxDefaults.colors(checkedColor = DarkEmerald)
                                        )
                                        
                                        Text(
                                            text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                            color = if (item.isCompleted) Color.Gray else Color.Black,
                                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (item.description.isNotEmpty()) {
                                            Text(item.description, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
                                        }
                                        IconButton(onClick = { onEditItem(item) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, "ویرایش", tint = Color.Blue, modifier = Modifier.size(16.dp)) }
                                        IconButton(onClick = { scope.launch { collectionDao.deleteItem(item) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(16.dp)) }
                                    }
                                }
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
    task: TaskEntity, taskDao: TaskDao, scope: kotlinx.coroutines.CoroutineScope, 
    showPriority: Boolean, onEdit: () -> Unit, onPostpone: () -> Unit, onPriorityChange: (String) -> Unit = {},
    showTick: Boolean = true // پارامتر جدید برای کنترل نمایش تیک
) {
    val color = when(task.priority) { "RED" -> EisenhowerRed; "GREEN" -> EisenhowerGreen; "ORANGE" -> EisenhowerOrange; else -> EisenhowerGray }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showPriority) {
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).clickable { 
                    val nextColor = when(task.priority) { "RED" -> "GREEN"; "GREEN" -> "ORANGE"; "ORANGE" -> "GRAY"; else -> "RED" }
                    onPriorityChange(nextColor) 
                }, contentAlignment = Alignment.Center) { Box(modifier = Modifier.size(10.dp).background(color, CircleShape)) }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = task.title, fontSize = 14.sp, color = if (task.isCompleted) Color.Gray else Color.Black, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None, modifier = Modifier.weight(1f))
            
            IconButton(onClick = onPostpone, modifier = Modifier.size(30.dp)) { 
                Icon(Icons.Default.ArrowForward, "موکول", tint = Color.DarkGray, modifier = Modifier.size(18.dp)) 
            }
            
            IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Edit, "ویرایش", tint = Color.Blue, modifier = Modifier.size(16.dp)) }
            
            // نمایش تیک فقط اگر مجاز باشد (در روزنگار غیرفعال شده)
            if (showTick) {
                IconButton(onClick = { 
                    scope.launch { 
                        if (task.recurrenceType != "NONE") {
                            taskDao.updateTask(task.copy(dateString = "SKIPPED_${PersianDate.getCurrentDateNumeric()}"))
                        } else {
                            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted)) 
                        }
                    } 
                }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Check, "انجام", tint = if (task.isCompleted) Color.Green else Color.LightGray, modifier = Modifier.size(18.dp)) }
            }
            
            IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun BoltJournalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = DarkEmerald, background = Cream, surface = Cream, tertiary = Gold), content = content)
}
