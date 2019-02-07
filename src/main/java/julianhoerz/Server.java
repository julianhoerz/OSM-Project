package julianhoerz;



import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;


public class Server{

    Graph graph;

    Server(Graph graph){
        this.graph = graph;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/dijkstra", new Navigation(graph));
        server.createContext("/api", new Table(graph));
        server.createContext("/mapmatching", new MapMatchingServer(graph));
        server.setExecutor(null); // creates a default executor
        server.start();
    }


}

