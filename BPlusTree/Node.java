package BPlusTree;

import java.util.ArrayList;
import java.util.Collections;

public class Node {
    // Refactored
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

    /**
     * setKey(int key): A binary search algorithm that repeatedly divides the search
     * interval in half until the key is
     * found or the search interval is empty. In each iteration, the algorithm
     * compares the key to the middle element
     * of the search interval and updates the interval accordingly.
     * 
     * If the key is smaller than the middle element:
     * the search interval is updated to the left half of the middle element.
     * If the key is larger than the middle element:
     * the search interval is updated to the right half of the middle element.
     * If the key is equal to the middle element:
     * the index of the first occurrence of the key is recorded
     */
    public int setKey(float newKey) {
        int keysSize = keysSet.size();
        int targetIndex = 0;
        if (keysSize == 0) {
            keysSet.add(newKey);
        } else {
            keysSet.add(newKey);
            keysSize = keysSet.size();
            Collections.sort(keysSet);
            int highPtr = keysSize - 1;
            int lowPtr = 0;
            targetIndex = -1;

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
        }
        return targetIndex;
    }

    public int findMid(int low, int high) {
        // Divides by 2
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

    // delete key in the node
    public void doKeyDeletion(int targetIndex) {
        this.keysSet.remove(targetIndex);
    }

    public void doKeysDeletion() {
        // this.keysSet = new ArrayList<>();
        doClearKeys();
    }

    /**
     * doSmallestKeyRetrieval(): Retrieves the smallest key from a B+ tree.
     * 
     * If the node is a leaf node:
     * the smallest key will be the first key in the node. In this case, the method
     * retrieves the first key
     * of the current node and returns it as the smallest key.
     * If the node is an internal node (non-leaf node):
     * Traverse down the tree to the left-most child of the tree until it reaches a
     * leaf node.
     * This is achieved by checking if the left-most child of the current node is a
     * leaf node.
     * If it is not a leaf node, the method continues traversing down the tree by
     * assigning the left-most
     * child of the current node as the new current node. Once the method reaches a
     * leaf node, it retrieves
     * the first key of the left-most child of the current node and returns it as
     * the smallest key.
     */
    public float doSmallestKeyRetrieval() {
        float minKey;
        InternalNode temp;
        int firstIndex = 0;
        if (isLeaf == true) {
            minKey = this.getKey(firstIndex);
        } else {
            temp = (InternalNode) this;
            while (temp.getChildNode(firstIndex).getIsLeafNode() == false) {
                temp = (InternalNode) temp.getChildNode(firstIndex);
            }
            minKey = temp.getChildNode(firstIndex).getKey(firstIndex);
        }
        return minKey;
    }

    /**
     * doNodeDeletion(): deletes a node from a B+ tree.
     * 
     * If the node has an internal node:
     * Deletes itself and sets the internal node as null by calling the
     * doChildNodeDeletion() method
     * of the internal node and passing itself as a parameter. After the deletion,
     * the internal node
     * of the node is set to null.
     * If the node IS a leaf node:
     * Casts itself as a LeafNode and calls the deleteAddresses() method of the
     * LeafNode object
     * to delete all the addresses. Then, it sets the next node of the LeafNode
     * object to null.
     * If the node IS an internal node:
     * Casts itself as an InternalNode and calls the doAllChildNodesDeletion()
     * method of the InternalNode
     * object to delete all its child nodes.
     * 
     * After the deletion of the node and its descendants, the method resets the
     * node to its original state.
     * Sets the isRootNode and isLeafNode flags to true and initializing the keys
     * list to a new empty list.
     */
    public void doNodeDeletion() {
        if (internalNode != null) {
            internalNode.doChildNodeDeletion(this);
            internalNode = null;
        }
        if (isLeaf == true) {
            LeafNode tempLeaf = (LeafNode) this;
            tempLeaf.deleteAddresses();
            tempLeaf.setNextNode(null);
        } else {
            InternalNode tempInt = (InternalNode) this;
            tempInt.doAllChildNodesDeletion();
        }

        this.isRoot = true;
        this.isLeaf = true;
        // keys = new ArrayList<>();
        doClearKeys();
    }

    public void doClearKeys() {
        this.keysSet = new ArrayList<>();
    }
}
