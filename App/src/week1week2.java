import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class week1week2 {

    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime time;

        Transaction(int id, double amount, String merchant, String account, String timeStr) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    private List<Transaction> transactions;

    public week1week2() {
        transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum
    public List<int[]> findTwoSum(double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum within 1-hour window
    public List<int[]> findTwoSumWithinWindow(double target, Duration window) {
        List<int[]> result = new ArrayList<>();
        transactions.sort(Comparator.comparing(t -> t.time));

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);
                if (Duration.between(t1.time, t2.time).abs().compareTo(window) > 0) break;
                if (Math.abs(t1.amount + t2.amount - target) < 1e-6) {
                    result.add(new int[]{t1.id, t2.id});
                }
            }
        }
        return result;
    }

    // K-Sum using recursion
    public List<List<Integer>> findKSum(double target, int k) {
        List<List<Integer>> result = new ArrayList<>();
        transactions.sort(Comparator.comparingDouble(t -> t.amount));
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k, double target, List<Integer> path, List<List<Integer>> result) {
        if (k == 2) {
            int left = start, right = transactions.size() - 1;
            while (left < right) {
                double sum = transactions.get(left).amount + transactions.get(right).amount;
                if (Math.abs(sum - target) < 1e-6) {
                    List<Integer> pair = new ArrayList<>(path);
                    pair.add(transactions.get(left).id);
                    pair.add(transactions.get(right).id);
                    result.add(pair);
                    left++;
                    right--;
                } else if (sum < target) left++;
                else right--;
            }
            return;
        }

        for (int i = start; i < transactions.size() - k + 1; i++) {
            path.add(transactions.get(i).id);
            kSumHelper(i + 1, k - 1, target - transactions.get(i).amount, path, result);
            path.remove(path.size() - 1);
        }
    }

    // Duplicate detection: same amount, same merchant, different accounts
    public List<Map<String, Object>> detectDuplicates() {
        Map<String, Map<Double, Set<String>>> merchantAmountMap = new HashMap<>();
        List<Map<String, Object>> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            merchantAmountMap.putIfAbsent(t.merchant, new HashMap<>());
            Map<Double, Set<String>> amountMap = merchantAmountMap.get(t.merchant);
            amountMap.putIfAbsent(t.amount, new HashSet<>());
            Set<String> accounts = amountMap.get(t.amount);
            accounts.add(t.account);
        }

        for (Map.Entry<String, Map<Double, Set<String>>> entry : merchantAmountMap.entrySet()) {
            String merchant = entry.getKey();
            for (Map.Entry<Double, Set<String>> amountEntry : entry.getValue().entrySet()) {
                if (amountEntry.getValue().size() > 1) {
                    Map<String, Object> dup = new HashMap<>();
                    dup.put("merchant", merchant);
                    dup.put("amount", amountEntry.getKey());
                    dup.put("accounts", amountEntry.getValue());
                    duplicates.add(dup);
                }
            }
        }
        return duplicates;
    }

    public static void main(String[] args) {
        week1week2 processor = new week1week2();

        processor.addTransaction(new Transaction(1, 500, "Store A", "acc1", "10:00"));
        processor.addTransaction(new Transaction(2, 300, "Store B", "acc2", "10:15"));
        processor.addTransaction(new Transaction(3, 200, "Store C", "acc3", "10:30"));
        processor.addTransaction(new Transaction(4, 500, "Store A", "acc2", "11:00"));

        System.out.println("Two-Sum (500): " + processor.findTwoSum(500));
        System.out.println("Two-Sum 1hr window (500): " + processor.findTwoSumWithinWindow(500, Duration.ofHours(1)));
        System.out.println("K-Sum k=3 target=1000: " + processor.findKSum(1000, 3));
        System.out.println("Duplicate Detection: " + processor.detectDuplicates());
    }
}