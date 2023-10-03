package index;

import java.util.ArrayList;

public class InternalNode extends Node {
    private ArrayList<Node> childNodesSet;

    public InternalNode() {
        // Refactored
        super();
        this.setIsLeafNode(false);
        this.setIsRootNode(false);
        childNodesSet = new ArrayList<Node>();
    }

    public ArrayList<Node> getChildNodes() {
        return this.childNodesSet;
    }

    public Node getChildNode(int targetIndex) {
        return childNodesSet.get(targetIndex);
    }

    /**
     * doChildInsertion(Node child): Inserts a child node into an internal node of a
     * B+ tree.
     * 
     * If there are no child nodes in the internal node:
     * Add the child node at position 0 and set the internal node of the child node
     * to be the current internal node.
     * Return position as 0.
     * If there are already child nodes:
     * Retrieve the smallest key in the parent node and the smallest key in the
     * child node, which determines
     * whether to insert the child node before or after the existing child node
     * based on the comparison of the smallest keys.
     * Set the internal node of the child node to be the current internal node and
     * return the position where the
     * child node was inserted.
     */
    public int insertChild(Node newChild) {
        int targetPos = 0;
        int childNodesSetSize = childNodesSet.size();
        if (childNodesSetSize == 0) {
            childNodesSet.add(newChild);
            newChild.setInternalNode(this);
            // return targetPos;
        } else {
            float minChildKey = newChild.retrieveSmallestKey();
            float minParentKey = retrieveSmallestKey();

            if (minParentKey > minChildKey) {
                this.setKey(minParentKey);
                this.childNodesSet.add(targetPos, newChild);
            } else {
                targetPos = setKey(minChildKey);
                this.childNodesSet.add(targetPos + 1, newChild);
            }
            newChild.setInternalNode(this);
            // return targetPos;
        }
        return targetPos;
    }

    /**
     * insertChildToFront(Node child): add a new child node to the front of the
     * current node's child list.
     * 
     * Adds the new child node to the beginning of the list, and sets the current
     * node as its parent
     * using the setInternalNode() method.
     * After adding the new child, the keys list is initialized as a new empty list
     * by calling the doKeysDeletion() method
     * from the Node class.
     * Then add the key value of each child node to the current node's list of keys,
     * in ascending order.
     * Calls doSmallestKeyRetrieval() on each child node to retrieve its smallest
     * key value, and then sets
     * that value as a key in the current node using setKey().
     */
    public void insertChildToFront(Node newChild) {
        childNodesSet.add(0, newChild);
        newChild.setInternalNode(this);
        deleteAllKeys();
        // add the new child's key and together with the rest of the keys
        int childNodesSetSize = childNodesSet.size();
        float targetKeyIndex = -1;
        for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
            targetKeyIndex = childNodesSet.get(ptr).retrieveSmallestKey();
            // setKey(childNodesSet.get(ptr).doSmallestKeyRetrieval());
            setKey(targetKeyIndex);
        }
    }

    public void doSeparation() {
        deleteAllKeys();
        resetChild();
    }

    public void deleteAllChildNodes() {
        resetChild();
    }

    public void resetChild() {
        this.childNodesSet = new ArrayList<Node>();
    }

    /**
     * doChildNodeDeletion(Node child): Deletes a child node from an internal node
     * in a B+ tree.
     * 
     * Removes the given child node from the list of child nodes in the current
     * node, the keys list is initialized
     * as a new empty list by calling doKeysDeletion(), and then updates the keys of
     * the remaining
     * child nodes by calling setKey() with the smallest key of each node.
     * This is to ensure that the internal node remains valid after the removal of a
     * child node by
     * updating the key values of the remaining child nodes.
     */
    public void deleteChildNode(Node targetChild) {
        childNodesSet.remove(targetChild);
        this.deleteAllKeys();

        int childNodesSetSize = childNodesSet.size();
        float targetKeyIndex = -1;
        for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
            targetKeyIndex = childNodesSet.get(ptr).retrieveSmallestKey();
            setKey(targetKeyIndex);
        }
    }

    /**
     * getLeftSiblingNode(Node node): Returns the left sibling node of that node in
     * the current internal node.
     * 
     * If the node is not the first child node of the current internal node:
     * Return the child node immediately to the left of the input node
     * If the input Node object is the first child node of the current internal
     * node:
     * Return null
     */
    public Node getLeftSiblingNode(Node targetNode) {
        Node leftSib = null;
        if (childNodesSet.indexOf(targetNode) > 0) {
            int targetIndex = childNodesSet.indexOf(targetNode) - 1;
            leftSib = childNodesSet.get(targetIndex);
        }
        return leftSib;
    }

    /**
     * getRightSiblingNode(Node node): Returns the right sibling node of that node
     * in the current internal node.
     * 
     * If the node is not the first child node of the current internal node:
     * Return the child node immediately to the right of the input node
     * If the input Node object is the first child node of the current internal
     * node:
     * Return null
     */
    public Node getRightSiblingNode(Node targetNode) {
        Node rightSib = null;
        if (childNodesSet.indexOf(targetNode) < childNodesSet.size() - 1) {
            int targetIndex = childNodesSet.indexOf(targetNode) + 1;
            rightSib = childNodesSet.get(targetIndex);
        }
        return rightSib;
    }
}