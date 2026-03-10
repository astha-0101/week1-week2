import java.util.*;

public class week1week2 {

    static class VideoData {
        String videoId;
        String content; // Simulated video content
        public VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;

    // L1 cache: LinkedHashMap with access-order for LRU
    private LinkedHashMap<String, VideoData> L1Cache;

    // L2 cache: HashMap simulating SSD with LRU via LinkedList
    private Map<String, VideoData> L2Cache;
    private LinkedList<String> L2LRU;
    private Map<String, Integer> accessCount; // track accesses for promotion

    // L3: Database simulation
    private Map<String, VideoData> database;

    // Stats
    private int L1Hits = 0, L1Miss = 0;
    private int L2Hits = 0, L2Miss = 0;
    private int L3Hits = 0, L3Miss = 0;
    private double L1Time = 0, L2Time = 0, L3Time = 0;

    public week1week2() {
        L1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_CAPACITY;
            }
        };
        L2Cache = new HashMap<>();
        L2LRU = new LinkedList<>();
        accessCount = new HashMap<>();
        database = new HashMap<>();
    }

    // Simulate adding videos to database
    public void addVideoToDB(String videoId, String content) {
        database.put(videoId, new VideoData(videoId, content));
    }

    public VideoData getVideo(String videoId) {
        long start = System.nanoTime();

        // Check L1
        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            L1Time += 0.5; // ms
            return L1Cache.get(videoId);
        } else {
            L1Miss++;
        }

        // Check L2
        if (L2Cache.containsKey(videoId)) {
            L2Hits++;
            L2Time += 5; // ms
            // Update access count
            accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);

            // LRU update
            L2LRU.remove(videoId);
            L2LRU.addLast(videoId);

            // Promote if frequent
            if (accessCount.get(videoId) > 3) {
                L1Cache.put(videoId, L2Cache.get(videoId));
            }
            return L2Cache.get(videoId);
        } else {
            L2Miss++;
        }

        // L3 database
        if (database.containsKey(videoId)) {
            L3Hits++;
            L3Time += 150; // ms

            VideoData video = database.get(videoId);

            // Add to L2 cache
            if (L2Cache.size() >= L2_CAPACITY) {
                // Evict least recently used
                String evict = L2LRU.removeFirst();
                L2Cache.remove(evict);
                accessCount.remove(evict);
            }
            L2Cache.put(videoId, video);
            L2LRU.addLast(videoId);
            accessCount.put(videoId, 1);

            return video;
        } else {
            L3Miss++;
            return null; // video not found
        }
    }

    public void invalidateVideo(String videoId) {
        L1Cache.remove(videoId);
        L2Cache.remove(videoId);
        accessCount.remove(videoId);
        database.remove(videoId);
        L2LRU.remove(videoId);
    }

    public void getStatistics() {
        int L1Total = L1Hits + L1Miss;
        int L2Total = L2Hits + L2Miss;
        int L3Total = L3Hits + L3Miss;

        double L1HitRate = L1Total == 0 ? 0 : (L1Hits * 100.0 / L1Total);
        double L2HitRate = L2Total == 0 ? 0 : (L2Hits * 100.0 / L2Total);
        double L3HitRate = L3Total == 0 ? 0 : (L3Hits * 100.0 / L3Total);

        double overallHits = L1Hits + L2Hits + L3Hits;
        double overallTotal = overallHits + L1Miss + L2Miss + L3Miss;
        double overallHitRate = overallTotal == 0 ? 0 : (overallHits * 100.0 / overallTotal);

        double avgTime = (L1Time + L2Time + L3Time) / overallTotal;

        System.out.printf("L1: Hit Rate %.1f%%, Avg Time: %.1fms\n", L1HitRate, L1Time / L1Total);
        System.out.printf("L2: Hit Rate %.1f%%, Avg Time: %.1fms\n", L2HitRate, L2Time / L2Total);
        System.out.printf("L3: Hit Rate %.1f%%, Avg Time: %.1fms\n", L3HitRate, L3Time / L3Total);
        System.out.printf("Overall: Hit Rate %.1f%%, Avg Time: %.1fms\n", overallHitRate, avgTime);
    }

    public static void main(String[] args) {
        week1week2 cacheSystem = new week1week2();

        // Add videos to DB
        cacheSystem.addVideoToDB("video_123", "Video Content 123");
        cacheSystem.addVideoToDB("video_999", "Video Content 999");

        // Simulate requests
        cacheSystem.getVideo("video_123"); // L1 MISS, L2 MISS, L3 HIT
        cacheSystem.getVideo("video_123"); // L1 HIT
        cacheSystem.getVideo("video_999"); // L1 MISS, L2 MISS, L3 HIT

        cacheSystem.getStatistics();
    }
}