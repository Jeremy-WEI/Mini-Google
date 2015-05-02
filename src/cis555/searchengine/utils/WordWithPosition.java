package cis555.searchengine.utils;


//public class WordWithPosition implements Comparable<WordWithPosition> {
public class WordWithPosition {

    private int pos;
    private String word;

    public WordWithPosition(String word, int pos) {
        this.word = word;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public String getWord() {
        return word;
    }
    //
    // @Override
    // public int compareTo(WordWithPosition o) {
    // return pos - o.pos;
    // }

}
