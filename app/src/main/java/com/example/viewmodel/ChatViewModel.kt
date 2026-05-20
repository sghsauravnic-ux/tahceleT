package com.example.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {
    private val geminiRepo = GeminiRepository()

    // Configuration / Theme State
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode = _isDarkMode.asStateFlow()

    // Profile State
    private val _userProfile = MutableStateFlow(
        Contact("me", "Saurav", "😎", true, "Online", false, "Coding away with Gemini AI!")
    )
    val userProfile = _userProfile.asStateFlow()

    // Searching and filtering state
    private val _searchChatQuery = MutableStateFlow("")
    val searchChatQuery = _searchChatQuery.asStateFlow()

    private val _searchMessageQuery = MutableStateFlow("")
    val searchMessageQuery = _searchMessageQuery.asStateFlow()

    // Calls state
    private val _callState = MutableStateFlow<CallState?>(null)
    val callState = _callState.asStateFlow()
    private var callTimerJob: Job? = null

    // Stories state
    private val _stories = MutableStateFlow<List<StatusStory>>(emptyList())
    val stories = _stories.asStateFlow()

    // Smart Suggestion states
    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies = _smartReplies.asStateFlow()

    // Main Chats State
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    // Selected Chat State
    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeChat = _activeChat.asStateFlow()

    // Floating UI States
    private val _chatSummary = MutableStateFlow<String?>(null)
    val chatSummary = _chatSummary.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading = _isAILoading.asStateFlow()

    // Interactive Call sounds & Ring simulation
    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()
    
    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn = _isSpeakerOn.asStateFlow()

    init {
        loadInitialData()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateProfile(name: String, emoji: String, statusText: String) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            avatarEmoji = emoji,
            statusText = statusText
        )
    }

    fun updateSearchQuery(query: String) {
        _searchChatQuery.value = query
    }

    fun updateMessageSearchQuery(query: String) {
        _searchMessageQuery.value = query
    }

    fun clearChatSummary() {
        _chatSummary.value = null
    }

    private fun loadInitialData() {
        // Feed initial stories
        _stories.value = listOf(
            StatusStory("s1", "Emma 🌸", "🌸", "sunset", "Chasing sunsets! What a beautiful beach.", "2 hrs ago"),
            StatusStory("s2", "John 💻", "💻", "neon", "Late night coding session. Cyber vibes only.", "4 hrs ago"),
            StatusStory("s3", "Alice ✨", "✨", "cats", "Met this friendly stray outside. Say hi!", "5 hrs ago"),
            StatusStory("s4", "Crypto Space", "🌌", "city", "Tokyo Skyline from the hotel. Stunning structure.", "10 hrs ago")
        )

        // Seed structured initial chats
        val defaultTime = System.currentTimeMillis()

        val emmaMessages = listOf(
            Message("m1_1", "emma", "Emma 🌸", "Hey Saurav! Are we still on for coffee this weekend?", defaultTime - 3600000 * 2, false, MessageStatus.SEEN),
            Message("m1_2", "me", "Saurav", "Hey Emma! Yes, absolutely. Saturday afternoon works perfectly.", defaultTime - 3600000, true, MessageStatus.SEEN),
            Message("m1_3", "emma", "Emma 🌸", "Yay! Let's try that new cyber-punk themed cafe downtown. 🦄", defaultTime - 1800000, false, MessageStatus.SEEN)
        )

        val johnMessages = listOf(
            Message("m2_1", "john", "John 💻", "Hey Saurav, did you verify the room database migration? Compiler is acting weird.", defaultTime - 3600000 * 4, false, MessageStatus.SEEN),
            Message("m2_2", "me", "Saurav", "Yeah, wait. It looked fine under standard unit tests. Let me push my branch.", defaultTime - 3600000 * 3, true, MessageStatus.SEEN),
            Message("m2_3", "john", "John 💻", "Sweet, let me know when it is merged. I will sync and re-run local tests.", defaultTime - 3600000 * 2, false, MessageStatus.SEEN)
        )

        val aiMessages = listOf(
            Message("m3_1", "me", "Saurav", "Can you explain what Jetpack Compose edge to edge achieves?", defaultTime - 3600000, true, MessageStatus.SEEN),
            Message("m3_2", "gemini", "Gemini AI", "Edge-to-edge layout allows your app's content to fill the entire physical screen, drawing seamlessly behind the status bar and navigation gestures. You adjust insets dynamically to prevent critical buttons from being clipped by camera notches or system swipes. This makes your designs look integrated, fluid, and hyper-modern!", defaultTime - 1700000, false, MessageStatus.SEEN)
        )

        val groupMessages = listOf(
            Message("m4_1", "alice", "Alice ", "Check out my new neon rendering design!", defaultTime - 3600000 * 8, false, MessageStatus.SEEN),
            Message("m4_2", "john", "John 💻", "Looks clean. Loving the glassmorphic opacity layer there.", defaultTime - 3600000 * 7, false, MessageStatus.SEEN),
            Message("m4_3", "emma", "Emma 🌸", "So aesthetic! Does it scale nicely to tall mobile aspect ratios?", defaultTime - 3600000 * 6, false, MessageStatus.SEEN)
        )

        _chats.value = listOf(
            Chat("c1", "Emma 🌸", "🌸", isGroup = false, unreadCount = 0, isPinned = true, wallpaperPreset = "gradient_sunset", messages = emmaMessages),
            Chat("c2", "John 💻", "💻", isGroup = false, unreadCount = 1, isPinned = false, wallpaperPreset = "glass_dark", messages = johnMessages),
            Chat("c3", "Gemini AI Assistant 🤖", "🤖", isGroup = false, unreadCount = 0, isPinned = true, wallpaperPreset = "gradient_blue", messages = aiMessages),
            Chat("c4", "Dev Space & Coffee 🌌", "🌌", isGroup = true, unreadCount = 0, isPinned = false, wallpaperPreset = "solid", messages = groupMessages)
        )
    }

    fun selectChat(chatId: String) {
        val chat = _chats.value.find { it.id == chatId }
        _activeChat.value = chat
        
        // Mark all messages as SEEN
        if (chat != null) {
            val updatedMessages = chat.messages.map { it.copy(status = MessageStatus.SEEN) }
            val updatedChat = chat.copy(messages = updatedMessages, unreadCount = 0)
            _chats.value = _chats.value.map { if (it.id == chatId) updatedChat else it }
            _activeChat.value = updatedChat
            
            // Regenerate AI smart replies based on last message from other sender
            fetchSmartReplies(updatedChat)
        } else {
            _smartReplies.value = emptyList()
        }
    }

    private fun fetchSmartReplies(chat: Chat) {
        // If the last message is from me, or the list is empty, don't generate, or generate on last other message
        val lastOther = chat.messages.lastOrNull { !it.isMe } ?: return
        viewModelScope.launch {
            val context = chat.messages.takeLast(4).joinToString("\n") { "${it.senderName}: ${it.text}" }
            val replies = geminiRepo.generateSmartReplies(context)
            _smartReplies.value = replies
        }
    }

    // Auto-Moderation check: Simple offline checking and dynamic prompt refinement
    private fun isContentAbusive(text: String): Boolean {
        val badWords = listOf("hate you", "kill", "stupid", "idiot", "dumb", "jerk", "abusive", "ugly", "trash")
        return badWords.any { text.lowercase(Locale.ROOT).contains(it) }
    }

    fun pinChat(chatId: String) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                val updatedMessages = chat.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(text = "edited: $newText") else msg
                }
                chat.copy(messages = updatedMessages)
            } else chat
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                val updatedMessages = chat.messages.filter { it.id != messageId }
                chat.copy(messages = updatedMessages)
            } else chat
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    fun addReaction(chatId: String, messageId: String, emoji: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                val updatedMessages = chat.messages.map { msg ->
                    if (msg.id == messageId) {
                        val currentCount = msg.reactions[emoji] ?: 0
                        val updatedReactions = msg.reactions.toMutableMap()
                        if (currentCount > 0) {
                            updatedReactions.remove(emoji) // Toggle off
                        } else {
                            updatedReactions[emoji] = 1
                        }
                        msg.copy(reactions = updatedReactions)
                    } else msg
                }
                chat.copy(messages = updatedMessages)
            } else chat
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    fun changeWallpaper(chatId: String, preset: String?) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(wallpaperPreset = preset) else it
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    fun createGroupChat(name: String, emoji: String, memberNames: List<String>) {
        val welcomeMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = "system",
            senderName = "System",
            text = "Group created with members: Saurav, " + memberNames.joinToString(", ") + ". Say hello!",
            timestamp = System.currentTimeMillis(),
            isMe = false,
            status = MessageStatus.SEEN
        )
        val newChat = Chat(
            id = "c_group_${System.currentTimeMillis()}",
            name = "$name $emoji",
            avatarEmoji = emoji,
            isGroup = true,
            messages = listOf(welcomeMessage)
        )
        _chats.value = listOf(newChat) + _chats.value
        selectChat(newChat.id)
    }

    fun sendMessage(text: String, replyToText: String? = null, attachmentPreset: String? = null, isAudio: Boolean = false, audioDuration: Int = 0) {
        val activeChatVal = _activeChat.value ?: return
        val textValue = text.trim()
        if (textValue.isEmpty() && attachmentPreset == null && !isAudio) return

        _smartReplies.value = emptyList() // clear current smart replies quickly

        // Run auto moderation logic
        val flaggedByAI = isContentAbusive(textValue)
        val finalMessageText = if (flaggedByAI) {
            " [Content Moderated by TeleAI - Warning: Please follow community guidelines. Abusive language detected]"
        } else {
            textValue
        }

        val myMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = "me",
            senderName = "Saurav",
            text = finalMessageText,
            timestamp = System.currentTimeMillis(),
            isMe = true,
            status = MessageStatus.SENDING,
            attachmentPreset = attachmentPreset,
            isAudio = isAudio,
            audioDuration = audioDuration,
            isModerated = flaggedByAI,
            repliedMessageText = replyToText
        )

        // Add my message to chat and update list
        val updatedMessages = activeChatVal.messages + myMessage
        val updatedChat = activeChatVal.copy(messages = updatedMessages)
        _chats.value = _chats.value.map { if (it.id == activeChatVal.id) updatedChat else it }
        _activeChat.value = updatedChat

        // Simulate delivery delay
        viewModelScope.launch {
            delay(400)
            markMyMessageDelivered(activeChatVal.id, myMessage.id)

            // If it is moderate/flagged, contact won't reply
            if (flaggedByAI) return@launch

            // Trigger recipient typing indicator and delayed reply
            simulateAutomaticReply(activeChatVal, finalMessageText, attachmentPreset)
        }
    }

    private fun markMyMessageDelivered(chatId: String, messageId: String) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                val updated = chat.messages.map { if (it.id == messageId) it.copy(status = MessageStatus.DELIVERED) else it }
                chat.copy(messages = updated)
            } else chat
        }
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _chats.value.find { it.id == chatId }
        }
    }

    // High fidelity simulated agents that talk using Gemini API
    private fun simulateAutomaticReply(chat: Chat, originalMsgText: String, fileAttachedPreset: String?) {
        val chatId = chat.id
        viewModelScope.launch {
            delay(800)
            // Trigger contact status -> "Typing..."
            setContactTyping(chatId, true)

            // Generate reply text via Gemini or fallback
            val isAiAssistant = (chatId == "c3")
            val systemPrompt = when (chatId) {
                "c1" -> "You are Emma, a bubbly, cheerful friend who uses custom emojis. Keep your response warm, caring, humorous, and very short (under 20 words). Act as if replying casually on Telegram."
                "c2" -> "You are John, a smart software engineer. Keep your response highly technical, pragmatic, slightly sarcastic but helpful, and very short (under 20 words). Act as if replying on Telegram."
                "c3" -> "You are Gemini AI, a helpful, futuristic chat assistant built inside TeleChat. Answer elegantly and format beautifully. Keep it clear, friendly, and under 50 words."
                else -> "You are a casual chat participant in a tech dev group. Keep it funny, enthusiastic, and under 15 words."
            }

            var promptText = originalMsgText
            if (fileAttachedPreset != null) {
                promptText = "I have uploaded an image preset of $fileAttachedPreset. Please generate an awesome, funny, creative reply specifically captioning and talking about this image."
            }

            _isAILoading.value = true
            val replyText = geminiRepo.generateReply(promptText, systemPrompt)
            _isAILoading.value = false

            delay(1000) // typing pause
            setContactTyping(chatId, false)

            val replyMsg = Message(
                id = UUID.randomUUID().toString(),
                senderId = if (isAiAssistant) "gemini" else chat.id,
                senderName = chat.name,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isMe = false,
                status = MessageStatus.SEEN
            )

            // Append incoming reply
            _chats.value = _chats.value.map { c ->
                if (c.id == chatId) {
                    val messages = c.messages + replyMsg
                    c.copy(messages = messages)
                } else c
            }

            // Sync active view
            if (_activeChat.value?.id == chatId) {
                val updatedChat = _chats.value.find { it.id == chatId }
                _activeChat.value = updatedChat
                if (updatedChat != null) {
                    fetchSmartReplies(updatedChat)
                }
            }
        }
    }

    private fun setContactTyping(chatId: String, typing: Boolean) {
        // Toggle the active group participant/friend status
        if (_activeChat.value?.id == chatId) {
            _activeChat.value = _activeChat.value?.copy() // trigger update state
        }
    }

    // Call Actions
    fun initiateCall(contactName: String, avatar: String, isVideo: Boolean) {
        _isMuted.value = false
        _isSpeakerOn.value = false
        _callState.value = CallState(
            contactName = contactName,
            avatarEmoji = avatar,
            isVideo = isVideo,
            isIncoming = false,
            status = CallStatus.RINGING,
            durationSeconds = 0
        )
        // Simulate phone answering after 2.5 seconds
        viewModelScope.launch {
            delay(2500)
            if (_callState.value?.status == CallStatus.RINGING) {
                _callState.value = _callState.value?.copy(status = CallStatus.ACTIVE)
                startActiveCallTimer()
            }
        }
    }

    fun receiveIncomingSimulatedCall(contactName: String, avatar: String, isVideo: Boolean) {
        _isMuted.value = false
        _isSpeakerOn.value = false
        _callState.value = CallState(
            contactName = contactName,
            avatarEmoji = avatar,
            isVideo = isVideo,
            isIncoming = true,
            status = CallStatus.RINGING,
            durationSeconds = 0
        )
    }

    fun acceptIncomingCall() {
        val current = _callState.value ?: return
        _callState.value = current.copy(status = CallStatus.ACTIVE)
        startActiveCallTimer()
    }

    fun endCall() {
        callTimerJob?.cancel()
        _callState.value = _callState.value?.copy(status = CallStatus.ENDED)
        viewModelScope.launch {
            delay(1000)
            _callState.value = null
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    private fun startActiveCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _callState.value
                if (current != null && current.status == CallStatus.ACTIVE) {
                    _callState.value = current.copy(durationSeconds = current.durationSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    // AI Features

    fun requestAIPresetImageImageCaptioning(presetType: String) {
        // Mocking an image attach with presets
        viewModelScope.launch {
            _isAILoading.value = true
            val caption = geminiRepo.generateImageCaption(presetType)
            _isAILoading.value = false
            sendMessage(caption, attachmentPreset = presetType)
        }
    }

    fun requestAIChatSummary() {
        val activeChatVal = _activeChat.value ?: return
        if (activeChatVal.messages.isEmpty()) {
            _chatSummary.value = "Chat history is empty. Try messaging back and forth to summarize!"
            return
        }

        viewModelScope.launch {
            _isAILoading.value = true
            val chatHistory = activeChatVal.messages.joinToString("\n") { "${it.senderName}: ${it.text}" }
            val summary = geminiRepo.summarizeChat(chatHistory)
            _chatSummary.value = summary
            _isAILoading.value = false
        }
    }

    fun translateMessageInPlace(message: Message, targetLang: String) {
        val activeChatVal = _activeChat.value ?: return
        viewModelScope.launch {
            _isAILoading.value = true
            val translatedText = geminiRepo.translateMessage(message.text, targetLang)
            _isAILoading.value = false

            // Update in the messages list
            _chats.value = _chats.value.map { chat ->
                if (chat.id == activeChatVal.id) {
                    val updated = chat.messages.map { msg ->
                        if (msg.id == message.id) msg.copy(text = msg.text + "\n🌐 (AI Translated to $targetLang): $translatedText") else msg
                    }
                    chat.copy(messages = updated)
                } else chat
            }
            if (_activeChat.value?.id == activeChatVal.id) {
                _activeChat.value = _chats.value.find { it.id == activeChatVal.id }
            }
        }
    }
}

data class CallState(
    val contactName: String,
    val avatarEmoji: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val status: CallStatus,
    val durationSeconds: Int = 0
)

enum class CallStatus {
    RINGING, ACTIVE, ENDED
}
