package com.qs.Backend.modules.workorder.servicelog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartReplacedDto {
    @JsonProperty("part_name")
    private String partName;
    private int quantity;
    @JsonProperty("part_code")
    private String partCode;
}
