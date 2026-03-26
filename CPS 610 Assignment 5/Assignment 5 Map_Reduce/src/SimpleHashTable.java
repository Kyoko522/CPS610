import java.util.LinkedHashMap;

// Simple hash table
public class SimpleHashTable {
    private MovieNode[] table;
    private int size;

    public SimpleHashTable(int size) {
        this.size = size;
        this.table = new MovieNode[size];
    }

    // Hash function for movie name
    private int hash(String key) {
        return Math.abs(key.toLowerCase().hashCode()) % size;
    }

    // Insert a movie record into the table
    public void insert(String key, String info) {
        int index = hash(key);
        MovieNode newNode = new MovieNode(key, info);

        if (table[index] == null) {
            table[index] = newNode;
        } else {
            MovieNode current = table[index];
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    // Return the full bucket array
    public MovieNode[] getTable() {
        return table;
    }

    // Print the full table
    public void printTable() {
        for (int i = 0; i < size; i++) {
            System.out.print("Bucket[" + i + "]: ");
            MovieNode current = table[i];

            if (current == null) {
                System.out.println("empty");
                continue;
            }

            while (current != null) {
                System.out.print("(" + current.key + ", " + shorten(current.info) + ") -> ");
                current = current.next;
            }
            System.out.println("null");
        }
    }

    // Count occurrences of each movie key
    public LinkedHashMap<String, Integer> countKeys() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            MovieNode current = table[i];
            while (current != null) {
                counts.put(current.key, counts.getOrDefault(current.key, 0) + 1);
                current = current.next;
            }
        }

        return counts;
    }

    // Shortens long review text for display
    private String shorten(String text) {
        if (text == null) return "";
        return text.length() > 35 ? text.substring(0, 35) + "..." : text;
    }
}