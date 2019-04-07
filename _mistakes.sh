javac *.java
jar cfm pa1.jar manifest.txt *.class
rm *.class

#Client tries to connect to an uninitalized server
java -jar pa1.jar client ls test
java -jar pa1.jar client rm test
java -jar pa1.jar client rmdir test
java -jar pa1.jar client mkdir test
java -jar pa1.jar client upload test1 test2
java -jar pa1.jar client download test1 test2

#User passes incorrect args
java -jar pa1.jar
java -jar pa1.jar blah
java -jar pa1.jar client blah
java -jar pa1.jar server blah
java -jar pa1.jar client upload blah
java -jar pa1.jar client download blah
java -jar pa1.jar client upload
java -jar pa1.jar client download
java -jar pa1.jar client rmdir
java -jar pa1.jar client mkdir
java -jar pa1.jar client ls
java -jar pa1.jar client rm
java -jar pa1.jar server start blah

#Initialize Server, for remainder of tests
java -jar pa1.jar server start 8000 &
sleep 1s

#Client tries to download non-existant file

sleep 2s

#Client tries to upload non-existant file
java -jar pa1.jar client upload blah blah2

sleep 2s

#Client tries to upload file to directory
java -jar pa1.jar client mkdir wut
java -jar pa1.jar client upload App.java wut
java -jar pa1.jar client rmdir wut

sleep 2s

#Client tries to delete non-empty directory
#AND Client tries to delete directory with rm command
#AND Client tries to delete file with rmdir command
#AND Client tries to delete non-existant directory
java -jar pa1.jar client mkdir wut
java -jar pa1.jar client upload App.java wut/App.java
java -jar pa1.jar client rmdir wut
java -jar pa1.jar client rm wut 
java -jar pa1.jar client rmdir wut/App.java
java -jar pa1.jar client rm wut/App.java
java -jar pa1.jar client rmdir wut
java -jar pa1.jar client rmdir wut

sleep 2s

#Tests for already-existant directory
java -jar pa1.jar client mkdir wut
java -jar pa1.jar client mkdir wut
java -jar pa1.jar client upload App.java wut
java -jar pa1.jar client download wut wut2
java -jar pa1.jar client rmdir wut

#Tests for already-existant file
java -jar pa1.jar client upload App.java testFile.txt
java -jar pa1.jar client ls testFile.txt
java -jar pa1.jar client mkdir testFile.txt
java -jar pa1.jar client rmdir testFile.txt
java -jar pa1.jar client rm testFile.txt

#Client tries to create already-existant file as directory

#End tests
java -jar pa1.jar client shutdown &