package com.labs.apply.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.labs.common.exception.ServiceException;
import com.labs.common.utils.SecurityUtils;
import com.labs.reservation.domain.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labs.apply.mapper.LabScheduleMapper;
import com.labs.apply.domain.LabSchedule;
import com.labs.apply.service.ILabScheduleService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约实验室Service业务层处理
 * 
 * @author santh
 * @date 2025-04-20
 */
@Service
public class LabScheduleServiceImpl implements ILabScheduleService {
    @Autowired
    private LabScheduleMapper labScheduleMapper;

    /**
     * 查询预约实验室
     *
     * @param id 预约实验室主键
     * @return 预约实验室
     */
    @Override
    public LabSchedule selectLabScheduleById(Long id) {
        return labScheduleMapper.selectLabScheduleById(id);
    }

    /**
     * 查询预约实验室列表
     *
     * @param labSchedule 预约实验室
     * @return 预约实验室
     */
    @Override
    public List<LabSchedule> selectLabScheduleList(LabSchedule labSchedule) {
        return labScheduleMapper.selectLabScheduleList(labSchedule);
    }

    /**
     * 新增预约实验室
     *
     * @param labSchedule 预约实验室
     * @return 结果
     */
    @Override
    public int insertLabSchedule(LabSchedule labSchedule) {
        return labScheduleMapper.insertLabSchedule(labSchedule);
    }

    /**
     * 修改预约实验室
     *
     * @param labSchedule 预约实验室
     * @return 结果
     */
    @Override
    public int updateLabSchedule(LabSchedule labSchedule) {
        return labScheduleMapper.updateLabSchedule(labSchedule);
    }

    @Override
    public List<LabSchedule> selectLabSchedulesByDate(LocalDate date) {
        return labScheduleMapper.selectLabSchedulesByDate(date);
    }


    /**
     * 批量删除预约实验室
     *
     * @param ids 需要删除的预约实验室主键
     * @return 结果
     */
    @Override
    public int deleteLabScheduleByIds(Long[] ids) {
        return labScheduleMapper.deleteLabScheduleByIds(ids);
    }

    /**
     * 删除预约实验室信息
     *
     * @param id 预约实验室主键
     * @return 结果
     */
    @Override
    public int deleteLabScheduleById(Long id) {
        return labScheduleMapper.deleteLabScheduleById(id);
    }

    @Override
    public void deleteByReservation(Reservation reservation) {
        labScheduleMapper.deleteByReservation(reservation);
    }

    @Override
    @Transactional
    public void publishAvailableSlots(Date date, List<Integer> timeSlots, List<Long> labIds) {
        // 参数校验
        if (date == null || timeSlots == null || labIds == null) {
            throw new ServiceException("参数不能为空");
        }

        // 插入数据逻辑
        String username = SecurityUtils.getUsername();
        LocalDateTime now = LocalDateTime.now();

        for (Long labId : labIds) {
            for (Integer timeSlot : timeSlots) {
                LabSchedule schedule = new LabSchedule();
                schedule.setLabId(labId);       // 对应 private Long labId;
                schedule.setDate(date);         // 对应 private Date date;
                schedule.setTimeSlot(timeSlot.longValue()); // 对应 private Long timeSlot;
                schedule.setIsAvailable(1);
                labScheduleMapper.insertLabSchedule(schedule);
            }
        }
    }
}
