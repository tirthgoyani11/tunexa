-classobfuscationdictionary obfuscation/classnames.txt
-obfuscationdictionary obfuscation/members.txt

-overloadaggressively
-dontusemixedcaseclassnames
-repackageclasses ''

-keep public class com.xapps.media.xmusic.** extends android.app.Activity
-keep public class com.xapps.media.xmusic.** extends android.app.Service
-keep public class com.xapps.media.xmusic.** extends android.content.BroadcastReceiver
-keep public class com.xapps.media.xmusic.** extends android.app.Application
-keep public class com.xapps.media.xmusic.** extends android.content.ContentProvider
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.** { *; }
-dontwarn org.mozilla.javascript.tools.**

-keep class org.schabi.newpipe.** { *; }
-keep interface org.schabi.newpipe.** { *; }
-keep class com.grack.nanojson.** { *; }
-keep class org.jsoup.** { *; }
-keep class com.xapps.media.xmusic.online.** { *; }
-keep class com.xapps.media.xmusic.models.** { *; }
-keep class com.xapps.media.xmusic.service.resume.** { *; }
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-dontwarn java.beans.**
-dontwarn javax.script.**

-dontwarn jdk.dynalink.**
