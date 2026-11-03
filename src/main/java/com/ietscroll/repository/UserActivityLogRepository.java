package com.ietscroll.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.UserActivityLog;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByUserEmailOrderByTimestampDesc(String userEmail);

    Page<UserActivityLog> findByUserEmailOrderByTimestampDesc(String userEmail, Pageable pageable);

    void deleteByUserEmail(String userEmail);
}
