package com.example.threaddump_analyzer.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadInfo {
    private String threadName;
    private String threadId;
    private String state;
    private List<String> stackTrace;
    private boolean deadlocked;
}
