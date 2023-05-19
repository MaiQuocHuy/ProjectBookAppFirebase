-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*

-dontwarn com.razorpay.**
-dontwarn com.google.firebase.**


-keep class com.razorpay.** {*;}

-optimizations !method/inlining/*