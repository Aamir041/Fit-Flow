# Proguard rules for FitFlow
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
