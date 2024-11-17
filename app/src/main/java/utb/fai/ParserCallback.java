package utb.fai;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class ParserCallback {

    URI pageURI;
    int depth = 0, maxDepth = 5;
    HashSet<URI> visitedURIs;
    LinkedList<URIinfo> foundURIs;
    int debugLevel = 0;
    private HashMap<String, Integer> wordFrequency = new HashMap<>();
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "to", "of", "in", "is", "for", "on", "that", "with", "as", "it", "at", "by", "an", "be", "this", "which", "or", "from", "but", "are", "not", "have", "was", "were", "has", "had", "a", "i", "you", "he", "she", "they", "we", "do", "does", "did", "will", "would", "can", "could", "should", "may", "might"
    );

    ParserCallback(HashSet<URI> visitedURIs, LinkedList<URIinfo> foundURIs, int maxDepth, int debugLevel) {
        this.foundURIs = foundURIs;
        this.visitedURIs = visitedURIs;
        this.maxDepth = maxDepth;
        this.debugLevel = debugLevel;
    }

    public void handleDocument(Document document) {
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String href = link.attr("abs:href");
            try {
                URI uri = new URI(href);
                if (!uri.isOpaque() && !visitedURIs.contains(uri) && uri.getHost() != null) {
                    visitedURIs.add(uri);
                    foundURIs.add(new URIinfo(uri, depth + 1));
                    if (debugLevel > 1) {
                        System.err.println("Adding URI: " + uri.toString());
                    }
                }
            } catch (Exception e) {
                if (debugLevel > 1) {
                    System.err.println("Found incorrect URI: " + href);
                }
            }
        }

        String[] words = document.text().toLowerCase().split("\\W+");
        for (String word : words) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
    }

    public void printWordFrequency() {
        wordFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(20)
                .forEach(entry -> System.out.println(entry.getKey() + ";" + entry.getValue()));
    }
}
