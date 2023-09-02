# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.apache.batik.anim.dom.SAXSVGDocumentFactory
-dontwarn org.apache.batik.bridge.BridgeContext
-dontwarn org.apache.batik.bridge.DocumentLoader
-dontwarn org.apache.batik.bridge.GVTBuilder
-dontwarn org.apache.batik.bridge.UserAgent
-dontwarn org.apache.batik.bridge.UserAgentAdapter
-dontwarn org.apache.batik.util.XMLResourceDescriptor
-dontwarn org.osgi.framework.Bundle
-dontwarn org.osgi.framework.BundleContext
-dontwarn org.osgi.framework.FrameworkUtil
-dontwarn org.osgi.framework.ServiceReference

# POI库混淆规则
# org.apache.poi:poi
## commons-codec:commons-codec
-keep public class org.apache.commons.codec.** {*;}
## org.apache.commons:commons-collections4
-keep public class org.apache.commons.collections4.** {*;}
## org.apache.commons:commons-math3
-keep public class org.apache.commons.math3.** {*;}
## commons-io:commons-io
-keep public class org.apache.commons.io.** {*;}
## com.zaxxer:SparseBitSet
-keep public class com.zaxxer.sparsebits.** {*;}
## org.apache.logging.log4j:log4j-api
-keep public class org.apache.logging.log4j.** {*;}

# org.apache.poi:poi-ooxml
## org.apache.poi:poi
-keep public class org.apache.poi.** {*;}
## org.apache.poi:poi-ooxml-lite
-keep public class com.microsoft.schemas.** {*;}
-keep public class org.apache.poi.schemas.** {*;}
-keep public class org.etsi.uri.x01903.** {*;}
-keep public class org.openxmlformats.schemas.** {*;}
-keep public class org.w3.x2000.x09.xmldsig.** {*;}
## org.apache.xmlbeans:xmlbeans
-keep public class org.apache.xmlbeans.** {*;}
## org.apache.commons:commons-compress
-keep public class org.apache.commons.compress.** {*;}
## com.github.virtuald:curvesapi
-keep public class com.graphbuilder.** {*;}


# Glide库混淆规则
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}