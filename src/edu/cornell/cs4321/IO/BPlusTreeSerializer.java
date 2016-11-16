package edu.cornell.cs4321.IO;

import edu.cornell.cs4321.BPlusTree.DataEntry;
import edu.cornell.cs4321.BPlusTree.IndexNode;
import edu.cornell.cs4321.BPlusTree.LeafNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by manjiangjie on 11/15/16.
 */
public class BPlusTreeSerializer {
    private FileOutputStream fout;
    private FileChannel fc;
    private ByteBuffer bb;
    private int SIZE = 4096;
    private int index = 0;

    /**
     * Constructor for BPlusTreeSerializer class. Initiate FileOutputStream and ByteBuffer.
     * @param queryPath a string for the path of the output file.
     */
    public BPlusTreeSerializer(String queryPath) {
        try {
            fout = new FileOutputStream(queryPath);
            fc = fout.getChannel();
            this.writeHeadPage(0, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rewrite page from current index.
     * @param pageIndex page index
     */
    public void setPage(int pageIndex) {
        try {
            fc.position((long) pageIndex * SIZE);
            bb = ByteBuffer.allocate(SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write next LeafNode to index file in binary format.
     * @param n LeafNode in B+ tree.
     */
    public void writeNextNode(LeafNode n) {
        bb.putInt(0);
        bb.putInt(n.numDataEntry());
        index = 8;
        for (Map.Entry<Integer, List<DataEntry>> entry : n.getMap().entrySet()) {
            int key = entry.getKey();
            List<DataEntry> rids = entry.getValue();
            bb.putInt(key);
            bb.putInt(rids.size());
            index += 8;
            for (DataEntry pairs : rids) {
                bb.putInt(pairs.getPageId());
                bb.putInt(pairs.getTupleId());
                index += 8;
            }
        }
        while (index < SIZE) {
            bb.putInt(0);
            index += 4;
        }
        try {
            bb.flip();
            fc.write(bb);
            if (bb.hasRemaining()) {
                bb.compact();
            } else {
                bb.clear();
            }
            index = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write next IndexNode to index file in binary format.
     * @param n IndexNode in B+ tree.
     */
    public void writeNextNode(IndexNode n) {
        bb.putInt(1);
        bb.putInt(n.getKeys().size());
        index = 8;
        for (int key : n.getKeys()) {
            bb.putInt(key);
            index += 4;
        }
        if (n.isUpperLayer()) {
            ArrayList<IndexNode> children = n.getIndexChildren();
            for (IndexNode child : children) {
                bb.putInt(child.getAddress());
            }
        } else {
            ArrayList<LeafNode> children = n.getChildren();
            for (LeafNode child : children) {
                bb.putInt(child.getAddress());
            }
        }
        while (index < SIZE) {
            bb.putInt(0);
            index += 4;
        }
        try {
            bb.flip();
            fc.write(bb);
            if (bb.hasRemaining()) {
                bb.compact();
            } else {
                bb.clear();
            }
            index = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write head page for the index file, which contains information about the B+ tree built for index.
     * @param address the address of the root, stored at offset 0 on the header page
     * @param numLeaf the number of leaves in the tree, at offset 4
     * @param order the order of the tree, at offset 8.
     */
    public void writeHeadPage(int address, int numLeaf, int order) {
        setPage(0);
        bb.putInt(address);
        bb.putInt(numLeaf);
        bb.putInt(order);
        for (int i = 0; i < SIZE / 4 - 3; i++) {
            bb.putInt(0);
        }
        try {
            bb.flip();
            fc.write(bb);
            if (bb.hasRemaining()) {
                bb.compact();
            } else {
                bb.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the index file.
     */
    public void close() {
        try {
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
