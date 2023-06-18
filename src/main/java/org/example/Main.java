package org.example;

import java.util.ArrayList;

//"C:\\Users\\vikto\\Desktop\\4.jpg.torrent"   "C:\\Users\\vikto\\Desktop\\tor.jpg.torrent"C:\Users\vikto\Desktop\4.jpg.torrent
public class Main {
    public static void main(String[] args){
        TorrentParser torrentParser = new TorrentParser("C:\\Users\\vikto\\Desktop\\RedDragon.mkv.torrent");//"C:\\Users\\vikto\\Desktop\\Spartak.mp4.torrent");
        System.out.println(torrentParser.amount);
        ArrayList<String> hostname = new ArrayList<>();
        hostname.add("localhost");
        hostname.add("localhost");
        ArrayList<Integer> port = new ArrayList<>();
        port.add(1023);
        port.add(1021);
        Client client = new Client(torrentParser);
        int[] ports = new int[2];
        ports[0] = 1012;
        ports[1] = 1032;
        Server server = new Server(torrentParser,ports, client.propPiece);
        server.start();
        long start = System.currentTimeMillis();
        client.start(hostname, port);
        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        System.out.println("Прошло времени, мс: " + elapsed);
    }
}