import java.util.List;

public class Main {

    public static void main(String[] args) {

        TwoPhaseLockScheduler scheduler = new TwoPhaseLockScheduler();

        // ── Part A test ──────────────────────────────
        scheduler.addRecord(1, 0);
        scheduler.addRecord(7, 42);
        scheduler.addRecord(9, 99);

        scheduler.addTransaction(1, List.of(
                new Operation(1, 5),
                new Operation()
        ));
        scheduler.addTransaction(2, List.of(
                new Operation(9),
                new Operation(7),
                new Operation()
        ));
        scheduler.addTransaction(3, List.of(
                new Operation(1),
                new Operation()
        ));

        // ── Part B test ──────────────────────────────
//        scheduler.addRecord(1, 1);
//        scheduler.addRecord(2, 2);
//
//        scheduler.addTransaction(1, List.of(
//                new Operation(1, 5),
//                new Operation(2),
//                new Operation(2, 3),
//                new Operation(1),
//                new Operation()
//        ));
//        scheduler.addTransaction(2, List.of(
//                new Operation(1),
//                new Operation(1, 2),
//                new Operation()
//        ));

        scheduler.run();
    }
}