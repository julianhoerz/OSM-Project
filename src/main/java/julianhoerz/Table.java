package julianhoerz;


import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


class Table implements HttpHandler {

    private Graph graph;


    Table(Graph graph){
        this.graph = graph;

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

        String response = createResponse();
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private String createResponse(){
        String q = "\"";
        Integer NodesLength = graph.getNodesLength();
        Integer EdgesLength = graph.getEdgesLength();
        int[] HighwayTags = graph.getHighwayTags();
        String[] HighwayTagsName = graph.getHighwayTagsName();
        String buff = "";

        for(int i = 0; i < HighwayTags.length; i ++){
            buff = buff + "," + q + HighwayTagsName[i] + q + ":" +  q + HighwayTags[i] + q;
        }
        String responseJson = "{\"Nodes\":\"" + NodesLength.toString() + "\", \"Edges\":\"" + EdgesLength.toString() + "\"" + buff + "}";

        return responseJson;
    }
}