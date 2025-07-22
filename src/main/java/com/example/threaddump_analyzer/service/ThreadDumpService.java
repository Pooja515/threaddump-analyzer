package com.example.threaddump_analyzer.service;

import com.example.threaddump_analyzer.entity.ThreadInfoEntity;
import com.example.threaddump_analyzer.entity.ThreadDumpRecord;
import com.example.threaddump_analyzer.model.ThreadInfo;
import com.example.threaddump_analyzer.repository.ThreadInfoRepository;
import com.example.threaddump_analyzer.repository.ThreadDumpRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class ThreadDumpService {

    @Autowired
    private ThreadInfoRepository repository;

    @Autowired
    private ThreadDumpRecordRepository dumpMetadataRepository;

    public Map<String, Object> parseThreadDump(MultipartFile file) throws Exception {
        List<ThreadInfo> threadList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        int skippedUnknown = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        StringBuilder currentBlock = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("\"")) {
                if (currentBlock.length() > 0) {
                    ThreadInfo thread = parseThreadBlock(currentBlock.toString());
                    if (thread != null && !thread.getState().equalsIgnoreCase("UNKNOWN")) {
                        threadList.add(thread);
                        saveToDatabase(thread);
                    } else {
                        skippedUnknown++;
                    }
                    currentBlock.setLength(0);
                }
            }
            currentBlock.append(line).append("\n");
        }

        if (currentBlock.length() > 0) {
            ThreadInfo thread = parseThreadBlock(currentBlock.toString());
            if (thread != null && !thread.getState().equalsIgnoreCase("UNKNOWN")) {
                threadList.add(thread);
                saveToDatabase(thread);
            } else {
                skippedUnknown++;
            }
        }

        result.put("totalThreads", threadList.size());
        result.put("threads", threadList);

        System.out.println(" Parsed valid threads: " + threadList.size());
        System.out.println(" Skipped UNKNOWN threads: " + skippedUnknown);

        return result;
    }

    private ThreadInfo parseThreadBlock(String block) {
        try {
            String[] lines = block.split("\n");
            String header = lines[0];

            String threadName = header.split("\"")[1];
            String threadId = extractThreadId(header);
            String state = extractState(lines);
            boolean deadlocked = header.contains("deadlock") || block.contains("waiting to lock");

            return new ThreadInfo(threadName, threadId, state, Arrays.asList(lines), deadlocked);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractThreadId(String header) {
        String[] parts = header.split(" ");
        for (String part : parts) {
            if (part.startsWith("tid=")) {
                return part.substring(4);
            }
        }
        return UUID.randomUUID().toString();
    }

    private String extractState(String[] lines) {
        for (String line : lines) {
            if (line.trim().startsWith("java.lang.Thread.State")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String rawState = parts[1].trim();
                    int parenIndex = rawState.indexOf(" (");
                    if (parenIndex != -1) {
                        rawState = rawState.substring(0, parenIndex).trim();
                    }
                    return rawState;
                }
            }
        }
        return "UNKNOWN";
    }

    private void saveToDatabase(ThreadInfo threadInfo) {
        ThreadInfoEntity entity = new ThreadInfoEntity();
        entity.setThreadName(threadInfo.getThreadName());
        entity.setThreadId(threadInfo.getThreadId());
        entity.setState(threadInfo.getState());
        entity.setStackTrace(threadInfo.getStackTrace());
        entity.setDeadlocked(threadInfo.isDeadlocked());
        repository.save(entity);
    }

    public void saveDumpMetadata(String pid, String filePath) {
        ThreadDumpRecord record = new ThreadDumpRecord();
        record.setPid(pid);
        record.setFilePath(filePath);
        record.setCapturedAt(LocalDateTime.now());
        dumpMetadataRepository.save(record);
    }

}