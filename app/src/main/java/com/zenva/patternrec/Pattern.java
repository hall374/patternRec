package com.zenva.patternrec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by tim on 6/5/17.
 */

public class Pattern {
    // There are 4 buttons
    private final int NUM_POSSIBILITIES = 4;

    private int[] generatedPattern;
    private ArrayList<Integer> inputPattern;

    /*
    Class for storing pattern information
     */
    public Pattern(){
        this.inputPattern = new ArrayList<Integer>();
    }

    /*
    Return generated pattern
     */
    public int[] getGeneratedPattern(){
        return this.generatedPattern;
    }

    /*
    Return generated pattern length
     */
    public int getGPLen(){
        return this.generatedPattern.length;
    }

    /*
    Randomly generate array for storing pattern (set)
     */
    public void generatePattern(int patternLen){
        int[] newPattern = new int[patternLen];
        Random rand = new Random();
        int random = -1;
        int newRandom = rand.nextInt(NUM_POSSIBILITIES);
        for (int i = 0; i<newPattern.length; i++) {
            // nextInt is exclusive on upper bound
            while(random == newRandom){
                newRandom = rand.nextInt(NUM_POSSIBILITIES);
            }
            random = newRandom;
            newPattern[i] = newRandom;
        }
        this.generatedPattern = newPattern;
    }

    /*
    Get input pattern
     */
    public ArrayList<Integer> getInputPattern(){
        return this.inputPattern;
    }

    /*
    Set the input pattern
     */
    public void setInputPattern(ArrayList<Integer> inputPattern){
        this.inputPattern = inputPattern;
    }

    /*
    Add an element to the input pattern
     */
    public void addToInPtrn(int val){
        this.inputPattern.add(new Integer(val));
    }

    /*
    Match length of input and generated patterns
     */
    public Boolean matchLen(){
        return this.generatedPattern.length == this.inputPattern.size();
    }

    /*
    Check if input pattern matches generated
     */
    public Boolean isMatch(){
        // Can't convert directly from Array<Integer> to int[]
        // This is so we can use Arrays.equals
        int[] inputPatternArray = new int[generatedPattern.length];
        for(int i = 0; i < inputPattern.size(); i++){
            inputPatternArray[i] = inputPattern.get(i).intValue();
        }
        return Arrays.equals(this.generatedPattern, inputPatternArray);
    }
}
