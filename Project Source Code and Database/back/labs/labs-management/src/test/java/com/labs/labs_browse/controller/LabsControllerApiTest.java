package com.labs.labs_browse.controller;

import com.github.pagehelper.PageHelper;
import com.labs.labs_browse.domain.Labs;
import com.labs.labs_browse.service.ILabsService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 实验室管理 REST 接口测试（接口/API 测试）。
 * <p>
 * 采用 {@code MockMvcBuilders.standaloneSetup} 独立装载单个 Controller，
 * 不启动完整 Spring 容器（无需 MySQL/Redis），Service 层以 Mockito 打桩。
 * 验证：URL 映射、HTTP 方法、请求/响应 JSON 结构与状态码。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M1 实验室管理 - LabsController 接口测试(MockMvc)")
class LabsControllerApiTest {

    @Mock
    private ILabsService labsService;

    @InjectMocks
    private LabsController labsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(labsController).build();
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    private Labs sampleLab() {
        Labs lab = new Labs();
        lab.setId(1L);
        lab.setLabName("软件工程实验室");
        lab.setLocation("教学楼A305");
        lab.setCapacity(60L);
        lab.setStatus(1);
        return lab;
    }

    @Test
    @DisplayName("API-LAB-01 GET /list 返回分页列表")
    void testList() throws Exception {
        when(labsService.selectLabsList(any(Labs.class))).thenReturn(Arrays.asList(sampleLab()));

        mockMvc.perform(get("/labs_browse/labs_browse/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].labName").value("软件工程实验室"));
    }

    @Test
    @DisplayName("API-LAB-02 GET /{id} 返回实验室详情")
    void testGetInfo() throws Exception {
        when(labsService.selectLabsById(1L)).thenReturn(sampleLab());

        mockMvc.perform(get("/labs_browse/labs_browse/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.location").value("教学楼A305"));
    }

    @Test
    @DisplayName("API-LAB-03 POST 新增实验室")
    void testAdd() throws Exception {
        when(labsService.insertLabs(any(Labs.class))).thenReturn(1);

        mockMvc.perform(post("/labs_browse/labs_browse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labName\":\"网络安全实验室\",\"location\":\"B101\",\"capacity\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("API-LAB-04 PUT 修改实验室")
    void testEdit() throws Exception {
        when(labsService.updateLabs(any(Labs.class))).thenReturn(1);

        mockMvc.perform(put("/labs_browse/labs_browse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"labName\":\"软件工程实验室(改)\",\"capacity\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("API-LAB-05 DELETE 批量删除实验室")
    void testRemove() throws Exception {
        when(labsService.deleteLabsByIds(any(Long[].class))).thenReturn(2);

        mockMvc.perform(delete("/labs_browse/labs_browse/{ids}", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
