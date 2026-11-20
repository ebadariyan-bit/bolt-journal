package ir.boltjournal.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
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
    val dailyNoteDao = db.dailyNoteDao()
    
    val tasks by taskDao.getAllTasks().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var taskTargetDate by remember { mutableStateOf(PersianDate.getCurrentPersianDate()) }
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
                    taskTargetDate = if (selectedItem == 1) PersianDate.getCurrentWeekDates().first() else PersianDate.getCurrentPersianDate()
                    showDialog = true 
                },
                containerColor = Gold,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن کار جدید")
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
        when (selectedItem) {
            0 -> DailyLogScreen(paddingValues, tasks, taskDao, dailyNoteDao)
            1 -> WeeklyLogScreen(paddingValues, tasks, taskDao)
            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("صفحه ${navItems[selectedItem]} به زودی...", fontSize = 18.sp, color = DarkEmerald, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("افزودن کار جدید", fontWeight = FontWeight.Bold, color = DarkEmerald) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("عنوان کار") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("برای روز: $taskTargetDate", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            scope.launch {
                                taskDao.insertTask(TaskEntity(title = newTaskTitle, date = taskTargetDate.hashCode().toLong()))
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
fun DailyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao, dailyNoteDao: DailyNoteDao) {
    val context = LocalContext.current
    val currentDate = PersianDate.getCurrentPersianDate()
    val savedNote by dailyNoteDao.getNoteForDate(currentDate).collectAsState(initial = null)
    var noteText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(savedNote) {
        if (savedNote != null) noteText = savedNote?.noteText ?: ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkEmerald),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("امروز", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(currentDate, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (tasks.isNotEmpty()) {
            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }
            val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("پیشرفت امروز", fontWeight = FontWeight.Bold, color = DarkEmerald)
                            Text("$completedTasks از $totalTasks", color = Color.Gray, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Gold,
                            trackColor = Cream
                        )
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("هیچ کاری ثبت نشده است.", fontSize = 16.sp, color = Color.Gray)
                }
            }
        } else {
            items(tasks) { task ->
                TaskItem(task, taskDao, scope)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("یادداشت و ارزیابی روزانه", fontWeight = FontWeight.Bold, color = DarkEmerald, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("امروز چطور بود؟...") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            scope.launch { 
                                dailyNoteDao.insertOrUpdateNote(DailyNoteEntity(currentDate, noteText)) 
                                Toast.makeText(context, "یادداشت با موفقیت ذخیره شد ✅", Toast.LENGTH_SHORT).show()
                            } 
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkEmerald)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "ذخیره", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ذخیره یادداشت")
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyLogScreen(paddingValues: PaddingValues, tasks: List<TaskEntity>, taskDao: TaskDao) {
    val weekDates = PersianDate.getCurrentWeekDates()
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDates) { date ->
                val index = weekDates.indexOf(date)
                Card(
                    modifier = Modifier.width(100.dp).height(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDayIndex == index) DarkEmerald else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { selectedDayIndex = index }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = if (selectedDayIndex == index) Color.White else DarkEmerald,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        val selectedDate = weekDates[selectedDayIndex]
        val dayTasks = tasks.filter { it.date == selectedDate.hashCode().toLong() }

        if (dayTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ کاری برای این روز ثبت نشده است.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayTasks) { task ->
                    TaskItem(task, taskDao, scope)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, taskDao: TaskDao, scope: kotlinx.coroutines.CoroutineScope) {
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
                IconButton(onClick = { scope.launch { taskDao.updateTask(task.copy(isCompleted = !task.isCompleted)) } }) {
                    Icon(Icons.Default.Check, contentDescription = "انجام شد", tint = if (task.isCompleted) Color.Green else Color.LightGray)
                }
                IconButton(onClick = { scope.launch { taskDao.deleteTask(task) } }) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
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
