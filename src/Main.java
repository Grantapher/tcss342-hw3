import java.io.*;
import java.util.List;

public class Main {

    public static final String INPUT_FILE = "finn.txt";
    public static final String OUTPUT_BITS = "compressed.txt";
    public static final String OUTPUT_CODES = "codes.txt";
    public static final String OUTPUT_DECODE = "decoded.txt";

    public static void main(String... args) throws IOException {
        long startTime = System.nanoTime();
        long initialSize = new File(INPUT_FILE).length();
        String message = readAllBytes(INPUT_FILE);

        CodingTree codingTree = new CodingTree(message);
        List<Byte> boxedBits = codingTree.bits;
        byte[] bits = new byte[boxedBits.size()];
        for (int i = 0; i < bits.length; i++) {
            bits[i] = boxedBits.get(i);
        }

        FileOutputStream outputStream = new FileOutputStream(OUTPUT_BITS);
        outputStream.write(bits);
        outputStream.close();
        long finalSize = new File(OUTPUT_BITS).length();

        FileWriter writer = new FileWriter(OUTPUT_CODES);
        writer.write(codingTree.codes.toString());
        writer.close();

        System.out.println("Compression percentage: " + ((double) finalSize / (double) initialSize) + '%');
        long endTime = System.nanoTime();
        System.out.println("Runtime:                " + (double) (endTime - startTime) / 1000000000 + " seconds");

        FileWriter codeWriter = new FileWriter(OUTPUT_DECODE);
        codeWriter.write(codingTree.decode(codingTree.bits, codingTree.codes));
        codeWriter.close();
    }

    private static String readAllBytes(String s) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(s));
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            sb.append((char) reader.read());
        }
        reader.close();
        return sb.toString();
    }

    private static void testCodingTree() {
        CodingTree codingTree = new CodingTree("oiua shdfiouas vdfmioausdyhoasuidrymaesoc;,prueosah");
        System.out.println(codingTree);
    }
}
