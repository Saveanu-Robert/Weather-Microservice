## Dependency Updates

This PR updates dependencies to their latest stable versions.

### Changes:
```diff
diff --git a/pom.xml b/pom.xml
index 64a20f4..f742a07 100644
--- a/pom.xml
+++ b/pom.xml
@@ -26,20 +26,20 @@
         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 
         <!-- Dependency versions -->
-        <springdoc.version>2.8.14</springdoc.version>
+        <springdoc.version>3.0.0</springdoc.version>
         <lombok.version>1.18.42</lombok.version>
         <caffeine.version>3.2.3</caffeine.version>
-        <resilience4j.version>2.2.0</resilience4j.version>
-        <logstash-logback.version>8.0</logstash-logback.version>
+        <resilience4j.version>2.3.0</resilience4j.version>
+        <logstash-logback.version>9.0</logstash-logback.version>
         <archunit.version>1.4.1</archunit.version>
 
         <!-- Plugin versions -->
         <jacoco.version>0.8.14</jacoco.version>
         <maven-compiler-plugin.version>3.14.1</maven-compiler-plugin.version>
-        <maven-surefire-plugin.version>3.5.2</maven-surefire-plugin.version>
-        <versions-maven-plugin.version>2.18.0</versions-maven-plugin.version>
-        <maven-checkstyle-plugin.version>3.5.0</maven-checkstyle-plugin.version>
-        <checkstyle.version>10.20.2</checkstyle.version>
+        <maven-surefire-plugin.version>3.5.4</maven-surefire-plugin.version>
+        <versions-maven-plugin.version>2.20.1</versions-maven-plugin.version>
+        <maven-checkstyle-plugin.version>3.6.0</maven-checkstyle-plugin.version>
+        <checkstyle.version>12.3.0</checkstyle.version>
         <spotless-maven-plugin.version>3.1.0</spotless-maven-plugin.version>
     </properties>
 
```
