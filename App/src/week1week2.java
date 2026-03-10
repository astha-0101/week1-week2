import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class FlashSaleInventoryManager {

    // HashMap for product stock
    private ConcurrentHashMap<String, AtomicInteger> inventory = new ConcurrentHashMap<>();

    // Waiting list FIFO
    private ConcurrentHashMap<String, LinkedHashMap<Integer, Integer>> waitingList = new ConcurrentHashMap<>();

    // Add product with stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedHashMap<>());
    }

    // Instant stock check
    public int checkStock(String productId) {
        if (!inventory.containsKey(productId)) {
            return -1;
        }
        return inventory.get(productId).get();
    }

    // Purchase item
    public synchronized String purchaseItem(String productId, int userId) {

        if (!inventory.containsKey(productId)) {
            return "Product not found";
        }

        AtomicInteger stock = inventory.get(productId);

        // If stock available
        if (stock.get() > 0) {
            int remaining = stock.decrementAndGet();
            return "Success, " + remaining + " units remaining";
        }

        // Stock finished → add to waiting list
        LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);

        queue.put(userId, queue.size() + 1);

        return "Added to waiting list, position #" + queue.size();
    }

    // Show waiting list
    public void showWaitingList(String productId) {
        LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);

        for (Map.Entry<Integer, Integer> entry : queue.entrySet()) {
            System.out.println("User " + entry.getKey() + " -> Position " + entry.getValue());
        }
    }
}

public class week1week2 {

    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        // Check stock
        System.out.println("Stock Available: " + manager.checkStock("IPHONE15_256GB"));

        // Simulate purchases
        for (int i = 1; i <= 105; i++) {
            String result = manager.purchaseItem("IPHONE15_256GB", i);
            System.out.println("User " + i + " -> " + result);
        }

        // Show waiting list
        System.out.println("\nWaiting List:");
        manager.showWaitingList("IPHONE15_256GB");
    }
}