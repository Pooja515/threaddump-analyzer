package com.example.threaddump_analyzer.controller;

import com.example.threaddump_analyzer.model.ThreadDumpResponse;
import com.example.threaddump_analyzer.service.ThreadDumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/thread-dump")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ThreadDumpController {

    private final ThreadDumpService threadDumpService;

    @PostMapping("/parse")
    public ResponseEntity<ThreadDumpResponse> parse(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(threadDumpService.parse(file));
    }

    @GetMapping("/java-processes")
    public ResponseEntity<?> listJavaProcesses() {
        try {
            Process process = new ProcessBuilder("jps", "-l").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Use Java 8-compatible toList
            return ResponseEntity.ok(reader.lines().collect(java.util.stream.Collectors.toList()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch Java processes");
        }
    }

    @PostMapping("/capture-dump/{pid}")
    public ResponseEntity<ThreadDumpResponse> captureAndParse(@PathVariable String pid) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "threaddump_" + pid + "_" + timestamp + ".txt";
            File dir = new File("/opt/thread-dumps");
            if (!dir.exists()) dir.mkdirs();

            File dumpFile = new File(dir, fileName);
            Process process = new ProcessBuilder("jstack", pid).redirectOutput(dumpFile).start();
            process.waitFor();

            MultipartFile multipartFile = new com.example.threaddump_analyzer.util.MockMultipartFileAdapter(dumpFile);
            return ResponseEntity.ok(threadDumpService.parse(multipartFile));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        File file = new File("/opt/thread-dumps/" + filename);
        if (!file.exists()) return ResponseEntity.notFound().build();

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}
