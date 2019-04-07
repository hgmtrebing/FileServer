java App client rm serverDir/Message.java
java App client rm serverDir/App.java
java App client rm clientDir/Message.java
java App client rm clientDir/App.java
sleep 1s
java App client rmdir clientDir
java App client rmdir serverDir
sleep 1s
java App client shutdown
sleep 1s
rm *.class