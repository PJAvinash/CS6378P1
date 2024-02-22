import java.util.Arrays;

public class Message implements Comparable<Message> {
    int from;
    int[] vectortimestamp;
    MessageType messageType;
    String content;
    public Message(int from, int[] vectortimestamp, MessageType messageType, String content){
        this.from = from;
        this.vectortimestamp = vectortimestamp;
        this.messageType = messageType;
        this.content = content;
    } 
    public void setTime(int[] input){
        this.vectortimestamp = input;
    }
     @Override
    public int compareTo(Message other) {
        // Compare based on the vectortimestamp
        return Arrays.compare(this.vectortimestamp, other.vectortimestamp);
    }

    @Override
    public String toString() {
        return " from : " + from + " timestamp: " + Arrays.toString(vectortimestamp) + " messageType: " + messageType.toString() + " content : " + this.content;
    }
}
