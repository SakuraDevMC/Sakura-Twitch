package me.banker.twitch

import me.banker.twitch.commands.TwitchChatCommand
import me.banker.twitch.handlers.TwitchChatHandler
import me.banker.twitch.listeners.PlayerEventListener
import me.banker.twitch.storage.TwitchCodeStorage
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class Twitch : JavaPlugin() {

    lateinit var twitchChatHandler: TwitchChatHandler
    lateinit var twitchCodeStorage: TwitchCodeStorage

    override fun onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig()

        // Initialize handlers and storage
        twitchChatHandler = TwitchChatHandler(this)
        twitchCodeStorage = TwitchCodeStorage(this)

        // Register event listeners
        registerEventListeners()

        // Register commands
        registerCommands()
    }

    override fun onDisable() {
        // Properly disable the TwitchChatHandler
        twitchChatHandler.disable()
    }

    private fun registerEventListeners() {
        server.pluginManager.registerEvents(PlayerEventListener(this, twitchChatHandler, twitchCodeStorage), this)
    }

    private fun registerCommands() {
        val handler = BukkitCommandHandler.create(this)
        handler.register(TwitchChatCommand(twitchChatHandler, twitchCodeStorage))
        handler.registerBrigadier() // Optional: Register colorful tooltips (Works on 1.13+ only)
    }
}
