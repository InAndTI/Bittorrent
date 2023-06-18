package org.example;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class SocketData {
    public boolean rw = true;
    //public int[] numPiece;
    long pieceIHave;
    public boolean closeChanel = false;
    public ArrayList<Integer> np = new ArrayList<Integer>();
    public byte[] bt;
    public int offset = 0;
    public int port;
    public String hostName;
    public SocketChannel socketChannelR;
    public SocketData(int sizePiece, int port, String hostName){//, SocketChannel scr){
        this.port = port;
        this.hostName = hostName;
        //socketChannelR = scr;
        bt = new byte[sizePiece];
    }
    public void close(){
        if(np.size()>0){
            np.remove(0);
            offset = 0;
        }
        if (np.size()==0){
            closeChanel = true;
        }
    }
}
