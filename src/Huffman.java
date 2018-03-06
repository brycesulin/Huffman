import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * This program uses the Huffman encoding algorithm to compress a file and then decompress
 * it afterwards.
 *
 * @author Bryce Sulin
 * @version February 24, 2018
 */
public class Huffman {

    public static TreeMap<String, Value> frequencyMap = new TreeMap<>();
    public static HashMap<String, String> huffmanMap = new HashMap<>();

    /**
     * Processes a file and creates two output files, a file containing the characters and their codes,
     * and a file containing the Huffman encoded version of the original file.
     *
     * @param originalFilename   is the original file
     * @param codeFilename       is the code file containing the characters and their codes
     * @param compressedFilename is the encoded file
     */
    public static void encode(String originalFilename, String codeFilename, String compressedFilename) {

        try {
            int i;

            BitOutputStream compressedFile = new BitOutputStream(compressedFilename);
            RandomAccessFile originalFile = new RandomAccessFile(originalFilename, "r");
            int b = originalFile.read();

            BufferedWriter fout = new BufferedWriter(new FileWriter(codeFilename));

            for (i = 1; i < 257; i++) {
                Value value = new Value();
                value.frequency = readCharFrequency
                        (originalFilename)[i];
                value.c = (char) i;
                frequencyMap.put((char) i + "", value);
            }

            Value tree = huffmanCode(frequencyMap);

            tree.generateCodes(tree.huffman);

            for (i = 1; i < 257; i++) {
                if (!frequencyMap.get((char) i + "").huffman.equals("")) {
                    fout.write(frequencyMap.get((char) i + "").huffman + "\t" + (int) frequencyMap.get((char) i + "").c + "\n");
                }
            }

            fout.close();

            while (b != -1) {
                if (huffmanMap.containsKey((char) b + "")) {
                    compressedFile.writeString(huffmanMap.get((char) b + ""));
                }
                b = originalFile.read();
            }

            compressedFile.close();

        } catch (Exception e) {
            System.err.println("Problem opening file");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Reads in the two files created by encode(), decompresses the file,
     * and writes the output to the decompressed file
     *
     * @param compressedFilename   is the compressed file created by encode()
     * @param codeFilename         is the code file created by encode()
     * @param decompressedFilename is the decompressed file
     */
    public static void decode(String compressedFilename, String codeFilename, String decompressedFilename) {
        try {
            HashMap<String, String> decodeMap = new HashMap<>();

            BufferedWriter decompressedOut = new BufferedWriter(new FileWriter(decompressedFilename));
            Scanner codeIn = new Scanner(new File(codeFilename));

            BitInputStream fin = new BitInputStream(compressedFilename);
            int next = fin.nextBit();

            StringBuilder temp = new StringBuilder();

            while (codeIn.hasNext()) {
                decodeMap.put(codeIn.next(), (char) codeIn.nextInt() + "");
            }

            while (next != -1) {

                temp = temp.append(next);
                next = fin.nextBit();

                if (decodeMap.containsKey(temp + "")) {
                    decompressedOut.write(decodeMap.get(temp.toString()));
                    temp = new StringBuilder();
                }
            }
            decompressedOut.close();
        } catch (Exception e) {
            System.err.println("Problem opening file");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Adds all unique characters from the file to a PriorityQueue, comparing them by frequency.
     *
     * @param values A TreeMap containing the frequencies of each character stored as value objects
     * @return A Tree of value objects containing the Huffman codes for each letter in the file
     */
    public static Value huffmanCode(TreeMap<String, Value> values) {
        int i;
        PriorityQueue<Value> priorityQueue = new PriorityQueue<>();

        for (i = 1; i <= values.size(); i++) {
            Value v = values.get((char) i + "");
            if (v.frequency != 0) {
                priorityQueue.add(values.get((char) i + ""));
            }
        }

        while (priorityQueue.size() > 1) {
            Value z = new Value();
            Value goLeft = priorityQueue.poll();
            Value goRight = priorityQueue.poll();
            z.left = goLeft;
            z.right = goRight;
            z.frequency = goLeft.frequency + goRight.frequency;
            priorityQueue.add(z);
        }

        return priorityQueue.poll();
    }

    /**
     * Read in a file bit by bit and return an array containing the frequencies of each character in the file
     *
     * @param fileName The name of the file read in
     * @return An array containing the frequency of each character in the file where the index of the array
     * matches the ascii value of the character
     * @throws Exception throws an exception if there is an error reading the file
     */
    public static int[] readCharFrequency(String fileName) throws Exception {

        RandomAccessFile fin = new RandomAccessFile(fileName, "r");
        int[] charFreqArr = new int[257];
        int input = fin.read();

        while (input != -1) {
            charFreqArr[input]++;
            input = fin.read();
        }

        return charFreqArr;
    }

    /**
     * Value object to be added to the TreeMap
     */
    public static class Value implements Comparable<Value> {

        private char c;
        private int frequency;
        private String huffman; //the Huffman Code
        private Value left;
        private Value right;

        // Default Constructor
        public Value() {
            this.frequency = 0;
            this.huffman = "";
        }

        // Creates the Huffman code for the character inside the object
        public void generateCodes(String str) {
            if (left == null && right == null) {
                huffmanMap.put(c + "", str);
                huffman = str;
            }
            if (left != null)
                left.generateCodes(str + "0");
            if (right != null)
                right.generateCodes(str + "1");
        }

        // Implements the compareTo method to store the value objects in a PriorityQueue
        public int compareTo(Value value) {
            if (this.frequency == value.frequency) {
                return 0;
            } else if (this.frequency > value.frequency) {
                return 1;
            } else
                return -1;

        }
    }

    public static class BitInputStream {

        private BufferedInputStream in;
        private int byt;
        private int bitMask;

        /**
         * Constructs a BitInputStream
         *
         * @param fileName is the name of the file
         */
        public BitInputStream(String fileName) {
            try {
                in = new BufferedInputStream(
                        new FileInputStream(
                                new File(fileName)));

                byt = in.read();
                bitMask = 0x80;
            } catch (Exception e) {
                System.err.println("Problem opening bitStream");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        /**
         * readBit() returns a 0 or 1.
         * -1 is returned at the end of file.<br><br>
         * There is a quirk that padded 0s are returned from
         * the last byte, if it is not full.
         */
        public int readBit() {
            try {
                if (bitMask == 0) {
                    bitMask = 0x80;
                    byt = in.read();
                    if (byt == -1) {
                        return -1;
                    }
                }
            } catch (Exception e) {
                System.err.println("Problem reading from BitStream");
                System.err.println(e.getMessage());
            }
            int result = ((bitMask & byt) != 0) ? 1 : 0;
            bitMask >>>= 1;
            return result;
        }

        /**
         * nextBit() is an alias for readBit()
         */
        public int nextBit() {
            return readBit();
        }
    }

    public static class BitOutputStream {

        BufferedOutputStream out;
        int byt;
        int offset;

        /**
         * constructs a BitOutStream.
         * Program exits if there is a problem
         * opening the file.
         */
        public BitOutputStream(String fileName) {
            try {
                out = new BufferedOutputStream(
                        new FileOutputStream(
                                new File(fileName)));

                offset = 8;
            } catch (Exception e) {
                System.err.println("Problem opening BitOutputStream");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        /**
         * Writes one bit to the file
         */
        public void writeBit(int bit) {
            byt <<= 1;
            byt += bit;
            offset -= 1;

            try {
                if (offset == 0) {
                    offset = 8;
                    out.write(byt);
                    byt = 0;
                }
            } catch (Exception e) {
                System.err.println("Problem writing to BitOutputStream");
                System.err.println(e.getMessage());
            }
        }

        /**
         * Write the bits contained in a String to the file.  The String is
         * interpreted as "1" is a 1 bit.  Any other character is a 0 bit.
         */
        public void writeString(String bits) {
            for (int i = 0; i < bits.length(); i++) {
                int bit = (bits.charAt(i) == '1') ? 1 : 0;
                writeBit(bit);
            }
        }

        /**
         * Closes the file.  Since the file must contain a even number of bytes,
         * the final byte is padded with 0s.
         */
        public void close() throws IOException {
            while (offset != 8) {
                writeBit(0);
            }

            out.close();
        }

        /**
         * A helper method to help with the testing
         */
        public static int getBit(char ch, int bitPos) {
            int b = ch << (32 - bitPos);
            b >>>= (31);
            return (int) b;
        }
    }
}