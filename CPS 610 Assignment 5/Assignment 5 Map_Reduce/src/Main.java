import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Main class
public class Main {

    // 10 Oscar nominated movies
    private static final String[] MOVIE_NAMES = {
            "American Fiction",
            "Anatomy of a Fall",
            "Barbie",
            "The Holdovers",
            "Killers of the Flower Moon",
            "Maestro",
            "Oppenheimer",
            "Past Lives",
            "Poor Things",
            "The Zone of Interest"
    };

    public static void main(String[] args) {
        String[] filePaths = {
                "Assignment 5 Map_Reduce/src/input/input1.txt",
                "Assignment 5 Map_Reduce/src/input/input2.txt",
                "Assignment 5 Map_Reduce/src/input/input3.txt",
                "Assignment 5 Map_Reduce/src/input/input4.txt"
        };

        int hashSize = 17;
        MapReduceEngine engine = new MapReduceEngine(MOVIE_NAMES, hashSize);

        List<DataMachine> machines = new ArrayList<>();

        // Create and process 4 machines
        for (int i = 0; i < filePaths.length; i++) {
            DataMachine machine = new DataMachine(
                    "Machine-" + (i + 1),
                    filePaths[i],
                    MOVIE_NAMES,
                    hashSize
            );

            machine.processFile();
            machines.add(machine);
        }

        // Print mapping phase results
        System.out.println("===== MAPPING PHASE =====");
        for (DataMachine machine : machines) {
            System.out.println("\n" + machine.getMachineName());
            machine.getHashTable().printTable();
        }

        // Shuffle phase
        SimpleHashTable majorTable = engine.shuffle(machines);

        System.out.println("\n===== SHUFFLING PHASE =====");
        majorTable.printTable();

        // Reduce phase
        LinkedHashMap<String, Integer> finalResult = engine.reduce(majorTable);

        System.out.println("\n===== REDUCING PHASE: FINAL OUTPUT =====");
        for (Map.Entry<String, Integer> entry : finalResult.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}