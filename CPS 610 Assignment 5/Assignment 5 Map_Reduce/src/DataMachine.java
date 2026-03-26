import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Represents one machine processing one input file
public class DataMachine {
    private String machineName;
    private String filePath;
    private String[] movieNames;
    private SimpleHashTable hashTable;

    public DataMachine(String machineName, String filePath, String[] movieNames, int hashSize) {
        this.machineName = machineName;
        this.filePath = filePath;
        this.movieNames = movieNames;
        this.hashTable = new SimpleHashTable(hashSize);
    }

    // Reads file and maps lines into the machine hash table
    public void processFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                mapLine(line);
            }

        } catch (IOException e) {
            System.out.println("Error reading " + filePath + ": " + e.getMessage());
        }
    }

    // Checks if a line contains any movie name
    private void mapLine(String line) {
        String lowerLine = line.toLowerCase();

        for (String movie : movieNames) {
            if (lowerLine.contains(movie.toLowerCase())) {
                hashTable.insert(movie, line);
            }
        }
    }

    public String getMachineName() {
        return machineName;
    }

    public SimpleHashTable getHashTable() {
        return hashTable;
    }
}