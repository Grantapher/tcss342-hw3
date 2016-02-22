import java.util.*;

public class CodingTree {
    public String message;
    public Map<Character, String> codes;
    public HuffmanTree huffmanTree;
    public Map<Character, Integer> counts;
    public List<Byte> bits;

    public CodingTree(String message) {
        this.message = message;
        counts = countCharacters(message);
        huffmanTree = new HuffmanTree(counts);
        codes = huffmanTree.generateCodes();
        bits = convertToBits(message);
    }

    private List<Byte> convertToBits(String message) {
        StringBuilder sb = new StringBuilder();
//        message.chars().mapToObj(CodingTree::intToCharCast)
//                .map(codes::get)
//                .forEach(sb::append);
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            String code = codes.get(c);
            sb.append(code);
        }
        String converted = sb.toString();

        //taken from http://stackoverflow.com/a/23664301
        String[] splits = converted.split("(?<=\\G.{8})");
//        List<Byte> bytes = Arrays.stream(splits)
//                .map(this::parseBinary)
//                .collect(Collectors.toList());
        List<Byte> bytes = new ArrayList<>();
        for (String split : splits) {
            byte b = parseBinary(split);
            bytes.add(b);
        }

        int padding = 8 - converted.length() % 8;
        if (padding != 8) {
            int finalIndex = bytes.size() - 1;
            byte finalByte = bytes.get(finalIndex);
            finalByte <<= padding;
            bytes.set(finalIndex, finalByte);
        }

        return bytes;
    }

    private byte parseBinary(String s) {
        byte b = 0;
        for (int i = 0; i < s.length(); i++) {
            b <<= 1;
            if ('1' == s.charAt(i)) b += 1;
        }
        return b;
    }

    private String byteToBinary(byte bits) {
        byte b = bits;
        String str = "";
        for (int i = 0; i < 8; i++) {
            str += (b & 0b10000000) > 0 ? '1' : '0';
            b <<= 1;
        }
        return str;
    }

    private Map<Character, Integer> countCharacters(String message) {
        Map<Character, Integer> counts = new HashMap<>();
//        message.chars().mapToObj(CodingTree::intToCharCast)
//                .forEach(c -> counts.merge(c, 1, (oldVal, newVal) -> oldVal + newVal));
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            Integer count = counts.get(c);
            if (count == null) {
                counts.put(c, 1);
            } else {
                counts.put(c, count + 1);
            }
        }

        return counts;
    }

    public String decode(List<Byte> bits, Map<Character, String> codes) {
        HuffmanTree tree = new HuffmanTree(codes, true);
        StringBuilder sb = new StringBuilder();
        HuffmanTree.Traverser traverser = tree.getTraverser();
        for (byte b : bits) {
            for (int i = 0; i < 8; i++) {
                boolean bitSet = (b & 0b1 << 7 - i) > 0;
                traverser.traverse(bitSet);
                if (traverser.isLeaf()) {
                    char character = traverser.getCharacter();
                    sb.append(character);
                    traverser.reset();
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        //don't print out novels
        boolean isLong = message.length() > 100;

        StringBuilder sb = new StringBuilder();
        if (!isLong) sb.append(message).append('\n');
        sb.append(codes.toString()).append('\n');
        if (!isLong) {
//            sb.append(bits.stream().map(this::byteToBinary).reduce((s, s2) -> s + " " + s2).get()).append('\n');
            for (byte b : bits) {
                String bin = byteToBinary(b);
                sb.append(bin).append(' ');
            }
            sb.append('\n');
        }
        sb.append(counts.toString()).append('\n');
        if (!isLong) sb.append(huffmanTree).append('\n');
        return sb.toString();
    }

    private class HuffmanTree {
        private Node root;

        private HuffmanTree(Map<Character, Integer> counts) {
            PriorityQueue<Node> pq = new PriorityQueue<>();
//            counts.entrySet().stream()
//                    .map(Node::new)
//                    .forEach(pq::offer);
            for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
                Node newNode = new Node(entry);
                pq.offer(newNode);
            }

            while (pq.size() > 1) {
                pq.offer(pq.poll().merge(pq.poll()));
            }

            root = pq.poll();
        }

        public HuffmanTree(Map<Character, String> codes, boolean isCodes) {
            if (!isCodes)
                throw new IllegalStateException("Java Generic type erasure is dumb, change this to true or " +
                        "delete it for the other constructor.");

            root = new Node();
//            codes.entrySet().stream().forEach(this::addCode);
            for (Map.Entry<Character, String> entry : codes.entrySet()) {
                addCode(entry);
            }

        }

        private void addCode(Map.Entry<Character, String> entry) {
            Node current = root;
            char character = entry.getKey();
            String path = entry.getValue();

            for (int i = 0; i < path.length(); i++) {
                boolean isLeft = '0' == path.charAt(i);
                if (isLeft) {
                    if (null == current.left) current.left = new Node();
                    current = current.left;
                } else {
                    if (null == current.right) current.right = new Node();
                    current = current.right;
                }
            }

            current.character = character;
        }

        public Map<Character, String> generateCodes() {
            Map<Character, String> map = new HashMap<>();
            generateCodesRecursive(map, root, "");
            return map;
        }

        private void generateCodesRecursive(Map<Character, String> map, Node node, String code) {
            if (node.isLeaf()) {
                map.put(node.character, code);
            } else {
                generateCodesRecursive(map, node.left, code + "0");
                generateCodesRecursive(map, node.right, code + "1");
            }
        }

        public HuffmanTree.Traverser getTraverser() {
            return new Traverser();
        }

        @Override
        public String toString() {
            return new TreePrinter().printNodes();
        }

        private class Node implements Comparable<Node> {
            private Node left, right;
            private Integer count;
            private Character character;

            private Node(Character character, Integer count) {
                this.character = character;
                this.count = count;
            }

            private Node(Map.Entry<Character, Integer> entry) {
                this(entry.getKey(), entry.getValue());
            }

            public Node() {
                this(null, null);
            }

            public boolean isLeaf() {
                return null != character;
            }

            @Override
            public int compareTo(@SuppressWarnings("NullableProblems") Node o) {
                return Integer.compare(count, o.count);
            }

            /**
             * Combines the two nodes with a joining node with the counts of both side nodes added.
             * <p/>
             * "this" will be the left node and other will be the right node.
             *
             * @param other the right node
             * @return the joining node
             */
            public Node merge(Node other) {
                Node newNode = new Node(null, this.count + other.count);
                newNode.left = this;
                newNode.right = other;
                return newNode;
            }
        }

        private class Traverser {
            private Node current;

            public Traverser() {
                this.current = root;
            }

            public void traverse(boolean traverseRight) {
                if (traverseRight) current = current.right;
                else current = current.left;
            }

            public boolean isLeaf() {
                return current.isLeaf();
            }

            public char getCharacter() {
                return current.character;
            }

            public void reset() {
                current = root;
            }
        }

        // taken from http://stackoverflow.com/a/4973083
        private class TreePrinter {

            public String printNodes() {
                int maxLevel = maxLevel(root);

                return printNodeInternal(Collections.singletonList(root), new StringBuilder(), 1, maxLevel);
            }

            private String printNodeInternal(List<Node> nodes, StringBuilder sb, int level, int maxLevel) {
                if (nodes.isEmpty() || isAllElementsNull(nodes))
                    return "";

                int floor = maxLevel - level;
                int endgeLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
                int firstSpaces = (int) Math.pow(2, (floor)) - 1;
                int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

                printWhitespaces(firstSpaces, sb);

                List<Node> newNodes = new ArrayList<>();
                for (Node node : nodes) {
                    if (node != null) {
                        if (null != node.character) sb.append(node.character);
                        else if (null != node.count) sb.append(node.count);
                        else sb.append(' ');
                        newNodes.add(node.left);
                        newNodes.add(node.right);
                    } else {
                        newNodes.add(null);
                        newNodes.add(null);
                        sb.append(" ");
                    }

                    printWhitespaces(betweenSpaces, sb);
                }
                sb.append('\n');

                for (int i = 1; i <= endgeLines; i++) {
                    for (Node node : nodes) {
                        printWhitespaces(firstSpaces - i, sb);
                        if (node == null) {
                            printWhitespaces(endgeLines + endgeLines + i + 1, sb);
                            continue;
                        }

                        if (node.left != null)
                            sb.append("/");
                        else
                            printWhitespaces(1, sb);

                        printWhitespaces(i + i - 1, sb);

                        if (node.right != null)
                            sb.append("\\");
                        else
                            printWhitespaces(1, sb);

                        printWhitespaces(endgeLines + endgeLines - i, sb);
                    }

                    sb.append('\n');
                }

                printNodeInternal(newNodes, sb, level + 1, maxLevel);

                return sb.toString();
            }

            private void printWhitespaces(int count, StringBuilder sb) {
                for (int i = 0; i < count; i++)
                    sb.append(" ");
            }

            private int maxLevel(Node node) {
                if (node == null)
                    return 0;

                return Math.max(maxLevel(node.left), maxLevel(node.right)) + 1;
            }

            private boolean isAllElementsNull(List<Node> list) {
                for (Node node : list) {
                    if (node != null)
                        return false;
                }
                return true;
            }

        }

    }
}
