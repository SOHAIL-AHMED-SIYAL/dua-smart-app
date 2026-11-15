# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to rules stored
# in /Users/.../build/intermediates/proguard-rules/debug/aapt_rules.txt
# and can be overridden here.
# If you want to remove comments, use -dontnote, -dontwarn, -ignorewarnings.

-dontwarn androidx.room.**
-keep class androidx.room.** { *; }

-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
