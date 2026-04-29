# ────────────────────────────────────────────────────────────────────────────
# BenimGunlerim — ProGuard / R8 rules
# ────────────────────────────────────────────────────────────────────────────

# ── Kotlin ───────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class kotlin.Metadata { *; }
# Serialisable data classes — keep field names used in Room or DataStore
-keepclassmembers class * {
    @kotlin.jvm.JvmField *;
}

# ── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
# Hilt-generated classes are referenced by name at runtime.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.EntryPoint interface * { *; }
-keepclassmembers class **.Hilt_* { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
# Entity, DAO, and Database classes must survive minification.
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Embedded class * { *; }
-keep @androidx.room.Relation class * { *; }
# Type converters
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

# ── DataStore / Preferences ───────────────────────────────────────────────────
-dontwarn com.google.protobuf.**

# ── Compose ─────────────────────────────────────────────────────────────────
# Composable functions are called via reflection in some tooling paths.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
# Compose stability / intrinsic optimisation markers
-keepattributes RuntimeVisibleAnnotations

# ── Enums ────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ───────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Serializable ─────────────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── WorkManager ──────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# ── Navigation ───────────────────────────────────────────────────────────────
-keep class * extends androidx.navigation.NavArgs { *; }

# ── Analytics / diagnostics ──────────────────────────────────────────────────
# StoredErrorReport is accessed via getStoredReports() at runtime
-keep class com.benimgunlerim.analytics.StoredErrorReport { *; }

# ── Domain model data classes ────────────────────────────────────────────────
# These Kotlin data classes cross architectural layers or are serialised;
# keep their field names so that reflection-based tooling and debuggers work.
-keep class com.benimgunlerim.domain.usecase.TodaySnapshot { *; }
-keep class com.benimgunlerim.domain.AchievementDef { *; }
-keep class com.benimgunlerim.domain.GameEngine$LevelInfo { *; }
-keep class com.benimgunlerim.domain.OperationResult { *; }
-keep class com.benimgunlerim.domain.OperationResult$* { *; }
-keep class com.benimgunlerim.domain.service.GrantResult { *; }
-keep class com.benimgunlerim.ui.today.TodayUiState { *; }
-keep class com.benimgunlerim.ui.today.GameEvent { *; }
-keep class com.benimgunlerim.ui.today.GameEvent$* { *; }

