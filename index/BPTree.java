package index;

import java.util.ArrayList;

import storage.Address;

public class BPTree {
    // Pointer = 8B, Key = 4B, Bool = 1B
    private static final int POINTER_SIZE = 8;
    private static final int KEY_SIZE = 4;
    private static final int BOOL_SIZE = 1;
    Node root;
    int numLevels;
    int numNodes;
    int maxKeys;
    int minInternalKeys;
    int minLeafKeys;

    public BPTree(int blkSize) {
        // InternalNode_ptr(8B) + isRoot(1B) + isLeaf(1B) + 4n + 8(n+1) <= blkSize
        // Calculation for n, maximum number of keys in a node
        maxKeys = (blkSize - 2 * POINTER_SIZE - 2 * BOOL_SIZE) / (POINTER_SIZE +
                KEY_SIZE);
        minInternalKeys = (int) Math.floor(maxKeys / 2);
        minLeafKeys = (int) Math.floor((maxKeys + 1) / 2);

        root = new LeafNode();
        numLevels = 1;
        numNodes = 1;
        root.setIsRoot(true);

        numNodes = 0;
    }

    public void insertKey(float key, Address address) {
        // Search the leafNode for a given key.
        // Insert the key and address to the leafNode.
        this.insertLeafNode(this.searchLeafNode(key), key, address);
    }

    public LeafNode searchLeafNode(float key) {
        if (this.root.getIsLeaf())
            // If this root is a leafNode implies this is a 1-level tree, the key should be
            // inserted into this rootNode.
            return (LeafNode) root;

        // Search for the leafNode.
        InternalNode internalNode = (InternalNode) root;
        while (true) {
            ArrayList<Float> keys = internalNode.getKeys();

            int i;

            for (i = 0; i < keys.size(); i++) {
                if (key < keys.get(i)) {
                    // Get appropraite child node to travel by checking if keys.get(i)>key
                    break;
                }
            }

            Node child = internalNode.getChildNode(i);

            if (child.getIsLeaf()) {
                // leafNode where key should be inserted is found.
                return (LeafNode) child;
            } else {
                // leafNode is not found, hence continue to traverse downwards.
                internalNode = (InternalNode) child;
            }
        }
    }

    public void insertLeafNode(LeafNode leafNode, float key, Address address) {
        // Check if the key exist.
        if (leafNode.getKeys().contains(key) == false && leafNode.getKeys().size() >= maxKeys) {
            // If does not exist and need to split.
            splitLeafNode(leafNode, key, address);
        } else {
            // If does not need splitting, just insertion will do.
            leafNode.setAddress(key, address);
        }
    }

    public void splitLeafNode(LeafNode prevLeaf, float key, Address address) {
        // Only called when need splitting due to the key and address
        // Generate a local ArrayList of ArrayLists to store addresses and keys, with
        // the intention of preserving the original keys and accommodating new ones for
        // insertion.
        ArrayList<ArrayList<Address>> addresses = new ArrayList<ArrayList<Address>>();
        float keys[] = new float[maxKeys + 1];
        LeafNode newLeaf = new LeafNode();

        // Makes a copy of the keys and addresses
        int i;
        for (i = 0; i < maxKeys; i++) {
            keys[i] = prevLeaf.getKey(i);
            addresses.add(prevLeaf.getAddress(i));
        }

        // A flag to check if the key is inserted
        boolean inserted = false;

        // Some temporary and dummy linked list for data manupulation
        ArrayList<Address> addressLL = new ArrayList<Address>();
        ArrayList<Address> dummyLL = new ArrayList<Address>();
        addresses.add(dummyLL);
        addressLL.add(address);

        // Comparing the new key intended for insertion with the keys present in the old
        // leaf, starting from the end of the keys array
        for (i = maxKeys - 1; i >= 0; i--) {
            if (Float.compare(keys[i], key) <= 0) {
                // If the new key surpasses the existing key in value, it's placed on the
                // right side.
                inserted = true;
                i++;
                keys[i] = key;
                addresses.set(i, addressLL);
                break;
            }
            // If the new key is smaller than the existing key, all keys larger than the new
            // key are shifted to the right to create room for the new key, which is then
            // inserted into its appropraite position.
            keys[i + 1] = keys[i];
            addresses.set(i + 1, addresses.get(i));
        }
        // Check if the new key should be inserted at the leftmost position.
        if (inserted == false) {
            keys[0] = key;
            addresses.set(0, addressLL);
        }

        // Erase all keys and restore all addresses to their original state in the old
        // leaf.
        prevLeaf.doSeparation();

        // Reinsert the most recent keys and addresses from the local array into the two
        // leaf nodes. When the old leaf node becomes full, proceed with the insertion
        // into the new leaf node.
        for (i = 0; i < minLeafKeys; i++) {
            ArrayList<Address> tempLL = addresses.get(i);
            for (int j = 0; j < tempLL.size(); j++) {
                prevLeaf.setAddress(keys[i], tempLL.get(j));
            }
        }
        for (i = minLeafKeys; i < maxKeys + 1; i++) {
            ArrayList<Address> tempLL = addresses.get(i);
            for (int j = 0; j < tempLL.size(); j++) {
                newLeaf.setAddress(keys[i], tempLL.get(j));
            }
        }

        // After distributing the keys and addresses between the old and new leaf nodes,
        // their pointers are modified. The old leaf node now points to the
        // new leaf node, and the new leaf node is linked to the previous next node
        newLeaf.setNextNode(prevLeaf.getNextNode());
        prevLeaf.setNextNode(newLeaf);

        if (prevLeaf.getIsRoot()) {
            // If the old leaf node served as the root node.
            // A new internal node is created because every pair of nodes requires a parent
            // node.The old root node is transformed into a regular node, with the newly
            // created internal node as its parent, now acting as the new root node.
            InternalNode newRoot = new InternalNode();
            prevLeaf.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.insertChild(prevLeaf);
            newRoot.insertChild(newLeaf);
            root = newRoot;
            numLevels++;
        } else if (prevLeaf.getInternalNode().getKeys().size() < maxKeys) {
            // If the old leaf node was not the root node and has enough space for the new
            // leaf node, the new leaf node is added to the parent node of the old leaf
            // node.
            prevLeaf.getInternalNode().insertChild(newLeaf);
        } else {
            // If the old leaf node was not the root node and lacks space for the new leaf
            // node.
            // Node separation process occur to generate a new parent node.
            splitParentNode(prevLeaf.getInternalNode(), newLeaf);
        }

        // Increment the number of nodes
        numNodes++;
    }

    public void splitParentNode(InternalNode parentNode, Node childNode) {

        // Generate two fresh arrays, 'childNodes' and 'keys, to accommodate children
        // and keys.
        // Obtain the smallest key value from childNode.
        // Create parentNode2 as an internal node, distinct from being the root node.
        Node childNodes[] = new Node[maxKeys + 2];
        float keys[] = new float[maxKeys + 2];
        float key = childNode.retrieveSmallestKey();
        InternalNode parentNode2 = new InternalNode();
        parentNode2.setIsRoot(false);

        // Makes a copy of the current children and smallest keys.
        for (int i = 0; i < maxKeys + 1; i++) {
            childNodes[i] = parentNode.getChildNode(i);
            keys[i] = childNodes[i].retrieveSmallestKey();
        }

        // Add the smallest key from the nodes into the sorted array.
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

        // Clear and empty out parentNode
        parentNode.doSeparation();

        // The parentNode is re-populated with the first [minNoOfInternalKeys + 2]
        // children from the childNodes, while the remaining children are
        // allocated to parentNode2.
        for (int i = 0; i < minInternalKeys + 2; i++)
            parentNode.insertChild(childNodes[i]);
        for (int i = minInternalKeys + 2; i < maxKeys + 2; i++)
            parentNode2.insertChild(childNodes[i]);

        // Verifies if a parentNode is a root
        if (parentNode.getIsRoot()) {
            // If parentNode is a root.
            // A new root is created to serve as the root of B+Tree with children parentNode
            // and parentNode2.
            InternalNode newRoot = new InternalNode();
            parentNode.setIsRoot(false);
            newRoot.setIsRoot(true);
            newRoot.insertChild(parentNode);
            newRoot.insertChild(parentNode2);
            root = newRoot;
            numLevels++;
        } else if (parentNode.getInternalNode().getKeys().size() < maxKeys) {
            // If parentNode is not the root and there is still available space for key
            // insertion.
            // Add parentNode2 as its child
            parentNode.getInternalNode().insertChild(parentNode2);
        } else {
            // If parentNode is full.
            // Recursively call splitParentNode() to restructure B+tree
            splitParentNode(parentNode.getInternalNode(), parentNode2);
        }

        // Increment number of nodes
        numNodes++;
    }

    public ArrayList<Address> removeKey(float lowerBound, float upperBound) {
        // Create temporary memory for data manipulation
        ArrayList<Address> addressOfRecordsToDelete = new ArrayList<>();
        ArrayList<Float> keys;
        LeafNode leafNode;

        // Get keys of records to be deleted
        ArrayList<Float> keyOfRecordsToDelete = retrieveRangeOfKeys(lowerBound, upperBound);

        int length = keyOfRecordsToDelete.size();
        for (int j = 0; j < length; j++) {
            // Get one of the keys from keyOfRecordsToDelete
            float key = keyOfRecordsToDelete.get(j);
            // Obtain the leafNode where this key is located
            leafNode = searchLeafNode(key);
            // Retrieve the all the keys of this leafNode
            keys = leafNode.getKeys();

            // Find and delete the key along with the addresses in this leafNode
            for (int i = 0; i < keys.size(); i++) {
                if (Float.compare(keys.get(i), key) == 0) {
                    addressOfRecordsToDelete.addAll(leafNode.getAddress(i));
                    leafNode.deleteAddress(i);
                    if (!leafNode.getIsRoot()) {
                        // If this is not a root.
                        // Call cleanLeafNode to check if cleaning is needed
                        cleanLeafNode(leafNode);
                    }
                    break;
                }
            }
        }

        return addressOfRecordsToDelete;
    }

    public void cleanLeafNode(LeafNode leafNode) {
        if (leafNode.getKeys().size() >= minLeafKeys) {
            cleanParentNode(leafNode.getInternalNode());
            return;
        }

        int required = minLeafKeys - leafNode.getKeys().size();
        int leftExcess = 0;
        int rightExcess = 0;

        LeafNode left = (LeafNode) leafNode.getInternalNode().getLeftSiblingNode(leafNode);
        LeafNode right = (LeafNode) leafNode.getInternalNode().getRightSiblingNode(leafNode);
        InternalNode duplicate;

        if (left != null) {
            leftExcess += left.getKeys().size() - minLeafKeys;
        }

        if (right != null) {
            rightExcess += right.getKeys().size() - minLeafKeys;
        }

        if (leftExcess + rightExcess >= required) {
            if (left != null && leftExcess > 0) {
                for (int i = 0; i < left.getAddress(left.getKeys().size() - 1).size(); i++) {
                    leafNode.setAddress(left.getKey(left.getKeys().size() - 1),
                            left.getAddress(left.getKeys().size() - 1).get(i));
                }
                left.deleteAddress(left.getKeys().size() - 1);
            } else {
                for (int i = 0; i < right.getAddress(0).size(); i++) {
                    leafNode.setAddress(right.getKey(0), right.getAddress(0).get(i));
                }
                right.deleteAddress(0);
            }

            duplicate = leafNode.getInternalNode();
        }

        else {
            if (left != null) {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    for (int j = 0; j < leafNode.getAddress(i).size(); j++) {
                        left.setAddress(leafNode.getKey(i), leafNode.getAddress(i).get(j));
                    }

                }
            } else {
                for (int i = 0; i < leafNode.getKeys().size(); i++) {
                    for (int j = 0; j < leafNode.getAddress(i).size(); j++) {
                        right.setAddress(leafNode.getKey(i), leafNode.getAddress(i).get(j));
                    }

                }
            }

            duplicate = leafNode.getInternalNode();

            if (left == null) {
                if (!duplicate.getIsRoot()) {
                    left = searchLeafNode(duplicate.retrieveSmallestKey() - 1);
                }
            }
            if (left != null) {
                left.setNextNode(leafNode.getNextNode());
            }

            leafNode.deleteNode();
            numNodes--;
        }

        cleanParentNode(duplicate);
    }

    public void cleanParentNode(InternalNode parent) {
        if (parent.getIsRoot()) {
            if (parent.getChildNodes().size() > 1) {
                Node child = parent.getChildNode(0);
                parent.deleteChildNode(child);
                parent.insertChild(child);
                return;
            } else {
                root = parent.getChildNode(0);
                parent.getChildNode(0).setIsRoot(true);
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

        if (leftSiblingNode != null) {
            leftExcess += leftSiblingNode.getKeys().size() - minInternalKeys;
        }

        if (rightSiblingNode != null) {
            rightExcess += rightSiblingNode.getKeys().size() - minInternalKeys;
        }
        if (required <= 0) {
            ArrayList<Node> childNodes = parent.getChildNodes();
            ArrayList<Float> keys = parent.getKeys();
            parent.deleteAllKeys();
            for (int i = 0; i < keys.size(); i++) {
                Node childNode = parent.getChildNode(i + 1);
                System.out.println("Required<=0");
                float key = childNode.retrieveSmallestKey();
                parent.setKey(key);
            }
            duplicate = parent.getInternalNode();
        } else {
            if (required <= leftExcess + rightExcess) {
                if (leftSiblingNode != null && leftExcess > 0) {
                    for (int i = 0; i < required; i++) {
                        parent.insertChildToFront(
                                leftSiblingNode.getChildNode(leftSiblingNode.getChildNodes().size() - 1));
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
                if (leftSiblingNode == null) {
                    for (int i = 0; i < parent.getChildNodes().size(); i++) {
                        rightSiblingNode.insertChild(parent.getChildNode(i));
                    }
                } else {
                    for (int i = 0; i < parent.getChildNodes().size(); i++) {
                        leftSiblingNode.insertChild(parent.getChildNode(i));
                    }
                }

                duplicate = parent.getInternalNode();

                parent.deleteNode();
                numNodes--;
            }
        }

        cleanParentNode(duplicate);
    }

    private ArrayList<Float> retrieveRangeOfKeys(float lowerBound, float upperBound) {
        ArrayList<Float> result = new ArrayList<>();
        Node currNode = root;
        InternalNode internalNode;

        while (!currNode.getIsLeaf()) {
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
        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;
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
                if (curr.getNextNode() != null) {
                    curr = curr.getNextNode();
                } else {
                    break;
                }
            }
        }

        return result;
    }

    public ArrayList<Address> retrieveRecordsWithKey(float searchingKey) {
        ArrayList<Address> result = new ArrayList<>();
        int blockAccess = 1;
        Node currNode = root;
        InternalNode internalNode;

        while (!currNode.getIsLeaf()) {
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

        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;

        while (!finish && curr != null) {
            for (int i = 0; i < curr.getKeys().size(); i++) {
                if (Float.compare(curr.getKey(i), searchingKey) == 0) {
                    result.addAll(curr.getAddress(i));
                    continue;
                }
                if (curr.getKey(i) > searchingKey) {
                    finish = true;
                    break;
                }
            }
            if (!finish) {
                if (curr.getNextNode() != null) {
                    curr = curr.getNextNode();
                    blockAccess++;
                } else {
                    break;
                }
            }
        }

        int numDataBlockAccessed = 0;
        if (result.size() == 1) {
            numDataBlockAccessed = 1;
        }
        if (result.size() > 1) {
            numDataBlockAccessed = 1;
            int previousBlockID = result.get(0).blockID;
            for (int i = 1; i < result.size(); i++) {
                if (result.get(i).blockID != previousBlockID) {
                    previousBlockID = result.get(i).blockID;
                    numDataBlockAccessed += 1;
                }
            }
        }

        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("The number of index nodes accessed: %d\n", blockAccess);
        System.out.printf("The number of data blocks accessed: %d\n", numDataBlockAccessed);

        return result;
    }

    public ArrayList<Address> retrieveRecordsWithKey(float lowerBound, float upperBound) {
        ArrayList<Address> addressResult = new ArrayList<>();
        int totalBlockAccessed = 1;
        InternalNode tempIntNode;
        Node thisNode = root;

        while (thisNode.getIsLeaf() == false) {
            tempIntNode = (InternalNode) thisNode;
            int numKeys = tempIntNode.getKeys().size();
            int lastIndex = numKeys - 1;
            for (int ptr = 0; ptr < numKeys; ptr++) {
                if (tempIntNode.getKey(ptr) >= lowerBound) {
                    totalBlockAccessed += 1;
                    thisNode = tempIntNode.getChildNode(ptr);
                    break;
                }

                if (ptr == lastIndex) {
                    int target = lastIndex + 1;
                    totalBlockAccessed += 1;
                    thisNode = tempIntNode.getChildNode(target);
                    break;
                }
            }
        }

        boolean end = false;
        LeafNode currentLeafNode = (LeafNode) thisNode;
        while (end == false && currentLeafNode != null) {
            for (int ptr = 0; ptr < currentLeafNode.getKeys().size(); ptr++) {

                float targetKey = currentLeafNode.getKey(ptr);

                if (targetKey <= upperBound && currentLeafNode.getKey(ptr) >= lowerBound) {
                    ArrayList<Address> targetAddresses = currentLeafNode.getAddress(ptr);
                    for (int i = 0; i < targetAddresses.size(); i++) {
                        addressResult.add(targetAddresses.get(i));
                    }
                    continue;
                }

                if (targetKey > upperBound) {
                    end = true;
                    break;
                }
            }
            if (end == false) {

                if (currentLeafNode.getNextNode() == null) {
                    break;
                } else {
                    totalBlockAccessed += 1;
                    currentLeafNode = (LeafNode) currentLeafNode.getNextNode();
                }
            }
        }

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

        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("The number of index nodes accessed: %d\n", totalBlockAccessed);
        System.out.printf("The number of data blocks accessed: %d\n", numDataBlockAccessed);

        return addressResult;
    }

    public void printInfo() {
        // Print out the infomation of the B+Tree
        InternalNode rootDuplicate = (InternalNode) root;

        System.out.println("The parameter n of the B+ tree: " + this.maxKeys);
        System.out.println("The number of nodes of the B+ tree: " + this.numNodes);
        System.out.println("The number of levels of the B+ tree: " + this.numLevels);
        System.out.println("The content of the root node (only the keys): " + rootDuplicate.getKeys().toString());
    }

    public void printTree() {
        System.out.println("#### Printing Tree ####");
        System.out.printf("maxKeys = %d\n", maxKeys);
        System.out.printf("minIntenalKeys = %d\n", minInternalKeys);
        System.out.printf("minLeafKeys = %d\n", minLeafKeys);

        // Create a temporary Queue Data structure, Q1 & Q2 for BFS
        ArrayList<Node> Q1 = new ArrayList<>();
        Q1.add(root);
        ArrayList<Node> Q2 = new ArrayList<>();

        // Keep traversing until both Queues are empty
        while (Q1.size() > 0 || Q2.size() > 0) {
            // While Q1 is not empty.
            // Pop a node from Q1 and put its children into Q2 if this is not a leafNode.
            // Print out the keys of the node
            while (Q1.size() > 0) {
                Node temp = Q1.get(0);
                Q1.remove(0);
                if (temp.getIsLeaf() == false) {

                    InternalNode temp2 = (InternalNode) temp;
                    ArrayList<Node> children = temp2.getChildNodes();
                    for (int i = 0; i < children.size(); i++) {
                        Q2.add(children.get(i));
                    }
                }

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

            // While Q2 is not empty.
            // Pop a node from Q2 and put its children into Q1 if this is not a leafNode.
            // Print out the keys of the node
            while (Q2.size() > 0) {

                Node temp = Q2.get(0);

                Q2.remove(0);
                if (temp.getIsLeaf() == false) {

                    InternalNode temp2 = (InternalNode) temp;
                    ArrayList<Node> children = temp2.getChildNodes();
                    for (int i = 0; i < children.size(); i++) {
                        Q1.add(children.get(i));
                    }
                }

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