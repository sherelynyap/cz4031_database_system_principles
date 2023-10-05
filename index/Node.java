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
        // Return the parent node.
        return this.internalNode;
    }

    public void setInternalNode(InternalNode updateInternalNode) {
        // Setter for the parent node.
        this.internalNode = updateInternalNode;
    }

    public float getKey(int targetIndex) {
        // retrieve the key based on index
        return keysSet.get(targetIndex);
    }

    public int setKey(float newKey) {
        int keysSize = keysSet.size();
        int targetIndex = 0;
        if (keysSize == 0) {
            // If this node has no key, just insert this new key
            keysSet.add(newKey);
        } else {
            if (keysSet.contains(newKey)) {
                // If this key is a duplicate, just find the targetIndex
                targetIndex = binarySearch(newKey);
            } else {
                // If this key is not a duplicate, just insert and find the targetIndex
                keysSet.add(newKey);
                Collections.sort(keysSet);
                targetIndex = binarySearch(newKey);
            }
        }
        return targetIndex;
    }

    public ArrayList<Float> getKeys() {
        // Return all the keys
        return this.keysSet;
    }

    public int binarySearch(float newKey) {
        // use binary search to find the newkey
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
        // Find middle value
        return (low + high) >>> 1;
    }

    public boolean getIsRoot() {
        // Getter for isRoot
        return this.isRoot;
    }

    public void setIsRoot(boolean rootBool) {
        // Setter for isRoot
        this.isRoot = rootBool;
    }

    public boolean getIsLeaf() {
        // Getter for isLeaf
        return this.isLeaf;
    }

    public void setIsLeaf(boolean leafBool) {
        // Setter for isLeaf
        this.isLeaf = leafBool;
    }

    public void deleteKey(int targetIndex) {
        // Delete a key based on index
        this.keysSet.remove(targetIndex);
    }

    public void deleteAllKeys() {
        // Delete all the keys
        resetKeys();
    }

    public void resetKeys() {
        // Delete all the keys
        this.keysSet = new ArrayList<>();
    }

    public float retrieveSmallestKey() {
        // Find the smallest key
        InternalNode temp;
        int firstIndex = 0;
        if (isLeaf == true) {
            // If this is a leaf, the leftmost key is the smallest key
            return this.getKey(firstIndex);
        } else {
            // If this is not a leaf, keep travelling LEFT of its childnode
            temp = (InternalNode) this;
            while (temp.getChildNode(firstIndex).getIsLeaf() == false) {
                temp = (InternalNode) temp.getChildNode(firstIndex);
            }
            return temp.getChildNode(firstIndex).getKey(firstIndex);
        }
    }

    public void deleteNode() {
        // Delete the node
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
        resetKeys();
        this.setIsLeaf(true);
        this.setIsRoot(true);
    }
}