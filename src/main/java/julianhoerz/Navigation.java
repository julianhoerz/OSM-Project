package julianhoerz;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;




public class Navigation implements HttpHandler {



    Graph graph;
    Dijkstra dijkstra;
    Draw draw;

    Navigation(Graph graph){
        this.graph = graph;
        this.dijkstra = new Dijkstra(graph);
        this.draw = new Draw(graph);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

       // System.out.println(httpExchange.getRequestURI().toString());

        String url = httpExchange.getRequestURI().toString();
        String[] parts = url.split("\\/");

        String geojson = "no route found...";

        if(parts.length == 3){
            String[] parts2 = parts[2].split("\\,");
            if(parts2.length == 4){

                double startLat;
                double startLng;
                double endLat;
                double endLng;

                startLat = Double.valueOf(parts2[1]);
                startLng = Double.valueOf(parts2[0]);
                endLat = Double.valueOf(parts2[3]);
                endLng = Double.valueOf(parts2[2]);

                System.out.println("Start: " + startLat + ", " + startLng);
                System.out.println("End: " + endLat + ", " + endLng);
                NodeProj startProj = this.dijkstra.findNextStreet(startLat, startLng);
                NodeProj endProj = this.dijkstra.findNextStreet(endLat, endLng);
                ArrayList<double[]> coordinates = this.dijkstra.dijkstraCoordinates(startProj, endProj);
                geojson = this.draw.buildGeoJson(coordinates, startLat, startLng, endLat, endLng);
                // geojson = this.dijkstra.startDijkstra(startLat,startLng,endLat,endLng);
            }
        }

        //System.out.println("Partslength: " + parts.length);
        // URL url = httpExchange.getRequestURI().toURL();

        //System.out.println("URL: " + test);

        String response = geojson;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}