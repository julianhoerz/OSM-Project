


package julianhoerz;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Graph {

    private static Integer[][] Offset_Frames_Final;

    private static Long[] Node_Id_Final;
    private static Double[][] Node_Coords_Final;
    private static Integer[] Offset_Edges_Final;

    private static Integer[] Edges_Final;
    private static Double[] Edges_Length_Final;

    public Graph(){

    }

    public boolean readBin(String filename) throws IOException{

        byte[] bytes = new byte[4];
        byte[] bytes_8 = new byte[8];
        int cnt = 0;

        InputStream inputstream = new FileInputStream(filename);


        
        /** Read Length of Arrays */
        int data = inputstream.read();
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        int length = ByteBuffer.wrap(bytes).getInt();
        
        if(length <= 0){
            return false;
        }

        Offset_Frames_Final = new Integer[length][2];

        System.out.println("Number of Frames: " + length);


        cnt = 0;
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        length = ByteBuffer.wrap(bytes).getInt();
        
        if(length <= 0){
            return false;
        }

        Node_Id_Final = new Long[length];
        Node_Coords_Final = new Double[length][2];
        Offset_Edges_Final = new Integer[length];

        System.out.println("Number of Nodes: " + length);



        cnt = 0;
        while(data != -1 && cnt < 4){
            bytes[cnt] = (byte) data;
            data = inputstream.read();
            cnt ++;
        }
        length = ByteBuffer.wrap(bytes).getInt();
        
        if(length <= 0){
            return false;
        }

        Edges_Final = new Integer[length];
        Edges_Length_Final = new Double[length];

        System.out.println("Number of Edges: " + length);


        /** Read Data */
        System.out.println("Read \"Offset_Frames\"");

        for(int i=0; i < Offset_Frames_Final.length ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            Offset_Frames_Final[i][0] = ByteBuffer.wrap(bytes).getInt();

            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            Offset_Frames_Final[i][1] = ByteBuffer.wrap(bytes).getInt();
        }


        System.out.println("Read \"Node_Id\"");

        for(int i=0; i < Node_Id_Final.length ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Node_Id_Final[i] = ByteBuffer.wrap(bytes_8).getLong();
        }


        System.out.println("Read \"Node_Coords\"");

        for(int i=0; i < Node_Coords_Final.length ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Node_Coords_Final[i][0] = ByteBuffer.wrap(bytes_8).getDouble();

            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Node_Coords_Final[i][1] = ByteBuffer.wrap(bytes_8).getDouble();
        }


        System.out.println("Read \"Offset_Edges\"");

        for(int i=0; i < Offset_Edges_Final.length ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            Offset_Edges_Final[i] = ByteBuffer.wrap(bytes).getInt();
        }


        System.out.println("Read \"Edges\"");

        for(int i=0; i < Edges_Final.length ; i ++){
            for(int p=0; p<4; p++){
                bytes[p] = (byte) data;
                data = inputstream.read();
            }
            Edges_Final[i] = ByteBuffer.wrap(bytes).getInt();
        }


        System.out.println("Read \"Edges_Length\"");

        for(int i=0; i < Edges_Length_Final.length ; i ++){
            for(int p=0; p<8; p++){
                bytes_8[p] = (byte) data;
                data = inputstream.read();
            }
            Edges_Length_Final[i] = ByteBuffer.wrap(bytes_8).getDouble();
        }

        inputstream.close();

        System.out.println("Last data: " + data);

        return true;
    }




}



