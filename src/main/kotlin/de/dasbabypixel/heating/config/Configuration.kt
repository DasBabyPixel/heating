package de.dasbabypixel.heating.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.nio.file.Files
import java.nio.file.Path

interface Configuration {
    fun getString(name: String): String?
    fun getInt(name: String): Int?
}

class JsonConfiguration(private val json: JsonObject) : Configuration {
    constructor(data: String) : this(gson.fromJson(data, JsonObject::class.java))
    constructor(path: Path) : this(Files.readString(path))

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
    }

    override fun getString(name: String): String? {
        val element: JsonElement = json.get(name) ?: return null
        if (!element.isJsonPrimitive) return null
        val primitive = element.asJsonPrimitive
        if (!primitive.isString) return null
        return primitive.asString
    }

    override fun getInt(name: String): Int? {
        val element: JsonElement = json.get(name) ?: return null
        if (!element.isJsonPrimitive) return null
        val primitive = element.asJsonPrimitive
        if (!primitive.isNumber) return null
        return primitive.asInt
    }
}