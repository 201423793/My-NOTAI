# OpenCV
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
