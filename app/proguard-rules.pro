# ProGuard 规则：自用构建默认关闭混淆，仅保留 Room/Compose 必要规则以备 release 模式生效。

# 保留 Kotlin 反射相关元数据
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Room 实体与 DAO
-keep class com.bakeerp.app.data.database.entity.** { *; }
-keep class com.bakeerp.app.data.dao.** { *; }

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# WorkManager Worker 子类
-keep public class * extends androidx.work.Worker
-keep public class * extends androidx.work.CoroutineWorker
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}