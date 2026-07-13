package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.LoveGuruViewModel
import com.example.ui.UiState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LocalLoveColors
import com.example.ui.theme.ThemePresets
import com.example.ui.theme.LoveThemeColors
import androidx.compose.ui.text.TextStyle

object Color {
    val White = ComposeColor.White
    val Black = ComposeColor.Black
    val Red = ComposeColor.Red
    val Green = ComposeColor.Green
    val Blue = ComposeColor.Blue
    val Gray = ComposeColor.Gray
    val LightGray = ComposeColor.LightGray
    val DarkGray = ComposeColor.DarkGray
    val Yellow = ComposeColor.Yellow
    val Magenta = ComposeColor.Magenta
    val Cyan = ComposeColor.Cyan
    val Transparent = ComposeColor.Transparent

    @Composable
    operator fun invoke(value: Long): ComposeColor {
        val default = ComposeColor(value)
        val colors = LocalLoveColors.current
        return when (value) {
            0xFFE11D48L -> colors.primary
            0xFFFB7185L -> colors.secondary
            0xFFFBCFE8L -> colors.tertiary
            0xFF4C0519L -> colors.textDark
            0xFFFFF1F2L -> colors.bgLight
            0xFFFFF8F8L -> colors.cardBg
            0xFFFFF0F3L -> colors.cardBg
            0xFFFEE2E2L -> colors.alertBg
            0xFFFECACAL -> colors.alertBorder
            else -> default
        }
    }

    @Composable
    operator fun invoke(value: Int): ComposeColor {
        val default = ComposeColor(value)
        val colors = LocalLoveColors.current
        return when (value.toLong() and 0xFFFFFFFFL) {
            0xFFE11D48L -> colors.primary
            0xFFFB7185L -> colors.secondary
            0xFFFBCFE8L -> colors.tertiary
            0xFF4C0519L -> colors.textDark
            0xFFFFF1F2L -> colors.bgLight
            0xFFFFF8F8L -> colors.cardBg
            0xFFFFF0F3L -> colors.cardBg
            0xFFFEE2E2L -> colors.alertBg
            0xFFFECACAL -> colors.alertBorder
            else -> default
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LoveGuruViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()

            MyApplicationTheme(colors = currentTheme) {
                LoveGuruApp(viewModel = viewModel)
            }
        }
    }
}

enum class Screen(val title: String, val iconFilled: androidx.compose.ui.graphics.vector.ImageVector, val iconOutlined: androidx.compose.ui.graphics.vector.ImageVector) {
    TRICKY("Tricky Solver", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    GUSSA("Gussa Regulator", Icons.Filled.Shield, Icons.Outlined.Shield),
    REPLY("Reply Decoder", Icons.Filled.Forum, Icons.Outlined.Forum),
    SAVED("Saved Gems", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

@Composable
fun LoveGuruApp(viewModel: LoveGuruViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(Screen.TRICKY) }
    val savedGems by viewModel.savedGems.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Screen.values().forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.iconFilled else screen.iconOutlined,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar
            LoveGuruHeader(viewModel = viewModel)

            // Dynamic Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.TRICKY -> TrickySolverView(viewModel)
                    Screen.GUSSA -> GussaRegulatorView(viewModel)
                    Screen.REPLY -> ReplyDecoderView(viewModel)
                    Screen.SAVED -> SavedGemsView(viewModel, savedGems)
                }
            }
        }
    }
}

@Composable
fun LoveGuruHeader(viewModel: LoveGuruViewModel) {
    val isApiKeyConfigured = viewModel.isApiKeyConfigured
    val currentTheme by viewModel.currentTheme.collectAsState()
    val themeGenerationState by viewModel.themeGenerationState.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var customThemePrompt by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular logo matching HTML
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFB7185), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = "Love Guru Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Love Guru",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "ULTRA-EQ MENTOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11D48),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Right Control Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Customizer Palette Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                        .clickable { showThemeDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = "Theme Customizer",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Info Notification Bell Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                        .clickable { showInfoDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Info & Alerts",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(22.dp)
                        )
                        if (!isApiKeyConfigured) {
                            // Badge indicating warning/notice
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFE11D48), shape = RoundedCornerShape(50))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }

        // Expanded notice in bento card style if API key is not configured
        if (!isApiKeyConfigured) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F3)),
                border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guru is in Offline/Preview mode. Set your GEMINI_API_KEY in AI Studio to unlock dynamic answers!",
                        fontSize = 11.sp,
                        color = Color(0xFF4C0519),
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("About Love Guru", fontWeight = FontWeight.Bold, color = Color(0xFF4C0519)) },
                text = {
                    Text(
                        "Love Guru is an Ultra-EQ relationship advisor powered by advanced psychology to decode subtle replies, diffuse anger (Gussa), and solve relationship trap questions.\n\nStyled in an ultra-modern Bento Grid theme.",
                        color = Color(0xFF4C0519)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Dismiss", color = Color(0xFFE11D48))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = "Theme", tint = currentTheme.primary)
                        Text("AI Theme Customizer 🎨", fontWeight = FontWeight.Bold, color = currentTheme.textDark)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Choose a gorgeous 'Lovers' preset theme or let the AI dynamically generate an optimal palette based on your mood!",
                            fontSize = 12.sp,
                            color = currentTheme.textDark.copy(alpha = 0.8f)
                        )
                        
                        Text(
                            text = "LOVERS PRESETS:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary,
                            letterSpacing = 1.sp
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ThemePresets.list) { preset ->
                                val isSelected = preset.name == currentTheme.name
                                Card(
                                    modifier = Modifier
                                        .width(115.dp)
                                        .clickable { viewModel.selectTheme(preset) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) preset.primary else preset.tertiary),
                                    colors = CardDefaults.cardColors(containerColor = preset.bgLight)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(12.dp).background(preset.primary, RoundedCornerShape(50)))
                                            Box(modifier = Modifier.size(12.dp).background(preset.secondary, RoundedCornerShape(50)))
                                            Box(modifier = Modifier.size(12.dp).background(preset.bgLight, RoundedCornerShape(50)))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = preset.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = preset.textDark,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        
                        Divider(color = currentTheme.tertiary.copy(alpha = 0.5f))
                        
                        Text(
                            text = "AI CUSTOM THEME GENERATOR:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary,
                            letterSpacing = 1.sp
                        )
                        
                        OutlinedTextField(
                            value = customThemePrompt,
                            onValueChange = { customThemePrompt = it },
                            placeholder = { Text("E.g., Barbie Pink, Forest Date, Neon Gothic, Peach Sunset...", fontSize = 12.sp, color = currentTheme.textDark.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp, color = currentTheme.textDark),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentTheme.primary,
                                unfocusedBorderColor = currentTheme.tertiary,
                                focusedContainerColor = currentTheme.bgLight.copy(alpha = 0.3f),
                                unfocusedContainerColor = currentTheme.bgLight.copy(alpha = 0.3f)
                            )
                        )
                        
                        if (themeGenerationState is UiState.Loading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = currentTheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI is designing your palette...", fontSize = 12.sp, color = currentTheme.primary)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (customThemePrompt.isNotBlank()) {
                                        viewModel.generateThemeFromPrompt(customThemePrompt) { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Sparkles", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Design Palette", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Done", color = currentTheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------
// CAPABILITY A: TRICKY QUESTION SOLVER
// ---------------------------------------------------------------------------------
@Composable
fun TrickySolverView(viewModel: LoveGuruViewModel) {
    var questionInput by remember { mutableStateOf("") }
    val state by viewModel.trickyState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val trickyTemplates = listOf(
        "Does this dress make me look fat?",
        "Who is more beautiful, me or your girl best friend?",
        "If we were on a desert island and had only one slice of cake, would you give it to me?",
        "Do you ever think about your ex?",
        "What would you do if I suddenly disappeared?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Decorative Hero Banner
        LoveGuruHeroBanner()

        // MAIN TRAP SOLVER BENTO CARD (Wide, White background, pink outline)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Bolt Icon",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Trap Solver",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF0F3), shape = RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HIGH ALERT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text(
                    text = "Analyze why she is asking and get the perfect, customized response.",
                    fontSize = 13.sp,
                    color = Color(0xFF4C0519).copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("E.g., Does this dress make me look fat?", color = Color(0xFF4C0519).copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tricky_question_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE11D48),
                        unfocusedBorderColor = Color(0xFFFBCFE8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFF8F8),
                        focusedTextColor = Color(0xFF4C0519),
                        unfocusedTextColor = Color(0xFF4C0519)
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.QuestionMark, contentDescription = "Question", tint = Color(0xFFE11D48))
                    },
                    trailingIcon = {
                        if (questionInput.isNotEmpty()) {
                            IconButton(onClick = { questionInput = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = Color(0xFFE11D48))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (questionInput.isNotBlank()) {
                            viewModel.solveTrickyQuestion(questionInput)
                        } else {
                            Toast.makeText(context, "Please enter a question!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("tricky_solve_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "Solve", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solve Trap 💥", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // CLASSIC TRAPS SUGGESTIONS BENTO CARD (Soft red BG)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Classic Trap Templates 📌",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C0519),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(trickyTemplates) { item ->
                        SuggestionChip(
                            onClick = { questionInput = item },
                            label = { Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4C0519)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        )
                    }
                }
            }
        }

        // STATE OUTPUTS
        when (val currentState = state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE11D48))
                }
            }
            is UiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentState.message,
                        color = Color(0xFF4C0519),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is UiState.Success -> {
                val data = currentState.data
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // PSYCHOLOGICAL ANALYSIS CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lightbulb,
                                        contentDescription = "Analysis",
                                        tint = Color(0xFFE11D48)
                                    )
                                    Text(
                                        text = "Guru's Psychological Check 🧠",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF4C0519)
                                    )
                                }
                                Text(
                                    text = data.analysis,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // RESPONSES LIST HEADER
                        Text(
                            text = "Draft Responses (Dil Jeetne Wale):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C0519),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // THE 3 RESPONSES
                        ResponseCard(
                            label = data.option1Label,
                            text = data.option1Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option1Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option2Label,
                            text = data.option2Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option2Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option3Label,
                            text = data.option3Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option3Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // QUICK ACTION BLACK BAR (Matches HTML capsule bottom bar)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B1B)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Love these master replies?",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = {
                                        viewModel.saveGem(
                                            SavedGem(
                                                type = "tricky",
                                                title = questionInput,
                                                analysis = data.analysis,
                                                option1Label = data.option1Label,
                                                option1Text = data.option1Text,
                                                option2Label = data.option2Label,
                                                option2Text = data.option2Text,
                                                option3Label = data.option3Label,
                                                option3Text = data.option3Text
                                            )
                                        )
                                        Toast.makeText(context, "Saved to Saved Gems! 💎", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Bookmark, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Gems", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------------
// CAPABILITY B: GUSSA REGULATOR
// ---------------------------------------------------------------------------------
@Composable
fun GussaRegulatorView(viewModel: LoveGuruViewModel) {
    var situationInput by remember { mutableStateOf("") }
    val state by viewModel.gussaState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Slider state for the interactive Gussa Meter widget
    var gussaLevel by remember { mutableFloatStateOf(0.75f) }

    val preloadedSituations = listOf(
        "Ignoring my calls & silent treatment",
        "Late reply and dry 'Hmm' / 'K' texts",
        "Argument about forgetting to plan date",
        "Extremely angry because I replied late"
    )

    // Automatically update gussa level slider if result succeeds
    LaunchedEffect(state) {
        if (state is UiState.Success) {
            gussaLevel = (70..98).random() / 100f
        }
    }

    val gussaLabel = when {
        gussaLevel <= 0.20f -> "Cool / Chill 🧊"
        gussaLevel <= 0.45f -> "Mildly Annoyed 🤨"
        gussaLevel <= 0.70f -> "Angry 😡"
        gussaLevel <= 0.90f -> "Furious 🔥"
        else -> "Volcanic / Nuclear 🌋"
    }

    val gussaAdvice = when {
        gussaLevel <= 0.20f -> "Advice: Chill vibes. Keep doing what you're doing."
        gussaLevel <= 0.45f -> "Advice: Send a sweet meme or ask about her day. Quick fix."
        gussaLevel <= 0.70f -> "Advice: No explanation. Simple 'sorry' and a cute callback works."
        gussaLevel <= 0.90f -> "Advice: Silence for 4-6 hours. Give space, send a soft ping at 9 PM."
        else -> "Advice: Red Alert! Order her favorite food, apologize sincerely, stay on standby."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MAIN INPUT BENTO CARD (Wide, White background, pink outline)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Shield Icon",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "The Gussa Regulator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519)
                    )
                }

                Text(
                    text = "When she is extremely angry or ignoring you. Keep calm and get the Battle Plan. Please don't text bomb her!",
                    fontSize = 13.sp,
                    color = Color(0xFF4C0519).copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = situationInput,
                    onValueChange = { situationInput = it },
                    placeholder = { Text("Describe what happened or select a quick option below...", color = Color(0xFF4C0519).copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gussa_situation_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE11D48),
                        unfocusedBorderColor = Color(0xFFFBCFE8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFF8F8),
                        focusedTextColor = Color(0xFF4C0519),
                        unfocusedTextColor = Color(0xFF4C0519)
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.SmsFailed, contentDescription = "Anger", tint = Color(0xFFE11D48))
                    },
                    trailingIcon = {
                        if (situationInput.isNotEmpty()) {
                            IconButton(onClick = { situationInput = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = Color(0xFFE11D48))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (situationInput.isNotBlank()) {
                            viewModel.regulateGussa(situationInput)
                        } else {
                            Toast.makeText(context, "Please describe the situation!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("gussa_solve_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Icon(imageVector = Icons.Filled.Shield, contentDescription = "Protect", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Battle Plan 🛡️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // BENTO COLUMN GRID: TWO SQUARE-ISH CARDS (arranged elegantly)
        // Card A: Interactive Gussa Meter Card (soft pink)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Interactive Gussa Meter 🌡️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE11D48), shape = RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gussaLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom gradient progress bar using simple rows
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(Color(0xFFFFF0F3), shape = RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(gussaLevel)
                            .fillMaxHeight()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFB7185), Color(0xFFE11D48))
                                ),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = gussaAdvice,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE11D48),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slider so they can manually play with the Gussa level!
                Slider(
                    value = gussaLevel,
                    onValueChange = { gussaLevel = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFE11D48),
                        activeTrackColor = Color(0xFFFB7185),
                        inactiveTrackColor = Color(0xFFFFF0F3)
                    )
                )
            }
        }

        // Card B: Anger Templates Card (soft red)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Common Anger Scenarios 📌",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C0519),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(preloadedSituations) { item ->
                        SuggestionChip(
                            onClick = { situationInput = item },
                            label = { Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4C0519)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        )
                    }
                }
            }
        }

        // OUTPUT BATTLE PLAN STATE
        when (val currentState = state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE11D48))
                }
            }
            is UiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentState.message,
                        color = Color(0xFF4C0519),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is UiState.Success -> {
                val data = currentState.data
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // GURU'S BATTLE PLAN CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.List,
                                        contentDescription = "Plan",
                                        tint = Color(0xFFE11D48)
                                    )
                                    Text(
                                        text = "Guru's Battle Plan 📝",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF4C0519)
                                    )
                                }
                                Text(
                                    text = data.analysis,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // RESPONSES LIST HEADER
                        Text(
                            text = "Draft Messages to Send:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C0519),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // THE 3 RESPONSES
                        ResponseCard(
                            label = data.option1Label,
                            text = data.option1Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option1Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option2Label,
                            text = data.option2Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option2Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option3Label,
                            text = data.option3Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option3Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // QUICK ACTION BLACK BAR (Matches HTML capsule bottom bar)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B1B)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lock in this plan?",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = {
                                        viewModel.saveGem(
                                            SavedGem(
                                                type = "gussa",
                                                title = situationInput,
                                                analysis = data.analysis,
                                                option1Label = data.option1Label,
                                                option1Text = data.option1Text,
                                                option2Label = data.option2Label,
                                                option2Text = data.option2Text,
                                                option3Label = data.option3Label,
                                                option3Text = data.option3Text
                                            )
                                        )
                                        Toast.makeText(context, "Saved to Saved Gems! 💎", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Bookmark, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Plan", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------------
// CAPABILITY C: REPLY ANALYZER & MULTI-OPTION GENERATOR
// ---------------------------------------------------------------------------------
@Composable
fun ReplyDecoderView(viewModel: LoveGuruViewModel) {
    var messageInput by remember { mutableStateOf("") }
    val state by viewModel.replyState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val preloadedReplies = listOf(
        "Nothing, I'm fine.",
        "K.",
        "Do whatever you want.",
        "Who was that girl you were talking to?",
        "Yeah sure, enjoy."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MAIN DECODER BENTO CARD (Wide, White background, pink outline)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Forum,
                        contentDescription = "Forum Icon",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Reply Analyzer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF0F3), shape = RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE NOW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text(
                    text = "Copy-paste her text reply to analyze her actual mood, hidden meanings, and get 4 beautiful response variations.",
                    fontSize = 13.sp,
                    color = Color(0xFF4C0519).copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Paste her text reply here...", color = Color(0xFF4C0519).copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reply_analyzer_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE11D48),
                        unfocusedBorderColor = Color(0xFFFBCFE8),
                        focusedContainerColor = Color(0xFFFFF8F8),
                        unfocusedContainerColor = Color(0xFFFFF8F8),
                        focusedTextColor = Color(0xFF4C0519),
                        unfocusedTextColor = Color(0xFF4C0519)
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.ContentPaste, contentDescription = "Paste", tint = Color(0xFFE11D48))
                    },
                    trailingIcon = {
                        if (messageInput.isNotEmpty()) {
                            IconButton(onClick = { messageInput = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = Color(0xFFE11D48))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            viewModel.analyzeReply(messageInput)
                        } else {
                            Toast.makeText(context, "Please paste a message!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reply_solve_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Icon(imageVector = Icons.Filled.AutoFixHigh, contentDescription = "Magic", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Decode Vibes 🔍", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // BENTO COLUMN GRID: TWO SQUARE-ISH CARDS
        // Card A: Quick Translation Dictionary Card (soft pink)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Subtle Translation Guide 📖",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C0519),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dict = listOf(
                        "\"Nothing, I'm fine.\"" to "Translation: She's definitely NOT fine.",
                        "\"K.\"" to "Translation: Active danger zone. Fast damage control needed.",
                        "\"Do whatever you want.\"" to "Translation: IT'S A TRAP! Do NOT do it."
                    )
                    dict.forEach { (her, guru) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "•", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                            Column {
                                Text(text = her, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4C0519))
                                Text(text = guru, fontSize = 11.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Card B: Preloaded replies chips Card (soft red)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Classic Dry Texts 📌",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C0519),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(preloadedReplies) { item ->
                        SuggestionChip(
                            onClick = { messageInput = item },
                            label = { Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4C0519)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        )
                    }
                }
            }
        }

        // STATE OUTPUTS
        when (val currentState = state) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE11D48))
                }
            }
            is UiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentState.message,
                        color = Color(0xFF4C0519),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is UiState.Success -> {
                val data = currentState.data
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // VIBES DECODE CARD
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mood,
                                        contentDescription = "Vibes Decode",
                                        tint = Color(0xFFE11D48)
                                    )
                                    Text(
                                        text = "Guru's Vibes Decode 💖",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF4C0519)
                                    )
                                }
                                Text(
                                    text = data.analysis,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // VARIATIONS HEADER
                        Text(
                            text = "Guru's Reply Variations (Select Vibe):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C0519),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // THE 4 RESPONSES
                        ResponseCard(
                            label = data.option1Label,
                            text = data.option1Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option1Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option2Label,
                            text = data.option2Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option2Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option3Label,
                            text = data.option3Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option3Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ResponseCard(
                            label = data.option4Label,
                            text = data.option4Text,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(data.option4Text))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // QUICK ACTION BLACK BAR (Matches HTML capsule bottom bar)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B1B)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Keep these replies handy?",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = {
                                        viewModel.saveGem(
                                            SavedGem(
                                                type = "reply",
                                                title = messageInput,
                                                analysis = data.analysis,
                                                option1Label = data.option1Label,
                                                option1Text = data.option1Text,
                                                option2Label = data.option2Label,
                                                option2Text = data.option2Text,
                                                option3Label = data.option3Label,
                                                option3Text = data.option3Text,
                                                option4Label = data.option4Label,
                                                option4Text = data.option4Text
                                            )
                                        )
                                        Toast.makeText(context, "Saved to Saved Gems! 💎", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Bookmark, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Gems", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------------
// CAPABILITY D: SAVED GEMS VIEW
// ---------------------------------------------------------------------------------
@Composable
fun SavedGemsView(viewModel: LoveGuruViewModel, gems: List<SavedGem>) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER BENTO CARD (Wide, White background, pink outline)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Saved Icons",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Saved Gems & Advice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C0519)
                    )
                }

                Text(
                    text = "Your saved Guru wisdom, battle plans, and perfect responses in one secure place.",
                    fontSize = 13.sp,
                    color = Color(0xFF4C0519).copy(alpha = 0.7f)
                )
            }
        }

        if (gems.isEmpty()) {
            // EMPTY STATE BENTO CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.BookmarkBorder,
                        contentDescription = "Empty Bookmarks",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFE11D48).copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved gems yet!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF4C0519)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Once you solve a tricky trap question, regulate a gussa scenario, or decode a dry message, click 'Save' to see them here.",
                        fontSize = 12.sp,
                        color = Color(0xFF4C0519).copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(gems) { gem ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Dynamic Category Icon with beautiful Bento styling
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (gem.type) {
                                                "tricky" -> Color(0xFFFEE2E2)
                                                "gussa" -> Color(0xFFFFF0F3)
                                                else -> Color(0xFFFFF8F8)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (gem.type) {
                                            "tricky" -> Icons.Filled.AutoAwesome
                                            "gussa" -> Icons.Filled.Shield
                                            else -> Icons.Filled.Forum
                                        },
                                        contentDescription = gem.type,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = when (gem.type) {
                                            "tricky" -> "TRICKY QUESTION SOLUTION"
                                            "gussa" -> "GUSSA BATTLE PLAN"
                                            else -> "VIBE DECODING"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE11D48),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = gem.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4C0519),
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteGem(gem.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFE11D48).copy(alpha = 0.8f)
                                    )
                                }
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFFFBCFE8).copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Psycho Check / Analysis
                                Text(
                                    text = "Guru's Analysis:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gem.analysis,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519).copy(alpha = 0.8f),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Saved Responses:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C0519),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                ResponseCard(
                                    label = gem.option1Label,
                                    text = gem.option1Text,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(gem.option1Text))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                ResponseCard(
                                    label = gem.option2Label,
                                    text = gem.option2Text,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(gem.option2Text))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                ResponseCard(
                                    label = gem.option3Label,
                                    text = gem.option3Text,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(gem.option3Text))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                if (gem.option4Label != null && gem.option4Text != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ResponseCard(
                                        label = gem.option4Label,
                                        text = gem.option4Text,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(gem.option4Text))
                                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to view Guru's solutions & advice...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4C0519).copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SHARED CUSTOM UI COMPONENTS
// ---------------------------------------------------------------------------------
@Composable
fun LoveGuruHeroBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_love_guru_1783975857545),
                contentDescription = "Love Guru Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Transparent overlay for cute caption
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "Relation-chips are tough.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Let Love Guru handle the dip! 🌸",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ResponseCard(label: String, text: String, onCopy: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFE11D48)
                )

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy message",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = Color(0xFF4C0519),
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


