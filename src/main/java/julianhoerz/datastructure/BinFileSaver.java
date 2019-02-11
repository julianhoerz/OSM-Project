package julianhoerz.datastructure;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BinFileSaver {


    Graph graph;

    public BinFileSaver(Graph graph){
        this.graph = graph;
    }


    public void saveFile(String filename) throws IOException{
        System.out.println("Save file...");

        OutputStream output = new FileOutputStream(filename);


        //Write Length of Arrays
        byte[] bytearray = new byte[4];
        ByteBuffer.wrap(bytearray).putInt(graph.getFramesLength());
        for(int p=0; p<4; p++){
            output.write((int) bytearray[p]);
        }
        bytearray = new byte[4];
        ByteBuffer.wrap(bytearray).putInt(graph.getNodesLength());
        for(int p=0; p<4; p++){
            output.write((int) bytearray[p]);
        }
        bytearray = new byte[4];
        ByteBuffer.wrap(bytearray).putInt(graph.getEdgesLength());
        for(int p=0; p<4; p++){
            output.write((int) bytearray[p]);
        }

        



        System.out.println("Save \"Offset_Frames\"");

        for(int i=0; i < graph.getFramesLength() ; i ++){
            byte[] bytes = new byte[4];
            ByteBuffer.wrap(bytes).putInt(graph.getOffsetFrames(i,0));
            for(int p=0; p<4; p++){
                output.write((int) bytes[p]);
            }

            ByteBuffer.wrap(bytes).putInt(graph.getOffsetFrames(i,1));
            for(int p=0; p<4; p++){
                output.write((int) bytes[p]);
                
            }
        }


        System.out.println("Save \"Node_Id\"");

        for(int i=0; i < graph.getNodesLength() ; i ++){
            byte[] bytes = new byte[8];
            ByteBuffer.wrap(bytes).putLong(graph.getNodeId(i));
            for(int p=0; p<8; p++){
                output.write((int) bytes[p]);
            }
        }


        System.out.println("Save \"Node_Coords\"");

        for(int i=0; i < graph.getNodesLength() ; i ++){
            byte[] bytes = new byte[8];
            ByteBuffer.wrap(bytes).putDouble(graph.getNodeLat(i));
            for(int p=0; p<8; p++){
                output.write((int) bytes[p]);
            }

            ByteBuffer.wrap(bytes).putDouble(graph.getNodeLng(i));
            for(int p=0; p<8; p++){
                output.write((int) bytes[p]);
                
            }
        }


        System.out.println("Save \"Offset_Edges\"");

        for(int i=0; i < graph.getNodesLength() ; i ++){
            byte[] bytes = new byte[4];
            ByteBuffer.wrap(bytes).putInt(graph.getNodeOffset(i));
            for(int p=0; p<4; p++){
                output.write((int) bytes[p]);
            }
        }


        System.out.println("Save \"Edges\"");

        for(int i=0; i < graph.getEdgesLength() ; i ++){
            byte[] bytes = new byte[4];
            ByteBuffer.wrap(bytes).putInt(graph.getEdges(i));
            for(int p=0; p<4; p++){
                output.write((int) bytes[p]);
            }
        }


        System.out.println("Save \"Edges_Length\"");

        for(int i=0; i < graph.getEdgesLength() ; i ++){
            byte[] bytes = new byte[8];
            ByteBuffer.wrap(bytes).putDouble(graph.getEdgesLength(i));
            for(int p=0; p<8; p++){
                output.write((int) bytes[p]);
            }
        }

        output.close();

        System.out.println("Program Completeted");
    }


}