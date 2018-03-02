
package graph;

import java.util.HashMap;

/**
 * Created by suresh on 30/4/17.
 */
public class TestGraph {
    public static void main(String[] args) {
        Graph graph = new Graph("/home/suresh/rec/leveldb");
        HashMap<String,String> props = new HashMap<String, String>();
        props.put("value","1");
        graph.createNewNode("1",props);
        props.put("value","2");
        graph.createNewNode("2",props);
        props.put("value","3");
        graph.createNewNode("3",props);
        props.put("value","4");
        graph.createNewNode("4",props);
        props.put("value","5");
        graph.createNewNode("5",props);
        props.put("value","6");
        graph.createNewNode("6",props);
        props.put("value","7");
        graph.createNewNode("7",props);

        props.put("value","1-2");
        graph.createEdge("1", "2", props);

        props.put("value","2-3");
        graph.createEdge("2","3",props);

        props.put("value","2-4");
        graph.createEdge("2","4",props);

        props.put("value","4-5");
        graph.createEdge("4","5",props);
        props.put("value","4-6");
        graph.createEdge("4","6",props);

        props.put("value","3-7");
        graph.createEdge("3","7",props);
        for(String tmp:graph.getOutgoingEdgesNodesFor("2")){
            System.out.println(tmp);
        }
        graph.getDFSWays("1");
//        graph.detailedPrint();
    }
}
