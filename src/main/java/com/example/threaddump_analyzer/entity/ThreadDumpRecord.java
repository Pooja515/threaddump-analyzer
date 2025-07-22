package com.example.threaddump_analyzer.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "thread_dump_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDumpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pid;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
}
