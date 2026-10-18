package org.megalab.taskflow.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class StatsResponse {
    private long total;
    private Map<String, Long> byStatus;
    private long overdue;
}
