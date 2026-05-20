package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.viewmodel.*
import com.example.ui.theme.*
import java.util.Date
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- CUSTOM DRAWN SYSTEM SYMBOLS FOR Cyberpunk Vibe ---
@Composable
fun CyberIcon(
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint)
    )
}

// --- CORE RESPONSIVE CHAT SCREEN ---
@Composable
fun MainResponsiveChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val activeChat by viewModel.activeChat.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val callState by viewModel.callState.collectAsState()

    // Modals
    var showProfileModal by remember { mutableStateOf(false) }
    var showGroupModal by remember { mutableStateOf(false) }
    var selectedStory by remember { mutableStateOf<StatusStory?>(null) }

    val rootBgColor = if (isDarkMode) SleekBgDark else SleekBgLight
    val glowColor1 = if (isDarkMode) SleekPrimary.copy(alpha = 0.15f) else SleekPrimary.copy(alpha = 0.08f)
    val glowColor2 = if (isDarkMode) SleekSecondary.copy(alpha = 0.15f) else SleekSecondary.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(rootBgColor)
            .drawBehind {
                // Create subtle radial ambient sleek glows in corners
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor1, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.8f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor2, Color.Transparent),
                        center = Offset(size.width, size.height),
                        radius = size.width * 0.8f
                    )
                )
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth > 650.dp

            if (isWide) {
                // Wide Landscape Split view
                Row(modifier = Modifier.fillMaxSize()) {
                    ChatListScreen(
                        viewModel = viewModel,
                        isWide = true,
                        onOpenProfile = { showProfileModal = true },
                        onOpenGroup = { showGroupModal = true },
                        onOpenStory = { selectedStory = it },
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    )
                    VerticalDivider(color = if (isDarkMode) Color(0xFF202330) else Color(0xFFE5E7EB))
                    Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        if (activeChat != null) {
                            ChatDetailScreen(
                                viewModel = viewModel,
                                isWide = true
                            )
                        } else {
                            // Beautiful glass empty state
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkMode) Color(0x1000E5FF) else Color(0x0A000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "No active chat",
                                        tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF8C52FF),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ready to start talking?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isDarkMode) Color(0xFFE2E4EB) else Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select a contact or open our Gemini AI assistant\nto experience real-time smart replies.",
                                    color = if (isDarkMode) Color(0xFF9095A6) else Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Compact Single-Pane Switch view
                AnimatedContent(
                    targetState = activeChat,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "PaneNavigation"
                ) { targetChat ->
                    if (targetChat == null) {
                        ChatListScreen(
                            viewModel = viewModel,
                            isWide = false,
                            onOpenProfile = { showProfileModal = true },
                            onOpenGroup = { showGroupModal = true },
                            onOpenStory = { selectedStory = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ChatDetailScreen(
                            viewModel = viewModel,
                            isWide = false
                        )
                    }
                }
            }
        }

        // Calling Overlay (Takes priority over screen)
        if (callState != null) {
            CallScreenOverlay(
                viewModel = viewModel,
                callState = callState!!,
                isDarkMode = isDarkMode
            )
        }

        // Story Viewer Modal
        if (selectedStory != null) {
            StoryViewerOverlay(
                story = selectedStory!!,
                onDismiss = { selectedStory = null },
                isDarkMode = isDarkMode
            )
        }

        // Profile dialog
        if (showProfileModal) {
            ProfileSettingsDialog(
                viewModel = viewModel,
                onDismiss = { showProfileModal = false },
                isDarkMode = isDarkMode
            )
        }

        // Create Group dialog
        if (showGroupModal) {
            CreateGroupDialog(
                viewModel = viewModel,
                onDismiss = { showGroupModal = false },
                isDarkMode = isDarkMode
            )
        }
    }
}

// --- CHAT LIST SCREEN PANEL ---
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    isWide: Boolean,
    onOpenProfile: () -> Unit,
    onOpenGroup: () -> Unit,
    onOpenStory: (StatusStory) -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.chats.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val searchQuery by viewModel.searchChatQuery.collectAsState()

    // Filtered chats based on search text
    val filteredChats = chats.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.messages.any { m -> m.text.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // App Launcher & Action Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekPrimary, SleekSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TeleChat AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkMode) SleekTextDark else SleekTextLight
                    )
                    Text(
                        text = "Realtime Sync Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) SleekSecondary else SleekPrimary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Light/Dark Toggle
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("theme_toggle_btn")
                ) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }

                // Profile Configuration Trigger
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) SleekSurfaceDark else SleekContainerLight)
                        .clickable { onOpenProfile() }
                        .testTag("profile_settings_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = profile.avatarEmoji, fontSize = 18.sp)
                }
            }
        }

        // Search text-field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("chat_search_input"),
            placeholder = { Text("Search chats, friends or keywords...", fontSize = 13.sp, color = if (isDarkMode) SleekMutedDark else SleekMutedLight) },
            leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = if (isDarkMode) SleekMutedDark else SleekMutedLight) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Refresh, "Clear Info", tint = if (isDarkMode) SleekMutedDark else SleekMutedLight)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkMode) SleekSurfaceDark else SleekSurfaceLight,
                unfocusedContainerColor = if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.5f) else SleekSurfaceLight.copy(alpha = 0.5f),
                focusedBorderColor = if (isDarkMode) SleekSecondary else SleekPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = if (isDarkMode) SleekTextDark else SleekTextLight,
                unfocusedTextColor = if (isDarkMode) SleekTextDark else SleekTextLight
            )
        )

        // Stories Header & Horizontal List row
        Text(
            text = "Active Stories",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color(0xFF9095A6) else Color(0xFF4B5563),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // My Story Trigger
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenGroup() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkMode) SleekSurfaceDark else SleekContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Add Story", tint = if (isDarkMode) SleekSecondary else SleekPrimary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New Group",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) SleekMutedDark else SleekMutedLight
                    )
                }
            }

            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenStory(story) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(SleekPrimary, SleekSecondary)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(3.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkMode) SleekSurfaceDark else SleekSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = story.userAvatar, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.userName.take(8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) SleekTextDark else SleekTextLight,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chats Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Conversations",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFF9095A6) else Color(0xFF4B5563)
            )
            if (filteredChats.size != chats.size) {
                Text(
                    text = "Matching ${filteredChats.size}",
                    fontSize = 11.sp,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Lazy lists of chat items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Sort by pinned, then by last message timestamp
            val sortedChats = filteredChats.sortedWith(
                compareByDescending<Chat> { it.isPinned }
                    .thenByDescending { it.lastMessage?.timestamp ?: 0L }
            )

            if (sortedChats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No chats found",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(sortedChats) { chat ->
                val isSelected = viewModel.activeChat.collectAsState().value?.id == chat.id
                ChatCardItem(
                    chat = chat,
                    isSelected = isSelected,
                    isDarkMode = isDarkMode,
                    onSelect = { viewModel.selectChat(chat.id) },
                    onPinToggle = { viewModel.pinChat(chat.id) }
                )
            }
        }
        
        // System status padding navigation bar spacer
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

// --- CHAT CARD COMPONENT ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatCardItem(
    chat: Chat,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onSelect: () -> Unit,
    onPinToggle: () -> Unit
) {
    val lastMsg = chat.lastMessage
    val formattedTime = lastMsg?.timestamp?.let { formatShortTime(it) } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onPinToggle
            )
            .testTag("chat_card_${chat.id}"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> if (isDarkMode) SleekSurfaceDark else SleekContainerLight
                chat.isPinned -> if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.5f) else SleekContainerLight.copy(alpha = 0.35f)
                else -> if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.2f) else Color.White
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isSelected -> if (isDarkMode) SleekSecondary else SleekPrimary
                else -> if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.5f) else SleekContainerLight.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) SleekSurfaceDark else SleekSurfaceLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = chat.avatarEmoji, fontSize = 22.sp)
                // If Online, present a cyan badge status sphere
                if (chat.id != "c2" && chat.id != "c_group_mock") { // simulated offline elements
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) SleekSecondary else SleekPrimary)
                            .align(Alignment.BottomEnd)
                            .border(1.5.dp, if (isDarkMode) SleekBgDark else Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text section
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDarkMode) SleekTextDark else SleekTextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = if (isDarkMode) SleekMutedDark else SleekMutedLight
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            lastMsg == null -> "No messages yet."
                            lastMsg.isAudio -> "Voice Message (Duration ${lastMsg.audioDuration}s)"
                            lastMsg.attachmentPreset != null -> "Photo preset: ${lastMsg.attachmentPreset}"
                            else -> lastMsg.text
                        },
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) {
                            if (isDarkMode) SleekTextDark else SleekPrimary
                        } else {
                            if (isDarkMode) SleekMutedDark else SleekMutedLight
                        }
                    )

                    // Badges or pins row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chat.isPinned) {
                            Text(
                                text = "📌",
                                fontSize = 11.sp
                            )
                        }

                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkMode) SleekSecondary else SleekPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = if (isDarkMode) SleekContainerDark else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CHAT SANDBOX WINDOW / DETAIL SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: ChatViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier
) {
    val chat by viewModel.activeChat.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val smartReplies by viewModel.smartReplies.collectAsState()
    val summaryText by viewModel.chatSummary.collectAsState()
    val aiLoading by viewModel.isAILoading.collectAsState()
    val query by viewModel.searchMessageQuery.collectAsState()

    if (chat == null) return

    val messages = chat!!.messages
    val matchingMessages = if (query.isEmpty()) {
        messages
    } else {
        messages.filter { it.text.contains(query, ignoreCase = true) }
    }

    var selectedMessageForOption by remember { mutableStateOf<Message?>(null) }
    var selectedWallpaperMenu by remember { mutableStateOf(false) }
    var selectPresetAttachmentMenu by remember { mutableStateOf(false) }

    // Replying parameters
    var replyModelText by remember { mutableStateOf<String?>(null) }
    var userTypedInput by remember { mutableStateOf("") }

    // Voice record states
    var isRecordingActive by remember { mutableStateOf(false) }
    var secondsRecorded by remember { mutableStateOf(0) }

    // Sound effect wave timer
    LaunchedEffect(isRecordingActive) {
        if (isRecordingActive) {
            secondsRecorded = 0
            while (isRecordingActive) {
                delay(1000)
                secondsRecorded += 1
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (chat!!.wallpaperPreset) {
                    "gradient_blue" -> if (isDarkMode) Color(0xFF0F1524) else Color(0xFFE0F7FA)
                    "gradient_sunset" -> if (isDarkMode) Color(0xFF1E101C) else Color(0xFFFFF0F5)
                    "glass_dark" -> Color(0xFF06070B)
                    else -> if (isDarkMode) SleekBgDark else SleekBgLight
                }
            )
            .drawBehind {
                // Cool tech grids or background styling based on presets
                if (chat!!.wallpaperPreset == "gradient_blue") {
                    drawLine(
                        color = SleekSecondary.copy(alpha = 0.15f),
                        start = Offset(0f, size.height * 0.3f),
                        end = Offset(size.width, size.height * 0.3f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
    ) {
        // Chat Window Top Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.9f) else SleekSurfaceLight.copy(alpha = 0.9f),
                titleContentColor = if (isDarkMode) SleekTextDark else SleekTextLight
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Simulated incoming call directly from this contact!
                        viewModel.receiveIncomingSimulatedCall(chat!!.name, chat!!.avatarEmoji, false)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) SleekSurfaceDark else SleekContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = chat!!.avatarEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = chat!!.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDarkMode) SleekTextDark else SleekTextLight
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (aiLoading) SleekPrimary else Color(0xFF4CAF50)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (aiLoading) "Typing..." else "Online",
                                fontSize = 11.sp,
                                color = if (isDarkMode) SleekMutedDark else SleekMutedLight
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                if (!isWide) {
                    IconButton(
                        onClick = { viewModel.selectChat("") },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDarkMode) Color.White else Color(0xFF1F2937)
                        )
                    }
                }
            },
            actions = {
                // Voice Call Launcher
                IconButton(onClick = { viewModel.initiateCall(chat!!.name, chat!!.avatarEmoji, false) }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = if (isDarkMode) SleekSecondary else SleekPrimary
                    )
                }

                // Video Call Launcher (custom icon matching core)
                IconButton(onClick = { viewModel.initiateCall(chat!!.name, chat!!.avatarEmoji, true) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Video Call",
                        tint = if (isDarkMode) SleekSecondary else SleekPrimary
                    )
                }

                // Summarize Capsule option
                IconButton(onClick = { viewModel.requestAIChatSummary() }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Summarize",
                        tint = if (isDarkMode) Color(0xFFFF2E93) else Color(0xFF8C52FF)
                    )
                }

                // Wallpaper & configuration details Menu
                IconButton(onClick = { selectedWallpaperMenu = !selectedWallpaperMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Wallpaper",
                        tint = Color.Gray
                    )
                }

                // Dropdowns
                DropdownMenu(
                    expanded = selectedWallpaperMenu,
                    onDismissRequest = { selectedWallpaperMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cyber Obsidian Wallpaper") },
                        onClick = {
                            viewModel.changeWallpaper(chat!!.id, "glass_dark")
                            selectedWallpaperMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cosmic Blue Grid") },
                        onClick = {
                            viewModel.changeWallpaper(chat!!.id, "gradient_blue")
                            selectedWallpaperMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sunset Pink Glow") },
                        onClick = {
                            viewModel.changeWallpaper(chat!!.id, "gradient_sunset")
                            selectedWallpaperMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Solid Default") },
                        onClick = {
                            viewModel.changeWallpaper(chat!!.id, "solid")
                            selectedWallpaperMenu = false
                        }
                    )
                }
            }
        )

        // Floating AI Summary Presentation
        AnimatedVisibility(
            visible = summaryText != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("ai_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xE61E2130) else Color(0xFFE2E8F0)
                ),
                border = BorderStroke(1.dp, Color(0xFFFF2E93))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔥", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TeleChat AI Summarizer",
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color(0xFF1F2937),
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearChatSummary() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Close Summary", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = summaryText ?: "",
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color(0xFFE2E4EB) else Color(0xFF374151),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "End of generated brief.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Inside-chat Search Filter Indicator
        if (query.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF2E93).copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtering search messages: \"$query\"",
                    color = if (isDarkMode) Color(0xFFFF2E93) else Color(0xFF851F4E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { viewModel.updateMessageSearchQuery("") }, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Refresh, "Clear message search filter", tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Active chat bubble list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(matchingMessages) { msg ->
                ChatBubble(
                    message = msg,
                    isDarkMode = isDarkMode,
                    onReact = { emoji -> viewModel.addReaction(chat!!.id, msg.id, emoji) },
                    onTranslate = { lang -> viewModel.translateMessageInPlace(msg, lang) },
                    onDelete = { viewModel.deleteMessage(chat!!.id, msg.id) },
                    onEdit = { newTxt -> viewModel.editMessage(chat!!.id, msg.id, newTxt) },
                    onReplyTrigger = { replyModelText = msg.text }
                )
            }

            if (aiLoading) {
                item {
                    SimulatedAgentTypingBubble(chatName = chat!!.name, isDarkMode = isDarkMode)
                }
            }
        }

        // Replying Header Banner
        AnimatedVisibility(
            visible = replyModelText != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDarkMode) Color(0xFF282B3D) else Color(0xFFE2E8F0))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Replying directly to active thread:",
                        fontSize = 10.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = replyModelText ?: "",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isDarkMode) Color.White else Color(0xFF1F2937)
                    )
                }
                IconButton(onClick = { replyModelText = null }) {
                    Icon(Icons.Default.Refresh, "Clear Reply thread", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Chat sandbox search message box (Collapsible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Query filter",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateMessageSearchQuery(it) },
                textStyle = TextStyle(
                    color = if (isDarkMode) Color.White else Color.Black,
                    fontSize = 11.sp
                ),
                placeholder = { Text("Search keywords inside this conversation...", color = Color.Gray, fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                maxLines = 1
            )
        }

        // Smart replies capsules suggested by Gemini
        if (smartReplies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                smartReplies.forEach { replyOption ->
                    Card(
                        modifier = Modifier
                            .clickable {
                                viewModel.sendMessage(replyOption, replyToText = replyModelText)
                                replyModelText = null
                            }
                            .testTag("smart_reply_pill"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF282B3D) else Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = replyOption,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF8C52FF),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom Input Tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Menu Picker Box
            IconButton(
                onClick = { selectPresetAttachmentMenu = !selectPresetAttachmentMenu },
                modifier = Modifier.testTag("attach_file_btn")
            ) {
                Text(text = "📎", fontSize = 20.sp)
            }

            DropdownMenu(
                expanded = selectPresetAttachmentMenu,
                onDismissRequest = { selectPresetAttachmentMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("🌆 Attach Beach Sunset photo") },
                    onClick = {
                        viewModel.requestAIPresetImageImageCaptioning("Sunset Beach")
                        selectPresetAttachmentMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("👾 Attach Cyberpunk workspace photo") },
                    onClick = {
                        viewModel.requestAIPresetImageImageCaptioning("Neon Desk Cyberpunk Console")
                        selectPresetAttachmentMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("🐱 Attach Cute Stray Cat photo") },
                    onClick = {
                        viewModel.requestAIPresetImageImageCaptioning("Cute fluffy sleeping kitten")
                        selectPresetAttachmentMenu = false
                    }
                )
            }

            AnimatedVisibility(
                visible = isRecordingActive,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDarkMode) Color(0x33FF2E93) else Color(0x11FF2E93))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording: $secondsRecorded s",
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simulated Microphone audio soundwave lines!
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(Random.nextInt(5, 25).dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(if (isDarkMode) SleekSecondary else SleekPrimary)
                            )
                        }
                    }
                }
            }

            if (!isRecordingActive) {
                // Interactive Emoji picker shortcut
                IconButton(onClick = { userTypedInput += "😅" }) {
                    Text(text = "😀", fontSize = 20.sp)
                }

                // Styled Glass Input Field
                OutlinedTextField(
                    value = userTypedInput,
                    onValueChange = { userTypedInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = { Text("Type message securely encrypted...", fontSize = 13.sp, color = if (isDarkMode) SleekMutedDark else SleekMutedLight) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkMode) SleekSurfaceDark else Color.White,
                        unfocusedContainerColor = if (isDarkMode) SleekSurfaceDark.copy(alpha = 0.5f) else SleekSurfaceLight,
                        focusedBorderColor = if (isDarkMode) SleekSecondary else SleekPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = if (isDarkMode) SleekTextDark else SleekTextLight,
                        unfocusedTextColor = if (isDarkMode) SleekTextDark else SleekTextLight
                    ),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        // Quick send key inside field
                        if (userTypedInput.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(userTypedInput, replyToText = replyModelText)
                                    userTypedInput = ""
                                    replyModelText = null
                                },
                                modifier = Modifier.testTag("send_msg_btn")
                            ) {
                                Icon(Icons.Default.Send, "Send", tint = if (isDarkMode) SleekSecondary else SleekPrimary)
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Mic / Audio record toggle
            IconButton(
                onClick = {
                    if (isRecordingActive) {
                        // Stop recording and send audio message preset!
                        isRecordingActive = false
                        viewModel.sendMessage("", isAudio = true, audioDuration = secondsRecorded)
                    } else {
                        isRecordingActive = true
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isRecordingActive) Color.Red else (if (isDarkMode) Color(0xFF1E2130) else Color(0xFFE5E7EB))
                    )
                    .testTag("voice_record_btn")
            ) {
                Text(
                    text = if (isRecordingActive) "🛑" else "🎤",
                    fontSize = 18.sp
                )
            }
        }
    }
}

// --- SIMULATED CHAT BUBBLE BINDER SCREEN ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    isDarkMode: Boolean,
    onReact: (String) -> Unit,
    onTranslate: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit,
    onReplyTrigger: () -> Unit
) {
    var expandedBubbleMenu by remember { mutableStateOf(false) }
    var expandedReactRow by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val formattedTime = formatShortTime(message.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        // Replied segment
        if (message.repliedMessageText != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (isDarkMode) Color(0xFF191B24) else Color(0xFFE2E8F0))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↪ " + message.repliedMessageText,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Long Press action card wrapper
            Card(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(
                        onClick = { expandedBubbleMenu = true },
                        onLongClick = { expandedReactRow = !expandedReactRow }
                    )
                    .testTag("msg_bubble_${message.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        message.isModerated -> Color(0x4DFF0000)
                        message.isMe -> if (isDarkMode) Color(0xFF2E2442) else SleekContainerLight
                        else -> if (isDarkMode) SleekSurfaceDark else SleekSurfaceLight
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isMe) 16.dp else 2.dp,
                    bottomEnd = if (message.isMe) 2.dp else 16.dp
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        message.isModerated -> Color.Red
                        else -> if (isDarkMode) SleekSecondary.copy(alpha = 0.2f) else SleekPrimary.copy(alpha = 0.15f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Attachment preset viewer if any
                    if (message.attachmentPreset != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (message.attachmentPreset) {
                                    "Sunset Beach" -> "🌆 Beach Sunset Preset"
                                    "Neon Desk Cyberpunk Console" -> "👾 Cyberpunk Workspace Preset"
                                    else -> "🐱 Tiny Cute Kitten Preset"
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Message audio recorder template if isAudio
                    if (message.isAudio) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { /* Simulated Audio play */ }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.PlayArrow, "Play Audio", tint = if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF8C52FF))
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            // Interactive waveform bars
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                Alignment.CenterVertically
                            ) {
                                repeat(10) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(Random.nextInt(8, 24).dp)
                                            .background(if (isDarkMode) Color(0xFF00E5FF) else Color(0xFF8C52FF))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Played: ${message.audioDuration}s",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        // Standard text description
                        Text(
                            text = message.text,
                            fontSize = 13.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF1F2937),
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Footnotes containing formatting time and delivery ticks!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.End
                        )
                        if (message.isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (message.status) {
                                    MessageStatus.SENDING -> "⏳"
                                    MessageStatus.SENT -> "✓"
                                    MessageStatus.DELIVERED -> "✓✓"
                                    MessageStatus.SEEN -> "👀"
                                },
                                fontSize = 9.sp,
                                color = if (message.status == MessageStatus.SEEN) Color(0xFF00E5FF) else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Reactions Drawer inline
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color(0xFF1C2035) else Color(0xFFECEFF1))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { (emoji, count) ->
                    Text(text = "$emoji $count", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Toggled Reactions Panel
        AnimatedVisibility(
            visible = expandedReactRow,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF1C1D29) else Color(0xFFCBD5E1))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emojis = listOf("❤️", "😂", "👍", "🔥", "🎉", "😢")
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable {
                                onReact(emoji)
                                expandedReactRow = false
                            }
                            .padding(2.dp)
                    )
                }
            }
        }

        // Actions Dialog options list
        DropdownMenu(
            expanded = expandedBubbleMenu,
            onDismissRequest = { expandedBubbleMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Reply to message") },
                onClick = {
                    onReplyTrigger()
                    expandedBubbleMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Translate to English (AI)") },
                onClick = {
                    onTranslate("English")
                    expandedBubbleMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Translate to Spanish (AI)") },
                onClick = {
                    onTranslate("Spanish")
                    expandedBubbleMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Translate to French (AI)") },
                onClick = {
                    onTranslate("French")
                    expandedBubbleMenu = false
                }
            )
            if (message.isMe) {
                DropdownMenuItem(
                    text = { Text("Edit Message") },
                    onClick = {
                        showEditDialog = true
                        expandedBubbleMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Message") },
                    onClick = {
                        onDelete()
                        expandedBubbleMenu = false
                    }
                )
            }
        }
    }

    if (showEditDialog) {
        var textToEdit by remember { mutableStateOf(message.text) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = textToEdit,
                    onValueChange = { textToEdit = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(textToEdit)
                    showEditDialog = false
                }) {
                    Text("Save Content Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

// --- LIVE ACTIVE AGENT TYPING BUBBLE ---
@Composable
fun SimulatedAgentTypingBubble(chatName: String, isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotsMotion")
    val dotOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val dotOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    val dotOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E212D) else Color.White
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 2.dp),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF202330) else Color(0xFFECEFF1))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$chatName typing",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(y = dotOffset1.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(y = dotOffset2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(y = dotOffset3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
            }
        }
    }
}

// --- PREMIUM VOIP AUDIO & VIDEO CALLING SURFACE OVERLAY ---
@Composable
fun CallScreenOverlay(
    viewModel: ChatViewModel,
    callState: CallState,
    isDarkMode: Boolean
) {
    val muteMic by viewModel.isMuted.collectAsState()
    val speakerOn by viewModel.isSpeakerOn.collectAsState()

    // Pulse animation around avatar circles!
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSizeMultiplier by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlphaMultiplier by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080B))
            .drawBehind {
                // Glow call background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (callState.status == CallStatus.ACTIVE) Color(0xFF00F5D4).copy(alpha = 0.12f)
                            else Color(0xFFFF2E93).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.9f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Subdued type indicator
            Text(
                text = if (callState.isVideo) "⚡ VIDEO METAVERSE CALL ⚡" else "📞 SECURED END-TO-END AUDIO CALL",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (callState.status == CallStatus.ACTIVE) Color(0xFF00F5D4) else Color(0xFFFF2E93),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulse avatar frame
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Outer glow wave
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                color = if (callState.status == CallStatus.ACTIVE) Color(0xFF00F5D4).copy(alpha = 0.1f * pulseAlphaMultiplier)
                                else Color(0xFFFF2E93).copy(alpha = 0.1f * pulseAlphaMultiplier),
                                radius = (size.width / 2) * pulseSizeMultiplier
                            )
                        }
                )

                // Main circular card containing avatar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161822))
                        .border(
                            width = 2.dp,
                            color = if (callState.status == CallStatus.ACTIVE) Color(0xFF00F5D4) else Color(0xFFFF2E93),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = callState.avatarEmoji, fontSize = 52.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = callState.contactName,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Active Calling Timer format OR Ringing prompt
            Text(
                text = when (callState.status) {
                    CallStatus.RINGING -> if (callState.isIncoming) "Incoming secure request..." else "Ringing line..."
                    CallStatus.ACTIVE -> "Encrypted • " + formatDuration(callState.durationSeconds)
                    CallStatus.ENDED -> "Call Terminated"
                },
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            // Video Simulated feed space
            if (callState.isVideo && callState.status == CallStatus.ACTIVE) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp)
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171924)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷 Live Video Simulation Feed\nDevice Camera streaming securely...",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Interactive Buttons Drawer tray
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (callState.status == CallStatus.RINGING && callState.isIncoming) {
                    // Incoming call sliders: Accept or Decline!
                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Text(text = "❌", fontSize = 20.sp)
                    }

                    IconButton(
                        onClick = { viewModel.acceptIncomingCall() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F5D4))
                            .testTag("accept_call_btn")
                    ) {
                        Text(text = "📞", fontSize = 20.sp)
                    }
                } else {
                    // Active call sliders: Mute, Speaker, video display on/off & Decline!
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (muteMic) Color.Red else Color(0x35ECEFF1))
                    ) {
                        Text(text = if (muteMic) "🔇" else "🎙️")
                    }

                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .testTag("end_call_btn")
                    ) {
                        Text(text = "❌", fontSize = 24.sp)
                    }

                    IconButton(
                        onClick = { viewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (speakerOn) Color(0xFF00E5FF) else Color(0x35ECEFF1))
                    ) {
                        Text(text = if (speakerOn) "🔊" else "🔈")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- STORY PRESENTATION OVERLAY VIEWER ---
@Composable
fun StoryViewerOverlay(
    story: StatusStory,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    // Timer auto advance count
    var slideProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Increment progress over 4 seconds
        val step = 0.05f
        while (slideProgress < 1.0f) {
            delay(200)
            slideProgress += step
        }
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090A0E))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Linear slide progress bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { slideProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = Color(0xFF00E5FF),
                        trackColor = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Story Header Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF282B3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = story.userAvatar, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = story.userName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = story.timestamp,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Content visual placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            when (story.contentPreset) {
                                "sunset" -> Brush.verticalGradient(listOf(Color(0xFFFF5E62), Color(0xFFFF9966)))
                                "neon" -> Brush.verticalGradient(listOf(Color(0xFF130CB7), Color(0xFF52E5E7)))
                                "cats" -> Brush.verticalGradient(listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)))
                                else -> Brush.verticalGradient(listOf(Color(0xFF00F5D4), Color(0xFF8C52FF)))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Big emoji representation
                    Text(
                        text = when (story.contentPreset) {
                            "sunset" -> "🌆"
                            "neon" -> "👾"
                            "cats" -> "🐱"
                            else -> "🌌"
                        },
                        fontSize = 90.sp
                    )
                }

                // Story caption
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161822))
                        .padding(16.dp)
                ) {
                    Text(
                        text = story.caption,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("Close Story", color = Color.Black)
                    }
                }
            }
        }
    }
}

// --- PROFILE SETTINGS MODIFIER DIALOG ---
@Composable
fun ProfileSettingsDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    val activeProfile by viewModel.userProfile.collectAsState()

    var customNameInput by remember { mutableStateOf(activeProfile.name) }
    var selectedEmoji by remember { mutableStateOf(activeProfile.avatarEmoji) }
    var customBioText by remember { mutableStateOf(activeProfile.statusText) }

    val emojisList = listOf("😎", "🤖", "🦄", "🌸", "🍕", "🔥", "🐱", "🌌", "💻", "✨")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF161822) else Color.White
            ),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF282B3D) else Color(0xFFECEFF1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Premium Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Display Select
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF232536) else Color(0xFFF3F4F6))
                        .border(1.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedEmoji, fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji list picker
                Text(text = "Choose your Avatar Character Emoji:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojisList) { itemEmoji ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(
                                    width = (if (selectedEmoji == itemEmoji) 2.dp else 0.dp),
                                    color = Color(0xFF00E5FF),
                                    shape = CircleShape
                                )
                                .clickable { selectedEmoji = itemEmoji }
                                .background(if (isDarkMode) Color(0xFF232536) else Color(0xFFECEFF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = itemEmoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input fields
                OutlinedTextField(
                    value = customNameInput,
                    onValueChange = { customNameInput = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customBioText,
                    onValueChange = { customBioText = it },
                    label = { Text("Custom Status Bio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss")
                    }
                    Button(
                        onClick = {
                            viewModel.updateProfile(customNameInput, selectedEmoji, customBioText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                    ) {
                        Text("Apply Rules", color = Color.White)
                    }
                }
            }
        }
    }
}

// --- CREATE GROUP SANDBOX DIALOG ---
@Composable
fun CreateGroupDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    var groupNameInput by remember { mutableStateOf("") }
    var selectedGroupEmoji by remember { mutableStateOf("🌌") }

    val candidates = listOf("Emma 🌸", "John 💻", "Alice ✨", "Bob 🚀")
    val selectedMembers = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF161822) else Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Establish secure group chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = { groupNameInput = it },
                    label = { Text("Group Name input") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pick active members code indices:",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                candidates.forEach { candidate ->
                    val isChecked = selectedMembers.contains(candidate)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedMembers.remove(candidate)
                                else selectedMembers.add(candidate)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) selectedMembers.add(candidate)
                                else selectedMembers.remove(candidate)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = candidate,
                            color = if (isDarkMode) Color.White else Color(0xFF1F2937),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Dismiss")
                    }
                    Button(
                        onClick = {
                            if (groupNameInput.isNotEmpty()) {
                                viewModel.createGroupChat(groupNameInput, selectedGroupEmoji, selectedMembers.toList())
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("Generate Group", color = Color.Black)
                    }
                }
            }
        }
    }
}

// --- UTILITY DATE FORMATTING ---
private fun formatShortTime(timestamp: Long): String {
    val date = Date(timestamp)
    val cal = Calendar.getInstance()
    cal.time = date
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    return String.format(Locale.ROOT, "%02d:%02d", hour, min)
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.ROOT, "%02d:%02d", mins, secs)
}
