import java.util.Arrays;
import java.io.Serializable;

public class Message implements Comparable<Message>, Serializable {
    int from;
    int[] vectortimestamp;
    MessageType messageType;
    String content;
    long T; 

    public Message(int from, int[] vectortimestamp, MessageType messageType, String content) {
        this.from = from;
        this.vectortimestamp = vectortimestamp;
        this.messageType = messageType;
        this.content = content;
        this.T = System.currentTimeMillis();
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

    // @Override
    // public int compareTo(Message other) {
    // // Compare based on the vectortimestamp at 'from' index
    // int comparison = Integer.compare(this.vectortimestamp[from],
    // other.vectortimestamp[from]);

    // if (comparison != 0) {
    // // If values at 'from' index are not equal, return the comparison result
    // return comparison;
    // } else {
    // // Values at 'from' index are equal, compare other indexes
    // for (int i = 0; i < vectortimestamp.length; i++) {
    // if (i != from) { // Skip comparison for 'from' index
    // comparison = Integer.compare(this.vectortimestamp[i],
    // other.vectortimestamp[i]);
    // if (comparison != 0) {
    // // If values at current index are not equal, return the comparison result
    // return comparison;
    // }
    // }
    // }
    // // All elements are equal, compare based on the 'from' field
    // return Integer.compare(this.from, other.from);
    // }
    // }

    @Override
    public String toString() {
        return "T: "+  this.T +" from : " + from + " timestamp: " + Arrays.toString(vectortimestamp) + " messageType: "
                + messageType.toString() + " content : " + this.content;
    }
}
