package com.labs.apply.mapper;

import java.time.LocalDate;
import java.util.List;
import com.labs.apply.domain.LabSchedule;
import com.labs.reservation.domain.Reservation;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.Date;
/**
 * 预约实验室Mapper接口
 * 
 * @author santh
 * @date 2025-04-20
 */
public interface LabScheduleMapper 
{
    /**
     * 查询预约实验室
     * 
     * @param id 预约实验室主键
     * @return 预约实验室
     */
    public LabSchedule selectLabScheduleById(Long id);

    List<LabSchedule> selectLabSchedulesByDate(@Param("date") LocalDate date);


    /**
     * 查询预约实验室列表
     * 
     * @param labSchedule 预约实验室
     * @return 预约实验室集合
     */
    public List<LabSchedule> selectLabScheduleList(LabSchedule labSchedule);

    /**
     * 新增预约实验室
     * 
     * @param labSchedule 预约实验室
     * @return 结果
     */
    public int insertLabSchedule(LabSchedule labSchedule);

    /**
     * 修改预约实验室
     * 
     * @param labSchedule 预约实验室
     * @return 结果
     */
    public int updateLabSchedule(LabSchedule labSchedule);

    /**
     * 删除预约实验室
     * 
     * @param id 预约实验室主键
     * @return 结果
     */
    public int deleteLabScheduleById(Long id);

    /**
     * 批量删除预约实验室
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabScheduleByIds(Long[] ids);

    /**
     * |删除冲突预约申请  暂时不用
     */
    void deleteByReservation(@Param("reservation") Reservation reservation);


    /**
     * 查询该日期可约时段
     * @param labId 实验室id
     * @param date 日期
     * @return 可约时段
     */
    List<Integer> selectTimeSlotsByLabAndDate(Long labId, LocalDate date);

    /**
     * 发布可约时段
     * @param date 日期
     * @param timeSlots 时段
     * @param labIds 发布实验室
     */
    void batchInsertLabSchedules(@Param("date") LocalDate date,
                                 @Param("timeSlots") List<Integer> timeSlots,
                                 @Param("labIds") List<Long> labIds);
}
