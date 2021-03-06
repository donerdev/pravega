/**
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.test.integration.demo.interactive;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Output formatter.
 */
abstract class Formatter {
    abstract List<String> format(String... items);

    abstract String separator();

    /**
     * Outputs in table-like format, with columns.
     */
    @RequiredArgsConstructor
    static class TableFormatter extends Formatter {
        static final char SEPARATOR = '-';
        @NonNull
        private final int[] columnLengths;

        @Override
        List<String> format(String... items) {
            String[] currentLine = Arrays.copyOf(items, Math.min(items.length, this.columnLengths.length));
            List<String> result = new ArrayList<>();
            do {
                String[] nextLine = null;
                for (int i = 0; i < currentLine.length; i++) {
                    String s = Strings.nullToEmpty(currentLine[i]);
                    int maxLen = this.columnLengths[i];
                    if (s.length() > maxLen) {
                        if (nextLine == null) {
                            nextLine = new String[currentLine.length];
                        }
                        nextLine[i] = s.substring(maxLen);
                        s = s.substring(0, maxLen);
                    } else {
                        s = Strings.padEnd(s, maxLen, ' ');
                    }
                    currentLine[i] = s;
                }
                result.add(String.join(" | ", currentLine));

                currentLine = nextLine;
            } while (currentLine != null);
            assert result.size() > 0;
            return result;
        }

        @Override
        String separator() {
            return format(Arrays.stream(this.columnLengths).boxed().map(l -> Strings.padEnd("", l, SEPARATOR)).toArray(String[]::new)).get(0);
        }
    }

    /**
     * Outputs in JSON format.
     */
    static class JsonFormatter extends Formatter {
        static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        @Override
        List<String> format(String... items) {
            if (items.length == 1) {
                return Collections.singletonList(GSON.toJson(items[0]));
            } else {
                return Collections.singletonList(GSON.toJson(items));
            }
        }

        @Override
        String separator() {
            return "";
        }
    }
}
