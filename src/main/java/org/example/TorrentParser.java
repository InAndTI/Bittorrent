package org.example;

import com.dampcake.bencode.BencodeInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class TorrentParser {
    public long fileLength;
    public int pieceLength;
    public String nameFile;
    public int amount;
    public TorrentParser(String nameF) {
        try {
            FileInputStream fileInputStream = new FileInputStream(nameF);
            BencodeInputStream bencodeInputStream = new BencodeInputStream(fileInputStream);

            Map<String, Object> torrentData = bencodeInputStream.readDictionary();
            Map<String, ?> info = (Map<String, ?>) torrentData.get("info");
            nameFile = (String) info.get("name");
            System.out.println(nameFile);
            fileLength = (Long) info.get("length");
            pieceLength = ((Long) info.get("piece length")).intValue();
            long o;
            if (fileLength%pieceLength==0) {
                o = fileLength/pieceLength;
            }else {
                o = fileLength/pieceLength + 1;
            }
            amount =(int) o/5+1;
            pieceLength*=5;
            bencodeInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//            byte[] piecesData = ((String) info.get("pieces")).getBytes("ISO-8859-15");
//            System.out.println(Arrays.toString(piecesData));