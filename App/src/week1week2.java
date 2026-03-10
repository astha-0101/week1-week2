import java.io.*;
import java.nio.file.*;
import java.util.*;

public class week1week2 {

    static class Document {
        String id;
        List<String> words;
        Set<String> ngrams;

        Document(String id, List<String> words) {
            this.id = id;
            this.words = words;
            this.ngrams = new HashSet<>();
        }
    }

    private Map<String, Set<String>> ngramIndex; // ngram -> set of document IDs
    private Map<String, Document> documents;     // document ID -> Document
    private int nGramSize;

    public week1week2(int nGramSize) {
        this.ngramIndex = new HashMap<>();
        this.documents = new HashMap<>();
        this.nGramSize = nGramSize;
    }

    // Load document and index its n-grams
    public void addDocument(String docId, String content) {
        List<String> words = Arrays.asList(content.split("\\s+"));
        Document doc = new Document(docId, words);

        for (int i = 0; i <= words.size() - nGramSize; i++) {
            List<String> ngramWords = words.subList(i, i + nGramSize);
            String ngram = String.join(" ", ngramWords);
            doc.ngrams.add(ngram);

            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }

        documents.put(docId, doc);
        System.out.println("Indexed " + doc.ngrams.size() + " n-grams for " + docId);
    }

    // Analyze a new document for plagiarism
    public void analyzeDocument(String docId, String content) {
        List<String> words = Arrays.asList(content.split("\\s+"));
        Document newDoc = new Document(docId, words);

        Set<String> matchedDocs = new HashSet<>();
        Map<String, Integer> matchCounts = new HashMap<>();

        for (int i = 0; i <= words.size() - nGramSize; i++) {
            String ngram = String.join(" ", words.subList(i, i + nGramSize));

            if (ngramIndex.containsKey(ngram)) {
                Set<String> docIds = ngramIndex.get(ngram);
                for (String otherDocId : docIds) {
                    matchCounts.put(otherDocId, matchCounts.getOrDefault(otherDocId, 0) + 1);
                    matchedDocs.add(otherDocId);
                }
            }
        }

        System.out.println("Extracted " + (words.size() - nGramSize + 1) + " n-grams from " + docId);

        for (String otherDocId : matchedDocs) {
            int matches = matchCounts.get(otherDocId);
            int total = documents.get(otherDocId).ngrams.size();
            double similarity = (matches * 100.0) / total;

            String status = similarity > 50 ? "PLAGIARISM DETECTED" :
                    (similarity > 10 ? "suspicious" : "low");

            System.out.printf("→ Found %d matching n-grams with \"%s\"\n", matches, otherDocId);
            System.out.printf("→ Similarity: %.1f%% (%s)\n", similarity, status);
        }
    }

    public static void main(String[] args) throws IOException {

        week1week2 detector = new week1week2(5); // 5-grams

        // Simulate loading previous documents
        detector.addDocument("essay_089.txt", "This is a sample essay about Java programming and data structures.");
        detector.addDocument("essay_092.txt", "Data structures and algorithms are essential for coding interviews and Java programming.");
        detector.addDocument("essay_101.txt", "Machine learning and AI are rapidly growing fields in computer science.");

        // Analyze a new document
        String newEssay = "Java programming and data structures are essential for coding interviews and algorithms.";
        detector.analyzeDocument("essay_123.txt", newEssay);
    }
}