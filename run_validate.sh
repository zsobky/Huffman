#!/bin/bash

# Usage function to display help
usage() {
    echo "Usage: $0 <file_path>"
    exit 1
}

# Check if the correct number of arguments are provided
if [ $# -ne 1 ]; then
    usage
fi

# Assign the input arguments to variables
file_path=$1

# Define the path to the Java JAR file
jar_file="huffman.jar"

# Define the other path to compare the hash
compressed_file_path="${file_path}-huffman.hc"

decompressed_file_path="extracted.${file_path}-huffman"

# Define the log file
log_file="script.log"

printf "\n\n" >> "$log_file"
date >> "$log_file"

# Run the Java JAR file with the provided arguments and redirect output to the log file
java -jar "$jar_file" c "$file_path" 1 >> "$log_file" 2>&1


# Check if the Java program succeeded
if [ $? -eq 0 ]; then
    echo "Compression is done on ${file_path}." >> "$log_file"
else
    echo "Java program failed to execute." >> "$log_file"
    exit 1
fi

# run decompression
java -jar "$jar_file" d "$compressed_file_path"  >> "$log_file" 2>&1

if [ $? -eq 0 ]; then
    echo "Decompression is done on ${file_path}." >> "$log_file"
    
    # Check if both files exist
    if [ ! -f "$file_path" ]; then
        echo "Error: File at '$file_path' does not exist." >> "$log_file"
        exit 1
    fi

    if [ ! -f "$compressed_file_path" ]; then
        echo "Error: File at '$compressed_file_path' does not exist." >> "$log_file"
        exit 1
    fi
    
    if [ ! -f "$decompressed_file_path" ]; then
        echo "Error: File at '$decompressed_file_path' does not exist." >> "$log_file"
        exit 1
    fi

    # Calculate the SHA256 hash of both files
    hash1=$(sha256sum "$file_path" | awk '{ print $1 }')
    hash2=$(sha256sum "$decompressed_file_path" | awk '{ print $1 }')

    # Compare the hashes
    if [ "$hash1" == "$hash2" ]; then
        echo "✅: The files have the same SHA256 hash." >> "$log_file"
    else
        echo "❌: The files do not have the same SHA256 hash." >> "$log_file"
        exit 1
    fi
else
    echo "Java program failed to execute." >> "$log_file"
    exit 1
fi

# remove generated files
rm "${compressed_file_path}"
rm "${decompressed_file_path}"