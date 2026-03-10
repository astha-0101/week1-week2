import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class week1week2 {

    enum SpotStatus { EMPTY, OCCUPIED, DELETED }

    static class ParkingSpot {
        SpotStatus status = SpotStatus.EMPTY;
        String licensePlate;
        LocalDateTime entryTime;
        int probes; // number of probes taken to park
    }

    private final ParkingSpot[] spots;
    private final int capacity;
    private int totalProbes = 0;
    private int parkedVehicles = 0;

    public week1week2(int capacity) {
        this.capacity = capacity;
        spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new ParkingSpot();
    }

    private int hashLicense(String licensePlate) {
        // Simple custom hash: sum of char codes modulo capacity
        int hash = 0;
        for (char c : licensePlate.toCharArray()) hash += c;
        return hash % capacity;
    }

    public synchronized int parkVehicle(String licensePlate) {
        int preferred = hashLicense(licensePlate);
        int probeCount = 0;

        for (int i = 0; i < capacity; i++) {
            int idx = (preferred + i) % capacity;
            if (spots[idx].status == SpotStatus.EMPTY || spots[idx].status == SpotStatus.DELETED) {
                spots[idx].status = SpotStatus.OCCUPIED;
                spots[idx].licensePlate = licensePlate;
                spots[idx].entryTime = LocalDateTime.now();
                spots[idx].probes = probeCount;
                totalProbes += probeCount;
                parkedVehicles++;
                System.out.println("Assigned spot #" + idx + " (" + probeCount + " probes)");
                return idx;
            }
            probeCount++;
        }

        System.out.println("Parking Full! Cannot assign spot to " + licensePlate);
        return -1;
    }

    public synchronized double exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.OCCUPIED && spots[i].licensePlate.equals(licensePlate)) {
                LocalDateTime exitTime = LocalDateTime.now();
                Duration duration = Duration.between(spots[i].entryTime, exitTime);
                double hours = duration.toMinutes() / 60.0;
                double fee = calculateFee(hours);

                System.out.println("Spot #" + i + " freed, Duration: " +
                        duration.toHoursPart() + "h " + duration.toMinutesPart() + "m, Fee: $" + String.format("%.2f", fee));

                spots[i].status = SpotStatus.DELETED;
                spots[i].licensePlate = null;
                spots[i].entryTime = null;
                parkedVehicles--;
                return fee;
            }
        }

        System.out.println("Vehicle " + licensePlate + " not found!");
        return 0;
    }

    private double calculateFee(double hours) {
        double rate = 5.0; // $5 per hour
        return Math.ceil(hours) * rate;
    }

    public synchronized void getStatistics() {
        double occupancy = (parkedVehicles * 100.0) / capacity;
        double avgProbes = parkedVehicles == 0 ? 0 : ((double) totalProbes / parkedVehicles);
        System.out.println("Occupancy: " + String.format("%.1f", occupancy) + "%, Avg Probes: " + String.format("%.2f", avgProbes));
    }

    public static void main(String[] args) throws InterruptedException {

        week1week2 parkingLot = new week1week2(500);

        // Park vehicles
        parkingLot.parkVehicle("ABC-1234");
        parkingLot.parkVehicle("ABC-1235");
        parkingLot.parkVehicle("XYZ-9999");

        Thread.sleep(2000); // simulate parking time

        // Exit vehicle
        parkingLot.exitVehicle("ABC-1234");

        // Statistics
        parkingLot.getStatistics();
    }
}