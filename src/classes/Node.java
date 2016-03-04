package classes;

import java.util.List;

/**
 * Created by Dimitri on 03/03/2016.
 */
public class Node {
    private Node parent;
    private List<Node> childs;
    private Object object;

    public Node(Object object, Node parent){
        this.object = object;
        this.parent = parent;
    }

    public void addChild(Object object, Node parent){
        Node child = new Node(object, parent);
        childs.add(child);
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
