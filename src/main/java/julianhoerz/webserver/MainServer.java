package julianhoerz.webserver;


import julianhoerz.datastructure.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;


public class MainServer{

    Graph graph;

    public MainServer(Graph graph){
        this.graph = graph;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/dijkstra", new NavigationServer(graph));
        server.createContext("/api", new TableServer(graph));
        server.createContext("/mapmatching", new MapMatchingServer(graph));
        server.setExecutor(null); // creates a default executor
        server.start();
    }


}

