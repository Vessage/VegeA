# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/alexchow/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn com.ut.mini.**
-dontwarn okio.**
-dontwarn com.xiaomi.**
-dontwarn com.squareup.**
-dontwarn android.support.v4.**
-dontwarn android.support.v7.**

-keepattributes *Annotation*

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.app.** { *; }

-keep class okio.** {*;}
-keep class com.squareup.** {*;}

-keep class com.umeng.message.protobuffer.* {
	 public <fields>;
         public <methods>;
}

-keep class com.umeng.message.* {
	 public <fields>;
         public <methods>;
}

-keep class org.android.agoo.impl.* {
	 public <fields>;
         public <methods>;
}

-keep class org.android.agoo.service.* {*;}

-keep class org.android.spdy.**{*;}

-keep public class **.R$*{
    public static final int *;
}
-dontwarn org.apache.http.**
-dontwarn android.webkit.**
-keep class org.apache.http.** { *; }
-keep class org.apache.commons.codec.** { *; }
-keep class org.apache.commons.logging.** { *; }
-keep class android.net.compatibility.** { *; }
-keep class android.net.http.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.tencent.mm.sdk.** {
   *;
}

#UPush
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class org.apache.thrift.** {*;}

-keep public class **.R$*{
   public static final int *;
}

-keep class com.lsjwzh.widget.recyclerviewpager.**
-dontwarn com.lsjwzh.widget.recyclerviewpager.**