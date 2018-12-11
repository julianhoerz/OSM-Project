package julianhoerz;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


class Table implements HttpHandler {

    private Graph graph;


    Table(Graph graph){
        this.graph = graph;

    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = createResponse();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private String createResponse(){
        String name = "bremen";
        Integer NodesLength = graph.getNodesLength();
        Integer EdgesLength = graph.getEdgesLength();

        String responseJson = "{\"Name\":\"" + name + "\", \"Nodes\":\"" + NodesLength.toString() + "\", \"Edges\":\"" + EdgesLength.toString() + "\"}";

        return responseJson;
    }
}