package BPlusTree;

import Storage.Address;

import java.util.ArrayList;
import java.lang.Float;

public class BPTree {
    // Pointer size is 8B
    private static final int POINTER_SIZE = 8;
    // Key size is 4B
    private static final int KEY_SIZE = 4;
    Node root;
    int noOfLevels;
    int noOfNodes;
    int noOfNodesDeleted;
    int maxNoOfKeys;
    int minNoOfInternalKeys;
    int minNoOfLeafKeys;

    public BPTree(int sizeOfBlock) {
        // calculation of n
        maxNoOfKeys = (sizeOfBlock - POINTER_SIZE) / (POINTER_SIZE + KEY_SIZE);
        minNoOfInternalKeys = (int) Math.floor(maxNoOfKeys / 2);
        minNoOfLeafKeys = (int) Math.floor((maxNoOfKeys + 1) / 2);

        root = new LeafNode();
        noOfLevels = 1;
        noOfNodes = 1;
        root.setIsRootNode(true);

        noOfNodes = 0;
        noOfNodesDeleted = 0;

        System.out.println("Block Size: " + sizeOfBlock + "B");
        System.out.println("Max no. of keys in a node: " + maxNoOfKeys);
        System.out.println("Min no. of keys in an internal node: " + minNoOfInternalKeys);
        System.out.println("Min no. of keys in a leaf node: " + minNoOfLeafKeys);
        System.out.println();
    }

    public void doBPTreeInsertion(float key, Address address) {
        this.doLeafNodeInsertion(this.doLeafNodeSearch(key), key, address);
    }

    /**
     * doLeafNodeSearch(float key): Search for a leaf node in a B+ tree that
     * contains a specific key.
     * 
     * Check if root node is a leaf node.
     * If it is a leaf node:
     * Returns the root node as a LeafNode object.
     * If it is not a leaf node:
     * Retrieves the keys and child nodes of the root's internal node.
     * 
     * Iterates through the keys in the internal node to find the index of the child
     * node that should contain the input key.
     * It retrieves the child node at that index and checks if it is a leaf node.
     * If it is a leaf node:
     * Return the child node as a leaf node.
     * If it is not a leaf node:
     * Calls the recursive function doLeafNodeSearch(InternalNode internalNode,
     * float key) until it finds a leaf node
     * containing the input key.
     */
    public LeafNode doLeafNodeSearch(float key) {
        if (this.root.getIsLeafNode())
            return (LeafNode) root;

        ArrayList<Float> keys;
        InternalNode internalNode = (InternalNode) root;

        keys = internalNode.getKeys();

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
            return doLeafNodeSearch((InternalNode) child, key);
        }

    }

    /**
     * doLeafNodeSearch(InternalNode internalNode, float key): Recursive method that
     * searches for a leaf node in the
     * B+ tree that corresponds to a given key.
     * 
     * Retrieves the keys of the internal node and iterates over them to find the
     * index of the child node that may
     * contain the given key. It then retrieves the child node at that index.
     * If the retrieved child node is a leaf node:
     * Returns the child node
     * If the retrieved child node is not a leaf node:
     * Recursively calls itself with the child node as the new InternalNode
     * parameter, and continues the search
     * for the leaf node corresponding to the given key.
     */
    public LeafNode doLeafNodeSearch(InternalNode internalNode, float key) {
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
            return doLeafNodeSearch((InternalNode) child, key);
        }

    }

    /**
     * doLeafNodeInsertion(LeafNode leafNode, float key, Address address): Insert a
     * new key-value pair into a leaf node in a B+ tree.
     * 
     * Checks if the leaf node is already full aka max keys allowed
     * If full:
     * Calls the splitLeafNode method to split the node into two and reorganize the
     * tree structure.
     * If not full:
     * Adds the new key-value pair to the leaf node
     */
    public void doLeafNodeInsertion(LeafNode leafNode, float key, Address address) {
        try {
            if (leafNode.getKeys().size() >= maxNoOfKeys) {
                splitLeafNode(leafNode, key, address);
            } else {
                leafNode.setAddress(key, address);
            }
        } catch (Error e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * splitLeafNode(LeafNode prevLeaf, float key, Address address): split the node
     * into two and reorganize the tree.
     * 
     * Description:
     * When a leaf node reaches the maximum number of keys, it needs to be split
     * into two separate leaf nodes.
     * Creates a new leaf node (newLeaf) and splits the original leaf node
     * (prevLeaf) into two nodes.
     * The original leaf node retains the first minNoOfLeafKeys keys and their
     * associated addresses, and the rest of
     * the keys and their addresses are moved to the new leaf node.
     * 
     * Steps Taken:
     * 1) Create local arrays for addresses and keys to store original and new key
     * to insert.
     * 2) It inserts the old leaf's keys and addresses into the arrays.
     * 3) Compares the new key to be inserted with the old leaf's keys from the tail
     * of the keys array.
     * If the new key is greater than the existing key:
     * Inserted to the right side
     * If it is smaller than the existing key:
     * then all the keys greater than the new key are shifted to the right to make
     * space for the new key,
     * and then it is inserted to the correct position
     * 4) Delete all keys and resets all the addresses in old leaf (using LeafNode
     * doSeparation())
     * 5) Re-insert the latest keys and addresses from local array into the two leaf
     * nodes;
     * Once old leaf node is full, continue inserting in new leaf node
     * 6) Once the keys and addresses have been split between the old and new leaf
     * nodes, their pointers are adjusted
     * accordingly.
     * The old leaf node is pointed to the new leaf node, and the new leaf node is
     * pointed to the previous next node.
     * 7) If the old leaf node was the root node:
     * Internal node is created (Every 2 nodes need a parent nodes, thus we need a
     * new node),
     * and the old root node is set to become a regular node with the new internal
     * node as its
     * parent (aka the new root node).
     * If the old leaf node was NOT the root node & has space for the new leaf node:
     * New leaf node is inserted to the parent node of the old leaf node.
     * If the old leaf node was NOT the root node & has NO space for the new leaf
     * node:
     * doParentSeparation(InternalNode parentNode, Node childNode) is called to do a
     * node separation operation
     * to create a new parent node (split similarly to the leaf node)
     * 8) noOfNodes is incremented to reflect the addition of the new nodes.
     */
    public void splitLeafNode(LeafNode prevLeaf, float key, Address address) {
        // (1)
        Address addresses[] = new Address[maxNoOfKeys + 1];
        float keys[] = new float[maxNoOfKeys + 1];
        LeafNode newLeaf = new LeafNode();

        // (2)
        int i;
        for (i = 0; i < maxNoOfKeys; i++) {
            keys[i] = prevLeaf.getKey(i);
            addresses[i] = prevLeaf.getAddress(i);
        }

        // (3)
        for (i = maxNoOfKeys - 1; i >= 0; i--) {

            if (Float.compare(keys[i], key) <= 0) {
                i++;
                keys[i] = key;
                addresses[i] = address;
                break;
            }
            // shift keys to the right
            keys[i + 1] = keys[i];
            addresses[i + 1] = addresses[i];
        }

        // (4)
        prevLeaf.doSeparation();

        // (5)
        for (i = 0; i < minNoOfLeafKeys; i++)
            prevLeaf.setAddress(keys[i], addresses[i]);
        for (i = minNoOfLeafKeys; i < maxNoOfKeys + 1; i++)
            newLeaf.setAddress(keys[i], addresses[i]);

        // (6)
        newLeaf.setNextNode(prevLeaf.getNextNode());
        prevLeaf.setNextNode(newLeaf);

        // (7)
        if (prevLeaf.getIsRootNode()) {

            InternalNode newRoot = new InternalNode();
            prevLeaf.setIsRootNode(false);
            newRoot.setIsRootNode(true); // New node become the root node
            newRoot.doChildInsertion(prevLeaf); // Add left child
            newRoot.doChildInsertion(newLeaf); // Add right child
            root = newRoot;
            noOfLevels++;
        } else if (prevLeaf.getInternalNode().getKeys().size() < maxNoOfKeys) {
            prevLeaf.getInternalNode().doChildInsertion(newLeaf);
        } else {
            doParentSeparation(prevLeaf.getInternalNode(), newLeaf);
        }

        // (8)
        noOfNodes++;
    }

    /**
     * doParentSeparation(InternalNode parentNode, Node childNode): Split the
     * parentNode node when it has become full
     * (i.e. contains the max num of keys) and insert a new child node.
     * 
     * Steps Taken:
     * 1) Create two new arrays, childNodes and keys, with sizes maxNoOfKeys + 2 to
     * store the children and keys.
     * Retrieves the smallest key value of childNode using doSmallestKeyRetrieval().
     * Create parentNode2 as an internal node and not a root node.
     * 2) Copies the current children to the childNodes array and their smallest
     * keys to key array (full and sorted lists)
     * 3) Insert the smallest key value of childNode into the sorted keys array.
     * How the code works:
     * Iterate from the maximum number of keys down to zero.
     * This is to move existing keys and child nodes down one position to make room
     * for the new one.
     * If the current key is less than or equal to the key being inserted:
     * the new key and child node are inserted at the next position in the array,
     * and the loop is broken.
     * If the current key is greater than the key being inserted:
     * Shift the current key and child node down one position in the array to make
     * room for the new one.
     * If the loop completes without finding a position to insert the new key and
     * child
     * node (i.e. keys[0] = key; childNodes[0] = childNode;):
     * New key and child node belong at the beginning of the array
     * 4) The parentNode object is then cleared of all its old values using
     * doSeparation().
     * 5) The first minNoOfInternalKeys + 2 children from the childNodes array are
     * inserted back into the parentNode,
     * while the remaining children are inserted into a new InternalNode object
     * named parentNode2.
     * 6) Checks whether parentNode is the root node.
     * If parentNode is the root node:
     * a new root node (newRoot) is created.
     * parentNode IsRootNode status becomes false.
     * newRoot IsRootNode status becomes true.
     * newRoot becomes the root node with parentNode and parentNode2 as its
     * children.
     * noOfLevels is incremented.
     * If parentNode is NOT the root node and there is still space for a new key in
     * parentNode:
     * parentNode2 is added as a child of parentNode
     * If there is no space in parentNode:
     * doParentSeparation() is called using the parent of parentNode and
     * parentNode2.
     * 7) Increments noOfNodes after the node separation operation is completed.
     */
    public void doParentSeparation(InternalNode parentNode, Node childNode) {

        // (1)
        Node childNodes[] = new Node[maxNoOfKeys + 2];
        float keys[] = new float[maxNoOfKeys + 2];
        float key = childNode.doSmallestKeyRetrieval();
        InternalNode parentNode2 = new InternalNode();
        parentNode2.setIsRootNode(false);

        // (2)
        for (int i = 0; i < maxNoOfKeys + 1; i++) {
            childNodes[i] = parentNode.getChildNode(i);
            keys[i] = childNodes[i].doSmallestKeyRetrieval();
        }

        // (3)
        for (int i = maxNoOfKeys; i >= 0; i--) {
            if (Float.compare(keys[i], key) <= 0) {
                i++;
                keys[i] = key;
                childNodes[i] = childNode;
                break;
            }

            keys[i + 1] = keys[i];
            childNodes[i + 1] = childNodes[i];
        }

        // (4)
        parentNode.doSeparation();

        // (5)
        for (int i = 0; i < minNoOfInternalKeys + 2; i++)
            parentNode.doChildInsertion(childNodes[i]);
        for (int i = minNoOfInternalKeys + 2; i < maxNoOfKeys + 2; i++)
            parentNode2.doChildInsertion(childNodes[i]);

        // (6)
        if (parentNode.getIsRootNode()) {

            InternalNode newRoot = new InternalNode();
            parentNode.setIsRootNode(false);
            newRoot.setIsRootNode(true);
            newRoot.doChildInsertion(parentNode);
            newRoot.doChildInsertion(parentNode2);
            root = newRoot;
            noOfLevels++;
        } else if (parentNode.getInternalNode().getKeys().size() < maxNoOfKeys) {
            parentNode.getInternalNode().doChildInsertion(parentNode2);
        } else {
            doParentSeparation(parentNode.getInternalNode(), parentNode2);
        }

        // (7)
        noOfNodes++;
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
     * doLeafNodeSearch().
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
    public ArrayList<Address> doKeyRemoval(float key) {
        // (1)
        ArrayList<Float> keys;
        LeafNode leafNode;
        ArrayList<Address> addressList = new ArrayList<>();

        // (2)
        ArrayList<Address> returnAddressListToDelete = doRecordsWithKeysRetrieval(key, false);

        // (3)
        int length = doRecordsWithKeysRetrieval(key, false).size();

        // (4)
        for (int j = 0; j < length; j++) {
            leafNode = doLeafNodeSearch(key);
            keys = leafNode.getKeys();
            for (int i = 0; i < keys.size(); i++) {
                if (Float.compare(keys.get(i), key) == 0) {
                    leafNode.deleteAddress(i);
                    if (!leafNode.getIsRootNode()) {
                        doLeafCleaning(leafNode);
                        addressList.addAll(leafNode.getAddresses());
                    }
                    break;
                }
            }
        }

        System.out.println("No of nodes deleted: " + noOfNodesDeleted);

        // (5)
        noOfNodes -= noOfNodesDeleted;

        // (6)
        return returnAddressListToDelete;
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
     * increments the noOfNodesDeleted counter.
     * 
     * Updates the parent node of the current node (i.e. internal node that points
     * to the leaf node) by calling doParentNodeCleaning().
     */
    public void doLeafCleaning(LeafNode leafNode) {

        if (leafNode.getKeys().size() >= minNoOfLeafKeys) {
            doParentNodeCleaning(leafNode.getInternalNode());
            return;
        }

        int required = minNoOfLeafKeys - leafNode.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;
        LeafNode left = (LeafNode) leafNode.getInternalNode().getLeftSiblingNode(leafNode);
        LeafNode right = (LeafNode) leafNode.getInternalNode().getRightSiblingNode(leafNode);
        InternalNode copy;

        if (left != null)
            leftExcess += left.getKeys().size() - minNoOfLeafKeys;
        if (right != null)
            rightExcess += right.getKeys().size() - minNoOfLeafKeys;

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
                    left = doLeafNodeSearch(copy.doSmallestKeyRetrieval() - 1);
                }
            }

            left.setNextNode(leafNode.getNextNode());

            leafNode.doNodeDeletion();
            noOfNodesDeleted++;
        }

        doParentNodeCleaning(copy);
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
     * Decrementing the noOfLevels.
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
     * doNodeDeletion().
     * Increment noOfNodesDeleted.
     * 
     * The function recursively calls itself with the parent node that was
     * duplicated during the cleaning operation to
     * perform cleaning operations on the parent's parent node, if necessary.
     */
    public void doParentNodeCleaning(InternalNode parent) {
        if (parent.getIsRootNode()) {

            if (parent.getChildNodes().size() > 1) {
                Node child = parent.getChildNode(0);
                parent.doChildNodeDeletion(child);
                parent.doChildInsertion(child);
                return;
            } else {
                root = parent.getChildNode(0);
                parent.getChildNode(0).setIsRootNode(true);
                parent.doNodeDeletion();
                noOfNodesDeleted++;
                noOfLevels--;
                return;
            }
        }

        int required = minNoOfInternalKeys - parent.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;

        InternalNode leftSiblingNode = (InternalNode) parent.getInternalNode().getLeftSiblingNode(parent);
        InternalNode rightSiblingNode = (InternalNode) parent.getInternalNode().getRightSiblingNode(parent);
        InternalNode duplicate;

        if (leftSiblingNode != null)
            leftExcess += leftSiblingNode.getKeys().size() - minNoOfInternalKeys;

        if (rightSiblingNode != null)
            rightExcess += rightSiblingNode.getKeys().size() - minNoOfInternalKeys;

        if (required <= leftExcess + rightExcess) {
            if (leftSiblingNode != null) {
                for (int i = 0; i < required; i++) {
                    parent.insertChildToFront(leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                    leftSiblingNode.doChildNodeDeletion(
                            leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
                }

            } else {
                for (int i = 0; i < required; i++) {
                    parent.doChildInsertion(rightSiblingNode.getChildNode(0));
                    rightSiblingNode.doChildNodeDeletion(rightSiblingNode.getChildNode(0));
                }
            }
            duplicate = parent.getInternalNode();
        }

        else {
            // If there is vacancy for right node
            if (leftSiblingNode == null) {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    rightSiblingNode.doChildInsertion(parent.getChildNode(i));
                }
            }

            // If there is vacancy for left node
            else {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    leftSiblingNode.doChildInsertion(parent.getChildNode(i));
                }
            }

            // After merging, we delete the node
            duplicate = parent.getInternalNode();

            // removes the parent node
            parent.doNodeDeletion();
            noOfNodesDeleted++;
        }
        doParentNodeCleaning(duplicate);
    }

    // Code for Experiment 2
    public void showExperiment2() {

        System.out.println("The parameter n of the B+ tree: " + this.maxNoOfKeys);
        System.out.println("The No of nodes of the B+ tree: " + this.noOfNodes);
        System.out.println("The No of levels of the B+ tree: " + this.noOfLevels);
        System.out.println("The content of the root node (only the keys): ");
        InternalNode rootDuplicate = (InternalNode) root; // to get the root node
        System.out.println(rootDuplicate.getKeys().toString());
    }

    public ArrayList<Address> showExperiment3(float searchingKey) {
        return doRecordsWithKeysRetrieval(searchingKey, true);
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
    private ArrayList<Address> doRecordsWithKeysRetrieval(float searchingKey, boolean isPrint) {
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1;
        int siblingAccess = 0;
        ArrayList<Address> recordsAddressList = new ArrayList<>();
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
                    siblingAccess++;
                } else {
                    break;
                }
            }
        }
        if (isPrint) {
            System.out.println();
            System.out.println("B+ tree");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("Total no of index nodes accesses: %d\n", blockAccess);
            System.out.printf("Total no of data block accesses: %d\n", result.size() + blockAccess);
        }

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
    public ArrayList<Address> doRangeRecordsRetrieval1(float lowBound, float highBound) {
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

        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Total no of index nodes accesses: %d\n", totalBlockAccessed);
        System.out.printf("Total no of data block accesses: %d\n", addressResult.size() + totalBlockAccessed);
        return addressResult;
    }

}
