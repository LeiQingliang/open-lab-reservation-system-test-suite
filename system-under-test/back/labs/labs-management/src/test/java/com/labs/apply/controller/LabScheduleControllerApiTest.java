package com.labs.apply.controller;

import com.github.pagehelper.PageHelper;
import com.labs.apply.domain.LabSchedule;
import com.labs.apply.service.ILabScheduleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 实验室排期 REST 接口测试（接口/API 测试）。
 * <p>
 * 采用 standaloneSetup 独立装载 Controller，Service 以 Mockito 打桩。
 * “发布可预约时段(publish)”涉及 LocalDate JSON 反序列化，已在
 * {@code LabScheduleServiceImplTest} 中通过 Service 层直接覆盖，此处覆盖其余 REST 端点。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M2 排期管理 - LabScheduleController 接口测试(MockMvc)")
class LabScheduleControllerApiTest {

    @Mock
    private ILabScheduleService labScheduleService;

    @InjectMocks
    private LabScheduleController labScheduleController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(labScheduleController).build();
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    private LabSchedule sample() {
        LabSchedule s = new LabSchedule();
        s.setId(1L);
        s.setLabId(1L);
        s.setTimeSlot(1L);
        s.setIsAvailable(1);
        return s;
    }

    @Test
    @DisplayName("API-SCH-01 GET /list 返回排期分页列表")
    void testList() throws Exception {
        when(labScheduleService.selectLabScheduleList(any(LabSchedule.class)))
                .thenReturn(Arrays.asList(sample()));

        mockMvc.perform(get("/apply/apply/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows.length()").value(1));
    }

    @Test
    @DisplayName("API-SCH-02 GET /gettoday 返回当日排期")
    void testGetToday() throws Exception {
        when(labScheduleService.selectLabSchedulesByDate(any(LocalDate.class)))
                .thenReturn(Arrays.asList(sample()));

        mockMvc.perform(get("/apply/apply/gettoday"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("API-SCH-03 GET /{id} 返回排期详情")
    void testGetInfo() throws Exception {
        when(labScheduleService.selectLabScheduleById(1L)).thenReturn(sample());

        mockMvc.perform(get("/apply/apply/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.labId").value(1));
    }

    @Test
    @DisplayName("API-SCH-04 POST 新增排期")
    void testAdd() throws Exception {
        when(labScheduleService.insertLabSchedule(any(LabSchedule.class))).thenReturn(1);

        mockMvc.perform(post("/apply/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labId\":1,\"date\":\"2026-06-22\",\"timeSlot\":1,\"isAvailable\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("API-SCH-05 DELETE 批量删除排期")
    void testRemove() throws Exception {
        when(labScheduleService.deleteLabScheduleByIds(any(Long[].class))).thenReturn(2);

        mockMvc.perform(delete("/apply/apply/{ids}", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
