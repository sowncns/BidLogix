package com.qs.Backend.modules.crm.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentListResponse {
    private List<StudentResponse> items;
    private long total;
    private int limit;
    private int offset;
}
