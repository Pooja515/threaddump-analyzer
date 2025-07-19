package com.example.threaddump_analyzer.model;

import lombok.*;
import com.example.threaddump_analyzer.model.ThreadInfo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadDumpResponse {
    private int totalThreads;
    private List<ThreadInfo> threads;
}
