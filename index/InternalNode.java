package index;

import java.util.ArrayList;

public class InternalNode extends Node {
    private ArrayList<Node> childNodesSet;

    public InternalNode() {
        super();
        this.setIsLeaf(false);
        this.setIsRoot(false);
        childNodesSet = new ArrayList<Node>();
    }

    public Node getChildNode(int targetIndex) {
        // Getter for childNode using index.
        return childNodesSet.get(targetIndex);
    }

    public ArrayList<Node> getChildNodes() {
        // Getter for childNodesSet.
        return this.childNodesSet;
    }

    public int insertChild(Node newChild) {
        // Insert the new child
        int targetPos = 0;
        int childNodesSetSize = childNodesSet.size();
        if (childNodesSetSize > 0) {
            // If this node has children.
            // Get smallest key of this new child.
            float minChildKey = newChild.retrieveSmallestKey();
            // Get smallest key of this node.
            float minParentKey = retrieveSmallestKey();

            // Assign smallest key to the keysSet.
            // Add this newChild based on the target position obtained.
            if (minParentKey > minChildKey) {
                this.setKey(minParentKey);
                this.childNodesSet.add(targetPos, newChild);
            } else {
                targetPos = setKey(minChildKey);
                this.childNodesSet.add(targetPos + 1, newChild);
            }
            newChild.setInternalNode(this);

        } else {
            // If this node has no children.
            childNodesSet.add(newChild);
            newChild.setInternalNode(this);

        }
        return targetPos;
    }

    public void insertChildToFront(Node newChild) {
        // Insert this new child to the front.
        childNodesSet.add(0, newChild);
        newChild.setInternalNode(this);
        deleteAllKeys();

        int childNodesSetSize = childNodesSet.size();
        float targetKeyIndex = -1;
        // Adjust the keys.
        for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
            targetKeyIndex = childNodesSet.get(ptr).retrieveSmallestKey();
            setKey(targetKeyIndex);
        }
    }

    public void doSeparation() {
        // Clear all the keys and child
        deleteAllKeys();
        resetChild();
    }

    public void deleteChildNode(Node targetChild) {
        // Delete this target child from the sets of children.
        childNodesSet.remove(targetChild);

        // Empty the keys for restructuring.
        this.deleteAllKeys();
        int childNodesSetSize = childNodesSet.size();

        // Restructure the keys.
        float targetKeyIndex = -1;
        for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
            targetKeyIndex = childNodesSet.get(ptr).retrieveSmallestKey();
            setKey(targetKeyIndex);
        }
    }

    public void deleteAllChildNodes() {
        // Clear all child nodes.
        resetChild();
    }

    public void resetChild() {
        // Clear all child nodes.
        this.childNodesSet = new ArrayList<Node>();
    }

    public Node getLeftSiblingNode(Node targetNode) {
        // Get left sibling.
        Node leftSib = null;
        if (childNodesSet.indexOf(targetNode) > 0) {
            int targetIndex = childNodesSet.indexOf(targetNode) - 1;
            leftSib = childNodesSet.get(targetIndex);
        }
        return leftSib;
    }

    public Node getRightSiblingNode(Node targetNode) {
        // Get right sibling.
        Node rightSib = null;
        if (childNodesSet.indexOf(targetNode) < childNodesSet.size() - 1) {
            int targetIndex = childNodesSet.indexOf(targetNode) + 1;
            rightSib = childNodesSet.get(targetIndex);
        }
        return rightSib;
    }
}