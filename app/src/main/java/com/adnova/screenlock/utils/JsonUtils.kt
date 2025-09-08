package com.adnova.screenlock.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

object JsonUtils {
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }
    
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun <T> fromJson(json: String, type: Type): T? {
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
    
    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    }
}
