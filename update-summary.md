## Dependency Updates

This PR updates dependencies to their latest stable versions.

### Changes:
```diff
diff --git a/pom.xml b/pom.xml
index dc9d9c8..b8424de 100644
--- a/pom.xml
+++ b/pom.xml
@@ -27,20 +27,20 @@
 
         <!-- Dependency versions -->
         <springdoc.version>2.8.14</springdoc.version>
-        <lombok.version>1.18.42</lombok.version>
+        <lombok.version>1.18.44</lombok.version>
         <caffeine.version>3.2.3</caffeine.version>
-        <resilience4j.version>2.3.0</resilience4j.version>
+        <resilience4j.version>2.4.0</resilience4j.version>
         <logstash-logback.version>9.0</logstash-logback.version>
         <archunit.version>1.4.1</archunit.version>
 
         <!-- Plugin versions -->
         <jacoco.version>0.8.14</jacoco.version>
-        <maven-compiler-plugin.version>3.14.1</maven-compiler-plugin.version>
-        <maven-surefire-plugin.version>3.5.4</maven-surefire-plugin.version>
-        <versions-maven-plugin.version>2.20.1</versions-maven-plugin.version>
+        <maven-compiler-plugin.version>3.15.0</maven-compiler-plugin.version>
+        <maven-surefire-plugin.version>3.5.5</maven-surefire-plugin.version>
+        <versions-maven-plugin.version>2.21.0</versions-maven-plugin.version>
         <maven-checkstyle-plugin.version>3.6.0</maven-checkstyle-plugin.version>
-        <checkstyle.version>13.0.0</checkstyle.version>
-        <spotless-maven-plugin.version>3.1.0</spotless-maven-plugin.version>
+        <checkstyle.version>13.3.0</checkstyle.version>
+        <spotless-maven-plugin.version>3.4.0</spotless-maven-plugin.version>
     </properties>
 
     <dependencies>
```
