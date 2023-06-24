import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Positional_Index
{
     Set<String> links = new HashSet<>();
     static Map<String, DictEntry> Positional = new HashMap<>();

    public static void build(List<String> fileNames) throws IOException {
        int docId = 0;
        for (String filename : fileNames) {
            docId++;
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            String line;
            int position = 0;
            while ((line = br.readLine()) != null) {
                String[] arrayWords = line.split(" ");

                for (String word : arrayWords) {
                    word = word.toLowerCase();
                    if (!Positional.containsKey(word)) {
                        Positional.put(word, new DictEntry(word));
                    }

                    DictEntry entry = Positional.get(word);
                    entry.incrementTermFrequency();
                    entry.getPositions().add(new posting(docId, position));
                    position++;
                }
            }

            br.close();
        }
    }


    public static ArrayList<String> split(String words) {
        ArrayList<String> Words = new ArrayList<String>();
        String[] splitword = words.split(" ");
        for (String Word : splitword) {
            Word = Word.toLowerCase();
            Words.add(Word);

        }

        return Words;
    }





    public static Double CosineSimilarity(String Doc1, String Doc2) {
        int firstDocCount = 0, secondDocCount = 0;
        int count ;
        int sum = 0;
        double similarity = 0;
        double sumDoc1 = 0, sumDoc2 = 0;
        ArrayList<String> doc1 = split(Doc1);
        ArrayList<String> doc2 = split(Doc2);
        HashSet<String> AllWords = new HashSet<>(doc1);
        AllWords.addAll(doc2);
        for (String word : AllWords)
        {
            for (String w : doc1)
            {
                if (word.equals(w))
                {
                    firstDocCount++;
                }
            }
            for (String w : doc2)
            {
                if (word.equals(w)) {
                    secondDocCount++;
                }
            }

            sumDoc1 += Math.pow(firstDocCount, 2.0);
            sumDoc2 += Math.pow(secondDocCount, 2.0);
            count = firstDocCount * secondDocCount;
            sum += count;
            firstDocCount = secondDocCount = 0;
        }
        sumDoc1 = Math.sqrt(sumDoc1);
        sumDoc2 = Math.sqrt(sumDoc2);

        similarity =( sum / (sumDoc1 * sumDoc2));


        return similarity;
    }









    public static void search(String query) {
        String[] words = query.toLowerCase().split(" ");

        List<Integer> docIds = new ArrayList<>();

        if (words.length == 1) {
            // Single-word query
            String word = words[0];
            if (Positional.containsKey(word)) {
                DictEntry entry = Positional.get(word);
                for (posting position : entry.getPositions()) {
                    docIds.add(position.getDocId());
                }
            } else {
                System.out.println("The word " + word + " was not found in any file");
                return;
            }
        } else {
            // Multi-word query
            String firstWord = words[0];
            if (Positional.containsKey(firstWord)) {
                DictEntry entry = Positional.get(firstWord);

                for (posting position : entry.getPositions()) {
                    int docId = position.getDocId();
                    boolean adjacent = true;

                    for (int i = 1; i < words.length; i++) {
                        String currentWord = words[i];
                        boolean found = false;

                        if (Positional.containsKey(currentWord)) {
                            DictEntry currentEntry = Positional.get(currentWord);
                            for (posting currentPosition : currentEntry.getPositions()) {
                                if (currentPosition.getDocId() == docId && currentPosition.getPosition() == position.getPosition() + i) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            adjacent = false;
                            break;
                        }
                    }

                    if (adjacent) {
                        docIds.add(docId);
                    }
                }
            } else {
                System.out.println("The word " + firstWord + " was not found in any file");
                return;
            }
        }

        if (!docIds.isEmpty()) {
            System.out.println("Query: " + query + " appears adjacent in " + docIds.size() + " documents");

            for (int docId : docIds) {
                System.out.print("file" + docId + "\t\n");
            }
        } else {
            System.out.println("The query " + query + " was not found in any file \n"+ "\t\t");
        }
    }

    public static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }

        reader.close();

        return content.toString();
    }



    public void getPageLinks(String URL, int depth)
    {
        if (depth > 0) {
            if (!links.contains(URL)) {
                try {
                    if (links.add(URL)) {
                        System.out.println(URL);

                    }

                    Document document = Jsoup.connect(URL).get();
                    Elements linksOnPage = document.select("a[href]");

                    int count = 0; // Variable to keep track of the number of links processed
                    for (Element page : linksOnPage)
                    {
                        if (count >= depth)
                        {
                            break; // Stop processing links if the limit has been reached
                        }
                        int newdepth=depth-1;

                        getPageLinks(page.attr("abs:href"), newdepth);
                        count++;

                    }
                } catch (IOException e) {
                    System.err.println("For " + URL + ": " + e.getMessage());
                }
            }
        }
    }



    public static double calculateTFIDF(int termFrequencyInDocument, int totalTermsInDocument, int documentFrequency, int totalDocuments) {
        double tf = (double) termFrequencyInDocument / totalTermsInDocument;
        double idf = Math.log10((double) totalDocuments / (documentFrequency + 1)); //total term in all doc
        return tf * idf;
    }



    public static int calculateTotalTermsInDocument(String document)  //size of doc
    {
        String[] words = document.toLowerCase().split(" ");
        return words.length;
    }

    public static int calculateTermFrequencyInDocument(String word, String fileContent)  //term in 1 doc
    {
        int termFrequency = 0;
        String[] words = fileContent.toLowerCase().split(" ");
        for (String w : words)
        {
            if (w.equals(word)) {
                termFrequency++;
            }
        }
        return termFrequency;
    }


    public static int calculate_Term_in_All_Documents(String word, List<String> fileNames) throws IOException  //term in all document
    {
        int documentFrequency = 0;

        for (String fileName : fileNames)
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            String line;

            while ((line = br.readLine()) != null)
            {
                String[] arrayWords = line.split(" ");
                for (String w : arrayWords)
                {
                    if (w.equalsIgnoreCase(word)) {
                        documentFrequency++;
                        break;
                    }
                }
            }

            br.close();
        }

        return documentFrequency;
    }



    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter a query you want to search for: ");
        String query = input.nextLine();

        List<String> fileNames = new ArrayList<>();
        fileNames.add("file1.txt");
        fileNames.add("file2.txt");
        fileNames.add("file3.txt");
        fileNames.add("file4.txt");
        fileNames.add("file5.txt");
        fileNames.add("file6.txt");
        fileNames.add("file7.txt");
        fileNames.add("file8.txt");
        fileNames.add("file9.txt");
        fileNames.add("file10.txt");

        build(fileNames);
        search(query);


        List<Result> results = new ArrayList<>();

        for (String fileName : fileNames) {
            String fileContent = readFile(fileName); // Read the content of the file
            Double score = CosineSimilarity(fileContent, query); // Calculate cosine similarity
            results.add(new Result(fileName, score));
        }

        // Sort the results based on the score in descending order
        Collections.sort(results, Comparator.comparing(Result::getScore).reversed());

        for (Result result : results) {
            String fileName = result.getFileName();
            Double score = result.getScore();
            System.out.println("Similarity score for " + fileName + ": " + score);

        }



        Scanner input1 = new Scanner(System.in);
        System.out.println("Enter a query you want to search for TF_IDF: ");
        String query1 = input1.nextLine();




        // Calculate TF-IDF for every word in the query
        Map<String, Double> tfidfScores = new HashMap<>();
        int totalDocuments = fileNames.size();
        for (String word : query1.toLowerCase().split(" "))
        {

            int documentFrequency = calculate_Term_in_All_Documents(word, fileNames);


            for (String fileName : fileNames) {
                String fileContent = readFile(fileName);
                int totalTermsInDocument = calculateTotalTermsInDocument(fileContent);
                int termFrequencyInDocument = calculateTermFrequencyInDocument(word, fileContent);
                double tfidf = calculateTFIDF(termFrequencyInDocument, totalTermsInDocument, documentFrequency, totalDocuments);
                tfidfScores.put(word, tfidf);
                System.out.println("Word: " + word + " - Document: " + fileName + " - TF-IDF Score: " + tfidf);
            }


        }

        Positional_Index post = new Positional_Index();
        String startingURL = "https://www.facebook.com/";

        int depth = 5; // Specify the link limit
        post.getPageLinks(startingURL, depth);
    }



}






