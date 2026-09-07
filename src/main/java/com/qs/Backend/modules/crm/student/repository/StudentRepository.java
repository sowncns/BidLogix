package com.qs.Backend.modules.crm.student.repository;

import com.qs.Backend.modules.crm.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudentRepository extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {
}
