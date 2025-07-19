package com.example.threaddump_analyzer.service;

import com.example.threaddump_analyzer.entity.ThreadInfoEntity;
import com.example.threaddump_analyzer.entity.ThreadDumpRecord;
import com.example.threaddump_analyzer.model.ThreadDumpResponse;
import com.example.threaddump_analyzer.model.ThreadInfo;
import com.example.threaddump_analyzer.repository.ThreadInfoRepository;
import com.example.threaddump_analyzer.repository.ThreadDumpRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadDumpService {

    private final ThreadInfoRepository threadInfoRepository;
    private final ThreadDumpRecordRepository dumpRecordRepository;

    public ThreadDumpResponse parse(MultipartFile file) {
        List<ThreadInfo> threadList = new ArrayList<>();
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder block = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\"")) {
                    if (block.length() > 0) {
                        ThreadInfo info = com.example.threaddump_analyzer.service.ThreadParserUtil.parseThreadBlock(block.toString());
                        if (info != null) {
                            threadList.add(info);
                            saveToDatabase(info);
                        } else {
                            skipped++;
                        }
                        block.setLength(0);
                    }
                }
                block.append(line).append("\n");
            }

            if (block.length() > 0) {
                ThreadInfo info = com.example.threaddump_analyzer.service.ThreadParserUtil.parseThreadBlock(block.toString());
                if (info != null) {
                    threadList.add(info);
                    saveToDatabase(info);
                } else {
                    skipped++;
                }
            }

        } catch (Exception e) {
            log.error("Error parsing thread dump", e);
        }

        return new ThreadDumpResponse(threadList.size(), threadList);
    }

    public void saveDumpMetadata(String pid, String filePath) {
        ThreadDumpRecord record = ThreadDumpRecord.builder()
                .pid(pid)
                .filePath(filePath)
                .capturedAt(LocalDateTime.now())
                .build();
        dumpRecordRepository.save(record);
    }

    private void saveToDatabase(ThreadInfo info) {
        ThreadInfoEntity entity = ThreadInfoEntity.builder()
                .threadName(info.getThreadName())
                .threadId(info.getThreadId())
                .state(info.getState())
                .deadlocked(info.isDeadlocked())
                .stackTrace(info.getStackTrace())
                .build();
        threadInfoRepository.save(entity);
    }
}
