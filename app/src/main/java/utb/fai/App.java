package utb.fai;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class App {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing parameter - start URL");
            return;
        }

        LinkedList<URIinfo> foundURIs = new LinkedList<>();
        HashSet<URI> visitedURIs = new HashSet<>();
        URI uri;
        final int maxDepth = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        final int debugLevel = args.length > 2 ? Integer.parseInt(args[2]) : 0;

        try {
            uri = new URI(args[0]);
            foundURIs.add(new URIinfo(uri, 0));
            visitedURIs.add(uri);

            ExecutorService executor = Executors.newFixedThreadPool(10);
            ParserCallback callBack = new ParserCallback(visitedURIs, foundURIs, maxDepth, debugLevel);

            while (!foundURIs.isEmpty()) {
                URIinfo URIinfo = foundURIs.removeFirst();
                final URI currentUri = URIinfo.uri;
                callBack.depth = URIinfo.depth;

                if (callBack.depth > maxDepth) {
                    continue;
                }

                executor.execute(() -> {
                    if (debugLevel > 0) {
                        System.err.println("Analyzing " + currentUri);
                    }
                    try {
                        Document doc = Jsoup.connect(currentUri.toString())
                                .userAgent("Mozilla/5.0")
                                .get();
                        callBack.handleDocument(doc);
                    } catch (IOException e) {
                        System.err.println("Error analyzing page: " + currentUri);
                    }
                });
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Output the word frequency results
            callBack.printWordFrequency();

        } catch (Exception e) {
            System.err.println("Unhandled exception, terminating...");
            e.printStackTrace();
        }
    }
}
