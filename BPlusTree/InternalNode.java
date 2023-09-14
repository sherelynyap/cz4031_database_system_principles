package BPlusTree;

import java.util.ArrayList;

public class InternalNode extends Node {
    private ArrayList<Node> childNodes;

    public InternalNode() {
        super();
        this.setIsLeafNode(false);
        this.setIsRootNode(false);
        childNodes = new ArrayList<Node>();
    }

    public ArrayList<Node> getChildNodes() {
        return childNodes;
    }

    public Node getChildNode(int i) {
        return childNodes.get(i);
    }

    /**
    doChildInsertion(Node child): Inserts a child node into an internal node of a B+ tree.

    If there are no child nodes in the internal node:
        Add the child node at position 0 and set the internal node of the child node to be the current internal node.
        Return position as 0.
    If there are already child nodes:
        Retrieve the smallest key in the parent node and the smallest key in the child node, which determines
        whether to insert the child node before or after the existing child node based on the comparison of the smallest keys.
        Set the internal node of the child node to be the current internal node and return the position where the
        child node was inserted.
    */
    public int doChildInsertion(Node child) {
        int position = 0;

        if (childNodes.size() == 0) {
            childNodes.add(child);
            child.setInternalNode(this);
            return position;
        }

        int smallestParentKey = this.doSmallestKeyRetrieval();

        int smallestChildKey = child.doSmallestKeyRetrieval();

        if (smallestParentKey <= smallestChildKey){
            position = this.setKey(smallestChildKey);
            this.childNodes.add(position + 1, child);
        }

        else{
            this.setKey(smallestParentKey);
            this.childNodes.add(position, child);
        }

        child.setInternalNode(this);
        return position;
    }

    /**
    insertChildToFront(Node child): add a new child node to the front of the current node's child list.

    Adds the new child node to the beginning of the list, and sets the current node as its parent
    using the setInternalNode() method.
    After adding the new child, the keys list is initialized as a new empty list by calling the doKeysDeletion() method
    from the Node class.
    Then add the key value of each child node to the current node's list of keys, in ascending order.
    Calls doSmallestKeyRetrieval() on each child node to retrieve its smallest key value, and then sets
    that value as a key in the current node using setKey().
     */
    public void insertChildToFront(Node child) {
        childNodes.add(0, child);
        child.setInternalNode(this);
        doKeysDeletion();
        //add the new child's key and together with the rest of the keys
        for (int i = 1; i < childNodes.size(); i++) {
            setKey(childNodes.get(i).doSmallestKeyRetrieval());
        }
    }

    public void doSeparation() {
        doKeysDeletion();
        childNodes = new ArrayList<Node>();
    }

    public void doAllChildNodesDeletion() {
        childNodes = new ArrayList<Node>();
    }

    /**
    doChildNodeDeletion(Node child): Deletes a child node from an internal node in a B+ tree.

    Removes the given child node from the list of child nodes in the current node, the keys list is initialized
    as a new empty list by calling doKeysDeletion(), and then updates the keys of the remaining
    child nodes by calling setKey() with the smallest key of each node.
    This is to ensure that the internal node remains valid after the removal of a child node by
    updating the key values of the remaining child nodes.
     */
    public void doChildNodeDeletion(Node child) {
        childNodes.remove(child);
        doKeysDeletion();

        for (int i = 1; i < childNodes.size(); i++) {
            setKey(childNodes.get(i).doSmallestKeyRetrieval());
        }
    }

    /**
    getLeftSiblingNode(Node node): Returns the left sibling node of that node in the current internal node.

    If the node is not the first child node of the current internal node:
        Return the child node immediately to the left of the input node
    If the input Node object is the first child node of the current internal node:
        Return null
     */
    public Node getLeftSiblingNode(Node node) {
        if (childNodes.indexOf(node) > 0) {
            return childNodes.get(childNodes.indexOf(node) - 1);
        }
        return null;
    }
    /**
     getRightSiblingNode(Node node): Returns the right sibling node of that node in the current internal node.

     If the node is not the first child node of the current internal node:
        Return the child node immediately to the right of the input node
     If the input Node object is the first child node of the current internal node:
        Return null
     */
    public Node getRightSiblingNode(Node node) {
        if (childNodes.indexOf(node) < childNodes.size() - 1) {
            return childNodes.get(childNodes.indexOf(node) + 1);
        }
        return null;
    }
}