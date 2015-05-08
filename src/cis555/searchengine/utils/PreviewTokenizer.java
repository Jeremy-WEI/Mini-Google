package cis555.searchengine.utils;

public class PreviewTokenizer {

    private int index;
    private int length;
    private String content;

    public PreviewTokenizer(String content) {
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
        for (boolean exitFlag = false; index < length; index++) {
            if (!exitFlag) {
                if (!Character.isLetterOrDigit(content.charAt(index))) {
                    exitFlag = true;
                }
            } else if (exitFlag) {
                if (Character.isLetterOrDigit(content.charAt(index)))
                    break;
            }
        }
        return content.substring(startIndex, index);
    }

    public static int getNonLetterIndex(String word) {
        if (word == null)
            return 0;
        word = word.trim();
        if (word.length() == 0)
            return 0;
        for (int i = 0; i < word.length(); i++) {
            if (!Character.isLetterOrDigit(word.charAt(i))) {
                return i;
            }
        }
        return word.length();
    }

    public boolean hasMoreTokens() {
        return index < length;
    }

    public static void main(String... args) {
    }
}
