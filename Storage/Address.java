package Storage;

public class Address {
    int blkId;
    int offset;

    public Address(int blkId, int offset) {
        this.blkId = blkId;
        this.offset = offset;
    }

    public int getBlkId() {
        return blkId;
    }

    public void setBlkId(int blockId) {
        this.blkId = blockId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return String.format("@%d-%d", blkId, offset);
    }
}
