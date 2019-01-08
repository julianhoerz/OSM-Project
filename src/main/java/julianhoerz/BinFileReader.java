package julianhoerz;



import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;



public class BinFileReader {


    private Graph graph;
    private byte[] bytes;
    private byte[] bytes_8;
    private int cnt;

    BinFileReader(Graph graph){

        this.bytes = new byte[4];
        this.bytes_8 = new byte[8];
        this.cnt = 0;

        this.graph = graph;
    }


    public boolean readBin(String filename) throws IOException{



        InputStream inputstream = new FileInputStream(filename);


        
        /** Read Length of Arrays */
        int data = inputstream.read();
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        int FramesLength = ByteBuffer.wrap(bytes).getInt();
        
        if(FramesLength <= 0){
            inputstream.close();
            return false;
        }

        graph.initOffsetFrames(FramesLength);

        System.out.println("Number of Frames: " + FramesLength);


        cnt = 0;
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        int NodesLength = ByteBuffer.wrap(bytes).getInt();
        
        if(NodesLength <= 0){
            inputstream.close();
            return false;
        }

        graph.initNodes(NodesLength);

        System.out.println("Number of Nodes: " + NodesLength);



        cnt = 0;
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        int EdgesLength = ByteBuffer.wrap(bytes).getInt();
        
        if(EdgesLength <= 0){
            inputstream.close();
            return false;
        }

        graph.initEdges(EdgesLength);

        System.out.println("Number of Edges: " + EdgesLength);


        /** Read Data */
        System.out.println("Read \"Offset_Frames\"");

        for(int i=0; i < FramesLength ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            int key = ByteBuffer.wrap(bytes).getInt();

            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            int offset = ByteBuffer.wrap(bytes).getInt();

            graph.setOffsetFrames(i, key, offset);

            //System.out.println("" + key);
        }


        System.out.println("Read \"Node_Id\"");

        for(int i=0; i < NodesLength ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Long id = ByteBuffer.wrap(bytes_8).getLong();

            graph.setNodeId(i, id);
        }


        System.out.println("Read \"Node_Coords\"");

        for(int i=0; i < NodesLength ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Double lat = ByteBuffer.wrap(bytes_8).getDouble();

            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Double lng = ByteBuffer.wrap(bytes_8).getDouble();

            graph.setNodeCoords(i, lat, lng);
        }


        System.out.println("Read \"Offset_Edges\"");

        for(int i=0; i < NodesLength ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            int offset = ByteBuffer.wrap(bytes).getInt();

            graph.setOffsetEdges(i, offset);
        }


        System.out.println("Read \"Edges\"");

        for(int i=0; i < EdgesLength ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            int reference = ByteBuffer.wrap(bytes).getInt();

            graph.setEdges(i, reference);
        }


        System.out.println("Read \"Edges_Length\"");

        for(int i=0; i < EdgesLength ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }

            Double distance = ByteBuffer.wrap(bytes_8).getDouble();

            graph.setEdgesLength(i, distance);
        }

        inputstream.close();

        System.out.println("Last data: " + data);

        return true;
    }



}



