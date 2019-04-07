
import java.io.Serializable;

public enum Command implements Serializable{
    UPLOAD_FILE,
    DOWNLOAD_FILE,
    CREATE_DIRECTORY,
    REMOVE_DIRECTORY,
    REMOVE_FILE,
    LIST_CONTENTS,
    SHUTDOWN
}