package julianhoerz;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


/**
 * 
 */
public final class App {

    private static Graph graph;


    private App() {
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Starting Program...");

        if(args.length == 0){
            System.out.println("No filename specified.");
            System.out.println("ERROR: Program Stopped.");
            return;
        }

        String filename = args[0];
        String[] parts = filename.split("\\.");

        graph = new Graph();

        if(parts.length <= 1){
            System.out.println("Wrong filename. Please use \"example.bin\" or \"example.pbf\".");
            System.out.println("ERROR: Program Stopped.");
            return;
        }


        
        System.out.println("Filename: " + filename);

        if(parts[1].equals("pbf")){
            PbfFileReader reader = new PbfFileReader(graph);
            reader.buildGraph(filename);
        }
        else if(parts[1].equals("bin")){
            graph.readBin(filename);
        }

        // HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        // server.createContext("/api", new MyHandler());
        // server.setExecutor(null); // creates a default executor
        // server.start();



    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }




}
