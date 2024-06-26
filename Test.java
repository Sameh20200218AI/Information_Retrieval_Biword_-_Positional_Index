package invertedIndex;
import java.util.Scanner;

import invertedIndex.Index5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Index5 index = new Index5();  // Create an instance of the Index5 class

        // Set the path to the collection directory
        String files = "C:\\Users\\osama ibrahim\\Downloads\\tmp11\\tmp11\\rl\\collection\\";

        File file = new File(files);  // Create a File object for the collection directory
        // Get the list of files in the collection directory
        String[] fileList = file.list();

        fileList = index.sort(fileList);  // Sort the file list
        // Update file paths to include the directory path
        for (int i = 0; i < fileList.length; i++) {
            System.out.println(fileList[i]);
            fileList[i] = files + fileList[i];
        }
        Index5.N = fileList.length;  // Set the total number of documents in the index
        System.out.println("Enter number of index : ");
        System.out.println("0 : Normal Index ");
        System.out.println("1 : BI Index ");
        System.out.println("2 : positionalIndex ");
        Integer val = scanner.nextInt();
        if (val ==0 || val == 1) {
            Boolean type = (val == 0 ? false :true );
            System.out.println(type);
            // Build the inverted index using the file list
            index.buildIndex(fileList, type);
            // Store the inverted index to a file
            index.store("index");
            // Print the inverted index dictionary
            index.printDictionary();

            String phrase = "";
//
//        // Continuous loop to accept search phrases from the user
            do {
                System.out.println("Print search phrase: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();  // Read search phrase from user input

                if (!phrase.isEmpty()) {
                    //print results
                    String result = (type ?index.find_24_01(phrase):index.find_24_00(phrase));
                    System.out.println("search result =");
                    if (!result.isEmpty()) {
                        System.out.println(result);
                    } else {
                        System.out.println("NOT FOUND!!");
                    }
                } else {
                    System.out.println("Your test is Empty!!");
                }

            } while (!phrase.isEmpty());  // Continue loop until an empty search phrase is entered (user presses Enter)
        } else if (val == 2) {
            // Build the inverted index using the file list
            index.buildPositionalIndex(fileList);
            String phrase = "";

//        // Continuous loop to accept search phrases from the user
            do {
                System.out.println("Print search phrase: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();  // Read search phrase from user input

                if (!phrase.isEmpty()) {
                    //print results
                    Map<Integer, Map<Integer, List<Integer>>> result = index.searchSentence(phrase);
                    System.out.println("search result =");
                    if (!result.isEmpty()) {
                        System.out.println(result);
                    } else {
                        System.out.println("NOT FOUND!!");
                    }
                } else {
                    System.out.println("Your test is Empty!!");
                }

            } while (!phrase.isEmpty());  // Continue loop until an empty search phrase is en
        } else {
            System.out.println("EXIT");
        }
    }
}
