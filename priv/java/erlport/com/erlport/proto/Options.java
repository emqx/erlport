package com.erlport.proto;

/**
 * @author wangwenhai
 * @date 2020/7/11
 * File description: Options
 */

public class Options {

    int packet;

    int compressed;

    private int buffer_size;

    String method;

    public Options(final String[] args) {
        for (String s : args) {
            String[] kv = s.split("=");
            if (kv.length < 1) continue;
            switch (kv[0]) {
                case "--packet":
                    packet = Integer.parseInt(kv[1]); break;
                case "--compressed":
                    compressed = Integer.parseInt(kv[1]); break;
                case "--buffer_size":
                    buffer_size = Integer.parseInt(kv[1]); break;
                case "--use_stdio":
                    method = "use_stdio"; break;
                case "--no_use_stdio":
                    method = "no_use_stdio"; break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Option(packet: %d, buffer_size: %d, compressed: %d, method: %s)", packet, buffer_size, compressed, method);
    }
}