package julianhoerz;


import java.io.IOException;


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
            reader = null;
            System.gc();
            // Check for save file
            if(args.length == 3){
                if(args[1].equals("-save")){
                    BinFileSaver saver = new BinFileSaver(graph);
                    saver.saveFile(args[2]);
                }
            }


        }
        else if(parts[1].equals("bin")){
            BinFileReader reader = new BinFileReader(graph);
            reader.readBin(filename);
            //graph.readBin(filename);
        } else{
            System.out.println("Wrong filename ending. Please use \"example.bin\" or \"example.pbf\".");
            System.out.println("ERROR: Program Stopped.");
            return;
        }

        
        Server server = new Server(graph);
        server.start();


        System.gc();
        System.runFinalization();


    }




}
