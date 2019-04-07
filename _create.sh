javac *.java
sleep 1s
java App server start 8000 &
sleep 1s
java App client mkdir clientDir
java App client mkdir serverDir
sleep 1s
java App client upload App.java serverDir/App.java 
java App client upload Message.java serverDir/Message.java
sleep 1s
java App client download serverDir/App.java clientDir/App.java
java App client download serverDir/Message.java clientDir/Message.java
sleep 1s
java App client ls serverDir
java App client ls clientDir
