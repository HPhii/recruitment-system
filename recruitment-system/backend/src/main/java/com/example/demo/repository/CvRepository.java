package com.example.demo.repository;

import com.example.demo.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    // Các phương thức truy vấn tùy chỉnh nếu cần
}
