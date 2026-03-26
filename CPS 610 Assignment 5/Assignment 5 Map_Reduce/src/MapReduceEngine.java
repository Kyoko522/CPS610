import java.util.LinkedHashMap;
import java.util.List;

// Handles shuffle and reduce phases
public class MapReduceEngine {
    private String[] movieNames;
    private int hashSize;

    public MapReduceEngine(String[] movieNames, int hashSize) {
        this.movieNames = movieNames;
        this.hashSize = hashSize;
    }

    // Merges all machine hash tables into one major table
    public SimpleHashTable shuffle(List<DataMachine> machines) {
        SimpleHashTable majorTable = new SimpleHashTable(hashSize);

        for (DataMachine machine : machines) {
            MovieNode[] buckets = machine.getHashTable().getTable();

            for (MovieNode bucketHead : buckets) {
                MovieNode current = bucketHead;
                while (current != null) {
                    majorTable.insert(current.key, current.info);
                    current = current.next;
                }
            }
        }

        return majorTable;
    }

    // Counts total occurrences for each movie
    public LinkedHashMap<String, Integer> reduce(SimpleHashTable majorTable) {
        LinkedHashMap<String, Integer> counts = majorTable.countKeys();
        LinkedHashMap<String, Integer> orderedResult = new LinkedHashMap<>();

        for (String movie : movieNames) {
            orderedResult.put(movie, counts.getOrDefault(movie, 0));
        }

        return orderedResult;
    }
}