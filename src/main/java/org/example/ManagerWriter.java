package org.example;

import java.io.*;
import java.util.Properties;

public class ManagerWriter {
    private int size;
    public int num = 4;
    private int off;
    private String str;
    //private FileOutputStream fileOut;
    private RandomAccessFile fileOut;
    private File file;
    private long length;
    public boolean[] pieceIHave;
    //
    public ManagerWriter(String s, long len, int sizePiece, int amount){
        pieceIHave = new boolean[amount];
        for(int i = 0; i < amount; i++) {
            pieceIHave[i] = false;
        }
        length = len;
        str = s;
        num = amount;
        size = sizePiece;
        file = new File(str);
        try {
            file.createNewFile();
            fileOut = new RandomAccessFile(file, "rw");//FileOutputStream(file, true);
            fileOut.setLength(length);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public void write(int np, byte[] bt, byte[] propPiece){
        off = np*size;
        try {
            long e = size;
            e*=np;
            //e+=offset;
            fileOut.seek(e);

            if(np == num - 1) {
                fileOut.write(bt, 0, (int)length%size);
                fileOut.setLength(length);
            }else {
                fileOut.write(bt);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
//        File fileP = new File("myPiece.properties");
//
//        Properties prop = new Properties();
//
//        try (InputStream in = new FileInputStream(fileP))
//        {
//            prop.load(in);
//            prop.setProperty(String.valueOf(np+1), "0");
//
//            OutputStream out = new FileOutputStream(fileP);
//            prop.store(out, "");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        propPiece[np] = 1;
        pieceIHave[np] = true;
    }
//    private int countOff(){
//        int p;
//        p = (int) (piece/pow(2,num - numPiece));
//        int sum = 0;
//        for (int i = 0; i < num - numPiece; i++) {
//            sum += p%2;
//            p/=2;
//        }
//        return sum*size;
//    }
}
