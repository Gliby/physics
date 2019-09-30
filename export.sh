echo "Changing Gradle Wrapper version to 3.0."
./gradlew wrapper --gradle-version 3.0  
echo "Starting to export build."
./gradlew -b export.gradle build
