package com.csed;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String command = args[0];
        String path = args[1];
        Huffman huffman = new Huffman();

        if (command.equals("c")) {
            int n = Integer.parseInt(args[2]);

            long origFileSize = new File(path).length();
            long startTime = System.currentTimeMillis();

            huffman.compress(path, n);

            long endTime = System.currentTimeMillis();
            long compressedFileSize = new File(huffman.getOutputPath(path, n)).length();

            System.out.println("Compression time(ms): " + (endTime - startTime));
            System.out.println("Compression ratio: " + compressedFileSize / (origFileSize * 1.0));

        } else {
            long startTime = System.currentTimeMillis();

            huffman.decompress(path);

            long endTime = System.currentTimeMillis();
            System.out.println("Decompression time(ms): " + (endTime - startTime));
        }
    }
}
