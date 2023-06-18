package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {
    private boolean rw = true;
    private final int sizeBlock = 1024 * 32;
    private int sizePiece;
    private Selector selector;
    private TorrentParser torrentParser;
    private int[] port;
    private Map<SocketChannel, int[]> socketData = new HashMap<>();
    public byte[] propPiece;
    public Server(TorrentParser tP,int[] p, byte[] pP){
        propPiece = pP;
        port = p;
        torrentParser = tP;
        sizePiece = torrentParser.pieceLength;
    }
    public void start(){
        try {
            selector = Selector.open();
            //accept().start();
            react().start();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    private Thread react(){//C:\Users\vikto\Desktop\4.jpg
        return new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < port.length; i++) {
                    ServerSocketChannel serverSocketChannel = null;
                    try {
                        serverSocketChannel = ServerSocketChannel.open();
                        serverSocketChannel.socket().bind(new InetSocketAddress(port[i]));
                        serverSocketChannel.configureBlocking(false);
                        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                RandomAccessFile fileIn = null;
                try {
                    fileIn = new RandomAccessFile(torrentParser.nameFile, "r");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                while (true) {
                    try {

                        selector.select();

                        Set<SelectionKey> selectedKeys = selector.selectedKeys();

                        for (SelectionKey key : selectedKeys) {

                            if(key.isAcceptable()){
                                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannelR = serverChannel.accept();
                                if(socketChannelR!=null) {
                                    socketChannelR.configureBlocking(false);
                                    socketChannelR.register(selector, SelectionKey.OP_READ);
                                    while (getBitfield(socketChannelR) == -1) {}
                                    PieceIHaveParser pieceIHaveParser = new PieceIHaveParser("piece.properties", torrentParser.amount, propPiece);
                                    Bitfield(socketChannelR, pieceIHaveParser.piece);
                                    socketData.put(socketChannelR, new int[3]);
                                }
                            }else if(key.isReadable()){
                                SocketChannel channel = (SocketChannel) key.channel();
                                if(channel.isConnected()) {
                                    int i = 0;
                                    int r;
                                    while ((r = getRequest(socketData.get(channel), channel)) < 0) {}
                                    if (r == 0) {
                                        socketData.remove(channel);
                                        channel.close();
                                        continue;
                                    }
                                    Piece(channel, socketData.get(channel)[0], socketData.get(channel)[1], fileIn);
                                }
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        });
    }
    private int Piece(SocketChannel channel, int numPiece, int offset, RandomAccessFile fileIn) throws IOException {
        byte[] bt = new byte[sizeBlock];
        System.out.println(numPiece);

        long k = sizePiece;
        k*=numPiece;
        k+=offset;
        fileIn.seek(k);

        int size = fileIn.read(bt);
        ByteBuffer outBuffer = ByteBuffer.allocate(sizeBlock + 13);
        outBuffer.putInt(sizeBlock + 13);
        outBuffer.put((byte) 7);
        outBuffer.putInt(numPiece);
        outBuffer.putInt(offset);
        outBuffer.put(bt);
        outBuffer.flip();

        //System.out.println(offset);
        try {
            channel.write(outBuffer);
        }catch (IOException e){
            return 0;
        }
        return 1;
    }
    private int getRequest(int[] nos, SocketChannel channel) throws IOException {
        ByteBuffer inBuffer = ByteBuffer.allocate(17);
        int bytesRead = 0;
        try {
            bytesRead = channel.read(inBuffer);
        }catch (IOException e){
            return 0;
        }

//        System.out.println(bytesRead);
        if(bytesRead==0)
            return -1;
        if(bytesRead<0)
            return 0;
        inBuffer.flip();
        byte[] bytes = new byte[inBuffer.remaining()];
        inBuffer.get(bytes);
        //System.out.println(bytesRead);
        ByteBuffer buffer1 = ByteBuffer.wrap(bytes, 0, 4);
        int size = buffer1.getInt();
        if ((size!=bytesRead) || (bytes[4]!=6)){
            System.out.println("getRequest ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
            return -1;
        }
        //int[] nos = new int[3];
        ByteBuffer buffer2 = ByteBuffer.wrap(bytes, 5, 4);
        nos[0] = buffer2.getInt();
        ByteBuffer buffer3 = ByteBuffer.wrap(bytes, 9, 4);
        nos[1] = buffer3.getInt();
        ByteBuffer buffer4 = ByteBuffer.wrap(bytes, 13, 4);
        nos[2] = buffer4.getInt();
        return 1;
    }
    private int Bitfield(SocketChannel channel, long pieceIHave) throws IOException {
        ByteBuffer outBuffer = ByteBuffer.allocate(12);
        outBuffer.putInt(12);
        outBuffer.putLong(pieceIHave);
        outBuffer.flip();
        try {
            channel.write(outBuffer);
        }catch (IOException e){
            return 0;
        }
        return 1;
    }
    private int getBitfield(SocketChannel channel) throws IOException {
        ByteBuffer inBuffer = ByteBuffer.allocate(8);
        int bytesRead=0;
        try {
            bytesRead = channel.read(inBuffer);
        }catch (IOException e){
            return 0;
        }

        if(bytesRead<=0)
            return -1;
//        if(bytesRead<0)
//            return  0;
        inBuffer.flip();
        byte[] bytes = new byte[inBuffer.remaining()];
        inBuffer.get(bytes);
        ByteBuffer buffer1 = ByteBuffer.wrap(bytes, 0, 4);
        int size = buffer1.getInt();
        if (size!=bytesRead){
            System.out.println("getPiece ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
            return -1;
        }
        ByteBuffer buffer2 = ByteBuffer.wrap(bytes, 4, 4);
        return buffer2.getInt();
    }

}