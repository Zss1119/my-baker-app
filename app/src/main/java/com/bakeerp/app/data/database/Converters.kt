package com.bakeerp.app.data.database

import androidx.room.TypeConverter

/**
 * Room 类型转换器。
 *
 * 当前所有字段均为基本类型，保留空实现以备后续扩展（如 List<String>）。
 */
class Converters {

    @TypeConverter
    fun fromString(value: String?): String? = value

    @TypeConverter
    fun toString(value: String?): String? = value
}