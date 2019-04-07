java -jar pa1.jar client rm serverDir/Message.java
java -jar pa1.jar client rm serverDir/App.java
java -jar pa1.jar client rm clientDir/Message.java
java -jar pa1.jar client rm clientDir/App.java
sleep 1s
java -jar pa1.jar client rmdir clientDir
java -jar pa1.jar client rmdir serverDir
sleep 1s
java -jar pa1.jar client shutdown