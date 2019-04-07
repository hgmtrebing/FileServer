
/* SWE 622 - Programming Assignment #1
 * DataTransmitArgs Class
 * Created By: Harry Trebing - G00583550
 */

public class DataTransmitArgs {
    private String clientFilePath;
    private String serverFilePath;
    private int start;
    private int currentStart;
    private int currentEnd;
    private int end;

    public DataTransmitArgs() {

    }

    public DataTransmitArgs (String[] args) {
        this.clientFilePath = args[0];
        this.serverFilePath = args[1];
        this.start = Integer.parseInt(args[2]);
        this.currentStart = Integer.parseInt(args[3]);
        this.currentEnd = Integer.parseInt(args[4]);
        this.end = Integer.parseInt(args[5]);
    }

    public String[] toArgs() {
        return new String[]{
            this.clientFilePath,
            this.serverFilePath,
            Integer.toString(start),
            Integer.toString(currentStart),
            Integer.toString(currentEnd),
            Integer.toString(end)
        };
    }

    public String getClientFilePath() {
        return this.clientFilePath;
    }

    public void setClientFilePath(String newPath) {
        this.clientFilePath = newPath;
    }

    public String getServerFilePath() {
        return this.serverFilePath;
    }

    public void setServerFilePath(String newPath) {
        this.serverFilePath = newPath;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart (int val) {
        this.start = val;
    }

    public int getCurrentStart() {
        return this.currentStart;
    }

    public void setCurrentStart(int val) {
        this.currentStart = val;
    }

    public int getCurrentEnd() {
        return this.currentEnd;
    }

    public void setCurrentEnd(int val) {
        this.currentEnd = val;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int val) {
        this.end = val;
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof DataTransmitArgs)) {
            return false;
        } else if (o == this) {
            return true;
        } else {
            DataTransmitArgs d = (DataTransmitArgs)o;
            return (this.clientFilePath == d.clientFilePath &&
                    this.serverFilePath == d.serverFilePath &&
                    this.start == d.start &&
                    this.currentStart == d.currentStart &&
                    //this.currentEnd == d.currentEnd &&
                    //This is commented out because the Server has no way of knowing what the new
                    //currentEnd is on the client
                    this.end == d.end
            );
        }
    }
}