
package com.example.threaddump_analyzer.repository;

import com.example.threaddump_analyzer.entity.ThreadDumpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadDumpRecordRepository extends JpaRepository<ThreadDumpRecord, Long> {
}
