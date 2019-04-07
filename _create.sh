javac *.java
jar cfm pa1.jar manifest.txt *.class
sleep 1s
rm *.class
sleep 1s
java -cp pa1.jar App server start 8000 &
sleep 1s
java -cp pa1.jar App client mkdir clientDir
java -cp pa1.jar App client mkdir serverDir
sleep 1s
java -cp pa1.jar App client upload App.java serverDir/App.java 
java -cp pa1.jar App client upload Message.java serverDir/Message.java
sleep 1s
java -cp pa1.jar App client download serverDir/App.java clientDir/App.java
java -cp pa1.jar App client download serverDir/Message.java clientDir/Message.java
sleep 1s
java -cp pa1.jar App client dir serverDir
java -cp pa1.jar App client dir clientDir
