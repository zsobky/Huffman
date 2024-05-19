package com.csed;

import java.io.Serializable;

public class Header implements Serializable {
    long blocks;
    Node treeRoot;

    public Header(long blocks, Node treeRoot) {
        this.blocks = blocks;
        this.treeRoot = treeRoot;
    }
}

