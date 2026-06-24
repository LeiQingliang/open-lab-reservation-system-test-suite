package com.labs.labs_browse.service;

import com.labs.labs_browse.domain.Labs;
import com.labs.labs_browse.mapper.LabsMapper;
import com.labs.labs_browse.service.impl.LabsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 实验室信息（资源浏览/管理）Service 单元测试。
 * <p>
 * 被测类：{@link LabsServiceImpl}，使用 Mockito 隔离 {@link LabsMapper}。
 * 覆盖：查询详情、列表查询、新增、修改、删除、批量删除 6 个业务方法的委派与返回值。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M1 实验室管理 - LabsServiceImpl 单元测试")
class LabsServiceImplTest {

    @Mock
    private LabsMapper labsMapper;

    @InjectMocks
    private LabsServiceImpl labsService;

    private Labs labs;

    @BeforeEach
    void setUp() {
        labs = new Labs();
        labs.setId(1L);
        labs.setLabName("软件工程实验室");
        labs.setLocation("教学楼A305");
        labs.setCapacity(60L);
        labs.setStatus(1);
        labs.setDescription("用于软件测试综合训练上机");
    }

    @Test
    @DisplayName("UT-LAB-01 根据ID查询实验室")
    void testSelectLabsById() {
        when(labsMapper.selectLabsById(1L)).thenReturn(labs);

        Labs result = labsService.selectLabsById(1L);

        assertNotNull(result);
        assertEquals("软件工程实验室", result.getLabName());
        assertEquals("教学楼A305", result.getLocation());
        verify(labsMapper).selectLabsById(1L);
    }

    @Test
    @DisplayName("UT-LAB-02 查询实验室列表(支持名称/地址/容量过滤)")
    void testSelectLabsList() {
        when(labsMapper.selectLabsList(any(Labs.class)))
                .thenReturn(Arrays.asList(labs, new Labs()));

        List<Labs> list = labsService.selectLabsList(new Labs());

        assertEquals(2, list.size());
        verify(labsMapper).selectLabsList(any(Labs.class));
    }

    @Test
    @DisplayName("UT-LAB-03 新增实验室")
    void testInsertLabs() {
        when(labsMapper.insertLabs(labs)).thenReturn(1);

        assertEquals(1, labsService.insertLabs(labs));
        verify(labsMapper).insertLabs(labs);
    }

    @Test
    @DisplayName("UT-LAB-04 修改实验室")
    void testUpdateLabs() {
        when(labsMapper.updateLabs(labs)).thenReturn(1);

        assertEquals(1, labsService.updateLabs(labs));
    }

    @Test
    @DisplayName("UT-LAB-05 根据ID删除实验室")
    void testDeleteLabsById() {
        when(labsMapper.deleteLabsById(1L)).thenReturn(1);

        assertEquals(1, labsService.deleteLabsById(1L));
    }

    @Test
    @DisplayName("UT-LAB-06 批量删除实验室")
    void testDeleteLabsByIds() {
        Long[] ids = {1L, 2L};
        when(labsMapper.deleteLabsByIds(ids)).thenReturn(2);

        assertEquals(2, labsService.deleteLabsByIds(ids));
        verify(labsMapper).deleteLabsByIds(ids);
    }
}
