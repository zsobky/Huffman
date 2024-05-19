package com.csed;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Stack;

public class Node implements Comparable<Node>, Externalizable {
    public Node left;
    public Node right;
    public int freq;
    public Block value;

    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
        this.freq = left.freq + right.freq;
    }

    public Node(Block value) {
        this.value = value;
        this.freq = 1;
    }

    public Node() {
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(freq, o.freq);
    }


    /**
     * A node is either an:
     *  - internal node and has no value
     *  - leaf node and has value (block)
     *  So will have this structure
     *  internal: |0|left|right
     *  leaf:     |1|---block--|
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Stack<Node> stack = new Stack<>();
        stack.add(this);

        while (!stack.empty()) {
            Node node = stack.pop();

            if (node.isLeaf()) {
                out.writeBoolean(true);
                out.writeObject(node.value);
            } else {
                out.writeBoolean(false);
                stack.push(node.right);
                stack.push(node.left);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Stack<Node> stack = new Stack<>();
        stack.push(this);

        while (!stack.empty()) {
            Node cur = stack.pop();
            boolean isLeaf = in.readBoolean();

            if (isLeaf) {
                cur.value = (Block) in.readObject();
            } else {
                cur.left = new Node();
                cur.right = new Node();
                stack.push(cur.right);
                stack.push(cur.left);
            }
        }
    }
}
