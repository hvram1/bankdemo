package org.nuxeo.common.xmap;

import java.util.Collection;

public final class PrimitiveArrays {

    // Utility class.
    private PrimitiveArrays() {
    }

    @SuppressWarnings("unchecked")
    public static Object toPrimitiveArray(Collection col, Class primitiveArrayType) {
        if (primitiveArrayType == Integer.TYPE) {
            return toIntArray(col);
        } else if (primitiveArrayType == Long.TYPE) {
            return toLongArray(col);
        } else if (primitiveArrayType == Double.TYPE) {
            return toDoubleArray(col);
        } else if (primitiveArrayType == Float.TYPE) {
            return toFloatArray(col);
        } else if (primitiveArrayType == Boolean.TYPE) {
            return toBooleanArray(col);
        } else if (primitiveArrayType == Byte.TYPE) {
            return toByteArray(col);
        } else if (primitiveArrayType == Character.TYPE) {
            return toCharArray(col);
        } else if (primitiveArrayType == Short.TYPE) {
            return toShortArray(col);
        }
        return null;
    }

    public static int[] toIntArray(Collection<Integer> col) {
        int size = col.size();
        int[] ar = new int[size];
        int i = 0;
        for (Integer t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static long[] toLongArray(Collection<Long> col) {
        int size = col.size();
        long[] ar = new long[size];
        int i = 0;
        for (Long t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static double[] toDoubleArray(Collection<Double> col) {
        int size = col.size();
        double[] ar = new double[size];
        int i = 0;
        for (Double t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static float[] toFloatArray(Collection<Float> col) {
        int size = col.size();
        float[] ar = new float[size];
        int i = 0;
        for (Float t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static boolean[] toBooleanArray(Collection<Boolean> col) {
        int size = col.size();
        boolean[] ar = new boolean[size];
        int i = 0;
        for (Boolean t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static short[] toShortArray(Collection<Short> col) {
        int size = col.size();
        short[] ar = new short[size];
        int i = 0;
        for (Short t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static byte[] toByteArray(Collection<Byte> col) {
        int size = col.size();
        byte[] ar = new byte[size];
        int i = 0;
        for (Byte t : col) {
            ar[i++] = t;
        }
        return ar;
    }

    public static char[] toCharArray(Collection<Character> col) {
        int size = col.size();
        char[] ar = new char[size];
        int i = 0;
        for (Character t : col) {
            ar[i++] = t;
        }
        return ar;
    }

}
