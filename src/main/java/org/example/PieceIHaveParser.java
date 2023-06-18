package org.example;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

import static java.lang.Math.pow;

public class PieceIHaveParser {
    public Properties properties = new Properties();
    public RandomAccessFile file = null;
    public byte[] bt;
    private int count;
    public long piece;
    public PieceIHaveParser(String nameFile, int coun, byte[] propPiece) {
        count = coun;
        try(FileReader f = new FileReader(new File(nameFile))){
            properties.load(f);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        long j;
        for (int i = 1; i < count; i++) {
            if(properties.getProperty(String.valueOf(i)).equals("1") || propPiece[i-1]==1){
                //piece += pow(2, (count - i));
                j = 1;
                for (int k = 0; k < count-i; k++) {
                    j*=2;
                }
                piece+=j;
            }

        }
        if(properties.getProperty(String.valueOf(count)).equals("1"))
            piece++;
        //System.out.println(piece);
    }
//        int r = 1;
//        if(coun%8==0){
//            r=0;
//        }
//        bt = new byte[coun/8+r];
//        count = coun;
//        try(FileReader f = new FileReader(new File(nameFile))){
//            properties.load(f);
//        }catch (IOException e){
//            System.out.println(e.getMessage());
//        }
//        int j = 0;
//        for (int i = 1; i <= count; i++) {
//            if(properties.getProperty(String.valueOf(i)).equals("1")){
//                bt[j] += pow(2, (i-1)%8);
//            }
//            if(i%8==0)
//                j++;
//        }
//        System.out.println(Arrays.toString(bt));
//    }

//    public byte[] getPieceIHave(){
//        return bt;
//    }

}
