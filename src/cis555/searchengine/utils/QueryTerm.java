package cis555.searchengine.utils;

import java.util.LinkedList;
import java.util.List;

public class QueryTerm {

    private String word;
    private int freq;
    private double idfValue;

    private List<Integer> positions;

    public QueryTerm(String word, int pos) {
        this.word = word;
        freq = 1;
        positions = new LinkedList<Integer>();
        positions.add(pos);
    }

    public void addPos(int pos) {
        freq++;
        positions.add(pos);
    }

    public String getWord() {
        return word;
    }

    public int getFreq() {
        return freq;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public double getIdfValue() {
        return idfValue;
    }

    public void setIdfValue(double idfValue) {
        this.idfValue = idfValue;
    }

    public String toString() {
        return word + " : freq : " + freq + " : positions : " + positions;
    }

}
