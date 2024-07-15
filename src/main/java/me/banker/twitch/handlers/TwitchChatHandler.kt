package me.banker.twitch.handlers

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.enums.CommandPermission
import com.github.philippheuer.events4j.api.domain.IEventSubscription
import me.banker.twitch.Twitch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class TwitchChatHandler(private val plugin: Twitch) {

    // TODO: get messages/annoucements for hype train, gifting sub.

    private val twitchClient: TwitchClient = TwitchClientBuilder.builder()
        .withEnableChat(true)
        .build()
    private val playerChannelMap: MutableMap<UUID, String> = mutableMapOf()
    private val playerListeners: MutableMap<UUID, IEventSubscription> = mutableMapOf()
    private var isEnabled = true

    init {
        plugin.logger.info("TwitchChatHandler initialized")
    }

    fun connectPlayer(player: Player, channel: String) {
        if (!isEnabled) return

        if (playerChannelMap.containsKey(player.uniqueId)) {
            player.sendMessage("You are already connected to a channel. Disconnect first before connecting to another.")
            return
        }

        twitchClient.chat.joinChannel(channel)
        playerChannelMap[player.uniqueId] = channel

        val listener = twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) { event ->
            if (!isEnabled) return@onEvent

            if (event.channel.name.equals(channel, ignoreCase = true)) {
                if (isBlockedUser(event.user.name) || containsBlockedPhrase(event.message) || startsWithBlockedPrefix(event.message) || containsBlockedWord(event.message)) {
                    return@onEvent
                }

                scheduleTaskSafely {
                    if (!plugin.isEnabled) return@scheduleTaskSafely

                    val user = event.user
                    val message = event.message

                    val isStreamer = user.name.equals(channel, ignoreCase = true)
                    val isModerator = event.permissions.contains(CommandPermission.MODERATOR)
                    val isSubscriber = event.permissions.contains(CommandPermission.SUBSCRIBER)
                    val isVip = event.permissions.contains(CommandPermission.VIP)
                    val isStaff = event.permissions.contains(CommandPermission.TWITCHSTAFF)

                    val statusColor = when {
                        isStreamer -> NamedTextColor.RED
                        isStaff -> NamedTextColor.DARK_PURPLE
                        isModerator -> NamedTextColor.BLUE
                        isVip -> NamedTextColor.LIGHT_PURPLE
                        isSubscriber -> NamedTextColor.GREEN
                        else -> NamedTextColor.GRAY
                    }

                    val status = when {
                        isStreamer -> "Streamer"
                        isStaff -> "Twitch Staff"
                        isModerator -> "Moderator"
                        isVip -> "VIP"
                        isSubscriber -> "Subscriber"
                        else -> "Viewer"
                    }

                    val chatMessage = Component.text()
                        .append(Component.text("[$status] ", statusColor))
                        .append(Component.text("${user.name}: ", NamedTextColor.GRAY))
                        .append(Component.text(message, NamedTextColor.WHITE))
                        .build()

                    player.sendMessage(chatMessage)
                }
            }
        }

        playerListeners[player.uniqueId] = listener
    }

    fun isPlayerConnected(player: Player): Boolean {
        return playerChannelMap.containsKey(player.uniqueId)
    }

    fun disconnectPlayer(player: Player) {
        if (!isEnabled) return

        val channel = playerChannelMap[player.uniqueId]
        if (channel != null) {
            twitchClient.chat.leaveChannel(channel)
            playerChannelMap.remove(player.uniqueId)
            playerListeners.remove(player.uniqueId)?.dispose()
            player.sendMessage("Disconnected from channel: $channel")
        } else {
            player.sendMessage("You are not connected to any channel.")
        }
    }

    fun disable() {
        isEnabled = false
        twitchClient.chat.channels.forEach { twitchClient.chat.leaveChannel(it) }
        playerChannelMap.clear()
        playerListeners.values.forEach { it.dispose() }
        playerListeners.clear()
        plugin.logger.info("TwitchChatHandler disabled")
    }

    private fun scheduleTaskSafely(task: Runnable) {
        if (plugin.isEnabled) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (plugin.isEnabled) {
                    task.run()
                }
            })
        }
    }

    private fun isBlockedUser(username: String): Boolean {
        val blockedUsers = plugin.config.getStringList("blocked-users")
        return blockedUsers.any { it.equals(username, ignoreCase = true) }
    }

    private fun containsBlockedPhrase(message: String): Boolean {
        val blockedPhrases = plugin.config.getStringList("blocked-phrases")
        return blockedPhrases.any { phrase -> message.contains(phrase, ignoreCase = true) }
    }

    private fun startsWithBlockedPrefix(message: String): Boolean {
        val blockedPrefix = plugin.config.getString("blocked-prefix")
        return blockedPrefix != null && message.startsWith(blockedPrefix)
    }

    private fun containsBlockedWord(message: String): Boolean {
        val blockedWords = plugin.config.getStringList("blocked-words")
        return blockedWords.any { word -> message.contains(word, ignoreCase = true) }
    }
}
