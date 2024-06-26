/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

/**
 *
 * @author ehab
 */
 
public class PostingPostional {

    public PostingPostional next = null;
    int docId;
    int dtf = 1;

    PostingPostional(int id, int t) {
        docId = id;
        dtf=t;
    }

    PostingPostional(int id) {
        docId = id;
    }
}