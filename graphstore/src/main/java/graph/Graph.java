package graph;
import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suresh on 30/4/17.
 */
public class Graph {
    DB db = null;
    private long node;
    public Graph(String databaseLocation){
        Options options = new Options();
        options.createIfMissing(true);
        try {
            db = factory.open(new File(databaseLocation), options);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
    }
    public void detailedPrint(){
        DBIterator iterator = db.iterator();
        try {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                String value = asString(iterator.peekNext().getValue());
                System.out.println(key+" <==> "+value);
            }
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            try {
                iterator.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void upsertKV(String index,String key,String value){
        db.put(bytes("index."+index+"="+key+"="), bytes(value));
    }
    public String getFromKV(String index,String key,String value){
        return asString(db.get(bytes("index."+index + "=" + key +"=")));
    }
    public void upsertListValue(String index,String listKey,String value,String extra){
        db.put(bytes("index."+index+"="+listKey+"="+value+"="), bytes(extra));
    }
    public ArrayList<String> getListValueWithExtras(String index,String listKey){
        ArrayList<String> values = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="index."+index+"="+listKey+"=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            String value = asString(iterator.peekNext().getValue());
            if (key.startsWith(prefix)){
                String node = key.replace(prefix, "");
                values.add(removeLastChar(node)+"="+value);
            }else{
                break;
            }
        }
        return values;
    }
    public void upsertListValue(String index,String listKey,String value){
        db.put(bytes("index."+index+"="+listKey+"="+value+"="), bytes(""));
    }
    public ArrayList<String> getListValue(String index,String listKey){
        ArrayList<String> values = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="index."+index+"="+listKey+"=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            if (key.startsWith(prefix)){
                String node = key.replace(prefix, "");
                values.add(removeLastChar(node));
            }else{
                break;
            }
        }
        return values;
    }

    public void createNewNode(String nodeName){
        db.put(bytes("V="+nodeName+"=props="), bytes(""));
    }

    public void createEdge(String from,String to){

        db.put(bytes("E=from"+"="+from+"="+"to="+to+"="), bytes(""));
        db.put(bytes("E=to"+"="+to+"="+"from="+from+"="), bytes(""));
    }

    public void createNewNode(String nodeName,String property,String propertyValue){
        StringBuilder sb = new StringBuilder();
        sb.append(property+",");
        db.put(bytes("V="+nodeName+"="+property+"="), bytes(propertyValue));
        db.put(bytes("V="+nodeName+"=props="), bytes(sb.toString()));
    }

    public void createEdge(String from,String to,String property,String propertyValue){
        StringBuilder sb = new StringBuilder();
        sb.append(property+",");

        db.put(bytes("E=from"+"="+from+"="+"to="+to+"="+property+"="), bytes(propertyValue));

        db.put(bytes("E=from"+"="+from+"="+"to="+to+"="), bytes(sb.toString()));
        db.put(bytes("E=to"+"="+to+"="+"from="+from+"="), bytes(""));
    }

    public void createNewNode(String nodeName,HashMap<String,String> properties){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry:properties.entrySet()){
            sb.append(entry.getKey()+",");
            db.put(bytes("V="+nodeName+"="+entry.getKey()+"="), bytes(entry.getValue()));
        }
        db.put(bytes("V="+nodeName+"=props="), bytes(sb.toString()));
    }

    public void createEdge(String from,String to,HashMap<String,String> properties){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry:properties.entrySet()){
            sb.append(entry.getKey()+",");
            db.put(bytes("E=from"+"="+from+"="+"to="+to+"="+entry.getKey()+"="), bytes(entry.getValue()));
        }
        db.put(bytes("E=from"+"="+from+"="+"to="+to+"="), bytes(sb.toString()));
        db.put(bytes("E=to"+"="+to+"="+"from="+from+"="), bytes(""));
    }

    public ArrayList<String> getOutgoingEdgesNodesFor(String forNode){
        ArrayList<String> nodes = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="E=from=" + forNode + "=to=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            if (key.startsWith(prefix) ){
                if(key.split("=").length==5){
                    String value = asString(iterator.peekNext().getValue());
                    String node = key.replace(prefix, "");
                    nodes.add(removeLastChar(node));
                }
            }else{
                break;
            }
        }
        return nodes;
    }

    public ArrayList<String> getIncomingEdgesNodesFor(String forNode){
        ArrayList<String> nodes = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="E=to=" + forNode + "=from=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            if (key.startsWith(prefix)){
                String value = asString(iterator.peekNext().getValue());
                String node = key.replace(prefix, "");
                nodes.add(removeLastChar(node));
            }else{
                break;
            }
        }
        return nodes;
    }
    public ArrayList<String> getOutgoingEdgesFor(String forNode){
        ArrayList<String> nodes = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="E=from=" + forNode + "=to=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            if (key.startsWith(prefix)){
                String value = asString(iterator.peekNext().getValue());
                String node = key.replace(prefix, "");
                nodes.add(node+forNode);
            }else{
                break;
            }
        }
        return nodes;
    }

    public ArrayList<String> getIncomingEdgesFor(String forNode){
        ArrayList<String> nodes = new ArrayList<String>();
        DBIterator iterator = db.iterator();
        String prefix="E=to=" + forNode + "=from=";
        for(iterator.seek(bytes(prefix)); iterator.hasNext(); iterator.next()) {
            String key = asString(iterator.peekNext().getKey());
            if (key.startsWith(prefix)){
                String value = asString(iterator.peekNext().getValue());
                String node = key.replace(prefix, "");
                nodes.add(forNode+"="+removeLastChar(node));
            }else{
                break;
            }
        }
        return nodes;
    }

    public HashMap<String,String> getNodeProperties(String nodeName){
        HashMap<String,String> result = new HashMap<String,String>();
        String key="V="+nodeName+"=props=";
        String value = asString(db.get(bytes(key)));
        if (value==null){
            return null;
        }else{
            for(String temp:value.split(",")){
                if(!temp.trim().equals("")){
                    String tempKey="V="+nodeName+"="+temp+"=";
                    result.put(temp,asString(db.get(bytes(tempKey))));
                }
            }
        }
        return result;
    }
    public HashMap<String,String> getEdgeProperties(String from,String to){
        HashMap<String,String> result = new HashMap<String,String>();
        String key="E=from"+"="+from+"="+"to="+to+"=";
        String value = asString(db.get(bytes(key)));
        if (value==null){
            return null;
        }else{
            for(String temp:value.split(",")){
                if(!temp.trim().equals("")){
                    String tempKey=key+temp+"=";
                    result.put(temp,asString(db.get(bytes(tempKey))));
                }
            }
        }
        return result;
    }

    public ArrayList<String> getDFSWays(String subGraphStartNode){
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> branching = new ArrayList<String>();
        Stack<String> adjList = new Stack<String>();
        adjList.add(subGraphStartNode+"="+subGraphStartNode);

        if (adjList.size()<=0){return result;}

        while (true){
            String adj= null;
            if (adjList.size()==0){
                break;
            }
            adj = adjList.pop();
            String val=adj.split("=")[1];
            adj=adj.split("=")[0];
//                System.out.println(adj);

            ArrayList<String> tmp = getOutgoingEdgesNodesFor(adj);
            if (tmp.size()==0){
//                break;
//                System.out.println(adj+"="+tmp.size()+"="+val);
                result.add(adj+"="+tmp.size()+"="+val);
            }else{
//                System.out.println(adj+"="+tmp.size()+"="+val);
                result.add(adj+"="+tmp.size()+"="+val);

                for (String t:tmp){
                    adjList.push(t+"="+adj);
                }

            }

        }
        return result;
    }

    public ArrayList<String> getDFSWaysWithTwoPropertyValue(String subGraphStartNode,String property1,String property2){
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> branching = new ArrayList<String>();
        Stack<String> adjList = new Stack<String>();
        adjList.add(subGraphStartNode+"="+subGraphStartNode);

        if (adjList.size()<=0){return result;}

        while (true){
            String adj= null;
            if (adjList.size()==0){
                break;
            }
            adj = adjList.pop();
            String val=adj.split("=")[1];
            adj=adj.split("=")[0];
//                System.out.println(adj);

            ArrayList<String> tmp = getOutgoingEdgesNodesFor(adj);
            if (tmp.size()==0){
                HashMap<String, String> nodeProp = getNodeProperties(adj);
                result.add(adj+"="+tmp.size()+"="+val+"="+nodeProp.get(property1)+"="+nodeProp.get(property2));
            }else{
                HashMap<String, String> nodeProp = getNodeProperties(adj);
                result.add(adj+"="+tmp.size()+"="+val+"="+nodeProp.get(property1)+"="+nodeProp.get(property2));

                for (String t:tmp){
                    adjList.push(t+"="+adj);
                }

            }

        }
        return result;
    }


    public ArrayList<String> getDFSWaysWithPropertyValue(String subGraphStartNode,String property){
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> branching = new ArrayList<String>();
        Stack<String> adjList = new Stack<String>();
        adjList.add(subGraphStartNode+"="+subGraphStartNode);

        if (adjList.size()<=0){return result;}

        while (true){
            String adj= null;
            if (adjList.size()==0){
                break;
            }
            adj = adjList.pop();
            String val=adj.split("=")[1];
            adj=adj.split("=")[0];
//                System.out.println(adj);

            ArrayList<String> tmp = getOutgoingEdgesNodesFor(adj);
            if (tmp.size()==0){
//                break;
//                System.out.println(adj+"="+tmp.size()+"="+val);
                result.add(adj+"="+tmp.size()+"="+val+"="+getNodeProperties(adj).get(property));
            }else{
//                System.out.println(adj+"="+tmp.size()+"="+val);
                result.add(adj+"="+tmp.size()+"="+val+"="+getNodeProperties(adj).get(property));

                for (String t:tmp){
                    adjList.push(t+"="+adj);
                }

            }

        }
        return result;
    }
    public void upsertEdgeProperty(String from,String to,String property,String updatedPropertyValue){
        HashMap<String,String> result = new HashMap<String,String>();
        String refactorKey="E=from"+"="+from+"="+"to="+to+"=";
        String tempKey=refactorKey+property+"=";
        db.put(bytes(tempKey),bytes(updatedPropertyValue));
        String propNames = asString(db.get(bytes("E=from"+"="+from+"="+"to="+to+"=")));
        boolean alreadyExists=false;
        for(String temp:propNames.split(",")){
            if(!temp.trim().equals("")){
                if(temp.equals(property)){
                    alreadyExists=true;
                    break;
                }
            }
        }
        if (alreadyExists==false){
            db.put(bytes("E=from"+"="+from+"="+"to="+to+"="), bytes(propNames + property + ","));
        }
    }
    public void upsertNodeProperty(String nodeName,String property,String updatedPropertyValue){
        HashMap<String,String> result = new HashMap<String,String>();
        String key="V="+nodeName+"="+property+"=";
        db.put(bytes(key),bytes(updatedPropertyValue));
        String propNames = asString(db.get(bytes("V="+nodeName+"=props=")));
        boolean alreadyExists=false;
        for(String temp:propNames.split(",")){
            if(!temp.trim().equals("")){
                if(temp.equals(property)){
                    alreadyExists=true;
                    break;
                }
            }
        }
        if (alreadyExists==false){
            db.put(bytes("V=" + nodeName + "=props="), bytes(propNames + property + ","));
        }

    }
}
