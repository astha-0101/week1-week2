import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class week1week2 {

    static class TokenBucket {
        private final int maxTokens;
        private final double refillRatePerMs; // tokens per ms
        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, int refillPerHour) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillRatePerMs = refillPerHour / 3600000.0; // per ms
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean allowRequest() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            } else {
                return false;
            }
        }

        public synchronized int tokensRemaining() {
            refill();
            return (int) tokens;
        }

        public synchronized long timeUntilResetMs() {
            refill();
            if (tokens >= maxTokens) return 0;
            return (long) ((maxTokens - tokens) / refillRatePerMs);
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            double refillTokens = elapsed * refillRatePerMs;
            tokens = Math.min(maxTokens, tokens + refillTokens);
            lastRefillTime = now;
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> buckets;
    private final int maxTokensPerClient;
    private final int refillPerHour;

    public week1week2(int maxTokensPerClient, int refillPerHour) {
        this.buckets = new ConcurrentHashMap<>();
        this.maxTokensPerClient = maxTokensPerClient;
        this.refillPerHour = refillPerHour;
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, id -> new TokenBucket(maxTokensPerClient, refillPerHour));

        boolean allowed = bucket.allowRequest();
        int remaining = bucket.tokensRemaining();
        long resetMs = bucket.timeUntilResetMs();

        if (allowed) {
            return "Allowed (" + remaining + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after " + (resetMs / 1000) + "s)";
        }
    }

    public Map<String, Object> getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, id -> new TokenBucket(maxTokensPerClient, refillPerHour));
        Map<String, Object> status = new HashMap<>();
        status.put("used", maxTokensPerClient - bucket.tokensRemaining());
        status.put("limit", maxTokensPerClient);
        status.put("reset", (System.currentTimeMillis() + bucket.timeUntilResetMs()) / 1000); // epoch seconds
        return status;
    }

    public static void main(String[] args) throws InterruptedException {

        week1week2 rateLimiter = new week1week2(1000, 1000); // 1000 requests per hour

        String clientId = "abc123";

        // Simulate 3 requests
        System.out.println(rateLimiter.checkRateLimit(clientId));
        System.out.println(rateLimiter.checkRateLimit(clientId));
        System.out.println(rateLimiter.checkRateLimit(clientId));

        // Show status
        Map<String, Object> status = rateLimiter.getRateLimitStatus(clientId);
        System.out.println("Rate Limit Status: " + status);

        // Simulate exceeding limit
        week1week2 smallLimit = new week1week2(3, 3); // 3 requests per hour
        String c = "xyz999";
        System.out.println(smallLimit.checkRateLimit(c));
        System.out.println(smallLimit.checkRateLimit(c));
        System.out.println(smallLimit.checkRateLimit(c));
        System.out.println(smallLimit.checkRateLimit(c)); // should be denied
    }
}