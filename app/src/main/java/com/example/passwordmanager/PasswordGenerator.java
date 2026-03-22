package com.example.passwordmanager;

import java.util.Random;

public class PasswordGenerator {

    public static String generate(int length, boolean digits, boolean letters, boolean symbols, boolean repeat) {

        String pool = "";

        if (digits) pool += "0123456789";
        if (letters) pool += "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (symbols) pool += "!@#$%^&*()_+[]{}|;:,.<>?";

        if (pool.isEmpty()) return "Select option!";
        if (!repeat && length > pool.length()) {
            return "Length exceeds unique characters available";
        }

        StringBuilder password = new StringBuilder();
        Random random = new Random();

        while (password.length() < length) {
            char c = pool.charAt(random.nextInt(pool.length()));

            if (repeat || password.indexOf(String.valueOf(c)) == -1) {
                password.append(c);
            }
        }

        return password.toString();
    }
}
