echo "Changing Gradle Wrapper version to 2.7."
./gradlew wrapper --gradle-version 2.7  
echo "Starting to export build."
./gradlew -b export.gradle build
