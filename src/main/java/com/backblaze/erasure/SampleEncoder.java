/**
 * Command-line program encodes one file using Reed-Solomon 4+2.
 *
 * Copyright 2015, Backblaze, Inc.
 */

package com.backblaze.erasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Command-line program encodes one file using Reed-Solomon 4+2.
 * 命令行程序使用Reed Solomon 4+2对一个文件进行编码。
 * The one argument should be a file name, say "foo.txt".  This program
 * 一个参数应该是一个文件名，比如“食品.txt".
 * will create six files in the same directory, breaking the input file
 * into four data shards, and two parity shards.  The output files are
 * 这个程序将在同一个目录中创建六个文件，将输入文件分成四个数据碎片和两个奇偶校验碎片。
 * called "foo.txt.0", "foo.txt.1", ..., and "foo.txt.5".  Numbers 4
 * and 5 are the parity shards.
 * 输出文件称为“食品.txt.0“，”食品.txt.1“，…，和”食品.txt0.5英寸。
 * 数字4和5是奇偶校验碎片。
 *
 * 存储的数据是文件大小（4字节int），后跟文件内容，然后用0填充为4字节的倍数。
 * 填充是因为所有四个数据碎片的大小必须相同。
 * The data stored is the file size (four byte int), followed by the
 * contents of the file, and then padded to a multiple of four bytes
 * with zeros.  The padding is because all four data shards must be
 * the same size.
 */
public class SampleEncoder {

    public static final int DATA_SHARDS = 1;
    public static final int PARITY_SHARDS = 2;
    public static final int TOTAL_SHARDS = 4;

    public static final int BYTES_IN_INT = 4;

    public static void main(String [] arguments) throws IOException {

        // Parse the command line
       /* if (arguments.length != 1) {
            System.out.println("Usage: SampleEncoder <fileName>");
            return;
        }
        final File inputFile = new File(arguments[0]);*/
        long start = System.currentTimeMillis();
        String file = "C:\\Root\\Downloads\\d\\LSYF3471.MP4";
        final File inputFile = new File(file);
        if (!inputFile.exists()) {
            System.out.println("Cannot read input file: " + inputFile);
            return;
        }

        // Get the size of the input file.  (Files bigger that
        // Integer.MAX_VALUE will fail here!)
        final int fileSize = (int) inputFile.length();

        // Figure out how big each shard will be.  The total size stored
        // will be the file size (8 bytes) plus the file.
        final int storedSize = fileSize + BYTES_IN_INT;
        final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;

        // Create a buffer holding the file size, followed by
        // the contents of the file.
        final int bufferSize = shardSize * DATA_SHARDS;
        final byte [] allBytes = new byte[bufferSize];
        ByteBuffer.wrap(allBytes).putInt(fileSize);
        InputStream in = new FileInputStream(inputFile);
        int bytesRead = in.read(allBytes, BYTES_IN_INT, fileSize);
        if (bytesRead != fileSize) {
            throw new IOException("not enough bytes read");
        }
        in.close();

        // Make the buffers to hold the shards.
        byte [] [] shards = new byte [TOTAL_SHARDS] [shardSize];

        // Fill in the data shards
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
        }

        // Use Reed-Solomon to calculate the parity.
        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, shardSize);

        // Write out the resulting files.
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            File outputFile = new File(
                    inputFile.getParentFile(),
                    inputFile.getName() + "." + i);
            OutputStream out = new FileOutputStream(outputFile);
            out.write(shards[i]);
            out.close();
            System.out.println("wrote " + outputFile);
        }
        long end = System.currentTimeMillis();
        System.out.println(end -start);
    }
}
