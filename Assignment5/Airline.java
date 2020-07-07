import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Airline {
    public AirGraph airGraph;
    public String flightName;
    public static void main(String[] args) {
        new Airline(args);
    }

    public Airline(String[] args) {
        airGraph = readFile();
        int flag = 1;
        while (flag != 0) {
            //display menu + choices
            flag = menu();
            switch (flag) {
                case 1:
                    airGraph.printGraph();
                    break;
                case 2:
                    MSTDist();
                    break;
                case 3:
                    ShortPath("dist");
                    break;
                case 4:
                    ShortPath("price");
                    break;
                case 5:
                    ShortPath("hop");
                    break;
                case 6:
                    allPathsLessThan();
                    break;
                case 7:
                    addRoute();
                    break;
                case 8:
                    deleteRoute();
                    break;
            }
        }
        saveFile();
    }

    private void saveFile() {
        try {
            File flightFile = new File(flightName);
            flightFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(flightFile, false));
            for (String s : airGraph.makeFile()) {
                bw.write(s);
                bw.write("\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.err.print(e);
        } 
    }
    
    private void deleteRoute() {
        //read "from" city
        Scanner s = new Scanner(System.in);
        System.out.print("From: ");
        String fromCity = s.nextLine();
        Integer fromIndex = airGraph.parseCityName(fromCity);
        if (fromIndex == null) { System.out.println("City not found."); return; }

        //ready "to" city
        System.out.print("To: ");
        String toCity = s.nextLine();
        Integer toIndex = airGraph.parseCityName(toCity);
        if (toIndex == null) { System.out.println("City not found."); return; }

        //remove from graph
        try {
            airGraph.removeEdge(fromIndex, toIndex);
            System.out.println("Route deleted.");
        }

        catch (IllegalArgumentException e) { System.out.println("Route not found."); }
    }
    
    private void addRoute() {
        //read "from" city  
        Scanner s = new Scanner(System.in);
        System.out.print("From: ");
        String fromCity = s.nextLine();
        Integer fromIndex = airGraph.parseCityName(fromCity);
        if (fromIndex == null) { System.out.println("City not found."); return; }

        //read "to" city
        System.out.print("To: ");
        String toCity = s.nextLine();
        Integer toIndex = airGraph.parseCityName(toCity);

        //check for valid route
        if (toIndex == null) { 
            System.out.println("City not found."); return;
        }
        else if ((int)toIndex == (int)fromIndex) { 
            System.out.println("Cities must be different."); return; 
        }

        if (airGraph.hasEdge(fromIndex, toIndex)) { 
            System.out.println("Path exists and will be updated."); 
        }

        //get new route information
        System.out.print("Enter distance: ");
        String distance = s.nextLine();
        while (! distance.matches("\\d+") || distance.isEmpty()) {
            System.out.print("Enter distance: ");
            distance = s.nextLine();
        }
        System.out.print("Enter price: ");
        String priceStr = s.nextLine();
        while (! priceStr.matches("-?\\d+.?\\d*") || priceStr.isEmpty()) {
            System.out.print("Enter price: ");
            priceStr = s.nextLine();
        }
        int dist = Integer.parseInt(distance);
        double price = Double.parseDouble(priceStr);
        airGraph.updateEdge(fromIndex, toIndex, dist, price);
        System.out.println("Route added.");
    }
    
    private void allPathsLessThan(){
        Scanner s = new Scanner(System.in);                                                                       
        System.out.print("Set max cost: $");
        String maxCostStr = s.nextLine();
        if (maxCostStr.isEmpty() || !maxCostStr.matches("\\d+")) { 
            System.out.println("Invalid input"); return; 
        }
        double maxCost = Double.parseDouble(maxCostStr);
        // convert graph with given mode
        EdgeWeightedDigraph convertedG = new EdgeWeightedDigraph(airGraph, airGraph.getAllEdge("price"));
        // compute shortest paths
        DijkstraAllPairsSP allPath = new DijkstraAllPairsSP(convertedG);
        System.out.println("\nALL PATHS THAT COST " + maxCost + " OR LESS \n");
        int numV = airGraph.getNumV();
        int pathNum = 0;
        for (int i = 0; i < numV; i++){
            for (int j = i+1; j < numV; j++){
                double thisCost = allPath.dist(i, j);
                if (thisCost <= maxCost){
                    System.out.println("Path #" + (++pathNum) + ":");
                    for (DirectedEdge e : allPath.path(i, j))
                    {
                        int[] codes = {e.from(), e.to()};
                        String[] names = airGraph.parseCityCode(codes);
                        System.out.println(names[0]+" <-> "+names[1] + ": " + "$"+e.weight());
                    }
                    System.out.println("Total cost: $" + thisCost + "\n");
                }
            }
        }
        if (pathNum == 0){ 
            System.out.println("No such path.");
        }
    }

    private void ShortPath(String type) {
        Scanner s = new Scanner(System.in);

        //read "from" city
        System.out.print("From: ");
        String from = s.nextLine();
        Integer fromIndex = airGraph.parseCityName(from);
        if (fromIndex == null) { 
            System.out.println("City not found."); return; 
        }

        //reacd "to" city
        System.out.print("To: ");
        String to = s.nextLine();
        Integer toIndex = airGraph.parseCityName(to);
        if (toIndex == null) { System.out.println("City not found."); return; }

        // use the graph with given type
        EdgeWeightedDigraph convertedGraph = new EdgeWeightedDigraph(airGraph, airGraph.getAllEdge(type));
        DijkstraSP dsp = new DijkstraSP(convertedGraph, fromIndex);

        String typeStr;
        String units;
        if (type.equalsIgnoreCase("dist")) { 
            typeStr = "DISTANCE"; 
            units = " miles";
        } else if (type.equalsIgnoreCase("price")) { 
            typeStr = "COST"; 
            units = " dollars";
        } else if (type.equalsIgnoreCase("hop")) { 
            typeStr = "HOPS"; 
            units = " hops";
        } else { 
            throw new IllegalArgumentException("mode: dist/price/hop"); 
        }
        System.out.println("SHORTEST " + typeStr + " PATH from " + from + " to " + to + " is " + dsp.distTo(toIndex) + units);

        //determine shortest path 
        if (dsp.hasPathTo(toIndex)) {
            for (DirectedEdge e : dsp.pathTo(toIndex)) {
                int[] codes = {e.from(), e.to()};
                String[] names = airGraph.parseCityCode(codes);
                String valString = "";

                if (type.equalsIgnoreCase("dist")) { 
                    valString = ": "+ (int)e.weight() + "mi"; 
                } else if (type.equalsIgnoreCase("price")) { 
                    valString = ": "+ "$"+e.weight(); }
                else if (type.equalsIgnoreCase("hop")) { 
                    valString = ""; 
                }

                System.out.println(names[0]+" -> "+names[1] +valString);  // total val: sp.distTo(toIndex)
            }
        } else { System.out.println("No path exists."); }
    }
    
    private void MSTDist() {
        //use Kruska for minimum spanning tree
        System.out.println("/nMINIMUM SPANNING TREE:");
        KruskalMST mstResult = new KruskalMST(airGraph);
        String[] from = airGraph.parseCityCode(mstResult.MSToutFrom);
        String[] to = airGraph.parseCityCode(mstResult.MSToutTo);
        for (int i = 0; i < from.length; i++) {
            System.out.println(from[i] +" <-> "+ to[i] +": "+ (int)mstResult.MSToutVal[i]+"mi");
        }
    }

    private int menu() {
        Scanner s = new Scanner(System.in);
        int userIn = 100;
        System.out.println("\n----------------MENU----------------\n"
                    + "1. Display Graph\n"
                    + "2. Minimum Spanning Tree\n"
                    + "3. Shortest Distance Path\n"
                    + "4. Cheapest Path\n"
                    + "5. Fewest Layovers (hops) Path\n"
                    + "6. All trips less than a given price\n"
                    + "7. Add a route\n"
                    + "8. Remove a route\n"
                    + "0. Exit\n--------------------------------");
        //keep displaying menu
        while (userIn < 0 || userIn > 8) {
            System.out.print("Please enter an option: ");
            String userInStr = s.nextLine();
            //check that input is an integer or input is empty
            if (! userInStr.matches("\\d+") || userInStr.isEmpty()) { 
                userIn = 100; continue; 
            }
            userIn = Integer.parseInt(userInStr);
        }
        return userIn;
    }
    
    private AirGraph readFile() {
        String s = "";
        Scanner scan = new Scanner(System.in);
        while (s.isEmpty()) {
            System.out.print("Please enter file name: ");
            s = scan.next();    
            this.flightName = s;
            File f = new File(s);
            try {
                InputStreamReader inputReader = new InputStreamReader(new FileInputStream(f));
                BufferedReader bf = new BufferedReader(inputReader);
                int num = Integer.parseInt(bf.readLine());
                String[] v = new String[num];
                String nextLine;
                for (int i = 0; i < num; i++) {
                    nextLine = bf.readLine();
                    v[i] = nextLine;
                }
                AirGraph g = new AirGraph(num, v);
                while ((nextLine = bf.readLine()) != null) {
                    String[] thisEdge = nextLine.split(" ");
                    if (thisEdge.length != 4) { throw new IOException(); }
                    int from = Integer.parseInt(thisEdge[0]) - 1;
                    int to = Integer.parseInt(thisEdge[1]) - 1;
                    int dist = Integer.parseInt(thisEdge[2]);
                    double price = Double.parseDouble(thisEdge[3]);
                    g.addEdge(from, to, dist, price);
                }
                return g;
            } catch (IOException e) {
                System.out.println("Invalid file");
                s = "";
            }
        }
        scan.close();
        return null; 
    }
}
