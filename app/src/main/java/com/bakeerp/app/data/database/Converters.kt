package com.bakeerp.app.data.database

import androidx.room.TypeConverter

/**
 * Room 类型转换器。
 *
 * 当前实体字段均为基本类型（String / Double / Int / Long / Boolean），Room 可原生支持。
 * 此处预置 List<String> 与 String 的互转能力，供后续扩展（如配方标签、多张成品图路径）使用。
 *
 * 两个必须避开的坑（均已在 CI 中实际踩到）：
 * 1. 不可定义两个签名相同的转换方法。例如同时存在
 *    `fun fromString(v: String?): String?` 与 `fun toString(v: String?): String?`，
 *    KSP 会报 "Multiple methods define the same conversion" 并中断构建。
 * 2. 被 @TypeConverters 引用的类不可为空实现，否则 KSP 会报
 *    "Class is referenced as a converter but it does not have any converter methods"。
 *    如需彻底移除转换器，必须同时删除 Database 上的 @TypeConverters 注解。
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.joinToString(SEPARATOR)

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.takeIf { it.isNotEmpty() }?.split(SEPARATOR)

    private companion object {
        /** 单元分隔符（US，ASCII 31），避免与用户输入的常规字符冲突。 */
        const val SEPARATOR = "\u001F"
    }
}
