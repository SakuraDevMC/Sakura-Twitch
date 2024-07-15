package me.banker.twitch.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.banker.twitch.Twitch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.util.UUID

class TwitchCodeStorage(private val plugin: Twitch) {

    private val file = File(plugin.dataFolder, "twitch_codes.json")
    private val gson = Gson()
    private val codeMapType: Type = object : TypeToken<Map<UUID, String>>() {}.type
    private var codes: MutableMap<UUID, String> = mutableMapOf()

    init {
        loadCodes()
    }

    fun getCode(uuid: UUID): String? {
        return codes[uuid]
    }

    fun setCode(uuid: UUID, code: String) {
        codes[uuid] = code
        saveCodes()
    }

    fun removeCode(uuid: UUID) {
        codes.remove(uuid)
        saveCodes()
    }

    private fun loadCodes() {
        if (file.exists()) {
            file.reader().use { reader ->
                codes = gson.fromJson(reader, codeMapType)
            }
        } else {
            saveCodes()
        }
    }

    private fun saveCodes() {
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            FileWriter(file).use { writer ->
                gson.toJson(codes, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
