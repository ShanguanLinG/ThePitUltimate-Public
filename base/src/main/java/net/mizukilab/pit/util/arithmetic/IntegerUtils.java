package net.mizukilab.pit.util.arithmetic;

public final class IntegerUtils {
    public static final int HIGH_BIT_U32 = Integer.MIN_VALUE;
    public static final long HIGH_BIT_U64 = Long.MIN_VALUE;

    public static int ceilLog2(int value) {
        return 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    public static long ceilLog2(long value) {
        return 64 - Long.numberOfLeadingZeros(value - 1L);
    }

    public static int floorLog2(int value) {
        return 0x1F ^ Integer.numberOfLeadingZeros(value);
    }

    public static int floorLog2(long value) {
        return 0x3F ^ Long.numberOfLeadingZeros(value);
    }

    public static int roundCeilLog2(int value) {
        return Integer.MIN_VALUE >>> Integer.numberOfLeadingZeros(value - 1) - 1;
    }

    public static long roundCeilLog2(long value) {
        return Long.MIN_VALUE >>> Long.numberOfLeadingZeros(value - 1L) - 1;
    }

    public static int roundFloorLog2(int value) {
        return Integer.MIN_VALUE >>> Integer.numberOfLeadingZeros(value);
    }

    public static long roundFloorLog2(long value) {
        return Long.MIN_VALUE >>> Long.numberOfLeadingZeros(value);
    }

    public static boolean isPowerOfTwo(int n) {
        return IntegerUtils.getTrailingBit(n) == n;
    }

    public static boolean isPowerOfTwo(long n) {
        return IntegerUtils.getTrailingBit(n) == n;
    }

    public static int getTrailingBit(int n) {
        return -n & n;
    }

    public static long getTrailingBit(long n) {
        return -n & n;
    }

    public static int trailingZeros(int n) {
        return Integer.numberOfTrailingZeros(n);
    }

    public static int trailingZeros(long n) {
        return Long.numberOfTrailingZeros(n);
    }

    public static int getDivisorMultiple(long numbers) {
        return (int)(numbers >>> 32);
    }

    public static int getDivisorShift(long numbers) {
        return (int)numbers;
    }

    public static long getDivisorNumbers(int d) {
        int delta;
        int ad = IntegerUtils.branchlessAbs(d);
        if (ad < 2) {
            throw new IllegalArgumentException("|number| must be in [2, 2^31 -1], not: " + d);
        }
        int two31 = Integer.MIN_VALUE;
        long mask = 0xFFFFFFFFL;
        int p = 31;
        int t = Integer.MIN_VALUE + (d >>> 31);
        int anc = t - 1 - (int)(((long)t & 0xFFFFFFFFL) % (long)ad);
        int q1 = (int)(0x80000000L / ((long)anc & 0xFFFFFFFFL));
        int r1 = Integer.MIN_VALUE - q1 * anc;
        int q2 = (int)(0x80000000L / ((long)ad & 0xFFFFFFFFL));
        int r2 = Integer.MIN_VALUE - q2 * ad;
        do {
            ++p;
            q1 = 2 * q1;
            if (((long)(r1 = 2 * r1) & 0xFFFFFFFFL) >= ((long)anc & 0xFFFFFFFFL)) {
                ++q1;
                r1 -= anc;
            }
            q2 = 2 * q2;
            if (((long)(r2 = 2 * r2) & 0xFFFFFFFFL) < ((long)ad & 0xFFFFFFFFL)) continue;
            ++q2;
            r2 -= ad;
        } while (((long)q1 & 0xFFFFFFFFL) < ((long)(delta = ad - r2) & 0xFFFFFFFFL) || q1 == delta && r1 == 0);
        int magicNum = q2 + 1;
        if (d < 0) {
            magicNum = -magicNum;
        }
        int shift = p;
        return (long)magicNum << 32 | (long)shift;
    }

    public static int branchlessAbs(int val) {
        int mask = val >> 31;
        return (mask ^ val) - mask;
    }

    public static long branchlessAbs(long val) {
        long mask = val >> 63;
        return (mask ^ val) - mask;
    }

    public static int hash0(int x) {
        x *= 915625301;
        x ^= x >>> 16;
        return x;
    }

    public static int hash1(int x) {
        x ^= x >>> 15;
        x *= 896182957;
        x ^= x >>> 17;
        return x;
    }

    public static int hash2(int x) {
        x ^= x >>> 16;
        x *= 2146121005;
        x ^= x >>> 15;
        x *= -2073254261;
        x ^= x >>> 16;
        return x;
    }

    public static int hash3(int x) {
        x ^= x >>> 17;
        x *= -312814405;
        x ^= x >>> 11;
        x *= -1404298415;
        x ^= x >>> 15;
        x *= 830770091;
        x ^= x >>> 14;
        return x;
    }

    public static long hash1(long x) {
        x ^= x >>> 27;
        x *= -5599904292771383989L;
        x ^= x >>> 28;
        return x;
    }

    public static long hash2(long x) {
        x ^= x >>> 32;
        x *= -2960836687051489901L;
        x ^= x >>> 32;
        x *= -2960836687051489901L;
        x ^= x >>> 32;
        return x;
    }

    public static long hash3(long x) {
        x ^= x >>> 45;
        x *= -4512136349728674695L;
        x ^= x >>> 41;
        x *= -2025150219368492809L;
        x ^= x >>> 56;
        x *= 2277337576034381939L;
        x ^= x >>> 53;
        return x;
    }

    public static int fastParse(String string) {
        return fastParse0(string,0,string.length());
    }
    public static int fastParse0(String string,int start,int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            char c = string.charAt(i);
            result = result * 10 + (c - '0');
        }
        return result;
    }
}

