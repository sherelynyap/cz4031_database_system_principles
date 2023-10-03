package index;

import java.util.ArrayList;

import storage.Address;

import java.lang.Float;

public class BPTree {

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
        this.leafNodeInsertion(this.searchLeafNode(key), key, address);
    }

   
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
                internalNode = (InternalNode) child;
            }
        }
    }

    
    public void leafNodeInsertion(LeafNode leafNode, float key, Address address) {
        if (leafNode.getKeys().size() >= maxKeys) {
            splitLeafNode(leafNode, key, address);
        } else {
            leafNode.setAddress(key, address);
        }
    }

   
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
            newRoot.setIsRootNode(true); 
            newRoot.insertChild(prevLeaf); 
            newRoot.insertChild(newLeaf); 
            root = newRoot;
            numLevels++;
        } else if (prevLeaf.getInternalNode().getKeys().size() < maxKeys) {
            prevLeaf.getInternalNode().insertChild(newLeaf);
        } else {
            parentNodeSeparation(prevLeaf.getInternalNode(), newLeaf);
        }

        numNodes++;
    }

    
    public void parentNodeSeparation(InternalNode parentNode, Node childNode) {
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
            parentNodeSeparation(parentNode.getInternalNode(), parentNode2);
        }

        numNodes++;
    }

    
    public ArrayList<Address> removeKey(float lowerBound, float upperBound) {

        ArrayList<Address> addressOfRecordsToDelete = new ArrayList<>();
        ArrayList<Float> keys;
        LeafNode leafNode;

        ArrayList<Float> keyOfRecordsToDelete = doRangeKeysRetrieval(lowerBound, upperBound);

        int length = keyOfRecordsToDelete.size();

        for (int j = 0; j < length; j++) {

            float key = keyOfRecordsToDelete.get(j);
            leafNode = searchLeafNode(key);
            keys = leafNode.getKeys();

            for (int i = 0; i < keys.size(); i++) {
                if (Float.compare(keys.get(i), key) == 0) {
                    addressOfRecordsToDelete.add(leafNode.getAddress(i));
                    leafNode.deleteAddress(i);
                    if (!leafNode.getIsRootNode()) {
                        cleaningLeaf(leafNode);
                    }
                    break;
                }
            }
        }

        return addressOfRecordsToDelete;
    }

    
    public void cleaningLeaf(LeafNode leafNode) {

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
            
            if (leftSiblingNode == null) {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    rightSiblingNode.insertChild(parent.getChildNode(i));
                }
            }

            else {
                for (int i = 0; i < parent.getChildNodes().size(); i++) {
                    leftSiblingNode.insertChild(parent.getChildNode(i));
                }
            }

            duplicate = parent.getInternalNode();

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

        LeafNode curr = (LeafNode) currNode;
        boolean finish = false;

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

    public ArrayList<Address> rangeRetrieveRecords(float lowBound, float highBound) {
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

                if (targetKey <= highBound && currentLeafNode.getKey(ptr) >= lowBound) {
                    Address targetAddress = currentLeafNode.getAddress(ptr);

                    addressResult.add(targetAddress);
                    continue;
                }

                if (targetKey > highBound) {
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
        InternalNode rootDuplicate = (InternalNode) root;

        System.out.println("The parameter n of the B+ tree: " + this.maxKeys);
        System.out.println("The number of nodes of the B+ tree: " + this.numNodes);
        System.out.println("The number of levels of the B+ tree: " + this.numLevels);
        System.out.println("The content of the root node (only the keys): " + rootDuplicate.getKeys().toString());
    }

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

            while (Q2.size() > 0) {

                Node temp = Q2.get(0);

                Q2.remove(0);
                if (temp.getIsLeafNode() == false) {
                  
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