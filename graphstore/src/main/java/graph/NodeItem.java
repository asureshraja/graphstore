package graph;

/**
 * Created by suresh on 30/4/17.
 */
public class NodeItem {
    private String itemName;
    private String nodeName;
    private String count;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public NodeItem(String itemName, String count) {
        this.itemName = itemName;
        this.count = count;
        
    }
}
