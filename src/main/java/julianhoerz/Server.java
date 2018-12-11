package julianhoerz;



import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class Server{

    Graph graph;

    Server(Graph graph){
        this.graph = graph;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/dijkstra", new Dijkstra());
        server.createContext("/api", new Table(graph));
        server.setExecutor(null); // creates a default executor
        server.start();
    }





    


    class Dijkstra implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


}

