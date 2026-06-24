package com.labs.apply.service;

import com.labs.apply.domain.LabSchedule;
import com.labs.apply.mapper.LabScheduleMapper;
import com.labs.apply.service.impl.LabScheduleServiceImpl;
import com.labs.common.core.domain.entity.SysUser;
import com.labs.common.core.domain.model.LoginUser;
import com.labs.common.exception.ServiceException;
import com.labs.reservation.domain.Reservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 实验室排期（可预约时段发布）Service 单元测试。
 * <p>
 * 被测类：{@link LabScheduleServiceImpl}，使用 Mockito 隔离 {@link LabScheduleMapper}。
 * 重点覆盖：CRUD 委派、按日期查询、删除冲突排期，以及
 * {@code publishAvailableSlots} 的参数校验（空参抛出 ServiceException）与
 * “实验室 × 时段”笛卡尔积批量插入（N×M 次）。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M2 排期管理 - LabScheduleServiceImpl 单元测试")
class LabScheduleServiceImplTest {

    @Mock
    private LabScheduleMapper labScheduleMapper;

    @InjectMocks
    private LabScheduleServiceImpl labScheduleService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockLogin(String username) {
        SysUser user = new SysUser();
        user.setUserName(username);
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null));
    }

    @Test
    @DisplayName("UT-SCH-01 根据ID查询排期")
    void testSelectById() {
        LabSchedule s = new LabSchedule();
        s.setId(1L);
        when(labScheduleMapper.selectLabScheduleById(1L)).thenReturn(s);

        assertEquals(1L, labScheduleService.selectLabScheduleById(1L).getId());
    }

    @Test
    @DisplayName("UT-SCH-02 查询排期列表")
    void testSelectList() {
        when(labScheduleMapper.selectLabScheduleList(any(LabSchedule.class)))
                .thenReturn(Arrays.asList(new LabSchedule(), new LabSchedule()));

        assertEquals(2, labScheduleService.selectLabScheduleList(new LabSchedule()).size());
    }

    @Test
    @DisplayName("UT-SCH-03 按日期查询当日排期")
    void testSelectByDate() {
        LocalDate today = LocalDate.now();
        when(labScheduleMapper.selectLabSchedulesByDate(today))
                .thenReturn(Arrays.asList(new LabSchedule()));

        assertEquals(1, labScheduleService.selectLabSchedulesByDate(today).size());
    }

    @Test
    @DisplayName("UT-SCH-04 新增排期")
    void testInsert() {
        LabSchedule s = new LabSchedule();
        when(labScheduleMapper.insertLabSchedule(s)).thenReturn(1);
        assertEquals(1, labScheduleService.insertLabSchedule(s));
    }

    @Test
    @DisplayName("UT-SCH-05 修改排期")
    void testUpdate() {
        LabSchedule s = new LabSchedule();
        when(labScheduleMapper.updateLabSchedule(s)).thenReturn(1);
        assertEquals(1, labScheduleService.updateLabSchedule(s));
    }

    @Test
    @DisplayName("UT-SCH-06 删除/批量删除排期")
    void testDelete() {
        when(labScheduleMapper.deleteLabScheduleById(1L)).thenReturn(1);
        Long[] ids = {1L, 2L};
        when(labScheduleMapper.deleteLabScheduleByIds(ids)).thenReturn(2);

        assertEquals(1, labScheduleService.deleteLabScheduleById(1L));
        assertEquals(2, labScheduleService.deleteLabScheduleByIds(ids));
    }

    @Test
    @DisplayName("UT-SCH-07 删除与某预约冲突的排期")
    void testDeleteByReservation() {
        Reservation r = new Reservation();
        r.setLabId(1L);
        r.setTimeSlot(1L);

        labScheduleService.deleteByReservation(r);

        verify(labScheduleMapper, times(1)).deleteByReservation(r);
    }

    @Test
    @DisplayName("UT-SCH-08 发布时段-日期为null抛出ServiceException")
    void testPublish_nullDate() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> labScheduleService.publishAvailableSlots(null, Arrays.asList(1), Arrays.asList(1L)));
        assertTrue(ex.getMessage().contains("参数不能为空"));
        verify(labScheduleMapper, never()).insertLabSchedule(any());
    }

    @Test
    @DisplayName("UT-SCH-09 发布时段-时段列表为null抛出ServiceException")
    void testPublish_nullSlots() {
        assertThrows(ServiceException.class,
                () -> labScheduleService.publishAvailableSlots(new Date(), null, Arrays.asList(1L)));
    }

    @Test
    @DisplayName("UT-SCH-10 发布时段-实验室列表为null抛出ServiceException")
    void testPublish_nullLabs() {
        assertThrows(ServiceException.class,
                () -> labScheduleService.publishAvailableSlots(new Date(), Arrays.asList(1), null));
    }

    @Test
    @DisplayName("UT-SCH-11 发布时段-2个实验室×3个时段=插入6条排期")
    void testPublish_success_cartesian() {
        mockLogin("admin");
        List<Long> labIds = Arrays.asList(1L, 2L);
        List<Integer> timeSlots = Arrays.asList(1, 2, 3);

        labScheduleService.publishAvailableSlots(new Date(), timeSlots, labIds);

        // 2 间实验室 × 3 个时段 = 6 条 is_available=1 的排期
        verify(labScheduleMapper, times(6)).insertLabSchedule(any(LabSchedule.class));
    }
}
