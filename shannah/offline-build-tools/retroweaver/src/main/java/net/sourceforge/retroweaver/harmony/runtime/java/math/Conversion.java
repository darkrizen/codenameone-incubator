/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//package java.math;
package net.sourceforge.retroweaver.harmony.runtime.java.math;

import java.math.BigInteger;

/**
 * Static library that provides {@link BigInteger} base conversion from/to any
 * integer represented in an {@link java.lang.String} Object.
 * 
 * @author Intel Middleware Product Division
 * @author Instituto Tecnologico de Cordoba
 */
class Conversion {

    /** Just to denote that this class can't be instantiated */
    private Conversion() {}

    /**
     * Holds the maximal exponent for each radix, so that radix<sup>digitFitInInt[radix]</sup>
     * fit in an {@code int} (32 bits).
     */
    static final int[] digitFitInInt = { -1, -1, 31, 19, 15, 13, 11,
            11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 5 };

    /**
     * bigRadices values are precomputed maximal powers of radices (integer
     * numbers from 2 to 36) that fit into unsigned int (32 bits). bigRadices[0] =
     * 2 ^ 31, bigRadices[8] = 10 ^ 9, etc.
     */

    static final int bigRadices[] = { -2147483648, 1162261467,
            1073741824, 1220703125, 362797056, 1977326743, 1073741824,
            387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056,
            170859375, 268435456, 410338673, 612220032, 893871739, 1280000000,
            1801088541, 113379904, 148035889, 191102976, 244140625, 308915776,
            387420489, 481890304, 594823321, 729000000, 887503681, 1073741824,
            1291467969, 1544804416, 1838265625, 60466176 };

    /* can process only 32-bit numbers */
    static String toDecimalScaledString(long value, int scale) {
        int resLengthInChars;
        int currentChar;
        char result[];
        boolean negNumber = value < 0;
        if(negNumber) {
            value = -value;
        }
        if (value == 0) {
            switch (scale) {
                case 0: return "0"; //$NON-NLS-1$
                case 1: return "0.0"; //$NON-NLS-1$
                case 2: return "0.00"; //$NON-NLS-1$
                case 3: return "0.000"; //$NON-NLS-1$
                case 4: return "0.0000"; //$NON-NLS-1$
                case 5: return "0.00000"; //$NON-NLS-1$
                case 6: return "0.000000"; //$NON-NLS-1$
                default:
                    StringBuffer result1 = new StringBuffer();
                    if (scale  < 0) {
                        result1.append("0E+"); //$NON-NLS-1$
                    } else {
                        result1.append("0E"); //$NON-NLS-1$
                    }
                    result1.append( (scale == Integer.MIN_VALUE) ? "2147483648" : Integer.toString(-scale)); //$NON-NLS-1$
                    return result1.toString();
            }
        }
        // one 32-bit unsigned value may contains 10 decimal digits
        resLengthInChars = 18;
        // Explanation why +1+7:
        // +1 - one char for sign if needed.
        // +7 - For "special case 2" (see below) we have 7 free chars for
        //  inserting necessary scaled digits.
        result = new char[resLengthInChars+1];
        //  Allocated [resLengthInChars+1] characters.
        // a free latest character may be used for "special case 1" (see below)
        currentChar = resLengthInChars;
        long v = value;
        do {
            long prev = v;
            v /= 10;
            result[--currentChar] = (char) (0x0030 + (prev - v * 10));
        } while (v != 0);
        
        long exponent = (long)resLengthInChars - (long)currentChar - scale - 1L;
        if (scale == 0) {
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
        if (scale > 0 && exponent >= -6) {
            if (exponent >= 0) {
                // special case 1
                int insertPoint = currentChar + (int) exponent ;
                for(int j=resLengthInChars-1; j>=insertPoint; j--) {
                    result[j+1] = result[j];
                }
                result[++insertPoint]='.';
                if (negNumber) {
                    result[--currentChar] = '-';
                }
                return new String(result, currentChar, resLengthInChars - currentChar + 1);
            }
            // special case 2
            for (int j = 2; j < -exponent + 1; j++) {
                result[--currentChar] = '0';
            }
            result[--currentChar] = '.';
            result[--currentChar] = '0';
            if (negNumber) {
                result[--currentChar] = '-';
            }
            return new String(result, currentChar, resLengthInChars - currentChar);
        }
        int startPoint = currentChar + 1;
        int endPoint = resLengthInChars;
        StringBuffer result1 = new StringBuffer(16+endPoint-startPoint);
        if (negNumber) {
            result1.append('-');
        }
        if (endPoint - startPoint >= 1) {
            result1.append(result[currentChar]);
            result1.append('.');
            result1.append(result,currentChar+1,resLengthInChars - currentChar-1);
        } else {
            result1.append(result,currentChar,resLengthInChars - currentChar);
        }
        result1.append('E');
        if (exponent > 0) {
            result1.append('+');
        }
        result1.append(Long.toString(exponent));
        return result1.toString();
    }
    
    static long divideLongByBillion(long a) {
        long quot;
        long rem;

        if (a >= 0) {
            long bLong = 1000000000L;
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            /*
             * Make the dividend positive shifting it right by 1 bit then get
             * the quotient an remainder and correct them properly
             */
            long aPos = a >>> 1;
            long bPos = 1000000000L >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            // double the remainder and add 1 if 'a' is odd
            rem = (rem << 1) + (a & 1);
        }
        return ((rem << 32) | (quot & 0xFFFFFFFFL));
    }

}
