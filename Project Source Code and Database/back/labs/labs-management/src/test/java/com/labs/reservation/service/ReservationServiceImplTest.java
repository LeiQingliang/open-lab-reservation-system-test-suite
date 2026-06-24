package com.labs.reservation.service;

import com.labs.reservation.domain.Reservation;
import com.labs.reservation.mapper.ReservationMapper;
import com.labs.reservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 预约（实验室使用记录）Service 单元测试
 * <p>
 * 被测类：{@link ReservationServiceImpl}
 * 测试框架：JUnit 5 (Jupiter) + Mockito
 * 测试策略：使用 Mockito 隔离 ReservationMapper（数据库访问层），
 *           仅验证 Service 业务层的方法调用、参数传递与返回值处理逻辑。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M3 预约管理 - ReservationServiceImpl 单元测试")
class ReservationServiceImplTest {

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(100L);
        reservation.setLabId(1L);
        reservation.setTimeSlot(1L);
        reservation.setPurpose("软件测试综合训练-上机实验");
        reservation.setStatus(0L);
    }

    @Test
    @DisplayName("UT-RSV-S-01 根据ID查询预约-记录存在")
    void testSelectReservationById_found() {
        when(reservationMapper.selectReservationById(1L)).thenReturn(reservation);

        Reservation result = reservationService.selectReservationById(1L);

        assertNotNull(result, "应返回预约对象");
        assertEquals(1L, result.getId());
        verify(reservationMapper, times(1)).selectReservationById(1L);
    }

    @Test
    @DisplayName("UT-RSV-S-02 根据ID查询预约-记录不存在返回null")
    void testSelectReservationById_notFound() {
        when(reservationMapper.selectReservationById(999L)).thenReturn(null);

        assertNull(reservationService.selectReservationById(999L), "不存在时应返回 null");
    }

    @Test
    @DisplayName("UT-RSV-S-03 查询预约列表")
    void testSelectReservationList() {
        when(reservationMapper.selectReservationList(any(Reservation.class)))
                .thenReturn(Arrays.asList(reservation, new Reservation()));

        List<Reservation> list = reservationService.selectReservationList(new Reservation());

        assertEquals(2, list.size());
        verify(reservationMapper).selectReservationList(any(Reservation.class));
    }

    @Test
    @DisplayName("UT-RSV-S-04 新增预约-成功返回1")
    void testInsertReservation_success() {
        when(reservationMapper.insertReservation(reservation)).thenReturn(1);

        assertEquals(1, reservationService.insertReservation(reservation));
        verify(reservationMapper).insertReservation(reservation);
    }

    @Test
    @DisplayName("UT-RSV-S-05 新增预约-数据库返回0表示失败")
    void testInsertReservation_failed() {
        when(reservationMapper.insertReservation(any(Reservation.class))).thenReturn(0);

        assertEquals(0, reservationService.insertReservation(reservation));
    }

    @Test
    @DisplayName("UT-RSV-S-06 修改预约")
    void testUpdateReservation() {
        when(reservationMapper.updateReservation(reservation)).thenReturn(1);

        assertEquals(1, reservationService.updateReservation(reservation));
    }

    @Test
    @DisplayName("UT-RSV-S-07 根据ID删除预约")
    void testDeleteReservationById() {
        when(reservationMapper.deleteReservationById(1L)).thenReturn(1);

        assertEquals(1, reservationService.deleteReservationById(1L));
    }

    @Test
    @DisplayName("UT-RSV-S-08 批量删除预约")
    void testDeleteReservationByIds() {
        Long[] ids = {1L, 2L, 3L};
        when(reservationMapper.deleteReservationByIds(ids)).thenReturn(3);

        assertEquals(3, reservationService.deleteReservationByIds(ids));
    }

    @Test
    @DisplayName("UT-RSV-S-09 审核预约-自动写入审核时间并依次调用更新")
    void testReviewReservation_setsReviewTimeAndUpdates() {
        reservation.setStatus(1L);
        when(reservationMapper.reviewReservation(reservation)).thenReturn(1);

        int rows = reservationService.reviewReservation(reservation);

        assertEquals(1, rows, "审核应返回受影响行数");
        assertNotNull(reservation.getReviewTime(), "Service 应自动设置审核时间 reviewTime");
        // 验证：先更新审核状态，再执行审核更新
        verify(reservationMapper, times(1)).updateReviewStatus(reservation);
        verify(reservationMapper, times(1)).reviewReservation(reservation);
    }

    @Test
    @DisplayName("UT-RSV-S-10 用户提交预约（来自排期）")
    void testInsertReservationFromSchedule() {
        when(reservationMapper.insertReservationFromSchedule(reservation)).thenReturn(1);

        assertEquals(1, reservationService.insertReservationFromSchedule(reservation));
        verify(reservationMapper).insertReservationFromSchedule(reservation);
    }

    @Test
    @DisplayName("UT-RSV-S-11 冲突查询-无冲突返回空列表")
    void testSelectConflicting_none() {
        when(reservationMapper.selectConflictingReservations(any(Reservation.class)))
                .thenReturn(new ArrayList<>());

        List<Reservation> conflicts = reservationService.selectConflictingReservations(reservation);

        assertNotNull(conflicts);
        assertTrue(conflicts.isEmpty(), "无冲突时应返回空列表");
    }

    @Test
    @DisplayName("UT-RSV-S-12 冲突查询-存在冲突返回冲突记录")
    void testSelectConflicting_exists() {
        Reservation conflict = new Reservation();
        conflict.setId(2L);
        conflict.setLabId(1L);
        conflict.setTimeSlot(1L);
        when(reservationMapper.selectConflictingReservations(any(Reservation.class)))
                .thenReturn(Arrays.asList(conflict));

        List<Reservation> conflicts = reservationService.selectConflictingReservations(reservation);

        assertEquals(1, conflicts.size());
        assertEquals(2L, conflicts.get(0).getId());
    }
}
