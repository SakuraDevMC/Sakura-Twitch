package me.banker.twitch.listeners

import me.banker.twitch.Twitch
import me.banker.twitch.handlers.TwitchChatHandler
import me.banker.twitch.storage.TwitchCodeStorage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level

class PlayerEventListener(
    private val plugin: Twitch,
    private val twitchChatHandler: TwitchChatHandler,
    private val codeStorage: TwitchCodeStorage
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val channel = codeStorage.getCode(player.uniqueId)
        if (channel != null) {
            plugin.logger.log(Level.INFO, "Reconnecting player ${player.name} to channel $channel")
            twitchChatHandler.connectPlayer(player, channel)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.logger.log(Level.INFO, "Disconnecting player ${player.name} from their Twitch channel")
        twitchChatHandler.disconnectPlayer(player)
    }
}
