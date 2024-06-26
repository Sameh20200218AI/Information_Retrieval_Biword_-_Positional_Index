/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;

import java.util.*;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    static int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.
    static Map<String, Map<Integer, Map<Integer ,Integer>>> positionalIndex ;

    public HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    /**
     * The function `buildPositionalIndex` reads multiple files, processes each word's position in the
     * documents, and builds a positional index mapping words to document IDs and positions.
     * 
     * @param files The `buildPositionalIndex` method you provided is designed to build a positional
     * index from the content of the files specified in the `files` array. The method reads each file,
     * processes its content line by line, and constructs a positional index mapping words to the
     * document IDs and positions where they occur
     * @return The method `buildPositionalIndex` is returning a `Map<String, Map<Integer, Map<Integer,
     * Integer>>>` which represents a positional index built from the provided array of file names. The
     * positional index maps words to document IDs, where each document ID maps to positions of the
     * word within the document.
     */
    public  Map<String, Map<Integer, Map<Integer ,Integer>>> buildPositionalIndex(String[] files) {
        Map<String, Map<Integer, Map<Integer ,Integer>>> positionalIndex = new HashMap<>();
        int fid = 0;  // Initialize the document ID
        for (String fileName : files) {  // Iterate through each file in the provided array of file names
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {  // Open the file for reading
                if (!sources.containsKey(fid)) {  // Check if the document ID is not already present in the sources map
                    // If not present, add a new entry to the sources map with the document ID and file name
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;  // Variable to store each line read from the file
                int flen = 0;  // Initialize the length of the current document
                // Read each line from the file until reaching the end
                int line =0 ;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    // Loop through each word in the document
                    for (int position = 0; position < words.length; position++) {
                        String word = words[position];

                        // If the word is not in the positional index, add it
                        if (!positionalIndex.containsKey(word)) {
                            positionalIndex.put(word, new HashMap<>());
                        }

                        // Get the map of document IDs and positions for the word
                        Map<Integer, Map<Integer,Integer>> documentMap = positionalIndex.get(word);

                        // If the document ID is not in the map, add it
                        if (!documentMap.containsKey(fid)) {
                            documentMap.put(fid, new HashMap<>());
                        }

                        // Add the position of the word in the document
                        documentMap.get(fid).put(line , position);
                    } // Process each line to update the inverted index and increment the document length
                line++;
                }
                // Update the document length for the current document in the sources map
                sources.get(fid).length = flen;

            } catch (IOException e) {  // Catch any IOException that occurs (e.g., file not found)
                System.out.println("File " + fileName + " not found. Skip it");  // Print a message indicating the file was not found
            }
            fid++;  // Increment the document ID for the next file

        }

        System.out.println(positionalIndex);
        return this.positionalIndex = positionalIndex;
    }

    /**
     * Search for a sentence in the positional index.
     *
     * @param sentence the sentence to search for
     * @return a map where the key is the document ID and the value is a list of starting positions
     *         where the sentence occurs in the document
     */
    public Map<Integer, Map<Integer, List<Integer>>> searchSentence(String sentence) {
        // Split the sentence into words
        String[] words = sentence.split("\\W+");
        // Create a map to store results
        Map<Integer, Map<Integer, List<Integer>>> results = new HashMap<>();

        if (words.length == 0) {
            return results;
        }

        // Iterate through each word in the sentence
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            // Check if the word exists in the positional index
            if (!positionalIndex.containsKey(word)) {
                // If any word in the sentence does not exist, there can't be any matching sentences
                return results;
            }
        }

        // Use the positions of the first word in the sentence as a starting point
        Map<Integer, Map<Integer, Integer>> firstWordPositions = positionalIndex.get(words[0]);

        // Iterate through each document where the first word appears
        for (Map.Entry<Integer, Map<Integer, Integer>> docEntry : firstWordPositions.entrySet()) {
            int docID = docEntry.getKey();
            Map<Integer, Integer> firstWordLinePositions = docEntry.getValue();

            // Create a map to hold line positions in the document
            Map<Integer, List<Integer>> linePositionMap = new HashMap<>();

            // Iterate through each line where the first word appears
            for (Map.Entry<Integer, Integer> lineEntry : firstWordLinePositions.entrySet()) {
                int lineNum = lineEntry.getKey();
                int startPosition = lineEntry.getValue();

                // Check if the rest of the words in the sentence appear in sequence on the same line
                boolean match = true;
                for (int i = 1; i < words.length; i++) {
                    Map<Integer, Map<Integer, Integer>> wordPositions = positionalIndex.get(words[i]);
                    if (wordPositions == null || !wordPositions.containsKey(docID)) {
                        match = false;
                        break;
                    }

                    Map<Integer, Integer> linePositions = wordPositions.get(docID);
                    // Check if the next word appears on the same line
                    if (!linePositions.containsKey(lineNum)) {
                        match = false;
                        break;
                    }

                    int nextPosition = linePositions.get(lineNum);
                    // Check if the next word appears at the expected position
                    if (nextPosition != startPosition + i) {
                        match = false;
                        break;
                    }
                }

                // If the words match in sequence on the same line, add the starting position to the results
                if (match) {
                    linePositionMap
                            .computeIfAbsent(lineNum, k -> new ArrayList<>())
                            .add(startPosition);
                }
            }

            // If there are matching positions, add them to the results
            if (!linePositionMap.isEmpty()) {
                results.put(docID, linePositionMap);
            }
        }

        return results;
    }

    //---------------------------------------------
    /**
     * The function `printPostingList` prints the document IDs in a posting list.
     * 
     * @param p Posting p is an object representing a posting list node in a linked list structure. Each
     * node contains a document ID (docId) and a reference to the next node in the list (next). The method
     * printPostingList is responsible for printing the document IDs in the posting list in the format [doc
     */
    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");
        while (p != null) {
            /// -4- **** complete here ****
            // fix get rid of the last comma
            System.out.print("" + p.docId + "," );
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    /**
     * The `printDictionary` function iterates through a dictionary, printing key-value pairs along
     * with associated document frequencies and posting lists.
     */
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }
 
    //-----------------------------------------------
    /**
     * The `buildIndex` function reads content from files on disk, indexes the content based on a
     * specified type, and updates document information in a sources map.
     * 
     * @param files The `files` parameter in the `buildIndex` method is an array of `String` that
     * contains the file names of the documents from which you want to build an index. The method
     * iterates through each file in the array, reads the content of the file, and indexes the content
     * based on
     * @param type The `type` parameter in the `buildIndex` method is a `Boolean` type that determines
     * which method to use for indexing each line of the file. If `type` is `true`, the method
     * `indexOneLineBI` is called to index each line. If `type` is
     */
    public void buildIndex(String[] files ,Boolean type) {  // from disk not from the internet
        int fid = 0;  // Initialize the document ID
        for (String fileName : files) {  // Iterate through each file in the provided array of file names
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {  // Open the file for reading
                if (!sources.containsKey(fid)) {  // Check if the document ID is not already present in the sources map
                    // If not present, add a new entry to the sources map with the document ID and file name
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;  // Variable to store each line read from the file
                int flen = 0;  // Initialize the length of the current document
                // Read each line from the file until reaching the end
                while ((ln = file.readLine()) != null) {
                    if(type) {
                        flen += indexOneLineBI(ln, fid);
                    }
                    else{
                        flen += indexOneLine(ln, fid);
                    }
                }
                // Update the document length for the current document in the sources map
                sources.get(fid).length = flen;

            } catch (IOException e) {  // Catch any IOException that occurs (e.g., file not found)
                System.out.println("File " + fileName + " not found. Skip it");  // Print a message indicating the file was not found
            }
            fid++;  // Increment the document ID for the next file

        }

        //   printDictionary();
    }
//-----------------------------------------------------------------------------
/**
 * The indexOneLine function processes a given line of text, extracting words, checking for stop words,
 * stemming words, updating a dictionary index with document frequencies, and printing specific
 * information for the word "lattice".
 * 
 * @param ln The `indexOneLine` method you provided seems to be part of a text indexing system. It
 * processes a given line of text to update an index with the words found in the line. The method
 * splits the input line into words, processes each word, and updates the index accordingly.
 * @param fid The `fid` parameter in the `indexOneLine` method stands for the document ID. It is used
 * to uniquely identify a document in the indexing process.
 * @return The method `indexOneLine` is returning the total number of words in the input line `ln`.
 */
public int indexOneLine(String ln, int fid) {
    int flen = 0;

    String[] words = ln.split("\\W+");
    //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
    flen += words.length;
    for (String word : words) {
        word = word.toLowerCase();
        if (stopWord(word)) {
            continue;
        }
        word = stemWord(word);
        // check to see if the word is not in the dictionary
        // if not add it
        if (!index.containsKey(word)) {
            index.put(word, new DictEntry());
        }
        // add document id to the posting list
        if (!index.get(word).postingListContains(fid)) {
            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
            if (index.get(word).pList == null) {
                index.get(word).pList = new Posting(fid);
                index.get(word).last = index.get(word).pList;
            } else {
                index.get(word).last.next = new Posting(fid);
                index.get(word).last = index.get(word).last.next;
            }
        } else {
            index.get(word).last.dtf += 1;
        }
        //set the term_fteq in the collection
        index.get(word).term_freq += 1;
        if (word.equalsIgnoreCase("lattice")) {

            System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
        }

    }
    return flen;
}
    //----------------------------------------------------------------------------
    /**
     * The `indexOneLineBI` function processes a given line of text to create an index of terms and
     * their frequencies in a document collection.
     * 
     * @param ln The `ln` parameter in the `indexOneLineBI` method represents a line of text that is
     * being processed. The method splits this line into words and performs various operations on them
     * to create an index for information retrieval purposes.
     * @param fid The `fid` parameter in the `indexOneLineBI` method stands for the document ID. It is
     * used to uniquely identify the document being processed in the indexing operation.
     * @return The method `indexOneLineBI` returns an integer value, which is the total number of words
     * processed in the input line `ln`.
     */
    public int indexOneLineBI(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+");
      //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        for (int i=0 ; i<flen-1 ;i++) {
            words[i] = words[i].toLowerCase();
            if (stopWord(words[i]) || stopWord(words[i+1])) {
                continue;
            }
            words[i] = stemWord(words[i]);
            words[i+1]=stemWord(words[i+1]);

            words[i] = words[i].concat(" ").concat(words[i+1]);

            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(words[i])) {
                index.put(words[i], new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(words[i]).postingListContains(fid)) {
                index.get(words[i]).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (index.get(words[i]).pList == null) {
                    index.get(words[i]).pList = new Posting(fid);
                    index.get(words[i]).last = index.get(words[i]).pList;
                } else {
                    index.get(words[i]).last.next = new Posting(fid);
                    index.get(words[i]).last = index.get(words[i]).last.next;
                }
            } else {
                index.get(words[i]).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(words[i]).term_freq += 1;
            if (words[i].equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(words[i]).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

//----------------------------------------------------------------------------  
    /**
     * The function `stopWord` checks if a given word is a stop word or has a length less than 2 in
     * Java.
     * 
     * @param word The `stopWord` method checks if a given word is a stop word or if it has a length
     * less than 2. If the word is one of the specified stop words or has a length less than 2, the
     * method returns `true`, indicating that it should be considered a stop word
     * @return The method `stopWord` returns `true` if the input word is a stop word (e.g., "the",
     * "to", "be") or if the word length is less than 2. Otherwise, it returns `false`.
     */
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------  

/**
 * The function `stemWord` currently returns the input word without stemming it.
 * 
 * @param word The `word` parameter in the `stemWord` method is a String representing a word that you
 * want to perform stemming on.
 * @return The `stemWord` method is currently returning the original `word` without any stemming
 * applied.
 */
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------  
/**
 * The intersect function takes two linked lists of Posting objects sorted by docID and returns a new
 * linked list containing the common docIDs between the two input lists.
 * 
 * @param pL1 Posting pL1 represents the first posting list, which contains document IDs in sorted
 * order.
 * @param pL2 Posting pL2 represents the second posting list that you want to find the intersection
 * with another posting list pL1. The function `intersect` compares the document IDs in both posting
 * lists and returns a new posting list containing the common document IDs.
 * @return The function `intersect` returns a new `Posting` object that contains the intersection of
 * the document IDs between the two input `Posting` objects `pL1` and `pL2`.
 */
    Posting intersect(Posting pL1, Posting pL2) {
///****  -1-   complete after each comment ****
        Posting answer = null;
        Posting last = null;
        // 2 while p1  != NIL and p2  != NIL
        while (pL1 != null && pL2 != null) {
            // 3 if docID ( p 1 ) = docID ( p2 )
            if (pL1.docId == pL2.docId) {
                // 4 then ADD ( answer, docID ( p1 ))
                if (answer == null) {
                    answer = new Posting(pL1.docId);
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId);
                    last = last.next;
                }
                // 5 p1 ← next ( p1 )
                pL1 = pL1.next;
                // 6 p2 ← next ( p2 )
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) { // 7 else if docID ( p1 ) < docID ( p2 )
                // 8 then p1 ← next ( p1 )
                pL1 = pL1.next;
            } else {
                // 9 else p2 ← next ( p2 )
                pL2 = pL2.next;
            }
        }
//      10 return answer
        return answer;
    }
    /**
     * The function `find_24_01` takes a phrase as input, splits it into words, checks if the words
     * exist in an index, intersects the posting lists of the words, and generates a result based on
     * the intersected posting lists.
     *
     * @param phrase It looks like the code you provided is a method that takes a phrase as input,
     * splits it into words, and then searches for documents that contain all the words in the phrase.
     * The method uses an index to find the relevant documents based on the words in the phrase.
     * @return The `find_24_01` method returns a String that contains information about documents that
     * contain all the words provided in the input phrase. The information includes the document ID,
     * title, and length of each document that matches the search criteria. If no documents match the
     * search criteria, an empty string is returned.
     */
    public String find_24_00(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        // Initialize posting as null
        Posting posting = null;

        // Check if the first word exists in the index
        if (index.containsKey(words[0].toLowerCase())) {
            // Get the posting list of the first word
            posting = index.get(words[0].toLowerCase()).pList;
        }

        // If posting is still null after checking the first word, return an empty result
        if (posting == null) {
            return result;
        }

        // Iterate through the rest of the words
        for (int i = 1; i < len; i++) {
            String word = words[i].toLowerCase();
            // Check if the word exists in the index and the posting list is not null
            if (index.containsKey(word) && index.get(word).pList != null) {
                // Intersect the current posting list with the posting list of the current word
                posting = intersect(posting, index.get(word).pList);
            } else {
                // If the word doesn't exist in the index or the posting list is null, set posting to null and break the loop
                posting = null;
                break;
            }
        }

        // If posting is not null after intersecting all words, generate the result
        if (posting != null) {
            while (posting != null) {
                result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
                posting = posting.next;
            }
        }
        return result;
    }

    /**
     * The function `find_24_01` searches for a phrase in an index and returns information about
     * documents containing that phrase.
     * 
     * @param phrase It looks like you have provided a method `find_24_01` that takes a phrase as input
     * and searches for the phrase in an index to find relevant documents. The method splits the input
     * phrase into words, checks if each word exists in the index, and then intersects the posting
     * lists to find
     * @return The `find_24_01` method is returning a string that contains information about documents
     * that match the search criteria specified in the input `phrase`. The method processes the input
     * phrase, searches for the words in the phrase in an index, and then generates a result string
     * that includes the document ID, title, and length of each matching document. If no matching
     * documents are found, an empty string is
     */
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search 
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        if(len > 1){
            words[0] = words[0].concat(" ").concat(words[1]);
        }
        // Initialize posting as null
        Posting posting = null;

        // Check if the first word exists in the index
        if (index.containsKey(words[0].toLowerCase())) {
            // Get the posting list of the first word
            posting = index.get(words[0].toLowerCase()).pList;
        }

        // If posting is still null after checking the first word, return an empty result
        if (posting == null) {
            return result;
        }

        // Iterate through the rest of the words
        for (int i = 1; i < len-1; i++) {
            String word = words[i].toLowerCase();
            words[i+1] = words[i+1].toLowerCase();
            word = words[i].concat(" ").concat(words[i+1]);
            // Check if the word exists in the index and the posting list is not null
            if (index.containsKey(word) && index.get(word).pList != null) {
                // Intersect the current posting list with the posting list of the current word
                posting = intersect(posting, index.get(word).pList);
            } else {
                // If the word doesn't exist in the index or the posting list is null, set posting to null and break the loop
                posting = null;
                break;
            }
        }

        // If posting is not null after intersecting all words, generate the result
        if (posting != null) {
            while (posting != null) {
                result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
                posting = posting.next;
            }
        }
        return result;
    }
    
    
    //---------------------------------
    /**
     * The function uses bubble sort to sort an array of strings in ascending order.
     * 
     * @param words The `sort` method you provided is a basic implementation of the bubble sort
     * algorithm for sorting an array of strings in ascending order. The method compares adjacent
     * elements and swaps them if they are in the wrong order, repeating this process until the array
     * is sorted.
     * @return The `sort` method is returning the sorted array of strings after applying the bubble
     * sort algorithm to the input array of strings.
     */
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------

/**
 * The `store` function in Java writes the contents of a data structure to a file, handling exceptions
 * during the process.
 * 
 * @param storageName The `store` method you provided seems to be storing data into a file specified by
 * the `storageName` parameter. The data being stored includes information from `sources` and `index`
 * maps.
 */
    public void store(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================    
/**
 * The function checks if a file with a given storage name exists in a specific directory.
 * 
 * @param storageName The `storageName` parameter is a `String` representing the name of the file in
 * the specified directory `/home/ehab/tmp11/rl/` that we want to check for existence.
 * @return The method `storageFileExists` returns a boolean value indicating whether a file with the
 * specified `storageName` exists in the directory "/home/ehab/tmp11/rl/". If the file exists and is
 * not a directory, it returns `true`, otherwise it returns `false`.
 */
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------    
    /**
     * The `createStore` function creates a new file in a specified directory with the given storage
     * name and writes "end" to the file.
     * 
     * @param storageName The `createStore` method you provided takes a `storageName` parameter, which
     * is used to create a new file in the specified directory. The `storageName` parameter represents
     * the name of the file that will be created in the directory specified by the path
     * "C:\Users\osama ib
     */
    public void createStore(String storageName) {
        try {
            String pathToStorage = "C:\\Users\\osama ibrahim\\Downloads\\tmp11\\tmp11\\rl\\collection\\"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------      
     //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "C:\\Users\\osama ibrahim\\Downloads\\tmp11\\tmp11\\rl\\collection\\"+storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}

//=====================================================================
