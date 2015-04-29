package cis555.indexer;

public class FastTokenizer {

    private int index;
    private int length;
    private String content;

    public FastTokenizer(String content) {
        this.content = content;
        this.length = content.length();
    }

    // public static boolean isLetterOrDigit(char ch) {
    // return (ch <= 90 && ch >= 65) || (ch <= 122 && ch >= 97)
    // || (ch <= 57 && ch >= 48);
    // }

    public String nextToken() {
        while (index < length
                && !Character.isLetterOrDigit(content.charAt(index))) {
            index++;
        }
        if (index >= length)
            return "";
        int startIndex = index;
        index += 1;
        for (; index < length; index++) {
            if (!Character.isLetterOrDigit(content.charAt(index)))
                return content.substring(startIndex, index);
        }
        return content.substring(startIndex, index);
    }

    public boolean hasMoreTokens() {
        return index < length;
    }

    public static void main(String... args) {
        // FastTokenizer tokenizer = new FastTokenizer("雅虎香港財經");
        // while (tokenizer.hasMoreTokens()) {
        // System.out.println(tokenizer.nextToken());
        // }
        // System.out.println(Indexer.isBasicLatin(","));
    }
}
