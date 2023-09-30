package Index;

import Storage.Address;

import java.util.ArrayList;
import java.lang.Float;

public class BPTree {
    // Pointer = 8B, Key = 4B, Bool = 1B
    private static final int POINTER_SIZE = 8;
    private static final int KEY_SIZE = 4;
    private static final int BOOL_SIZE = 1;
    Node root;
    int numLevels;
    int numNodes;
    int numDeletedNodes;
    int maxKeys;
    int minInternalKeys;
    int minLeafKeys;

    public BPTree(int blkSize) {
        // InternalNode_ptr(8B) + isRoot(1B) + isLeaf(1B) + 4n + 8(n+1) <= blkSize
        maxKeys = (blkSize - 2 * POINTER_SIZE - 2 * BOOL_SIZE) / (POINTER_SIZE +
                KEY_SIZE);
        minInternalKeys = (int) Math.floor(maxKeys / 2);
        minLeafKeys = (int) Math.floor((maxKeys + 1) / 2);

        root = new LeafNode();
        numLevels = 1;
        numNodes = 1;
        root.setIsRootNode(true);

        numNodes = 0;
    }

    public void insert(float key, Address address) {
        this.insertLeafNode(this.searchLeafNode(key), key, address);
    }

    /**
     * searchLeafNode(float key): Search for a leaf node in a B+ tree that contains
     * a specific key.
     */
    public LeafNode searchLeafNode(float key) {
        if (this.root.getIsLeafNode())
            return (LeafNode) root;

        InternalNode internalNode = (InternalNode) root;
        while (true) {
            ArrayList<Float> keys = internalNode.getKeys();

            int i;

            for (i = 0; i < keys.size(); i++) {
                if (key < keys.get(i)) {
                    break;
                }
            }

            Node child = internalNode.getChildNode(i);

            if (child.getIsLeafNode()) {
                return (LeafNode) child;
            } else {
                // continue loop with child node
                internalNode = (InternalNode) child;
            }
        }
    }

    /**
     * insertLeafNode(LeafNode leafNode, float key, Address address): Insert a new
     * key-value pair into a leaf node in a B+ tree.
     */
    public void insertLeafNode(LeafNode leafNode, float key, Address address) {
            if (leafNode.getKeys().size() >= maxKeys) {
                splitLeafNode(leafNode, key, address);
            } else {
                leafNode.setAddress(key, address);
            }
    }

    /**
     * splitLeafNode(LeafNode prevLeaf, float key, Address address): split the node
     * into two and reorganize the tree.
     * 
     */
    public void splitLeafNode(LeafNode prevLeaf, float key, Address address) {
        Address addresses[] = new Address[maxKeys + 1];
        float keys[] = new float[maxKeys + 1];
        LeafNode newLeaf = new LeafNode();

        int i;
        for (i = 0; i < maxKeys; i++) {
            keys[i] = prevLeaf.getKey(i);
            addresses[i] = prevLeaf.getAddress(i);
        }

        boolean inserted = false;
        for (i = maxKeys - 1; i >= 0; i--) {

            if (Float.compare(keys[i], key) <= 0) {
                inserted = true;
                i++;
                keys[i] = key;
                addresses[i] = address;
                break;
            }
            // shift keys to the right
            keys[i + 1] = keys[i];
            addresses[i + 1] = addresses[i];
        }
        if (inserted == false) {
            keys[0] = key;
            addresses[0] = address;
        }

        prevLeaf.doSeparation();

        for (i = 0; i < minLeafKeys; i++)
            prevLeaf.setAddress(keys[i], addresses[i]);
        for (i = minLeafKeys; i < maxKeys + 1; i++)
            newLeaf.setAddress(keys[i], addresses[i]);

        newLeaf.setNextNode(prevLeaf.getNextNode());
        prevLeaf.setNextNode(newLeaf);

        if (prevLeaf.getIsRootNode()) {

            InternalNode newRoot = new InternalNode();
            prevLeaf.setIsRootNode(false);
            newRoot.setIsRootNode(true); // New node become the root node
            newRoot.insertChild(prevLeaf); // Add left child
            newRoot.insertChild(newLeaf); // Add right child
            root = newRoot;
            numLevels++;
        } else if (prevLeaf.getInternalNode().getKeys().size() < maxKeys) {
            prevLeaf.getInternalNode().insertChild(newLeaf);
        } else {
            separateParentNode(prevLeaf.getInternalNode(), newLeaf);
        }

        numNodes++;
    }

    /**
     * separateParentNode(InternalNode parentNode, Node childNode): Split the
     * parentNode node when it has become full
     * (i.e. contains the max num of keys) and insert a new child node.
     */
    public void separateParentNode(InternalNode parentNode, Node childNode) {
        Node childNodes[] = new Node[maxKeys + 2];
        float keys[] = new float[maxKeys + 2];
        float key = childNode.retrieveSmallestKey();
        InternalNode parentNode2 = new InternalNode();
        parentNode2.setIsRootNode(false);

        for (int i = 0; i < maxKeys + 1; i++) {
            childNodes[i] = parentNode.getChildNode(i);
            keys[i] = childNodes[i].retrieveSmallestKey();
        }

        for (int i = maxKeys; i >= 0; i--) {
            if (Float.compare(keys[i], key) <= 0) {
                i++;
                keys[i] = key;
                childNodes[i] = childNode;
                break;
            }

            keys[i + 1] = keys[i];
            childNodes[i + 1] = childNodes[i];
        }

        parentNode.doSeparation();

        for (int i = 0; i < minInternalKeys + 2; i++)
            parentNode.insertChild(childNodes[i]);
        for (int i = minInternalKeys + 2; i < maxKeys + 2; i++)
            parentNode2.insertChild(childNodes[i]);

        if (parentNode.getIsRootNode()) {

            InternalNode newRoot = new InternalNode();
            parentNode.setIsRootNode(false);
            newRoot.setIsRootNode(true);
            newRoot.insertChild(parentNode);
            newRoot.insertChild(parentNode2);
            root = newRoot;
            numLevels++;
        } else if (parentNode.getInternalNode().getKeys().size() < maxKeys) {
            parentNode.getInternalNode().insertChild(parentNode2);
        } else {
            separateParentNode(parentNode.getInternalNode(), parentNode2);
        }

        numNodes++;
    }

    /**
     * doKeyRemoval(float key): Remove key from the leaf node
     * Steps Taken:
     * 1) Initialize a keys array list, a leaf node and an addressList array list
     * 2) Calls doRecordsWithKeysRetrieval() to retrieve the addresses of the
     * records with the given key value to be deleted.
     * 3) .size() is used with doRecordsWithKeysRetrieval() to determine the length
     * of the returned list.
     * 4) A leaf node is located that contains the key to be deleted using
     * searchLeafNode().
     * The keys in the leaf node are obtained using leafNode.getKeys().
     * Iterate over the keys in the leaf node:
     * If a key in the leaf node matches the key to be deleted:
     * the corresponding address is deleted from the leaf node.
     * If the leaf node is not the root node:
     * doLeafCleaning() is called to check if the leaf node does not meet the min no
     * of keys requirement.
     * Addresses from the leaf node are added to addressList.
     * The loop is continued until all records in length have been deleted.
     * 5) Number of nodes deleted during the leaf cleaning operation is subtracted
     * from the total number of nodes.
     * 6) Returns the list of addresses associated with the removed key.
     */
    public ArrayList<Address> removeKey(float lowerBound, float upperBound) {
        // (1)
        ArrayList<Address> addressOfRecordsToDelete = new ArrayList<>();
        ArrayList<Float> keys;
        LeafNode leafNode;

        // (2)
        ArrayList<Float> keyOfRecordsToDelete = doRangeKeysRetrieval(lowerBound, upperBound);

        // (3)
        int length = keyOfRecordsToDelete.size();

        // (4)
        for (int j = 0; j < length; j++) {
            // Locate potential leafNode
            float key = keyOfRecordsToDelete.get(j);
            leafNode = searchLeafNode(key);
            keys = leafNode.getKeys();

            // Check the candidate leafNode
            for (int i = 0; i < keys.size(); i++) {
                if (Float.compare(keys.get(i), key) == 0) {
                    addressOfRecordsToDelete.add(leafNode.getAddress(i));
                    leafNode.deleteAddress(i);
                    if (!leafNode.getIsRootNode()) {
                        LeafCleaning(leafNode);
                    }
                    break;
                }
            }
        }

        // (6)
        return addressOfRecordsToDelete;
    }

    /**
     * doLeafCleaning(LeafNode leafNode): check if leaf node does not meet the min
     * no of keys requirement
     * The purpose of this operation is to either borrow keys from neighboring nodes
     * or merge with them in order to
     * maintain the minimum number of keys.
     * 
     * If it fulfills the min no of keys requirement:
     * Calls the doParentNodeCleaning() to reset parent and returns
     * If it DOES NOT fulfill the min no of keys requirement:
     * Calculates how many keys are required to reach the requirement.
     * Retrieve the left and right siblings of the leaf node to see how many keys
     * they can spare
     * If there are enough keys (leftExcess + rightExcess >= required):
     * Borrows them and updates the leaf node's parent.
     * If left sibling exists:
     * sets the key and address from the last position of the left sibling node to
     * the current leaf node
     * and deletes the corresponding key and address from the left sibling node.
     * If left sibling DOES NOT exist:
     * sets the key and address from the first position of the right sibling node to
     * the current leaf node
     * and deletes the corresponding key and address from the right sibling node.
     * Returns the parent internal node of the current leaf node and assigns it to
     * copy.
     * If there are NOT enough keys:
     * it merges the leaf node with one of its siblings and updates the parent
     * accordingly.
     * If left sibling node exists:
     * Copy all the keys and addresses from the current leaf node to the left
     * sibling node.
     * If left sibling node DOES NOT exist:
     * Copy all the keys and addresses from the current leaf node to the right
     * sibling node.
     * Then it sets the pointer of the selected sibling to the next node of the
     * current node and deletes the current node,
     * increments the numDeletedNodes counter.
     * 
     * Updates the parent node of the current node (i.e. internal node that points
     * to the leaf node) by calling doParentNodeCleaning().
     */
    public void LeafCleaning(LeafNode leafNode) {

        if (leafNode.getKeys().size() >= minLeafKeys) {
            ParentNodeCleaning(leafNode.getInternalNode());
            return;
        }

        int required = minLeafKeys - leafNode.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;
        LeafNode left = (LeafNode) leafNode.getInternalNode().getLeftSiblingNode(leafNode);
        LeafNode right = (LeafNode) leafNode.getInternalNode().getRightSiblingNode(leafNode);
        InternalNode copy;

        if (left != null)
            leftExcess += left.getKeys().size() - minLeafKeys;
        if (right != null)
            rightExcess += right.getKeys().size() - minLeafKeys;

        if (leftExcess + rightExcess >= required) {
            if (left != null) {
                leafNode.setAddress(left.getKey(left.getKeys().size() - 1), left.getAddress(left.getKeys().size() - 1));
                left.deleteAddress(left.getKeys().size() - 1);
            } else {
                leafNode.setAddress(right.getKey(0), right.getAddress(0));
                right.deleteAddress(0);
            }

            copy = leafNode.getInternalNode();
        }

        else {
            if (left != null) {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    left.setAddress(leafNode.getKey(i), leafNode.getAddress(i));
                }
            } else {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    right.setAddress(leafNode.getKey(i), leafNode.getAddress(i));
                }
            }

            // Reset the parent after deleting
            copy = leafNode.getInternalNode();

            if (left == null) {
                if (!copy.getIsRootNode()) {
                    left = searchLeafNode(copy.retrieveSmallestKey() - 1);
                }
            }

            left.setNextNode(leafNode.getNextNode());

            leafNode.deleteNode();
            numNodes--;
        }

        ParentNodeCleaning(copy);
    }

    /**
     * doParentNodeCleaning(InternalNode parent): for parent nodes to do rebalancing
     * or merging of child nodes
     * to maintain the B+ tree's properties.
     * 
     * If parent is the root node:
     * If it has at least two children:
     * Resets the parent node by removing the first child and inserting it back,
     * which will update the parent's smallest key.
     * If it has only one child:
     * Set the child as the new root of the tree and deletes the old root node.
     * Incrementing the noOfNodeDeleted.
     * Decrementing the numLevels.
     * If parent is NOT the root node:
     * Determines the number of keys required in the node to satisfy the minimum
     * number of internal keys required.
     * Checks the excess number of keys in the node's left and right sibling nodes.
     * If there are enough excess keys:
     * borrow the required number of keys from the left or right sibling node and
     * updates the parent's keys.
     * If there are not enough excess keys:
     * Merge the node with its left or right sibling node and updates the parent's
     * keys accordingly.
     * After merging, delete the node and remove parent node by calling
     * deleteNode().
     * Increment numDeletedNodes.
     * 
     * The function recursively calls itself with the parent node that was
     * duplicated during the cleaning operation to
     * perform cleaning operations on the parent's parent node, if necessary.
     */
    public void ParentNodeCleaning(InternalNode parent) {
        if (parent.getIsRootNode()) {

            if (parent.getChildNodes().size() > 1) {
                Node child = parent.getChildNode(0);
                parent.deleteChildNode(child);
                parent.insertChild(child);
                return;
            } else {
                root = parent.getChildNode(0);
                parent.getChildNode(0).setIsRootNode(true);
                parent.deleteNode();
                numNodes--;
                numLevels--;
                return;
            }
        }

        int required = minInternalKeys - parent.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;

        InternalNode leftSiblingNode = (InternalNode) parent.getInternalNode().getLeftSiblingNode(parent);
        InternalNode rightSiblingNode = (InternalNode) parent.getInternalNode().getRightSiblingNode(parent);
        InternalNode duplicate;

        if (leftSiblingNode != null)
            leftExcess += leftSiblingNode.getKeys().size() - minInternalKeys;

        if (rightSiblingNode != null)
            rightExcess += rightSiblingNode.getKeys().size() - minInternalKeys;

        if (required <= leftExcess + rightExcess) {
            if (leftSiblingNode != null) {
                for (int i = 0; i < required; i++) {
                    parent.insertChildToFront(leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                    leftSiblingNode.deleteChildNode(
                            leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                }

            } else {
                for (int i = 0; i < required; i++) {
                    parent.insertChild(rightSiblingNode.getChildNode(0));
                    rightSiblingNode.deleteChildNode(rightSiblingNode.getChildNode(0));
                }
            }
            duplicate = parent.getInternalNode();
        }

        else {
            // If there is vacancy for right node
            if (leftSiblingNode == null) {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    rightSiblingNode.insertChild(parent.getChildNode(i));
                }
            }

            // If there is vacancy for left node
            else {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    leftSiblingNode.insertChild(parent.getChildNode(i));
                }
            }

            // After merging, we delete the node
            duplicate = parent.getInternalNode();

            // removes the parent node
            parent.deleteNode();
            numNodes--;
        }
        ParentNodeCleaning(duplicate);
    }

    private ArrayList<Float> doRangeKeysRetrieval(float lowerBound, float upperBound) {
        ArrayList<Float> result = new ArrayList<>();
        Node currNode = root;
        InternalNode internalNode;

        while (!currNode.getIsLeafNode()) {
            internalNode = (InternalNode) currNode;
            for (int i = 0; i < internalNode.getKeys().size(); i++) {
                if (Float.compare(lowerBound, internalNode.getKey(i)) <= 0) {
                    currNode = internalNode.getChildNode(i);
                    break;
                }
                if (i == internalNode.getKeys().size() - 1) {
                    currNode = internalNode.getChildNode(i + 1);
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;
        // compare the keys in the leaf node and the searching key
        while (!finish && curr != null) {
            for (int i = 0; i < curr.getKeys().size(); i++) {
                if (curr.getKey(i) <= upperBound && Float.compare(lowerBound, curr.getKey(i)) <= 0) {
                    result.add(curr.getKey(i));
                    continue;
                }
                if (curr.getKey(i) > upperBound) {
                    finish = true;
                    break;
                }
            }
            if (!finish) {
                // check sibling node has remaining records of same key
                // replace the curr node var with the next node
                if (curr.getNextNode() != null) {
                    curr = curr.getNextNode();
                } else {
                    break;
                }
            }
        }
        
        return result;
    }

    /**
     * doRecordsWithKeysRetrieval(float searchingKey, boolean isPrint): Search for
     * records with a specific key.
     * searchingKey -> the key to be searched
     * isPrint -> to determine whether to print out the search results or not.
     * 
     * Initializes an ArrayList called "result" to store the matching addresses
     * found during the search,
     * and another ArrayList called "recordsAddressList" to store the addresses of
     * all records accessed during the search.
     * Sets blockAccess to 1, indicating that the root node has been accessed.
     * Sets siblingAccess to 0, which will be used later to count the number of
     * sibling nodes accessed during the search.
     * currNode is set to root and an internal node is initialized.
     * 
     * In the while loop:
     * Search for the leaf node containing the specified key by traversing the B+
     * tree until a leaf node is found.
     * Checks whether the current node is an internal node or a leaf node.
     * If it is an internal node:
     * Casts the current node to an InternalNode object and iterates over its keys
     * to determine which child node
     * to access next.
     * If the searching key is less than or equal to the current key:
     * Access the child node at the current index and increments blockAccess.
     * If the searching key is greater than all keys in the internal node (i.e. look
     * at right child node):
     * Accesses the last child node in the list and increments blockAccess.
     * 
     * Once the leaf node containing the searching key is found, iterates over its
     * keys to find all records with the same key.
     * If a match is found (curr.getKey(i) == searchingKey):
     * Add the corresponding address to the result array list.
     * If the current key is greater than the searching key:
     * Stop search, set finish = true and break out of loop.
     * If there are no more records with the same key in the current leaf node
     * (!finish):
     * check whether there are any remaining records of same key in the next sibling
     * node.
     * If yes:
     * Set the current node to the next sibling node and repeats the search process
     * Continues until all sibling nodes have been searched or there are no more
     * records with the same key.
     * 
     * Prints search results if the "isPrint" == true.
     * Returns result array list containing the addresses of all records with the
     * same key.
     */
    public ArrayList<Address> doRecordsWithKeysRetrieval(float searchingKey) {
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1;
        Node currNode = root;
        InternalNode internalNode;

        while (!currNode.getIsLeafNode()) {
            internalNode = (InternalNode) currNode;
            for (int i = 0; i < internalNode.getKeys().size(); i++) {
                if (Float.compare(searchingKey, internalNode.getKey(i)) <= 0) {
                    currNode = internalNode.getChildNode(i);
                    blockAccess++;
                    break;
                }
                if (i == internalNode.getKeys().size() - 1) {
                    currNode = internalNode.getChildNode(i + 1);
                    blockAccess++;
                    break;
                }
            }
        }
        // after leaf node is found, find all records with same key
        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;
        // compare the keys in the leaf node and the searching key
        while (!finish && curr != null) {
            for (int i = 0; i < curr.getKeys().size(); i++) {
                if (Float.compare(curr.getKey(i), searchingKey) == 0) {
                    result.add(curr.getAddress(i));
                    continue;
                }
                if (curr.getKey(i) > searchingKey) {
                    finish = true;
                    break;
                }
            }
            if (!finish) {
                // check sibling node has remaining records of same key
                // replace the curr node var with the next node
                if (curr.getNextNode() != null) {
                    curr = curr.getNextNode();
                    blockAccess++;
                } else {
                    break;
                }
            }
        }

        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Total no of index nodes accesses: %d\n", blockAccess);
        System.out.printf("Total no of data block accesses: %d\n", result.size() + blockAccess);

        return result;
    }

    /**
     * doRangeRecordsRetrieval(float low, float high): Range query method on a
     * B-tree.
     * It retrieves all addresses stored in the tree that have a key within a
     * specified range.
     * 
     * Initializing a result array list that will hold the addresses that satisfy
     * the query.
     * Set the node count and sibling count to 1 and 0, respectively, and assigns
     * the root node to the variable "curr".
     * In the while loop:
     * Traverse down the tree from the root node until it reaches a leaf node.
     * During the traversal, it checks each internal node's keys to determine which
     * child node to move to next based
     * on the range being queried. It also increments the blockAccess for each node
     * visited.
     * If leaf node is found:
     * Loops through its keys to check which addresses fall within the specified
     * range.
     * If a matching address is found:
     * Add to result array list.
     * If the key exceeds the specified range:
     * Exit loop.
     * 
     * Also checks sibling nodes for remaining records that meet the query criteria.
     * Returns the result array list containing the addresses that meet the query
     * criteria.
     */
    // Code for Experiment 4
    public ArrayList<Address> doRangeRecordsRetrieval(float lowBound, float highBound) {
        ArrayList<Address> addressResult = new ArrayList<>();
        int totalBlockAccessed = 1;
        InternalNode tempIntNode;
        Node thisNode = root;

        while (thisNode.getIsLeafNode() == false) {
            tempIntNode = (InternalNode) thisNode;
            int numKeys = tempIntNode.getKeys().size();
            int lastIndex = numKeys - 1;
            for (int ptr = 0; ptr < numKeys; ptr++) {
                if (tempIntNode.getKey(ptr) >= lowBound) {
                    // If Key >= lowBound, get this child and break
                    totalBlockAccessed += 1;
                    thisNode = tempIntNode.getChildNode(ptr);
                    break;
                }

                if (ptr == lastIndex) {
                    // If reach end of searching key, just get the child node of the Most Right
                    int target = lastIndex + 1;
                    totalBlockAccessed += 1;
                    thisNode = tempIntNode.getChildNode(target);
                    break;
                }
            }
        }
        // System.out.println("thisNode.keys = " + thisNode.getKey(0));
        // Reach Leaf Node, find all records with key that satisfy requirement
        LeafNode currentLeafNode = (LeafNode) thisNode;
        boolean end = false;
        while (end == false && currentLeafNode != null) {
            for (int ptr = 0; ptr < currentLeafNode.getKeys().size(); ptr++) {
                // When found valid key, add into addressResult list
                float targetKey = currentLeafNode.getKey(ptr);
                /* float targetKey = currentLeafNode.getKey(ptr); */
                if (targetKey <= highBound && currentLeafNode.getKey(ptr) >= lowBound) {
                    Address targetAddress = currentLeafNode.getAddress(ptr);
                    // System.out.println("Target Key = " + targetKey);
                    addressResult.add(targetAddress);
                    continue;
                }
                // if curKey > searching key, stop searching and exit
                if (targetKey > highBound) {
                    end = true;
                    break;
                }
            }
            if (end == false) {
                // Check sibling node has remaining records of valid Keys
                if (currentLeafNode.getNextNode() == null) {
                    break;
                } else {
                    totalBlockAccessed += 1;
                    currentLeafNode = (LeafNode) currentLeafNode.getNextNode();
                }
            }
        }
        // Calculate the number of blocks in addressResult
        int numDataBlockAccessed = 0;
        if (addressResult.size() == 1) {
            numDataBlockAccessed = 1;
        }
        if (addressResult.size() > 1) {
            numDataBlockAccessed = 1;
            int previousBlockID = addressResult.get(0).blockID;
            for (int i = 1; i < addressResult.size(); i++) {
                if (addressResult.get(i).blockID != previousBlockID) {
                    previousBlockID = addressResult.get(i).blockID;
                    numDataBlockAccessed += 1;
                }
            }
        }

        // System.out.println("Print Block Accessed: " + numDataBlockAccessed);
        // System.out.println("Print Record Accessed: " + addressResult.size());

        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Total no of index nodes accesses: %d\n", totalBlockAccessed);
        System.out.printf("Total no of data block accesses: %d\n", numDataBlockAccessed);

        return addressResult;
    }

    // Print info of BPTree
    public void printInfo() {
        InternalNode rootDuplicate = (InternalNode) root;

        System.out.println("The parameter n of the B+ tree: " + this.maxKeys);
        System.out.println("The number of nodes of the B+ tree: " + this.numNodes);
        System.out.println("The number of levels of the B+ tree: " + this.numLevels);
        System.out.println("The content of the root node (only the keys): " + rootDuplicate.getKeys().toString());
    }

    // Print structure of BPTree
    public void printTree() {
        System.out.println("#### Printing Tree ####");

        ArrayList<Node> Q1 = new ArrayList<>();
        Q1.add(root);
        ArrayList<Node> Q2 = new ArrayList<>();

        while (Q1.size() > 0 || Q2.size() > 0) {
            while (Q1.size() > 0) {
                Node temp = Q1.get(0);
                Q1.remove(0);
                if (temp.getIsLeafNode() == false) {
                    // If not leaf node, add children to Queue
                    InternalNode temp2 = (InternalNode) temp;
                    ArrayList<Node> children = temp2.getChildNodes();
                    for (int i = 0; i < children.size(); i++) {
                        Q2.add(children.get(i));
                    }
                }
                // print out the keys in the form [key1, key2, ...]
                ArrayList<Float> keysSet = new ArrayList<>();
                keysSet = temp.getKeys();
                System.out.print("[");
                for (int j = 0; j < keysSet.size(); j++) {
                    System.out.print(keysSet.get(j) + ", ");
                }
                System.out.print("]");

            }

            // Print out spacing
            System.out.println(" ");
            System.out.println(" ");

            while (Q2.size() > 0) {
                // System.out.println("Q2.size() = " + Q2.size());
                Node temp = Q2.get(0);
                // System.out.println("Q2.get(0) passed");
                Q2.remove(0);
                if (temp.getIsLeafNode() == false) {
                    // If not leaf node, add children to Queue
                    InternalNode temp2 = (InternalNode) temp;
                    ArrayList<Node> children = temp2.getChildNodes();
                    for (int i = 0; i < children.size(); i++) {
                        Q1.add(children.get(i));
                    }
                }
                // print out the keys in the form [key1, key2, ...]
                ArrayList<Float> keysSet = new ArrayList<>();
                keysSet = temp.getKeys();
                System.out.print("[");
                for (int j = 0; j < keysSet.size(); j++) {
                    System.out.print(keysSet.get(j) + ", ");
                }
                System.out.print("]");
            }
            System.out.println(" ");
            System.out.println(" ");
        }
    }
}