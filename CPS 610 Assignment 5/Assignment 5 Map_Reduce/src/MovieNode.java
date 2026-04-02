// Node for linked list inside each hash table bucket
public class MovieNode {
    String key;      // movie name
    String info;     // line/review text
    MovieNode next;  // next node

    public MovieNode(String key, String info) {
        this.key = key;
        this.info = info;
        this.next = null;
    }
}
