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
        return childNodesSet.get(targetIndex);
    }

    public ArrayList<Node> getChildNodes() {
        return this.childNodesSet;
    }

    public int insertChild(Node newChild) {
        int targetPos = 0;
        int childNodesSetSize = childNodesSet.size();
        if (childNodesSetSize > 0) {
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

        } else {
            childNodesSet.add(newChild);
            newChild.setInternalNode(this);

        }
        return targetPos;
    }

    public void insertChildToFront(Node newChild) {
        childNodesSet.add(0, newChild);
        newChild.setInternalNode(this);
        deleteAllKeys();

        int childNodesSetSize = childNodesSet.size();
        float targetKeyIndex = -1;
        for (int ptr = 1; ptr < childNodesSetSize; ptr++) {
            targetKeyIndex = childNodesSet.get(ptr).retrieveSmallestKey();
            setKey(targetKeyIndex);
        }
    }

    public void doSeparation() {
        deleteAllKeys();
        resetChild();
    }

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

    public void deleteAllChildNodes() {
        resetChild();
    }

    public void resetChild() {
        this.childNodesSet = new ArrayList<Node>();
    }

    public Node getLeftSiblingNode(Node targetNode) {
        Node leftSib = null;
        if (childNodesSet.indexOf(targetNode) > 0) {
            int targetIndex = childNodesSet.indexOf(targetNode) - 1;
            leftSib = childNodesSet.get(targetIndex);
        }
        return leftSib;
    }

    public Node getRightSiblingNode(Node targetNode) {
        Node rightSib = null;
        if (childNodesSet.indexOf(targetNode) < childNodesSet.size() - 1) {
            int targetIndex = childNodesSet.indexOf(targetNode) + 1;
            rightSib = childNodesSet.get(targetIndex);
        }
        return rightSib;
    }
}