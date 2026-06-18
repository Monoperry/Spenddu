package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Expense
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

// Category Configuration with icons and Duo colors
data class CategoryInfo(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val shadowColor: Color
)

val Categories = listOf(
    CategoryInfo("Food", Icons.Default.Fastfood, DuoYellow, DuoYellowShadow),
    CategoryInfo("Travel", Icons.Default.FlightTakeoff, DuoBlue, DuoBlueShadow),
    CategoryInfo("Shopping", Icons.Default.ShoppingBag, DuoPurple, DuoPurpleShadow),
    CategoryInfo("Entertainment", Icons.Default.VideogameAsset, DuoRed, DuoRedShadow),
    CategoryInfo("Bills", Icons.Default.Receipt, DuoGreen, DuoGreenShadow),
    CategoryInfo("Other", Icons.Default.MoreHoriz, Color(0xFF00D2C4), Color(0xFF009B90))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ExpenseViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val monthlyReports by viewModel.monthlyReports.collectAsStateWithLifecycle()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val dailyCap by viewModel.dailyCap.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("home") } // "home" or "report"
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSetCapDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // Startup daily cap warning alert
    var hasShownStartupWarning by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var showStartupWarningDialog by remember { mutableStateOf(false) }

    LaunchedEffect(allExpenses, dailyCap) {
        if (!hasShownStartupWarning && dailyCap > 0.0) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            val todaySpend = allExpenses.filter { it.date >= startOfDay }.sumOf { it.amount }
            if (todaySpend > dailyCap) {
                showStartupWarningDialog = true
                hasShownStartupWarning = true
            }
        }
    }

    // On first launch, open the Add screen automatically as per user sketch instruction
    LaunchedEffect(isFirstLaunch) {
        if (isFirstLaunch) {
            showAddSheet = true
            viewModel.markFirstLaunchComplete()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                // Background Shadow layer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .statusBarsPadding()
                        .background(
                            color = DuoBorderGray,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                )
                // Foreground top header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .statusBarsPadding()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = DuoDarkGray,
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "spend",
                            fontWeight = FontWeight.Black,
                            color = DuoDarkGray,
                            fontSize = 32.sp,
                            letterSpacing = (-1.5).sp
                        )
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.offset(y = (-2).dp)) {
                            Text(
                                text = "D",
                                fontWeight = FontWeight.Black,
                                color = DuoDarkGray,
                                fontSize = 36.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                        miter = 10f,
                                        width = 16f,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            )
                            Text(
                                text = "D",
                                fontWeight = FontWeight.Black,
                                color = DuoYellow,
                                fontSize = 36.sp
                            )
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.offset(y = (-2).dp)) {
                            Text(
                                text = "o",
                                fontWeight = FontWeight.Black,
                                color = DuoDarkGray,
                                fontSize = 36.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                        miter = 10f,
                                        width = 16f,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            )
                            Text(
                                text = "o",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 36.sp
                            )
                        }
                    }

                    // Settings icon button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, DuoDarkGray, CircleShape)
                            .clickable { showSettingsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DuoDarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // DUOLINGO-STYLE CUSTOM BOTTOM NAVBAR (Aligns with User sketch)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = DuoBorderGray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    .navigationBarsPadding(),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home/Add Feed Tab Button
                    Box(modifier = Modifier.weight(1f)) {
                        DuolingoTabButton(
                            text = "Add / Spend",
                            selected = selectedTab == "home",
                            onClick = { selectedTab = "home" },
                            icon = { 
                                Icon(
                                    imageVector = Icons.Default.AddHome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                ) 
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Reports Tab Button
                    Box(modifier = Modifier.weight(1f)) {
                        DuolingoTabButton(
                            text = "Report",
                            selected = selectedTab == "report",
                            onClick = { selectedTab = "report" },
                            icon = { 
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                ) 
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == "home") {
                DuolingoButton(
                    onClick = { showAddSheet = true },
                    backgroundColor = DuoBlue,
                    shadowColor = DuoBlueShadow,
                    borderColor = DuoDarkGray,
                    modifier = Modifier
                        .width(160.dp)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .testTag("fab_add_expense")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add spent", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    "home" -> HomeFeedSection(
                        allExpenses = allExpenses,
                        selectedCurrency = selectedCurrency,
                        dailyCap = dailyCap,
                        onSetDailyCap = { showSetCapDialog = true },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onOpenAddForm = { showAddSheet = true }
                    )
                    "report" -> ReportsSection(
                        monthlyReports = monthlyReports,
                        selectedCurrency = selectedCurrency
                    )
                }
            }

            // Slide Up Bottom Sheet for Adding Expenses
            if (showAddSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DuoBorderGray) },
                ) {
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                    AddExpenseBottomSheetForm(
                        selectedCurrency = selectedCurrency,
                        isLoading = isLoading,
                        onDismiss = { showAddSheet = false },
                        onSubmit = { amount, forWhat, paidTo, cat ->
                            viewModel.addExpense(amount, forWhat, paidTo, cat, System.currentTimeMillis()) {
                                showAddSheet = false
                                showSuccessAnimation = true
                            }
                        }
                    )
                }
            }

            // Auth & Settings Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    selectedCurrency = selectedCurrency,
                    onCurrencyChange = { viewModel.setCurrency(it) },
                    dailyCap = dailyCap,
                    onDailyCapChange = { viewModel.setDailyCap(it) },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Specify Daily Cap Dialog
            if (showSetCapDialog) {
                SetDailyCapDialog(
                    currentCap = dailyCap,
                    selectedCurrency = selectedCurrency,
                    onSaveCap = { viewModel.setDailyCap(it) },
                    onDismiss = { showSetCapDialog = false }
                )
            }

            // Daily Cap Crossing Warning Dialog
            if (showStartupWarningDialog) {
                Dialog(onDismissRequest = { showStartupWarningDialog = false }) {
                    DuolingoCard(
                        backgroundColor = Color.White,
                        borderColor = DuoDarkGray,
                        shadowColor = DuoShadowGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DuoRed,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Over Cap Alert!",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = DuoRed
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "You have already crossed your daily cap of $selectedCurrency${String.format(Locale.getDefault(), "%.2f", dailyCap)} for today!",
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = DuoDarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Let's be extra frugal and practice mindfulness for the rest of the day!",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            DuolingoButton(
                                onClick = { showStartupWarningDialog = false },
                                backgroundColor = DuoBlue,
                                shadowColor = DuoBlueShadow,
                                borderColor = DuoDarkGray,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("I'll Stay Strong!", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // SUCCESS ANIMATION DIALOG
            if (showSuccessAnimation) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showSuccessAnimation = false
                }
                androidx.compose.ui.window.Dialog(onDismissRequest = { showSuccessAnimation = false }) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(3.dp, DuoDarkGray, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Famous lottie success animation
                        val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://assets10.lottiefiles.com/packages/lf20_U1084s.json"))
                        val progress by animateLottieCompositionAsState(
                            composition,
                            isPlaying = true,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.size(150.dp)
                            )
                            Text(
                                "Logged Successfully!",
                                fontWeight = FontWeight.Bold,
                                color = DuoGreen,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFeedSection(
    allExpenses: List<Expense>,
    selectedCurrency: String,
    dailyCap: Double,
    onSetDailyCap: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onOpenAddForm: () -> Unit
) {
    val totalThisMonth = allExpenses.sumOf { it.amount }

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis

    val todayExpenses = allExpenses.filter { it.date >= startOfDay }
    val todaySpend = todayExpenses.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DAILY SUMMARY / TODAY'S SPEND CARD
        item {
            val isCapReachedExactly = dailyCap > 0.0 && Math.abs(todaySpend - dailyCap) < 0.01
            val isCapCrossed = dailyCap > 0.0 && todaySpend > dailyCap + 0.01

            val cardBgColor = when {
                isCapCrossed -> DuoRed
                isCapReachedExactly -> DuoYellow
                else -> DuoGreen
            }
            val cardShadowColor = when {
                isCapCrossed -> DuoRedShadow
                isCapReachedExactly -> DuoYellowShadow
                else -> DuoGreenShadow
            }

            DuolingoCard(
                backgroundColor = cardBgColor,
                borderColor = DuoDarkGray,
                shadowColor = cardShadowColor,
                shadowDepth = 5.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S SPEND",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%s%.2f", selectedCurrency, todaySpend),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (dailyCap > 0.0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cap limit: $selectedCurrency${String.format(Locale.getDefault(), "%.2f", dailyCap)}" + 
                                       when {
                                           isCapCrossed -> " - OVER CAP"
                                           isCapReachedExactly -> " - CAP REACHED"
                                           else -> " (under control)"
                                       },
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "No daily cap set.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(
                            imageVector = if (isCapCrossed) Icons.Default.Warning else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Cute action button to customize cap
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { onSetDailyCap() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set Cap",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }



        // TITLE FOR TRANSACTIONS FEED
        item {
            Text(
                text = "Recent Spends",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = DuoDarkGray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (allExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DuoYellow,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No spends logged yet!",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DuolingoButton(
                            onClick = onOpenAddForm,
                            backgroundColor = DuoBlue,
                            shadowColor = DuoBlueShadow,
                            modifier = Modifier.width(180.dp)
                        ) {
                            Text("Log Spent", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(allExpenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    selectedCurrency = selectedCurrency,
                    onDelete = { onDeleteExpense(expense) }
                )
            }
        }

        // Dynamic spacing at the bottom to allow scrolling past FAB
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    selectedCurrency: String = "$",
    onDelete: () -> Unit
) {
    val category = Categories.find { it.name == expense.category } ?: Categories.first()
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    DuolingoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with solid cartoon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(category.color, RoundedCornerShape(12.dp))
                    .border(2.dp, category.shadowColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.forWhat,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = DuoDarkGray
                )
                Spacer(modifier = Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Paid to: ",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = expense.paidTo,
                        fontSize = 12.sp,
                        color = DuoDarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Pricing details & Delete
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%s%.2f", selectedCurrency, expense.amount),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = DuoDarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Spend",
                        tint = DuoRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Expandable Reports Section
@Composable
fun ReportsSection(
    monthlyReports: Map<String, List<Expense>>,
    selectedCurrency: String = "$"
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DuolingoCard(
                backgroundColor = DuoPurple,
                borderColor = DuoDarkGray,
                shadowColor = DuoPurpleShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "MONTHLY HISTORY",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check out your month-wise breakdowns below. Toggle to view listings in date-ascending order!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        if (monthlyReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No report records yet!",
                            color = DuoDarkGray,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            // Render each month's aggregated card
            monthlyReports.forEach { (monthStr, expenses) ->
                val totalAmount = expenses.sumOf { it.amount }
                val isExpanded = expandedStates[monthStr] ?: false

                item {
                    DuolingoCard(
                        backgroundColor = Color.White,
                        borderColor = DuoDarkGray,
                        shadowColor = if (isExpanded) DuoBlue else DuoShadowGray,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { expandedStates[monthStr] = !isExpanded }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = DuoDarkGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = monthStr,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = DuoDarkGray
                                        )
                                        Text(
                                            text = "${expenses.size} spent items",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%s%.2f", selectedCurrency, totalAmount),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = DuoGreen,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand month",
                                        tint = DuoDarkGray
                                    )
                                }
                            }

                            // EXPANDED LIST: ASCENDING ORDER OF DATES
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = DuoBorderGray, modifier = Modifier.padding(bottom = 8.dp))
                                    
                                    expenses.forEach { expense ->
                                        ReportExpenseRowItem(expense = expense, selectedCurrency = selectedCurrency)
                                        Spacer(modifier = Modifier.height(8.dp))
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
fun ReportExpenseRowItem(
    expense: Expense,
    selectedCurrency: String = "$"
) {
    val category = Categories.find { it.name == expense.category } ?: Categories.first()
    val simpleDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(expense.date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DuoLightGray, RoundedCornerShape(12.dp))
            .border(2.dp, DuoDarkGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date badge
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, DuoDarkGray, RoundedCornerShape(8.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = simpleDate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = DuoDarkGray
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )
                Text(
                    text = expense.forWhat,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = DuoDarkGray
                )
            }
            Text(
                text = "Paid to: ${expense.paidTo}",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Price Tag
        Text(
            text = String.format(Locale.getDefault(), "%s%.2f", selectedCurrency, expense.amount),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = DuoDarkGray
        )
    }
}

// SLIDABLE BOTTOM SHEET FORM (Simple, cartoonish, no hectic inputs)
@Composable
fun AddExpenseBottomSheetForm(
    selectedCurrency: String = "$",
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, forWhat: String, paidTo: String, category: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var forWhatStr by remember { mutableStateOf("") }
    var paidToStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How much paid?",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = DuoDarkGray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Giant amount entry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(200.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, DuoDarkGray, RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = selectedCurrency,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = DuoGreen,
                modifier = Modifier.padding(end = 4.dp)
            )
            BasicTextField(
                value = amountStr,
                onValueChange = { input: String ->
                    // Limit values to clean currency numbers (optional)
                    if (input.all { it.isDigit() || it == '.' }) {
                        amountStr = input
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoDarkGray,
                    textAlign = TextAlign.Start
                ),
                singleLine = true,
                modifier = Modifier
                    .width(120.dp)
                    .testTag("amount_textfield_input")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // For what input
        DuolingoTextField(
            value = forWhatStr,
            onValueChange = { forWhatStr = it },
            placeholder = "e.g. Tasty pizza",
            label = "Money spent for what?",
            testTag = "for_what_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Paid to input
        DuolingoTextField(
            value = paidToStr,
            onValueChange = { paidToStr = it },
            placeholder = "e.g. Papa Johns Pizza",
            label = "To whom it was paid?",
            testTag = "paid_to_input"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Category Selection Row with quick taps
        Text(
            text = "Select Category",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = DuoDarkGray,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp, bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(Categories) { category ->
                val isSelected = selectedCategory == category.name
                val borderCol = DuoDarkGray
                val shadowCol = if (isSelected) DuoYellowShadow else DuoShadowGray
                val bgCol = if (isSelected) DuoYellow else Color.White

                DuolingoCard(
                    backgroundColor = bgCol,
                    borderColor = borderCol,
                    shadowColor = shadowCol,
                    borderWidth = 2.dp,
                    shadowDepth = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        selectedCategory = category.name
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.width(90.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = if (isSelected) Color.White else DuoDarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = if (isSelected) Color.White else DuoDarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Submit Button
        val isValid = amountStr.toDoubleOrNull() != null && forWhatStr.isNotBlank() && paidToStr.isNotBlank()

        DuolingoButton(
            onClick = {
                val amountValue = amountStr.toDoubleOrNull() ?: 0.0
                onSubmit(amountValue, forWhatStr, paidToStr, selectedCategory)
            },
            enabled = isValid && !isLoading,
            backgroundColor = DuoGreen,
            shadowColor = DuoGreenShadow,
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_expense_button"
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = "LOG SPENT",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// LOCAL PROFILE INTERACTION SYSTEM (In Settings dialog)
@Composable
fun SettingsDialog(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    dailyCap: Double,
    onDailyCapChange: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DuolingoCard(
            backgroundColor = Color.White,
            borderColor = DuoBorderGray,
            shadowColor = DuoShadowGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App Settings",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = DuoDarkGray
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Backup Card
                DuolingoCard(
                    backgroundColor = DuoLightGray,
                    borderColor = DuoBorderGray,
                    shadowColor = DuoShadowGray,
                    borderWidth = 1.dp,
                    shadowDepth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = DuoGreen,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text("Local Save Active", fontWeight = FontWeight.Bold, color = DuoGreen, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Database status: Stored on device only", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DuoBorderGray)
                Spacer(modifier = Modifier.height(16.dp))

                // CURRENCY SELECTOR
                Text(
                    text = "SELECT CURRENCY",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = DuoDarkGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currencies = listOf("$", "€", "£", "¥", "₹", "₪", "₩")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currencies) { symbol ->
                        val isSelected = selectedCurrency == symbol
                        DuolingoCard(
                            backgroundColor = if (isSelected) DuoGreen else Color.White,
                            borderColor = DuoDarkGray,
                            shadowColor = if (isSelected) DuoGreenShadow else DuoShadowGray,
                            borderWidth = 2.dp,
                            shadowDepth = 3.dp,
                            onClick = { onCurrencyChange(symbol) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = symbol,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isSelected) Color.White else DuoDarkGray,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CHOSEN DAILY BUDGET CAP
                Text(
                    text = "DAILY SPENDING LIMIT",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = DuoDarkGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                DuolingoCard(
                    backgroundColor = DuoLightGray,
                    borderColor = DuoDarkGray,
                    shadowColor = DuoShadowGray,
                    borderWidth = 1.dp,
                    shadowDepth = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (dailyCap > 0.0) {
                                    "Limit: $selectedCurrency${String.format(Locale.getDefault(), "%.2f", dailyCap)}"
                                } else {
                                    "No spending limit set."
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DuoDarkGray
                            )
                            Text(
                                text = "Red alert warns when crossed.",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        // Customize button inline
                        var showEditField by remember { mutableStateOf(false) }
                        var capInput by remember { mutableStateOf(if (dailyCap > 0.0) dailyCap.toInt().toString() else "") }

                        if (showEditField) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                BasicTextField(
                                    value = capInput,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) capInput = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DuoDarkGray
                                    ),
                                    modifier = Modifier
                                        .width(60.dp)
                                        .background(Color.White, RoundedCornerShape(6.dp))
                                        .border(1.dp, DuoDarkGray, RoundedCornerShape(6.dp))
                                        .padding(4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val newCap = capInput.toDoubleOrNull() ?: 0.0
                                        onDailyCapChange(newCap)
                                        showEditField = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save", tint = DuoGreen)
                                }
                            }
                        } else {
                            DuolingoButton(
                                onClick = { showEditField = true },
                                backgroundColor = DuoBlue,
                                shadowColor = DuoBlueShadow,
                                borderColor = DuoDarkGray,
                                modifier = Modifier.height(32.dp).width(80.dp)
                            ) {
                                Text("Modify", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = DuoBorderGray)

                Spacer(modifier = Modifier.height(12.dp))

                // App details
                Text(
                    text = "SpendDu App v1.0.0",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crafted beautifully in Jetpack Compose",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SetDailyCapDialog(
    currentCap: Double,
    selectedCurrency: String,
    onSaveCap: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var capStr by remember { mutableStateOf(if (currentCap > 0.0) currentCap.toInt().toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        DuolingoCard(
            backgroundColor = Color.White,
            borderColor = DuoDarkGray,
            shadowColor = DuoShadowGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = DuoDarkGray,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Daily Spending Cap",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = DuoDarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set a budget cap limit. Duo will alert you if your single-day spends exceed this cap!",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Input box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DuoLightGray, RoundedCornerShape(12.dp))
                        .border(2.dp, DuoDarkGray, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = selectedCurrency,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoGreen,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    BasicTextField(
                        value = capStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) capStr = input
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoDarkGray
                        ),
                        modifier = Modifier.width(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DuolingoButton(
                        onClick = onDismiss,
                        backgroundColor = Color.White,
                        borderColor = DuoBorderGray,
                        shadowColor = DuoShadowGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = DuoDarkGray, fontWeight = FontWeight.Bold)
                    }

                    DuolingoButton(
                        onClick = {
                            val finalCap = capStr.toDoubleOrNull() ?: 0.0
                            onSaveCap(finalCap)
                            onDismiss()
                        },
                        backgroundColor = DuoGreen,
                        shadowColor = DuoGreenShadow,
                        borderColor = DuoDarkGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Cap", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
