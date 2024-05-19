package com.csed;

import java.io.*;
import java.util.*;


public class Huffman {
    /**
     * Compress file using huffman code
     *
     * @param path file's path
     * @param n    number of bytes per block
     * @throws IOException when file is not found
     */
    public void compress(String path, int n) throws IOException {
        if (n < 1)
            throw new IllegalArgumentException("N can't be less than 1");

        int blocksCount = getBlocksCount(path, n);

        Collection<Node> nodes = constructNodesFromFile(path, n);
        Node treeRoot = constructCodingTree(nodes);
        HashMap<Block,String> mapping = getBlockToCodeMapping(treeRoot);

        String outputPath = getOutputPath(path, n);
        serializeHeaders(blocksCount, treeRoot, outputPath);
        encodeFile(path, mapping, n, outputPath);
    }

    /**
     * Decompress huffman encoded file
     * @param path encoded file path
     * @throws IOException if the file is not found
     * @throws ClassNotFoundException if the serialized object is not compatible
     */
    public void decompress(String path) throws IOException, ClassNotFoundException {
        InputStream stream = getInputStream(path);

        Header header = deserializeHeader(stream);
        decodeFile(stream, header.treeRoot, header.blocks, getExtractingPath(path));

        stream.close();
    }
    
    public String getOutputPath(String path, int n) {
        File inputFile = new File(path);
        String outputFileName = inputFile.getName() + "-huffman.hc";
        File outputFile = new File(inputFile.getParent(), outputFileName);
        return outputFile.getPath();
    }

    private int getBlocksCount(String path, int n) throws IOException {
        InputStream inputStream = getInputStream(path);
        int blocksCount = inputStream.available() / n;
        inputStream.close();

        return blocksCount;
    }

    private Collection<Node> constructNodesFromFile(String filePath, int n) throws IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(filePath));
        Map<Block,Node> map = new HashMap<>();

        byte[] bytes = new byte[n];
        int c = 0;

        int r = stream.read();
        while (r != -1) {
            bytes[c++] = (byte) r;
            if (c == n) {
                Block block = new Block(bytes);
                if (map.containsKey(block))
                    map.get(block).freq += 1;
                else
                    map.put(block, new Node(block));
                c = 0;
            }
            r = stream.read();
        }

        stream.close();
        return map.values();
    }

    private Node constructCodingTree(Collection<Node> nodes) {
        PriorityQueue<Node> pq = new PriorityQueue<>(nodes);

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node merged = new Node(left, right);
            pq.add(merged);
        }

        return pq.poll();
    }

    private HashMap<Block, String> getBlockToCodeMapping(Node treeRoot) {
        HashMap<Block,String> mapping = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        backtrack(treeRoot, mapping, builder);
        return mapping;
    }

    private void backtrack(Node node, HashMap<Block,String> map, StringBuilder path) {
        if (node.isLeaf()) {
            map.put(node.value, path.toString());
        } else {
            path.append(0);
            backtrack(node.left, map, path);
            path.setCharAt(path.length()-1, '1');
            backtrack(node.right, map, path);
            // remove last bit char
            path.setLength(path.length() - 1);
        }
    }

    private void serializeHeaders(int size, Node huffmanTree, String outputPath) throws IOException {
        OutputStream fileOutputStream
                = new BufferedOutputStream(new FileOutputStream(outputPath));
        ObjectOutputStream objectOutputStream
                = new ObjectOutputStream(fileOutputStream);

        Header header = new Header(size, huffmanTree);
        objectOutputStream.writeObject(header);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    private void encodeFile(String inputPath, HashMap<Block,String> mapping, int n, String outputPath) throws IOException {
        InputStream in =
                new BufferedInputStream(new FileInputStream(inputPath));
        OutputStream out =
                new BufferedOutputStream(new FileOutputStream(outputPath, true));

        ByteBuffer buffer = new ByteBuffer();
        byte[] bytes = new byte[n];
        int bytesEncoded = 0;

        int r = in.read();
        while (r != -1) {
            bytes[bytesEncoded++] = (byte) r;
            if (bytesEncoded == n) {
                String code = mapping.get(new Block(bytes));

                for (int i=0; i < code.length(); i++) {
                    boolean bit = code.charAt(i) == '1';
                    buffer.append(bit);

                    if (buffer.isFullByte())
                        out.write(buffer.extract());
                }
                bytesEncoded = 0;
            }
            r = in.read();
        }

        if (!buffer.isEmpty())
            out.write(buffer.extract());

        // write the bytes that didn't make a block, if any
        for (int i=0; i < bytesEncoded; i++)
            out.write(bytes[i]);

        in.close();
        out.close();
    }

    private String getExtractingPath(String path) {
        File file = new File(path);
        String parent = file.getParent();
        String fileName = file.getName();
        return new File(parent, "extracted." + fileName.substring(0, fileName.length()-3)).getPath();
    }

    private InputStream getInputStream(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return new BufferedInputStream(fileInputStream);
    }

    private Header deserializeHeader(InputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream
                = new ObjectInputStream(stream);
        return (Header) objectInputStream.readObject();
    }

    private void decodeFile(InputStream in, Node treeRoot, long blocksEncoded, String outputPath) throws IOException {
        OutputStream out =
                new BufferedOutputStream(new FileOutputStream(outputPath));

        Node node = treeRoot;
        long blocksDecoded = 0;

        int b = in.read();
        while (b != -1 && blocksDecoded < blocksEncoded) {
            // parse the byte, bit by bit
            for (int i=0; i < 8; i++) {
                int bit = byteMSB(b);
                b = b << 1;

                if (bit == 0)
                    node = node.left;
                else
                    node = node.right;

                if (node.isLeaf()) {
                    for (byte blockByte: node.value.bytes())
                        out.write(blockByte);
                        
                    node = treeRoot;
                    blocksDecoded += 1;

                    if (blocksDecoded == blocksEncoded) {
                        writeLastPartialBlock(in, out);
                        break;
                    }
                }
            }
            b = in.read();
        }

        out.close();
    }

    private void writeLastPartialBlock(InputStream in, OutputStream out) throws IOException {
        while (in.available() > 0) {
            out.write(in.read());
        }
    }

    private int byteMSB(int m) {
        // returns most significant bit in a byte
        return (m & 0xff) >> 7;
    }

}
