/* 
 * The MIT License
 *
 * Copyright 2020 fabian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package blackhole.utils;

/**
 * The <code>Debug</code> class contains methods for logging output
 *
 * @author fabian
 */
public class Debug {

    /**
     * Write something to output. Currently only a wrapper for
     * <code>System.out.println(msg)</code>
     *
     * @param msg the message to be written
     */
    public static void log(Object msg) {
        System.out.println("[Log] " + msg);
    }

    /**
     * Write error to output. Currently only a wrapper for
     * <code>System.err.println(msg)</code>
     *
     * @param msg the message to be written
     */
    public static void logError(Object msg) {
        System.err.println("[Error] " + msg);
    }

    /**
     * Write something to output as a warning. Currently only a wrapper for
     * <code>System.out.println(msg)</code>
     *
     * @param msg the message to be written
     */
    public static void logWarn(Object msg) {
        System.err.println("[Warning] " + msg);
    }
}
