package com.example.threaddump_analyzer.entity;

import javax.persistence.*;

import lombok.*;

import java.util.List;

@Entity
@Table(name = "thread_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String threadName;
    private String threadId;
    private String state;
    private boolean deadlocked;

    @ElementCollection
    @CollectionTable(name = "thread_stacktrace", joinColumns = @JoinColumn(name = "thread_info_id"))
    @Column(name = "stack_line")
    private List<String> stackTrace;
}
