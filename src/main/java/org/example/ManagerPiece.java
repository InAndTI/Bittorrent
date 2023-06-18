package org.example;

import java.util.ArrayList;

public class ManagerPiece {
    private int num = 4;
    private short[] load;
    public ArrayList<ArrayList<Integer>> divide(long[] whichPiece, int n){
        num = n;
        load = new short[whichPiece.length];
        ArrayList<ArrayList<Integer>> choices = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> choicesTrue = new ArrayList<ArrayList<Integer>>();
        int[] prices = price(whichPiece);
        for (int i = 0; i < num; i++) {
            choices.add(choose(min(prices), whichPiece));
        }
        int k;
        for (int i = 0; i < num; i++) {
            prices = price(whichPiece);
            for (int j = 0; j < num ; j++) {
                if (i == (k = min(prices)))
                    choicesTrue.add(choices.get(j));
            }
        }
//        for (int i = 0; i < num; i++) {
//            choices.add(choose(i, whichPiece));
//        }
        System.out.println(choicesTrue);
        return choicesTrue;
    }
    private ArrayList<Integer> choose(int piece, long[] whichPiece){
        ArrayList<Integer> choice = new ArrayList<Integer>();
        for (int i = 0; i < whichPiece.length; i++) {
            long n = whichPiece[i];
            boolean have = false;
            for (int j = 0; j < num - piece; j++) {

                if(n%2 == 0)
                    have = false;
                else
                    have = true;
                n /= 2;
            }
            if (have)
                choice.add(i);
        }
        return min(choice);
    }
    private ArrayList<Integer> min(ArrayList<Integer> choice){
        ArrayList<Integer> choiceSort = new ArrayList<Integer>();
        short[] temporaryLoad = new short[load.length];
        for (int i = 0; i < load.length; i++) {
            temporaryLoad[i] = load[i];
        }
        for (int j = 0; j < choice.size(); j++) {
            int minim = 100000;
            int index = 0;
            for (int i = 0; i < choice.size(); i++) {
                if (minim > temporaryLoad[choice.get(i)]) {
                    minim = temporaryLoad[choice.get(i)];
                    index = i;
                }
            }
            choiceSort.add(choice.get(index));
            temporaryLoad[choice.get(index)] = 1000;
        }
        if (choiceSort.size()>0)
            load[choiceSort.get(0)]+=1;
        return choiceSort;
    }
    private int[] price(long[] whichPiece){
        int[] prices = new int[num];
        for (int i = 0; i < whichPiece.length; i++) {
            long n = whichPiece[i];
            for (int j = 0; j < num; j++) {
                prices[num - j - 1] += n%2;
                n /= 2;
            }

        }
        return prices;
    }
    private int min(int[] pieces){
        int minim = 100000;
        int index = 0;
        for (int i = 0; i < num; i++) {
            if(minim>pieces[i]){
                minim = pieces[i];
                index = i;
            }
        }
        pieces[index] = 1000000;
        return index;
    }
}

