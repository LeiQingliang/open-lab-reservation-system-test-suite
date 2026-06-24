package com.labs.apply.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import com.labs.apply.domain.LabSchedule;
import com.labs.reservation.domain.Reservation;
import org.apache.ibatis.annotations.Param;

/**
 * 预约实验室Service接口
 * 
 * @author santh
 * @date 2025-04-20
 */
public interface ILabScheduleService 
{
    /**
     * 查询预约实验室
     * 
     * @param id 预约实验室主键
     * @return 预约实验室
     */
    public LabSchedule selectLabScheduleById(Long id);

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
     * 批量删除预约实验室
     * 
     * @param ids 需要删除的预约实验室主键集合
     * @return 结果
     */
    public int deleteLabScheduleByIds(Long[] ids);

    /**
     * 删除预约实验室信息
     * 
     * @param id 预约实验室主键
     * @return 结果
     */
    public int deleteLabScheduleById(Long id);

    /**
     * 删除可约记录
     */
    void deleteByReservation(Reservation reservation);

    List<LabSchedule> selectLabSchedulesByDate(LocalDate date);


    /**
     * 发布实验室可约时段
     * @param date 预约日期
     * @param timeSlots 时段列表 (1:8-10, 2:10-12, 3:14:30-16:30, 4:16:30-18:30)
     * @param labIds 实验室ID列表
     * @throws IllegalArgumentException 如果参数无效
     */
    void publishAvailableSlots(Date date, List<Integer> timeSlots, List<Long> labIds);
}
