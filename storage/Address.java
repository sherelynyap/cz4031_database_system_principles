package storage;

public class Address {
    public int blockID;
    public int offset;

    public Address(int blockID, int offset) {
        this.blockID = blockID;
        this.offset = offset;
    }
}