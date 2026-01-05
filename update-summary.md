## Dependency Updates

This PR updates dependencies to their latest stable versions.

### Changes:
```diff
diff --git a/pom.xml b/pom.xml
index 66792d3..5a025bc 100644
--- a/pom.xml
+++ b/pom.xml
@@ -26,7 +26,7 @@
         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
 
         <!-- Dependency versions -->
-        <springdoc.version>2.8.14</springdoc.version>
+        <springdoc.version>3.0.1</springdoc.version>
         <lombok.version>1.18.42</lombok.version>
         <caffeine.version>3.2.3</caffeine.version>
         <resilience4j.version>2.3.0</resilience4j.version>
@@ -39,7 +39,7 @@
         <maven-surefire-plugin.version>3.5.4</maven-surefire-plugin.version>
         <versions-maven-plugin.version>2.20.1</versions-maven-plugin.version>
         <maven-checkstyle-plugin.version>3.6.0</maven-checkstyle-plugin.version>
-        <checkstyle.version>12.3.0</checkstyle.version>
+        <checkstyle.version>13.0.0</checkstyle.version>
         <spotless-maven-plugin.version>3.1.0</spotless-maven-plugin.version>
     </properties>
 
```
