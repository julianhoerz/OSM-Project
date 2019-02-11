package julianhoerz.webserver;

import julianhoerz.datastructure.*;
import julianhoerz.algorithms.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MapMatchingServer implements HttpHandler{

    Graph graph;
    MapMatching mapMatching;
    Draw draw;
    

    MapMatchingServer(Graph graph){
        this.graph = graph;
        this.mapMatching = new MapMatching(graph);
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

        StringBuilder body = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(httpExchange.getRequestBody(),"UTF-8")) {
            char[] buffer = new char[256];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                body.append(buffer, 0, read);
            }
        }
        
        String teststring = body.toString();
        teststring.replaceAll("\\s", "");
        JSONObject obj = new JSONObject(teststring);

        String geojson = "no route found...";
        if(obj.has("features")){
            ArrayList<double[]> rawData = new ArrayList<double[]>();
            // System.out.println(obj.getJSONArray("features"));
            JSONObject feature = obj.getJSONArray("features").getJSONObject(0);
            if(feature.has("properties")){
                JSONObject properties = feature.getJSONObject("properties");
                if(properties.has("sigma") && properties.has("beta") && properties.has("kNN")){
                    int sigma = properties.getInt("sigma");
                    int beta = properties.getInt("beta");
                    int kNN = properties.getInt("kNN");
                    this.mapMatching = new MapMatching(graph, sigma, beta, kNN);
                }
            }


            JSONArray coordsarray = feature.getJSONObject("geometry").getJSONArray("coordinates");
            // System.out.println(coordsarray);
            for(int i = 0; i < coordsarray.length(); i ++){
                double[] buff = new double[2];
                JSONArray coords = coordsarray.getJSONArray(i);
                buff[0] = coords.getDouble(1);
                buff[1] = coords.getDouble(0);
                rawData.add(buff);
            }

            if(rawData.size() > 1){
                // System.out.println("First Coord: " + rawData.get(0)[0] + "," + rawData.get(0)[1]);
                ArrayList<double[]> finalCoords;
                Date date = new Date();
                System.out.println("Mapmatching Startime: " + date.toString());
                finalCoords = this.mapMatching.startMapMatching(rawData);
                date = new Date();
                System.out.println("Mapmatching Endtime: " + date.toString());
                geojson = this.draw.buildGeoJson(finalCoords,0d,0d,0d,0d);
                // System.out.println(geojson);
            }
        }


        


        String response = geojson;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    

}