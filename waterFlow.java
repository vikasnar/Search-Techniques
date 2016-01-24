import java.io.*;
import java.util.*;

/**
 * Created by VikasN on 9/4/15.
 */

class Node {
    public String id;
    public int cost = 0;
    public HashMap<Node,Integer> adjacentNodesMap;
    public HashMap<Node,ArrayList<Integer>> brokenPipeTime;

    public Node(){
        this.adjacentNodesMap =  new HashMap<>();
        this.brokenPipeTime = new HashMap<>();
    }

    public int getNodeCost(){
        return cost;
    }

    public String getNodeId(){
        return id;
    }

    public void addNeighbors(Node child,Integer length){
        this.adjacentNodesMap.put(child,length);
    }

    public void setOffPeriods(Node child, ArrayList<Integer> t){
        this.brokenPipeTime.put(child, t);
    }
}

public class waterFlow {
    public HashMap<String,Node> allNodes = new HashMap<>();
    String outString = "";

    public static void main(String[] cmdInput){
        String fName = null;
        String startNode;
        waterFlow a = new waterFlow();
        ArrayList<String> destNodes = new ArrayList<>();
        int startTime = 0;
        if(cmdInput.length == 2) {
            fName = cmdInput[1].toString();
            if(!fName.substring(fName.length()-4,fName.length()).equalsIgnoreCase(".txt")){
                fName += ".txt";
            }
            try {
                File inputFile = new File(fName);
                BufferedReader inRead1 = new BufferedReader(new FileReader(inputFile));
                String l = inRead1.readLine();
                int numTest = Integer.parseInt(l);
                for(int i=0;i<numTest;i++){
                    //Read Search Method
                    l=inRead1.readLine();
                    String searchMethod = l.toString();
                    //Read StartNode
                    l= inRead1.readLine().toString();
                    startNode = l;
                    Node sNode = new Node();
                    sNode.id = l;
                    a.allNodes.put(sNode.id, sNode);
                    //Read destination Nodes
                    l= inRead1.readLine().toString();
                    String[] splited = l.split("\\s+");
                    for (int j = 0; j < splited.length; j++) {
                        Node destNode = new Node();
                        destNode.id = splited[j].toString();
                        a.allNodes.put(splited[j].toString(),destNode);
                        destNodes.add(destNode.id);
                    }
                    //Read Middle Nodes
                    l= inRead1.readLine().toString();
                    splited = l.split("\\s+");
                    for (int j = 0; j < splited.length; j++) {
                        Node midNode = new Node();
                        midNode.id = splited[j].toString();
                        a.allNodes.put(splited[j].toString(),midNode);
                    }
                    //Read pipe information
                    int _nopipes= Integer.parseInt(inRead1.readLine().toString());
                    for (int j = 0; j < _nopipes; j++) {
                        l= inRead1.readLine().toString();
                        splited = l.split("\\s+");
                        //logic to map nodes
                        Node root = a.allNodes.get(splited[0].toString());
                        Node child = a.allNodes.get(splited[1].toString());
                        //Read the off period information of the pipes
                        ArrayList<Integer> a1 = new ArrayList<>();
                        for (int k = 1; k <= Integer.parseInt(splited[3].toString()); k++) {
                            String off_period = splited[3+k].toString();
                            String[]start_end = off_period.split("-");
                            int start = Integer.parseInt(start_end[0]);
                            int end = Integer.parseInt(start_end[1]);
                            for (int lo = start ; lo <= end;lo++)
                                a1.add(lo);
                        }
                        root.setOffPeriods(child,a1);
                        //Add the child to the parent node along with length
                        root.addNeighbors(child,Integer.parseInt(splited[2].toString()));

                    }
                    startTime = Integer.parseInt(a.readLine(inRead1));

                    if(searchMethod.equalsIgnoreCase("BFS")){
                        a.outString += a.bfs(startNode, startTime, destNodes) + "\n";
                        a.allNodes.clear();
                        destNodes.clear();
                    }
                    if(searchMethod.equalsIgnoreCase("DFS")){
                        a.outString += a.dfs(startNode, startTime, destNodes) + "\n";
                        a.allNodes.clear();
                        destNodes.clear();
                    }
                    if(searchMethod.equalsIgnoreCase("UCS")){
                        a.outString += a.ucs(startNode, startTime, destNodes) + "\n";
                        a.allNodes.clear();
                        destNodes.clear();
                    }

                    if(i+1!=numTest) {
                        //Read the empty line if it is not the last test case
                        a.readLine(inRead1);
                    }
                }
                a.writeToFile(a.outString);
            } catch (Exception e) {
                a.writeToFile(a.outString);
                System.out.println("Problem reading the file!!");
                e.printStackTrace();
            }
        }
    }

    class alphaOrderComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return n1.getNodeId().compareTo(n2.getNodeId());
        }
    }

    class reverseAlphaOrderComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return n2.getNodeId().compareTo(n1.getNodeId());
        }
    }

    class leastCostComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2){
            if(n1.getNodeCost()<n2.getNodeCost())
                return -1;
            if(n2.getNodeCost()<n1.getNodeCost())
                return 1;
            else
                return n1.getNodeId().compareTo(n2.getNodeId());
        }
    }

    public String readLine(BufferedReader in){
        String l = "";
        try{
            if((l = in.readLine().toString())!=null){
                return l;
            }
            else
            {
                return null;
            }
        }
        catch(Exception e){
            System.out.println("Read Method : " + e.getMessage());
        }
        return l;
    }

    public void writeToFile(String s){
        try {
            PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
            if(s.charAt(s.length()-1)=='\n'){
                s = s.substring(0,s.length()-1);
            }
            writer.print(s);
            writer.close();
        }
        catch (Exception e){
            System.out.println("e.getMessage()");
        }

    }

    public String bfs(String startName, int sTime, ArrayList<String> destNames){
        String toWrite = "None";
        Queue<Node> tree = new LinkedList<Node>();
        ArrayList<Node> exploredNodes = new ArrayList<Node>();
        Node sNode = allNodes.get(startName);
        ArrayList<Node> desNodes = new ArrayList<Node>();
        //populate the destination nodes
        for(String dN : destNames)
            desNodes.add(allNodes.get(dN));
        //set initial brokenPipeTime
        sNode.cost += sTime;
        tree.add(sNode);

        while(!tree.isEmpty()){
            Node test = tree.remove();
            if(desNodes.contains(test)){
                for(Node a : exploredNodes)
                    System.out.print("(" + a.getNodeId() + "-" + a.getNodeCost() + "),");
                System.out.println("\n"+"-----BFS-------");
                return test.getNodeId()+" "+test.getNodeCost()%24;
            }
            //Sort them alphabetically to avoid ties
            ArrayList<Node> childNodes = new ArrayList<Node> (test.adjacentNodesMap.keySet());
            Collections.sort(childNodes,new alphaOrderComparator());

            for(Node child : childNodes)
                if (!exploredNodes.contains(child)) {
                    child.cost = test.getNodeCost() + 1;
                    tree.add(child);
                    exploredNodes.add(child);
                }
        }
        return (toWrite);
    }

    public String dfs(String startName, int sTime, ArrayList<String> destNames){
        String toWrite = "None";
        //DFS Algorithm
        Stack<Node> dfsTree = new Stack<Node>();
        ArrayList<Node> exploredNodes = new ArrayList<Node>();
        Node sNode = allNodes.get(startName);
        ArrayList<Node> desNodes = new ArrayList<Node>();
        //populate the destination nodes
        for(String dN : destNames)
            desNodes.add(allNodes.get(dN));
        //set initial brokenPipeTime
        sNode.cost += sTime;
        dfsTree.push(sNode);

        while (!dfsTree.isEmpty()){
            Node test = dfsTree.pop();
            exploredNodes.add(test);
            if(desNodes.contains(test)){
                for(Node a : exploredNodes)
                    System.out.print("(" + a.getNodeId() + "-" + a.getNodeCost() + "),");
                System.out.println("\n"+"-----DFS-------");
                return test.getNodeId()+" "+test.getNodeCost()%24;
            }
            //Sort them alphabetically to break ties
            ArrayList<Node> childNodes = new ArrayList<Node> (test.adjacentNodesMap.keySet());
            Collections.sort(childNodes, new reverseAlphaOrderComparator());
//            Collections.reverse(childNodes);

            for(Node child : childNodes)
                if(!exploredNodes.contains(child))
                {
                    child.cost = test.getNodeCost()+1;
                    dfsTree.push(child);
                }
        }
        return (toWrite);
    }

    public String ucs(String startName, int sTime, ArrayList<String> destNames){
        String toWrite = "None";
        PriorityQueue<Node> tree = new PriorityQueue<Node>(new leastCostComparator());

        ArrayList<Node> exploredNodes = new ArrayList<Node>();
        Node sNode = allNodes.get(startName);
        ArrayList<Node> desNodes = new ArrayList<Node>();
        //populate the destination nodes
        for(String dN : destNames)
            desNodes.add(allNodes.get(dN));
        //set initial brokenPipeTime
        sNode.cost += sTime;
        tree.offer(sNode);
        ArrayList<Node> paths = new ArrayList<>();
        while(!tree.isEmpty()){
            Node test = tree.poll();

            if(desNodes.contains(test)){
                for(Node a : exploredNodes)
                    System.out.print("(" + a.getNodeId() + "-" + a.getNodeCost() + "),");
                System.out.println("\n"+"-----UCS------");
                return test.getNodeId()+" "+test.getNodeCost()%24;
            }
            ArrayList<Node> childNodes = new ArrayList<Node> (test.adjacentNodesMap.keySet());
            Collections.sort(childNodes,new alphaOrderComparator());
//            int exploredChildCount = 0;
            for(Node child : childNodes) {
                if (!exploredNodes.contains(child) ) {
                    if (!tree.contains(child)) {
                        if (!test.brokenPipeTime.get(child).contains(test.cost % 24)) {
                            child.cost = test.getNodeCost() + test.adjacentNodesMap.get(child);
                            tree.offer(child);
//                            exploredChildCount++;
                        }
                    }
                    else if(tree.contains(child) && child.cost>test.getNodeCost() + test.adjacentNodesMap.get(child) && (!test.brokenPipeTime.get(child).contains(test.cost % 24))){
                        child.cost = test.getNodeCost() + test.adjacentNodesMap.get(child);
//                        tree.offer(child);
//                        exploredChildCount++;
                    }
                }
            }
//            if(exploredChildCount == childNodes.size())//check if all the children of the parent are explored if yes then add to explored list
            exploredNodes.add(test);
        }
        return (toWrite);
    }
}
