package com.ping.hello;

/**
 * @Author: W.Z
 * @Date: 2021/11/27 16:35
 */
public class HelloByteCode {
    public static void main(String[] args) {
        HelloByteCode helloByteCode = new HelloByteCode();
        helloByteCode.hello();

    }

    public void hello() {
        byte a = 20;
        char b = 30;
        short c = 40;
        int d = 500;
        long e = 600;
        float f = 700.2f;
        double g = 800.1;

        System.out.println(a + b);
        System.out.println(c - d);
        System.out.println(e * f);
        System.out.println(f / g);
        if (a != 3) {
            System.out.println("---");
        }

        for (int i = 0; i < 3; i++) {
            System.out.println(i);
        }

    }
}
