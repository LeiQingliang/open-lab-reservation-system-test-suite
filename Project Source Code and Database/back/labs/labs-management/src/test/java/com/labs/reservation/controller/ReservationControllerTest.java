package com.labs.reservation.controller;

import com.labs.apply.service.ILabScheduleService;
import com.labs.common.core.domain.AjaxResult;
import com.labs.common.core.domain.model.LoginUser;
import com.labs.reservation.domain.Reservation;
import com.labs.reservation.service.IReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 预约提交 / 预约审核 控制层业务逻辑单元测试。
 * <p>
 * 被测类：{@link ReservationController}
 * 测试框架：JUnit 5 (Jupiter) + Mockito，通过 {@code @InjectMocks} 注入被 Mock 的 Service，
 *           直接调用 Controller 方法，断言返回的 {@link AjaxResult} 状态码与提示信息。
 * <p>
 * 本测试类重点覆盖缺陷修复后的输入校验与业务规则（对应缺陷清单）：
 * <ul>
 *   <li>BUG-001 预约冲突检测</li>
 *   <li>BUG-002 时间段范围(1-4)校验</li>
 *   <li>BUG-003 过去日期拦截</li>
 *   <li>BUG-005 实验室ID非空校验</li>
 *   <li>BUG-006 预约用途非空校验</li>
 *   <li>BUG-004 审核通过后自动拒绝冲突预约</li>
 *   <li>BUG-010 防止重复审核</li>
 * </ul>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M3/M4 预约提交与审核 - ReservationController 业务逻辑测试")
class ReservationControllerTest {

    @Mock
    private IReservationService reservationService;

    @Mock
    private ILabScheduleService labScheduleService;

    @InjectMocks
    private ReservationController reservationController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /* ====================== 工具方法 ====================== */

    private int code(AjaxResult r) {
        return ((Integer) r.get(AjaxResult.CODE_TAG)).intValue();
    }

    private String msg(AjaxResult r) {
        return String.valueOf(r.get(AjaxResult.MSG_TAG));
    }

    private Reservation validRequest() {
        Reservation r = new Reservation();
        r.setLabId(1L);
        r.setTimeSlot(1L);
        r.setPurpose("软件测试综合训练-上机实验");
        r.setReservationDate(new Date(System.currentTimeMillis() + 86400000L)); // 明天
        return r;
    }

    private void loginAsTeacher(long teacherId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(teacherId);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null));
    }

    /* ============= insertReservation 输入校验 ============= */

    @Test
    @DisplayName("IT-RSV-01 时间段为null应被拒绝(BUG-002)")
    void testInsert_timeSlotNull() {
        Reservation r = validRequest();
        r.setTimeSlot(null);

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("时间段"));
        verify(reservationService, never()).insertReservationFromSchedule(any());
    }

    @Test
    @DisplayName("IT-RSV-02 时间段=0(下边界外)应被拒绝(BUG-002)")
    void testInsert_timeSlotBelowRange() {
        Reservation r = validRequest();
        r.setTimeSlot(0L);
        assertEquals(500, code(reservationController.insertReservation(r)));
    }

    @Test
    @DisplayName("IT-RSV-03 时间段=5(上边界外)应被拒绝(BUG-002)")
    void testInsert_timeSlotAboveRange() {
        Reservation r = validRequest();
        r.setTimeSlot(5L);
        assertEquals(500, code(reservationController.insertReservation(r)));
    }

    @Test
    @DisplayName("IT-RSV-04 过去日期应被拒绝(BUG-003)")
    void testInsert_pastDate() {
        Reservation r = validRequest();
        r.setReservationDate(new Date(0L)); // 1970-01-01

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("过去"));
    }

    @Test
    @DisplayName("IT-RSV-05 实验室ID为null应被拒绝(BUG-005)")
    void testInsert_labIdNull() {
        Reservation r = validRequest();
        r.setLabId(null);

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("实验室ID"));
    }

    @Test
    @DisplayName("IT-RSV-06 预约用途为空白应被拒绝(BUG-006)")
    void testInsert_blankPurpose() {
        Reservation r = validRequest();
        r.setPurpose("   ");

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("用途"));
    }

    @Test
    @DisplayName("IT-RSV-07 时段冲突应被拒绝(BUG-001)")
    void testInsert_conflict() {
        Reservation r = validRequest();
        Reservation exist = new Reservation();
        exist.setId(2L);
        when(reservationService.selectConflictingReservations(any(Reservation.class)))
                .thenReturn(Arrays.asList(exist));

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("已被预约"));
        verify(reservationService, never()).insertReservationFromSchedule(any());
    }

    @Test
    @DisplayName("IT-RSV-08 合法预约提交成功，且状态被强制置为待审核(0)")
    void testInsert_success() {
        Reservation r = validRequest();
        when(reservationService.selectConflictingReservations(any(Reservation.class)))
                .thenReturn(new ArrayList<>());
        when(reservationService.insertReservationFromSchedule(any(Reservation.class))).thenReturn(1);

        AjaxResult result = reservationController.insertReservation(r);

        assertEquals(200, code(result));
        assertEquals(0L, r.getStatus(), "提交后状态应被强制设为0(待审核)，防止前端越权传入");
        assertNotNull(r.getCreatedAt(), "提交时间应被设置");
        verify(reservationService, times(1)).insertReservationFromSchedule(r);
    }

    /* ============= reviewReservation 审核逻辑 ============= */

    @Test
    @DisplayName("IT-RSV-09 审核时预约ID为null应被拒绝")
    void testReview_idNull() {
        Reservation r = new Reservation();
        r.setStatus(1L);

        AjaxResult result = reservationController.reviewReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("预约ID"));
    }

    @Test
    @DisplayName("IT-RSV-10 审核时预约记录不存在应被拒绝")
    void testReview_notExist() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(1L);
        when(reservationService.selectReservationById(1L)).thenReturn(null);

        AjaxResult result = reservationController.reviewReservation(r);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("不存在"));
    }

    @Test
    @DisplayName("IT-RSV-11 已审核通过的预约不能重复审核(BUG-010)")
    void testReview_alreadyApproved() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setStatus(1L); // 已通过
        when(reservationService.selectReservationById(1L)).thenReturn(existing);

        Reservation req = new Reservation();
        req.setId(1L);
        req.setStatus(1L);

        AjaxResult result = reservationController.reviewReservation(req);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("已经审核过"));
        assertTrue(msg(result).contains("已通过"));
        verify(reservationService, never()).reviewReservation(any());
    }

    @Test
    @DisplayName("IT-RSV-12 已拒绝的预约不能重复审核(BUG-010)")
    void testReview_alreadyRejected() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setStatus(2L); // 已拒绝
        when(reservationService.selectReservationById(1L)).thenReturn(existing);

        Reservation req = new Reservation();
        req.setId(1L);
        req.setStatus(1L);

        AjaxResult result = reservationController.reviewReservation(req);

        assertEquals(500, code(result));
        assertTrue(msg(result).contains("已拒绝"));
    }

    @Test
    @DisplayName("IT-RSV-13 审核通过后自动拒绝同时段其他冲突预约(BUG-004)")
    void testReview_approve_autoRejectConflicts() {
        loginAsTeacher(88L);

        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setStatus(0L); // 待审核
        existing.setLabId(1L);
        existing.setTimeSlot(1L);
        existing.setReservationDate(new Date());
        when(reservationService.selectReservationById(1L)).thenReturn(existing);

        // 同时段还有一条待审核的冲突预约(ID=2)
        Reservation other = new Reservation();
        other.setId(2L);
        other.setStatus(0L);
        when(reservationService.selectConflictingReservations(any(Reservation.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(other)));
        when(reservationService.reviewReservation(any(Reservation.class))).thenReturn(1);

        Reservation req = new Reservation();
        req.setId(1L);
        req.setStatus(1L); // 通过

        AjaxResult result = reservationController.reviewReservation(req);

        assertEquals(200, code(result));
        assertEquals(2L, other.getStatus(), "冲突预约应被自动置为已拒绝(2)");
        // 冲突预约 + 当前预约 各审核一次
        verify(reservationService, times(2)).reviewReservation(any(Reservation.class));
        // 通过后应删除对应排期时段
        verify(labScheduleService, times(1)).deleteByReservation(any(Reservation.class));
    }

    @Test
    @DisplayName("IT-RSV-14 审核拒绝-不触发自动拒绝与排期删除")
    void testReview_reject() {
        loginAsTeacher(88L);

        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setStatus(0L);
        when(reservationService.selectReservationById(1L)).thenReturn(existing);
        when(reservationService.reviewReservation(any(Reservation.class))).thenReturn(1);

        Reservation req = new Reservation();
        req.setId(1L);
        req.setStatus(2L); // 拒绝

        AjaxResult result = reservationController.reviewReservation(req);

        assertEquals(200, code(result));
        verify(reservationService, times(1)).reviewReservation(any(Reservation.class));
        verify(labScheduleService, never()).deleteByReservation(any(Reservation.class));
        verify(reservationService, never()).selectConflictingReservations(any(Reservation.class));
    }
}
