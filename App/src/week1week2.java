import java.util.*;

public class week1week2 {

    static class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, int ttl) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = this.timestamp + (ttl * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;

    private LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;
    private long totalLookupTime = 0;
    private int totalRequests = 0;

    public week1week2(int capacity) {
        this.capacity = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > week1week2.this.capacity;
            }
        };

        startCleanupThread();
    }

    public synchronized String resolve(String domain) {

        long start = System.nanoTime();

        if (cache.containsKey(domain)) {

            DNSEntry entry = cache.get(domain);

            if (!entry.isExpired()) {
                hits++;
                totalRequests++;
                totalLookupTime += (System.nanoTime() - start);
                System.out.println("Cache HIT → " + entry.ipAddress);
                return entry.ipAddress;
            } else {
                cache.remove(domain);
                System.out.println("Cache EXPIRED → Query upstream");
            }
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        int ttl = 300;

        DNSEntry newEntry = new DNSEntry(domain, ip, ttl);
        cache.put(domain, newEntry);

        totalRequests++;
        totalLookupTime += (System.nanoTime() - start);

        System.out.println("Cache MISS → " + ip + " (TTL: " + ttl + "s)");

        return ip;
    }

    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        Random rand = new Random();
        return "172.217.14." + rand.nextInt(255);
    }

    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);

                    synchronized (week1week2.this) {

                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                        while (it.hasNext()) {
                            Map.Entry<String, DNSEntry> entry = it.next();

                            if (entry.getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    public void getCacheStats() {

        double hitRate = (totalRequests == 0) ? 0 : ((double) hits / totalRequests) * 100;
        double avgLookup = (totalRequests == 0) ? 0 : (totalLookupTime / totalRequests) / 1_000_000.0;

        System.out.println("Cache Stats:");
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
        System.out.println("Average Lookup Time: " + String.format("%.3f", avgLookup) + " ms");
    }

    public static void main(String[] args) throws Exception {

        week1week2 dnsCache = new week1week2(5);

        dnsCache.resolve("google.com");
        dnsCache.resolve("google.com");

        dnsCache.resolve("facebook.com");
        dnsCache.resolve("youtube.com");

        dnsCache.resolve("google.com");

        dnsCache.getCacheStats();
    }
}