package com.example.threaddump_analyzer.repository;

import com.example.threaddump_analyzer.entity.ThreadInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadInfoRepository extends JpaRepository<ThreadInfoEntity, Long> {
}
