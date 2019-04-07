
/* SWE 622 - Programming Assignment #1
 * Protocol Class
 * Created By: Harry Trebing - G00583550
 */
import java.io.Serializable;

public enum Protocol implements Serializable{
    REQUEST,
    ACKNOWLEDGE,
    TRANSMIT,
    RECEIPT,
    RETRANSMIT,
    RETRANSMIT_REQUEST,
    SUCCESS,
    ERROR,
    ERROR_INVALID_PROTOCOL,
    ERROR_INVALID_ARGUMENTS,
    ERROR_DIRECTORY_ALREADY_EXISTS,
    ERROR_DIRECTORY_NOT_EMPTY,
    ERROR_FILE_ALREADY_EXISTS,
    ERROR_DIRECTORY_DOES_NOT_EXIST,
    ERROR_FILE_DOES_NOT_EXIST,
    FINISH
}