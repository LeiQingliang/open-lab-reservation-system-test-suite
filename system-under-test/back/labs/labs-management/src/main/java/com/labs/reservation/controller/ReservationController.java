package com.labs.reservation.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.labs.apply.service.ILabScheduleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.labs.common.annotation.Log;
import com.labs.common.core.controller.BaseController;
import com.labs.common.core.domain.AjaxResult;
import com.labs.common.enums.BusinessType;
import com.labs.reservation.domain.Reservation;
import com.labs.reservation.service.IReservationService;
import com.labs.common.utils.poi.ExcelUtil;
import com.labs.common.core.page.TableDataInfo;

/**
 * 实验室使用记录Controller
 *
 * @author santh
 * @date 2025-04-10
 * 
 * T29 缺陷修复记录:
 * - BUG-001: insertReservation 增加预约冲突检测 [已修复]
 * - BUG-002: insertReservation 增加timeSlot范围校验(1-4) [已修复]
 * - BUG-003: insertReservation 增加过去日期拦截 [已修复]
 * - BUG-004: reviewReservation 审核通过后自动拒绝冲突预约 [已修复]
 * - BUG-005: insertReservation 增加labId非空校验 [已修复]
 * - BUG-006: insertReservation 增加purpose非空校验 [已修复]
 * - BUG-010: reviewReservation 增加重复审核防护 [已修复]
 */
@RestController
@RequestMapping("/reservation/reservation")
public class ReservationController extends BaseController
{
    @Autowired
    private IReservationService reservationService;

    @Autowired
    private ILabScheduleService labScheduleService;

    /**
     * 查询实验室使用记录列表
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:list')")
    @GetMapping("/list")
    public TableDataInfo list(Reservation reservation)
    {
        startPage();
        List<Reservation> list = reservationService.selectReservationList(reservation);
        return getDataTable(list);
    }

    /**
     * 导出实验室使用记录列表
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:export')")
    @Log(title = "实验室使用记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Reservation reservation)
    {
        List<Reservation> list = reservationService.selectReservationList(reservation);
        ExcelUtil<Reservation> util = new ExcelUtil<Reservation>(Reservation.class);
        util.exportExcel(response, list, "实验室使用记录数据");
    }

    /**
     * 获取实验室使用记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(reservationService.selectReservationById(id));
    }

    /**
     * 新增实验室使用记录
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:add')")
    @Log(title = "实验室使用记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Reservation reservation)
    {
        return toAjax(reservationService.insertReservation(reservation));
    }

    /**
     * 修改实验室使用记录
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:edit')")
    @Log(title = "实验室使用记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Reservation reservation)
    {
        return toAjax(reservationService.updateReservation(reservation));
    }

    /**
     * 删除实验室使用记录
     */
    @PreAuthorize("@ss.hasPermi('reservation:reservation:remove')")
    @Log(title = "实验室使用记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(reservationService.deleteReservationByIds(ids));
    }

    /**
     * 审核预约记录 (同意 / 拒绝)
     * 
     * 修复: BUG-004 (自动拒绝冲突), BUG-010 (防止重复审核)
     */
    @Log(title = "预约审核", businessType = BusinessType.UPDATE)
    @PutMapping("/review")
    public AjaxResult reviewReservation(@RequestBody Reservation reservation) {
        // BUG-010: 防止重复审核
        if (reservation.getId() == null) {
            return error("预约ID不能为空");
        }
        Reservation existing = reservationService.selectReservationById(reservation.getId());
        if (existing == null) {
            return error("预约记录不存在");
        }
        if (existing.getStatus() != null && existing.getStatus() != 0L) {
            String statusName = existing.getStatus() == 1L ? "已通过" : "已拒绝";
            return error("该预约已经审核过 (" + statusName + ")，不能重复审核");
        }

        Long teacherId = getUserId();
        reservation.setTeacherId(teacherId);

        // 如果是同意审核
        if (reservation.getStatus() != null && reservation.getStatus() == 1) {
            // BUG-004: 审核通过后自动拒绝其他冲突的待审核预约
            Reservation conflictQuery = new Reservation();
            conflictQuery.setLabId(existing.getLabId());
            conflictQuery.setReservationDate(existing.getReservationDate());
            conflictQuery.setTimeSlot(existing.getTimeSlot());
            conflictQuery.setStatus(0L);
            conflictQuery.setId(reservation.getId());

            List<Reservation> otherConflicts =
                reservationService.selectConflictingReservations(conflictQuery);
            if (otherConflicts != null && !otherConflicts.isEmpty()) {
                for (Reservation conflict : otherConflicts) {
                    if (!conflict.getId().equals(reservation.getId())) {
                        conflict.setStatus(2L);
                        conflict.setTeacherId(teacherId);
                        conflict.setReviewNote("该时段已被其他预约占用，系统自动拒绝");
                        reservationService.reviewReservation(conflict);
                    }
                }
            }

            // 删除 lab_schedule 中冲突时间段的记录
            labScheduleService.deleteByReservation(reservation);
        }

        return toAjax(reservationService.reviewReservation(reservation));
    }

    /**
     * 用户提交预约申请 (前台)
     * 
     * 修复: BUG-001/002/003/005/006
     */
    @Log(title = "用户预约申请", businessType = BusinessType.INSERT)
    @PostMapping("/insert")
    public AjaxResult insertReservation(@RequestBody Reservation reservation) {
        // BUG-002: timeSlot 范围校验 (1-4)
        if (reservation.getTimeSlot() == null
                || reservation.getTimeSlot() < 1
                || reservation.getTimeSlot() > 4) {
            return error("时间段无效，必须在 1-4 之间 (1:8:00-10:00, 2:10:00-12:00, 3:14:30-16:30, 4:16:30-18:30)");
        }

        // BUG-003: 不能预约过去日期
        if (reservation.getReservationDate() != null
                && reservation.getReservationDate().before(new java.util.Date())) {
            return error("不能预约过去的日期，请选择今天或未来的日期");
        }

        // BUG-005: 实验室ID非空校验
        if (reservation.getLabId() == null) {
            return error("实验室ID不能为空");
        }

        // BUG-006: purpose 不能为空
        if (reservation.getPurpose() == null || reservation.getPurpose().trim().isEmpty()) {
            return error("预约用途不能为空");
        }

        // BUG-001: 冲突检测
        Reservation conflictQuery = new Reservation();
        conflictQuery.setLabId(reservation.getLabId());
        conflictQuery.setReservationDate(reservation.getReservationDate());
        conflictQuery.setTimeSlot(reservation.getTimeSlot());
        conflictQuery.setStatus(0L);

        List<Reservation> conflicts =
            reservationService.selectConflictingReservations(conflictQuery);
        if (conflicts != null && !conflicts.isEmpty()) {
            return error("该实验室在所选日期和时间段已被预约，请选择其他时间或实验室");
        }

        // 设置初始状态 (0:待审核)，防止前端传非法状态
        reservation.setStatus(0L);
        reservation.setCreatedAt(new java.util.Date());

        return toAjax(reservationService.insertReservationFromSchedule(reservation));
    }
}