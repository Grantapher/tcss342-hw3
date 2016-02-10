import java.util.*;

public class CodingTree {
    public Map<Character, String> codes;
    public HuffmanTree huffmanTree;
    public Map<Character, Integer> counts;
    public List<Byte> bits;

    public CodingTree(String message) {
        counts = countCharacters(message);
        huffmanTree = new HuffmanTree(counts);
        codes = huffmanTree.generateCodes();
        bits = convertToBits(message);
    }

    private List<Byte> convertToBits(String message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sb.append(codes.get(message.charAt(i)));
        }
        String converted = sb.toString();

        //taken from http://stackoverflow.com/a/23664301
        List<Byte> bytes = new ArrayList<>();
        for (String s : converted.split("(?<=\\G.{8})")) {
            bytes.add(parseBinary(s));
        }

        int padding = 8 - converted.length() % 8;
        int finalIndex = bytes.size() - 1;
        byte finalByte = bytes.get(finalIndex);
        finalByte <<= padding;
        bytes.set(finalIndex, finalByte);

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

    private Map<Character, Integer> countCharacters(String message) {
        Map<Character, Integer> counts = new HashMap<>();
        for (int i = 0; i < message.length(); i++) {
            counts.merge(message.charAt(i), 1, (oldVal, newVal) -> oldVal + newVal);
        }
        return counts;
    }


    //optional
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

    private class HuffmanTree {
        private Node root;

        private HuffmanTree(Map<Character, Integer> counts) {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            counts.entrySet().stream()
                    .map(Node::new)
                    .forEach(pq::offer);

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
            for (Map.Entry<Character, String> entry : codes.entrySet()) {
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
            public int compareTo(Node o) {
                return Integer.compare(count, o.count);
            }

            /**
             * Combines the two nodes with a joining node with the counts of both side nodes added.
             * <p>
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
    }
}
