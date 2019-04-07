
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;

import java.io.File;

import java.util.stream.Stream;
import java.util.Arrays;

public class FileManager {

    private Path root;

    public FileManager (Path root) {
        this.root = root;
    }

    public FileManager (String root) {
        this.root = Paths.get(root);
    }

    public FileManager () {
        this.root = Paths.get(System.getProperty("user.dir"));
    }

    private Path resolveFilePath (String filePath) {
        return Paths.get(root.toAbsolutePath().toString(), filePath);
    }

    public boolean isFile (String filePath) {
        Path p = resolveFilePath(filePath);
        return Files.exists(p);
    }

    public boolean isDirectory (String filePath) {
        Path p = resolveFilePath(filePath);
        return Files.isDirectory(p);
    }

    public long getFileSize (String filePath) {
        Path p = resolveFilePath(filePath);
        try {
            return Files.size(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readFile (String filePath) {
        Path p = this.resolveFilePath(filePath);
        try {
            return Files.readAllBytes(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToFile (String filePath, byte[] bytes) {

        Path p = this.resolveFilePath(filePath);
        try {
            if (!this.isFile(filePath)) {
                Files.write(p, bytes, StandardOpenOption.CREATE);
            } else {
                Files.write(p, bytes, StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a file. Returns true if the file was deleted; false if the file did not exist to begin
     * with.
     */
    public boolean deleteFile (String filePath) {
        if (!this.isFile(filePath)) {
            return false;
        }
        Path p = resolveFilePath(filePath);
        try {
            Files.deleteIfExists(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Deletes an empty directory. Returns false if the directory does not exist or is not empty,
     * true if the directory was deleted.
     */
    public boolean deleteDirectory (String filePath) {
        if(!this.isDirectory(filePath)) {
            return false;
        }
        Path p = resolveFilePath(filePath);
        try {
            return Files.deleteIfExists(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a directory. Returns true if the directory now exists, false if the directory already
     * existed.
     */
    public boolean createDirectory (String filePath) {
        if (isDirectory(filePath)) {
            return false;
        }
        Path p = resolveFilePath(filePath);
        try {
            Files.createDirectory(p);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lists the contents of a directory, returning the names of the files and subdirectories
     * as an array of Strings
     * @param filePath
     */
    public String[] listContents (String filePath) {
        Path p = resolveFilePath(filePath);
        Stream<Path> s;

        try {
            s = Files.list(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Object[] tempArr = s.toArray();
        String[] contents = new String[tempArr.length];
        for (int i = 0; i < tempArr.length; i++) {
            Path tempPath = (Path)tempArr[i];
            contents[i] = tempPath.getFileName().toString();
        }
        return contents;
    }

    public static void main(String[] args) {
        FileManager f = new FileManager();
        System.out.println(f.isFile("FileManager.java"));
        System.out.println(f.isDirectory(".vscode"));
        System.out.println(f.isFile("fakeFile.java"));
        System.out.println(f.isDirectory("fakeDirectory.java"));
        f.createDirectory("test2");
        f.createDirectory("test3/");
        f.deleteDirectory("test3/");
    }
}