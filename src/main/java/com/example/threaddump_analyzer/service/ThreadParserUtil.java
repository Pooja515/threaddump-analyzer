package com.example.threaddump_analyzer.service;

import com.example.threaddump_analyzer.model.ThreadInfo;

import java.util.Arrays;
import java.util.UUID;

public class ThreadParserUtil {

    public static ThreadInfo parseThreadBlock(String block) {
        try {
            String[] lines = block.split("\n");
            String header = lines[0];

            String threadName = header.split("\"")[1];
            String threadId = extractThreadId(header);
            String state = extractState(lines);
            boolean deadlocked = header.contains("deadlock") || block.contains("waiting to lock");

            return ThreadInfo.builder()
                    .threadName(threadName)
                    .threadId(threadId)
                    .state(state)
                    .stackTrace(Arrays.asList(lines))
                    .deadlocked(deadlocked)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractThreadId(String header) {
        return Arrays.stream(header.split(" "))
                .filter(s -> s.startsWith("tid="))
                .findFirst()
                .map(s -> s.substring(4))
                .orElse(UUID.randomUUID().toString());
    }

    private static String extractState(String[] lines) {
        for (String line : lines) {
            if (line.trim().startsWith("java.lang.Thread.State")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String state = parts[1].trim();
                    int idx = state.indexOf(" (");
                    return idx > -1 ? state.substring(0, idx) : state;
                }
            }
        }
        return "UNKNOWN";
    }
}
