package com.labs.reservation.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.labs.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labs.reservation.mapper.ReservationMapper;
import com.labs.reservation.domain.Reservation;
import com.labs.reservation.service.IReservationService;

/**
 * 实验室使用记录Service业务层处理
 * 
 * @author santh
 * @date 2025-04-10
 */
@Service
public class ReservationServiceImpl implements IReservationService 
{
    @Autowired
    private ReservationMapper reservationMapper;

    /**
     * 查询实验室使用记录
     * 
     * @param id 实验室使用记录主键
     * @return 实验室使用记录
     */
    @Override
    public Reservation selectReservationById(Long id)
    {
        return reservationMapper.selectReservationById(id);
    }

    /**
     * 查询实验室使用记录列表
     * 
     * @param reservation 实验室使用记录
     * @return 实验室使用记录
     */
    @Override
    public List<Reservation> selectReservationList(Reservation reservation)
    {
        return reservationMapper.selectReservationList(reservation);
    }

    /**
     * 新增实验室使用记录
     * 
     * @param reservation 实验室使用记录
     * @return 结果
     */
    @Override
    public int insertReservation(Reservation reservation)
    {
        return reservationMapper.insertReservation(reservation);
    }

    /**
     * 修改实验室使用记录
     * 
     * @param reservation 实验室使用记录
     * @return 结果
     */
    @Override
    public int updateReservation(Reservation reservation)
    {
        return reservationMapper.updateReservation(reservation);
    }

    /**
     * 批量删除实验室使用记录
     * 
     * @param ids 需要删除的实验室使用记录主键
     * @return 结果
     */
    @Override
    public int deleteReservationByIds(Long[] ids)
    {
        return reservationMapper.deleteReservationByIds(ids);
    }

    /**
     * 删除实验室使用记录信息
     * 
     * @param id 实验室使用记录主键
     * @return 结果
     */
    @Override
    public int deleteReservationById(Long id)
    {
        return reservationMapper.deleteReservationById(id);
    }

    @Override
    public int reviewReservation(Reservation reservation) {
        reservation.setReviewTime(Timestamp.valueOf(LocalDateTime.now())); // 设置审核时间为当前
        reservationMapper.updateReviewStatus(reservation);
        return reservationMapper.reviewReservation(reservation);
    }

    @Override
    public int insertReservationFromSchedule(Reservation reservation) {
        return reservationMapper.insertReservationFromSchedule(reservation);
    }

    @Override
    public List<Reservation> selectConflictingReservations(Reservation reservation) {
        return reservationMapper.selectConflictingReservations(reservation);
    }


}
