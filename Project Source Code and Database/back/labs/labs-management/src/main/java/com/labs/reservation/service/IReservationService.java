package com.labs.reservation.service;

import java.util.List;

import com.labs.common.core.domain.AjaxResult;
import com.labs.reservation.domain.Reservation;

/**
 * 实验室使用记录Service接口
 * 
 * @author santh
 * @date 2025-04-10
 */
public interface IReservationService 
{
    /**
     * 查询实验室使用记录
     * 
     * @param id 实验室使用记录主键
     * @return 实验室使用记录
     */
    public Reservation selectReservationById(Long id);

    /**
     * 查询实验室使用记录列表
     * 
     * @param reservation 实验室使用记录
     * @return 实验室使用记录集合
     */
    public List<Reservation> selectReservationList(Reservation reservation);

    /**
     * 新增实验室使用记录
     * 
     * @param reservation 实验室使用记录
     * @return 结果
     */
    public int insertReservation(Reservation reservation);

    /**
     * 修改实验室使用记录
     * 
     * @param reservation 实验室使用记录
     * @return 结果
     */
    public int updateReservation(Reservation reservation);

    /**
     * 批量删除实验室使用记录
     * 
     * @param ids 需要删除的实验室使用记录主键集合
     * @return 结果
     */
    public int deleteReservationByIds(Long[] ids);

    /**
     * 删除实验室使用记录信息
     * 
     * @param id 实验室使用记录主键
     * @return 结果
     */
    public int deleteReservationById(Long id);

    /**
     * 审核预约记录（仅更新状态和审核意见）
     *
     * @param reservation 审核信息
     * @return AjaxResult
     */
    int reviewReservation(Reservation reservation);

    /**
     * 提交审核
     * @param reservation 审核信息
     * @return Ajax
     */
    int insertReservationFromSchedule(Reservation reservation);


    /**
     * 查询冲突的预约
     */
    List<Reservation> selectConflictingReservations(Reservation reservation);


}
