package com.example.threaddump_analyzer.controller;

import com.example.threaddump_analyzer.dto.RemoteDumpRequest;
import com.example.threaddump_analyzer.service.SshService;
import com.example.threaddump_analyzer.service.ThreadDumpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/thread-dump")
@CrossOrigin(origins = "http://localhost:4200")
public class ThreadDumpController {

    @Autowired
    private ThreadDumpService threadDumpService;

    @Autowired
    private SshService sshService;

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parseThreadDump(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = threadDumpService.parseThreadDump(file);
            if (result == null || result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Parsed result is empty"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error parsing thread dump", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    @GetMapping("/java-processes")
    public ResponseEntity<List<String>> listJavaProcesses() {
        List<String> processes = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("jps", "-l").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processes.add(line);
                }
            }
            return ResponseEntity.ok(processes);
        } catch (IOException e) {
            log.error("Error listing Java processes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Failed to fetch Java processes"));
        }
    }

    @PostMapping("/capture-dump/{pid}")
    public ResponseEntity<Map<String, Object>> captureThreadDump(@PathVariable String pid) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "threaddump_" + pid + "_" + timestamp + ".txt";

            File dumpDir = new File("/opt/thread-dumps");
            if (!dumpDir.exists() && !dumpDir.mkdirs()) {
                dumpDir = new File("thread-dumps");
                dumpDir.mkdirs();
            }

            File dumpFile = new File(dumpDir, fileName);
            log.info("Writing local dump to: {}", dumpFile.getAbsolutePath());

            Process process = new ProcessBuilder("jstack", pid)
                    .redirectOutput(dumpFile)
                    .start();
            process.waitFor();

            MultipartFile multipartFile = wrapFileAsMultipart(dumpFile, fileName);

            Map<String, Object> result = threadDumpService.parseThreadDump(multipartFile);
            result.put("downloadUrl", "/api/thread-dump/download/" + fileName);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to capture local thread dump for PID {}", pid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to capture thread dump: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadDumpFile(@PathVariable String filename) throws IOException {
        File file = new File("/opt/thread-dumps/" + filename);
        if (!file.exists()) {
            file = new File("thread-dumps/" + filename);
        }

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    @PostMapping("/remote-capture")
    public ResponseEntity<Map<String, Object>> captureRemoteDump(@RequestBody RemoteDumpRequest req) {
        try {
            if (req.getHost() == null || req.getUsername() == null || req.getPassword() == null || req.getPid() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required remote connection fields"));
            }

            log.info(" Capturing remote thread dump from {} PID {}", req.getHost(), req.getPid());

            String rawDump = sshService.executeRemoteJstack(
                    req.getHost(),
                    req.getPort(),
                    req.getUsername(),
                    req.getPassword(),
                    req.getPid()
            );

            File temp = File.createTempFile("remotedump_", ".txt");
            try (FileWriter writer = new FileWriter(temp)) {
                writer.write(rawDump);
            }

            MultipartFile file = wrapFileAsMultipart(temp, temp.getName());

            Map<String, Object> result = threadDumpService.parseThreadDump(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Remote capture failed for host {} pid {}", req.getHost(), req.getPid(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Remote capture failed: " + e.getMessage()));
        }
    }

    private MultipartFile wrapFileAsMultipart(File file, String originalName) {
        return new MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return originalName; }
            @Override public String getContentType() { return "text/plain"; }
            @Override public boolean isEmpty() { return file.length() == 0; }
            @Override public long getSize() { return file.length(); }
            @Override public byte[] getBytes() throws IOException { return java.nio.file.Files.readAllBytes(file.toPath()); }
            @Override public InputStream getInputStream() throws IOException { return new FileInputStream(file); }
            @Override public void transferTo(File dest) throws IOException { java.nio.file.Files.copy(file.toPath(), dest.toPath()); }
        };
    }
}
