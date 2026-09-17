package com.bakeerp.app.data.database

/**
 * Room 类型转换器。
 *
 * 当前所有字段均为基本类型（String / Double / Int / Long / Boolean），
 * Room 可原生支持，无需自定义转换，故此处保持为空实现。
 *
 * 后续如需存储 List<String>、Map 等复杂类型，在此添加 @TypeConverter 方法，
 * 并通过 BakeErpDatabase 上的 @TypeConverters 注册即可。
 *
 * 注意：不可定义两个签名相同的转换方法（例如同时存在
 * `fun fromString(v: String?): String?` 与 `fun toString(v: String?): String?`），
 * 否则 KSP 会报 "Multiple methods define the same conversion" 并导致构建失败。
 */
class Converters {
    // 预留扩展位：暂无需要自定义转换的字段
}
