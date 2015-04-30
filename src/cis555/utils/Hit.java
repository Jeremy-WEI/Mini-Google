package cis555.utils;

/*
 * For space efficiency, each hit is encoded as an integer (32 bit).
 * 000, 0000000000000000000000000000, 0
 * type, position, capitalization
 * 
 * Type: 
 * 111 for URL hit (7)
 * 110 for title hit (6)
 * 101 for meta hit (5)
 * 100 for anchor hit (4)
 * 011 for img hit (3)
 * ## for all above type, pos is not necessary
 * .............................
 * 000 for others
 * 
 * Position:
 * start with 0.
 */
public class Hit {

    public static int getHitValue(int type, int pos, boolean capitalization) {
        return type << 29 | pos << 1 | (capitalization == true ? 1 : 0);
    }

    public static int getHitType(int hit) {
        return (hit >> 29) & ((1 << 3) - 1);
    }

    public static int getHitPos(int hit) {
        return (hit >> 1) & ((1 << 28) - 1);
    }

    public static boolean getHitCap(int hit) {
        return (hit & 1) == 1;
    }

    public static void main(String... args) {
        System.out.println(Integer.toBinaryString(getHitValue(7, 2, true)));
        System.out.println(getHitType(getHitValue(7, 2, true)));
        System.out.println(getHitCap(getHitValue(7, 2, false)));
        System.out.println(getHitPos(getHitValue(7, 1020, false)));
        System.out.println(getHitType(1610612744));
    }
}
