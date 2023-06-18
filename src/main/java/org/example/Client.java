package org.example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Client {
    private boolean rw = true;
    private final int sizeBlock = 1024 * 32;
    private int sizePiece;
    //private final int np = 0;
    private ArrayList<SocketChannel> socketChannelsW = new ArrayList<SocketChannel>();
    //private ArrayList<SocketChannel> socketChannelsR = new ArrayList<SocketChannel>();
    private Map<SocketChannel, SocketData> socketData = new HashMap<>();
    private TorrentParser torrentParser;
    private int close;
    public byte[] propPiece;
    public Client(TorrentParser tP){
        torrentParser = tP;
        sizePiece = torrentParser.pieceLength;

        Properties prop = new Properties();
        try(FileReader f = new FileReader(new File("myPiece.properties"))){
            prop.load(f);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        propPiece = new byte[torrentParser.amount];
        for (int i = 0; i < torrentParser.amount; i++) {
            if (prop.getProperty(String.valueOf(i + 1), "0").equals("1"))
                propPiece[i] = 1;
            else
                propPiece[i] = 0;
        }

    }
    public void start(ArrayList<String> hostnames, ArrayList<Integer> ports){
        try {
            //close = hostnames.size();
            Selector selector = Selector.open();
            ManagerWriter managerWriter = new ManagerWriter(torrentParser.nameFile, torrentParser.fileLength, torrentParser.pieceLength, torrentParser.amount);


            connect(hostnames, ports, selector);
            choose();

            while (true) {
                while (close==0){
                    if(ports.size()>0){
                        int portsSize = ports.size();
                        connect(hostnames, ports, selector);
                        if (portsSize != ports.size())
                            choose();
                    }
                }
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (socketData.get(channel).closeChanel||socketData.get(channel).np.size()==0&&close>0) {
                        socketChannelsW.remove(channel);
                        socketData.remove(channel);
                        channel.close();
                        close--;
                        if (close==0){
                            break;
                        }
                        continue;
                    }
                    if (close==0){
                        break;
                    }

                    if (key.isWritable()&&channel.isConnected()){
                        Request(channel, socketData.get(channel).np.get(0), socketData.get(channel).offset);
                        int i = 0;
                        int k =-1;
                        while((k = getPiece(socketData.get(channel).bt, channel, socketData.get(channel).np.get(0))) < 0){}
                        if (k == 0){
                            ports.add(socketData.get(channel).port);
                            hostnames.add(socketData.get(channel).hostName);
                            socketChannelsW.remove(channel);
                            socketData.remove(channel);
                            channel.close();
                            choose();
                            close--;
                            continue;
                        }
                        socketData.get(channel).offset += sizeBlock;
                    }

                    if(torrentParser.amount-1!=socketData.get(channel).np.get(0)) {
                        if (socketData.get(channel).offset == sizePiece) {
                            managerWriter.write(socketData.get(channel).np.get(0), socketData.get(channel).bt, propPiece);
                            socketData.get(channel).close();
                        }
                    }else{
                        long o = sizePiece;
                        o*=torrentParser.amount-1;
                        if (socketData.get(channel).offset >= torrentParser.fileLength - o){
                            managerWriter.write(socketData.get(channel).np.get(0), socketData.get(channel).bt, propPiece);
                            socketData.get(channel).close();
                        }
                    }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    private void connect(ArrayList<String> hostname, ArrayList<Integer> port, Selector selector) throws IOException {
        int n = hostname.size();
        int countSC = socketChannelsW.size();
        for (int i = 0; i < n; i++){
            socketChannelsW.add(SocketChannel.open());
            try {
                close++;
                socketChannelsW.get(countSC+i).connect(new InetSocketAddress(hostname.get(0), port.get(0)));
                socketChannelsW.get(countSC+i).configureBlocking(false);
                socketChannelsW.get(countSC+i).register(selector, SelectionKey.OP_WRITE);
                Bitfield(socketChannelsW.get(countSC+i), 0);
                long k = -1;
                while (k == -1) {
                    k = getBitfield(socketChannelsW.get(countSC+i));
                }
                if (k == 0) {
                    socketChannelsW.get(countSC+i).close();
                }else{
                    socketData.put(socketChannelsW.get(countSC+i), new SocketData(sizePiece, port.get(0), hostname.get(0)));
                    socketData.get(socketChannelsW.get(countSC+i)).pieceIHave = k;
                }
            }catch (IOException e){
                socketChannelsW.get(countSC+i).close();
                socketChannelsW.remove(countSC+i);
                hostname.add(hostname.get(0));
                port.add(port.get(0));

                close--;
                i--;
                n--;
            }finally {
                hostname.remove(0);
                port.remove(0);
            }
        }
    }
    private void choose(){
        long[] whichPiece = new long[socketData.size()];
        ArrayList<ArrayList<Integer>> choices;
        ManagerPiece managerPiece = new ManagerPiece();
        for (int i = 0; i < socketData.size(); i++) {
            whichPiece[i] = socketData.get(socketChannelsW.get(i)).pieceIHave;
        }
        choices = managerPiece.divide(whichPiece, torrentParser.amount);


        for (SocketChannel socketChannel : socketChannelsW) socketData.get(socketChannel).np.clear();
        for (int i = 0; i < torrentParser.amount; i++) {
            if(choices.get(i).size()>0)
                if(propPiece[i]==0)
                    socketData.get(socketChannelsW.get(choices.get(i).get(0))).np.add(i);
        }
    }
    private int Request(SocketChannel channel, int numPiece, int offset) throws IOException {
        ByteBuffer outBuffer = ByteBuffer.allocate(17);
        outBuffer.putInt(17);
        outBuffer.put((byte) 6);
        outBuffer.putInt(numPiece);
        outBuffer.putInt(offset);
        outBuffer.putInt(sizeBlock);
        outBuffer.flip();
        try {
            channel.write(outBuffer);
        }catch (IOException e){
            return 0;
        }
        return 1;
    }
    private int getPiece(byte[] bt, SocketChannel channel, int numPiece) throws IOException {
        ByteBuffer inBuffer = ByteBuffer.allocate(sizeBlock + 13);
        int bytesRead = 0;
        try {
            bytesRead = channel.read(inBuffer);
        }catch (IOException e){
            return 0;
        }
//        System.out.println(bytesRead);
        if(bytesRead<=0)
            return -1;
        inBuffer.flip();
        byte[] bytes = new byte[inBuffer.remaining()];
        inBuffer.get(bytes);
        ByteBuffer buffer1 = ByteBuffer.wrap(bytes, 0, 4);
        int size = buffer1.getInt();
        ByteBuffer buffer2 = ByteBuffer.wrap(bytes, 5, 4);

        if ((size!=bytesRead) || (bytes[4]!=7) || (numPiece!= buffer2.getInt())){
            //System.out.println("getPiece ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR"+numPiece);
            return -1;
        }
        ByteBuffer buffer3 = ByteBuffer.wrap(bytes, 9, 4);
        socketData.get(channel).offset = buffer3.getInt();
        inBuffer.flip();
        //if(socketData.get(channel).offset<sizePiece)
            ByteBuffer.wrap(bytes, 13, sizeBlock).get(bt, socketData.get(channel).offset, sizeBlock);
        return 1;
    }
    private void Bitfield(SocketChannel channel, int pieceIHave) throws IOException {
        ByteBuffer outBuffer = ByteBuffer.allocate(8);
        outBuffer.putInt(8);
        outBuffer.putInt(pieceIHave);
        outBuffer.flip();
        channel.write(outBuffer);
    }
    private long getBitfield(SocketChannel channel) throws IOException {
        ByteBuffer inBuffer = ByteBuffer.allocate(12);
        int bytesRead = channel.read(inBuffer);
        if(bytesRead==0)
            return -1;
        inBuffer.flip();
        byte[] bytes = new byte[inBuffer.remaining()];
        inBuffer.get(bytes);
        ByteBuffer buffer1 = ByteBuffer.wrap(bytes, 0, 4);
        int size = buffer1.getInt();
        if (size!=bytesRead){
           // System.out.println("getB ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR");
            return -1;
        }
        ByteBuffer buffer2 = ByteBuffer.wrap(bytes, 4, 8);
        return buffer2.getLong();
    }
}
