public class TestMain {

    public static void main(String[] args) {
        Huffman huff = new Huffman();
        huff.encode("mcgee.txt",
                "mcgee.code", "mcgee.compressed");

        huff.decode("mcgee.compressed", "mcgee.code",
                "mcgeedecompressed.txt");
    }
}
