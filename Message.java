import java.util.Arrays;

public class Message implements Comparable<Message> {
    int from;
    int[] vectortimestamp;
    MessageType messageType;
    String content;

    public Message(int from, int[] vectortimestamp, MessageType messageType, String content) {
        this.from = from;
        this.vectortimestamp = vectortimestamp;
        this.messageType = messageType;
        this.content = content;
    }

    public void setTime(int[] input) {
        this.vectortimestamp = input;
    }

    @Override
    public int compareTo(Message other) {
        // Compare based on the vectortimestamp
        if (this.vectortimestamp[from] < other.vectortimestamp[from]) {
            return -1;
        } else if (this.vectortimestamp[from] > other.vectortimestamp[from]) {
            return 1;
        } else {
            int l = 0;
            int g = 0;
            for (int i = 0; i < vectortimestamp.length; i++) {
                if (this.vectortimestamp[i] != other.vectortimestamp[i]) {
                    if (this.vectortimestamp[i] > other.vectortimestamp[i]) {
                        l = l + 1;
                    } else {
                        g = g + 1;
                    }
                }
            }
            if ((l == 0 && g == 0) || (l > 0 && g > 0)) {
                return 0;
            } else if (l > 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    @Override
    public String toString() {
        return " from : " + from + " timestamp: " + Arrays.toString(vectortimestamp) + " messageType: "
                + messageType.toString() + " content : " + this.content;
    }
}
