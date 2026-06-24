package com.labs.reservation.mapper;

import java.util.List;
import com.labs.reservation.domain.Reservation;

/**
 * 实验室使用记录Mapper接口
 * 
 * @author santh
 * @date 2025-04-10
 */
public interface ReservationMapper 
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
     * 删除实验室使用记录
     * 
     * @param id 实验室使用记录主键
     * @return 结果
     */
    public int deleteReservationById(Long id);

    /**
     * 批量删除实验室使用记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReservationByIds(Long[] ids);

    /**
     *
     * 修改审核状态
     * @param reservation 审核参数
     * @return ajax
     */
    public int reviewReservation(Reservation reservation);


    /**
     * 提交审核
     * @param reservation 审核参数
     * @return Ajax
     */
    int insertReservationFromSchedule(Reservation reservation);

    /**
     * 教师审核预约（更新状态、教师 ID、审核时间）
     */
    void updateReviewStatus(Reservation reservation);

    List<Reservation> selectConflictingReservations(Reservation reservation);


}
