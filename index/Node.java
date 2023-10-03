package index;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
    private InternalNode internalNode;
    private ArrayList<Float> keysSet;
    private boolean isRoot;
    private boolean isLeaf;

    public Node() {
        this.keysSet = new ArrayList<>();
        this.isRoot = true;
        this.isLeaf = true;
    }

    public InternalNode getInternalNode() {
        return this.internalNode;
    }

    public void setInternalNode(InternalNode updateInternalNode) {
        this.internalNode = updateInternalNode;
    }

    public ArrayList<Float> getKeys() {
        return this.keysSet;
    }

    public float getKey(int targetIndex) {
        return keysSet.get(targetIndex);
    }

    public int setKey(float newKey) {
        int keysSize = keysSet.size();
        int targetIndex = 0;
        if (keysSize == 0) {
            keysSet.add(newKey);
        } else {
            keysSet.add(newKey);
            Collections.sort(keysSet);
            targetIndex = binarySearch(newKey);

        }
        return targetIndex;
    }

    public int binarySearch(float newKey) {
        int keysSize = keysSet.size();
        int highPtr = keysSize - 1;
        int lowPtr = 0;
        int targetIndex = -1;

        while (lowPtr <= highPtr) {
            int midPtr = findMid(lowPtr, highPtr);
            float valMid = keysSet.get(midPtr);

            if (valMid > newKey) {
                highPtr = midPtr - 1;
            } else if (valMid < newKey) {
                lowPtr = midPtr + 1;
            } else {
                targetIndex = midPtr;
                lowPtr = midPtr + 1;
            }
        }
        return targetIndex;
    }

    public int findMid(int low, int high) {
        return (low + high) >>> 1;
    }

    public boolean getIsLeafNode() {
        return this.isLeaf;
    }

    public void setIsLeafNode(boolean leafBool) {
        this.isLeaf = leafBool;
    }

    public boolean getIsRootNode() {
        return this.isRoot;
    }

    public void setIsRootNode(boolean rootBool) {
        this.isRoot = rootBool;
    }

    public void deleteKey(int targetIndex) {
        this.keysSet.remove(targetIndex);
    }

    public void deleteAllKeys() {
        doClearKeys();
    }

    public float retrieveSmallestKey() {
        InternalNode temp;
        int firstIndex = 0;
        if (isLeaf == true) {
            return this.getKey(firstIndex);
        } else {
            temp = (InternalNode) this;
            while (temp.getChildNode(firstIndex).getIsLeafNode() == false) {
                temp = (InternalNode) temp.getChildNode(firstIndex);
            }
            return temp.getChildNode(firstIndex).getKey(firstIndex);
        }
    }

    public void deleteNode() {
        if (this.internalNode != null) {
            this.internalNode.deleteChildNode(this);
            this.internalNode = null;
        }
        if (isLeaf == false) {
            InternalNode tempInt = (InternalNode) this;
            tempInt.deleteAllChildNodes();
        } else {
            LeafNode tempLeaf = (LeafNode) this;
            tempLeaf.setNextNode(null);
            tempLeaf.deleteAddresses();
        }
        doClearKeys();
        this.setIsLeafNode(true);
        this.setIsRootNode(true);
    }

    public void doClearKeys() {
        this.keysSet = new ArrayList<>();
    }
}