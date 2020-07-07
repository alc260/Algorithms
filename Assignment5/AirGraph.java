import java.util.ArrayList;
import java.util.Arrays;

//adjacent list
public class AirGraph {    

    private int num = 0;
    private String[] V;
    private ArrayList<edgeVals>[] adjList;

    public AirGraph(int numV, String[] allV) {
        this.num = numV;
        this.V = allV;
        adjList = new ArrayList[numV];
        for (int i = 0; i < numV; i++) { 
            adjList[i] = new ArrayList<>(); 
        }
    }

    //all the values stored at each edge
    private class edgeVals {
        private int from, to;
        private int dist;
        private double price;

        public edgeVals(int from, int to, int dist, double price) {
            this.from = from;
            this.to = to;
            this.dist = dist;
            this.price = price;
        }
    }
    
    public int getNumV() { 
        return num; 
    }
    
    //number of edges = list size
    public int numEdges() {
        int result = 0;
        for (int v = 0; v < num; v++) { 
            result += adjList[v].size(); 
        }
        return result;
    }
    
    //find all edges
    public double[][] getAllEdge(String mode) {
        int numE = numEdges();
        double[][] result = new double[numE][3];
        int i = 0;
        for (int v = 0; v < num; v++) { 
            for (edgeVals e : adjList[v]) {
                double edgeVal;
                if (mode.equalsIgnoreCase("dist")) { 
                    edgeVal = e.dist; 
                }
                else if (mode.equalsIgnoreCase("price")) { 
                    edgeVal = e.price; 
                }
                else if (mode.equalsIgnoreCase("hop")) { 
                    edgeVal = 1.0; 
                }
                else { 
                    throw new IllegalArgumentException("mode: dist/price/hop"); 
                }
                double[] thisEdge = {e.from, e.to, edgeVal};
                result[i++] = thisEdge;
            }
        }
        return result;
    }
    
    public boolean hasEdge(int v1, int v2) {
        return (getEdge(v1, v2) != null);
    }
    
    private edgeVals getEdge(int v1, int v2) {
        for (edgeVals e : adjList[v1]) {
            if (e.to == v2) { 
                return e; 
            }
        }
        for (edgeVals e : adjList[v2]) {
            if (e.to == v1) { 
                return e; 
            }
        }
        return null;
    }
    
    public String[] parseCityCode(int[] raw) {
        String[] r = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            int thisCode = raw[i];
            r[i] = V[thisCode];
        }
        return r;
    }
    
    public Integer parseCityName(String raw) {
        for (int i = 0; i < V.length; i++) {
            if (V[i].equalsIgnoreCase(raw)) {
                return i;
            }
        }
        return null;
    }
    
    public void updateEdge(int from, int to, int dist, double price) {
        try { removeEdge(from, to); } 
        catch (IllegalArgumentException e) {}
        addEdge(from, to, dist, price);
    }
    
    public void addEdge(int from, int to, int dist, double price) {
        adjList[from].add(new edgeVals(from, to, dist, price));
    }

    public void removeEdge(int v1, int v2) {
        for (edgeVals e : adjList[v1]) {
            if (e.to == v2) {
                adjList[v1].remove(e);
                return;
            }
        }
        for (edgeVals e : adjList[v2]) {
            if (e.to == v1) {
                adjList[v2].remove(e);
                return;
            }
        }
        throw new IllegalArgumentException("Cannot find given edge");
    }
    
    public void printGraph() {
        System.out.println();
        for (int from = 0; from < num; from++) {
            //prevent printing edge twice
            for (edgeVals e : adjList[from]) {
                System.out.println(V[from] + " <-> " + V[e.to] + ": " + e.dist + "mi, $" + e.price);
            }
        }
    }

    public ArrayList<String> makeFile() {
        ArrayList<String> result = new ArrayList<>();
        result.add(""+num);
        result.addAll(Arrays.asList(V));
        for (int v = 0; v < num; v++) { 
            for (edgeVals e : adjList[v]) {
                result.add((e.from+1)+" "+(e.to+1)+" "+e.dist+" "+e.price);
            }
        }
        return result;
    }

}
