package edu.cornell.cs4321.BPlusTree;

/**
 * This class builds a B+ Tree Node interface.
 *
 * @author Jiangjie Man: jm2559
 */
public interface Node {

    /**
     * Return the index of the node's page in the index file
     * @return address of the node
     */
    int getAddress();
}
