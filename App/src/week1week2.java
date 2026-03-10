import java.util.*;

public class week1week2 {

    // username -> userId
    private HashMap<String, Integer> usernameMap = new HashMap<>();

    // username -> attempt frequency
    private HashMap<String, Integer> attemptMap = new HashMap<>();

    // Register a user
    public void registerUser(String username, int userId) {
        usernameMap.put(username, userId);
    }

    // Check if username is available
    public boolean checkAvailability(String username) {

        // increase attempt count
        attemptMap.put(username, attemptMap.getOrDefault(username, 0) + 1);

        return !usernameMap.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        // append numbers
        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;

            if (!usernameMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        // replace "_" with "."
        String dotVersion = username.replace("_", ".");
        if (!usernameMap.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {

        String result = null;
        int max = 0;

        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {

            if (entry.getValue() > max) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }

        return result + " (" + max + " attempts)";
    }

    public static void main(String[] args) {

        week1week2 checker = new week1week2();

        // existing users
        checker.registerUser("john_doe", 101);
        checker.registerUser("admin", 1);

        // availability checks
        System.out.println("john_doe available: " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available: " + checker.checkAvailability("jane_smith"));

        // suggestions
        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // simulate multiple attempts
        for (int i = 0; i < 10; i++) {
            checker.checkAvailability("admin");
        }

        // most attempted username
        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}