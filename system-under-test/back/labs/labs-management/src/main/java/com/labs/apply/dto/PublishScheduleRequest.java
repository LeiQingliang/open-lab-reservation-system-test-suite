package com.labs.apply.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
public class PublishScheduleRequest {

    @NotNull(message = "日期不能为空")
    @FutureOrPresent(message = "日期必须为今天或将来")
    private LocalDate date;

    @NotEmpty(message = "至少需要选择一个时段")
    private List<Integer> timeSlots;

    @NotEmpty(message = "至少需要选择一个实验室")
    private List<Long> labIds;
}