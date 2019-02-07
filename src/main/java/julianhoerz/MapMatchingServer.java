package julianhoerz;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

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

        // String url = httpExchange.getRequestURI().toString();
        // String[] parts = url.split("\\/");

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
                ArrayList<double[]> finalCoords;
                finalCoords = this.mapMatching.startMapMatching(rawData);
                geojson = this.draw.buildGeoJson(finalCoords,0d,0d,0d,0d);
                System.out.println(geojson);
            }
        }


        


        String response = geojson;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    

}