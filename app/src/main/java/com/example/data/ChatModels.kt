package com.example.data

data class Message(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val attachmentPreset: String? = null,
    val isAudio: Boolean = false,
    val audioDuration: Int = 0, // in seconds
    val isModerated: Boolean = false,
    val repliedMessageText: String? = null,
    val reactions: Map<String, Int> = emptyMap() // Map of emoji string to count
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, SEEN
}

data class Contact(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val isOnline: Boolean,
    val lastSeen: String = "Online",
    val isTyping: Boolean = false,
    val statusText: String = "Hey there! I am using TeleChat."
)

data class Chat(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val isGroup: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val wallpaperPreset: String? = null, // "gradient_blue", "gradient_sunset", "glass_dark", "solid"
    val messages: List<Message> = emptyList()
) {
    val lastMessage: Message? get() = messages.lastOrNull()
}

data class StatusStory(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val contentPreset: String, // "sunset", "city", "cats", "neon"
    val caption: String,
    val timestamp: String
)
