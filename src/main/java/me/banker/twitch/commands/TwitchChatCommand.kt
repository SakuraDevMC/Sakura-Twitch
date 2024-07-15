package me.banker.twitch.commands

import me.banker.twitch.handlers.TwitchChatHandler
import me.banker.twitch.storage.TwitchCodeStorage
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.bukkit.player

class TwitchChatCommand(
    private val twitchChatHandler: TwitchChatHandler,
    private val codeStorage: TwitchCodeStorage
) {

    @Command("twitchchat")
    @CommandPermission("twitchchat.use")
    fun connect(actor: BukkitCommandActor, @Optional channel: String?) {
        val player: Player = actor.player
        if (channel.isNullOrEmpty()) {
            player.sendMessage("Usage: /twitchchat <channel> or /twitchchat disconnect")
        } else {
            if (channel.equals("disconnect", ignoreCase = true)) {
                twitchChatHandler.disconnectPlayer(player)
            } else {
                codeStorage.setCode(player.uniqueId, channel)
                player.sendMessage("Twitch channel set to: $channel")
                twitchChatHandler.connectPlayer(player, channel)
            }
        }
    }

    @Command("twitchchatcheck")
    @CommandPermission("twitchchat.admin")
    fun checkConnection(actor: BukkitCommandActor, target: Player) {
        val channel = codeStorage.getCode(target.uniqueId)
        if (channel.isNullOrEmpty() || !twitchChatHandler.isPlayerConnected(target)) {
            actor.reply("Player ${target.name} is not connected to any Twitch channel.")
        } else {
            actor.reply("Player ${target.name} is connected to Twitch channel: $channel")
        }
    }
}
