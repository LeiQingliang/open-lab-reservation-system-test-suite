package com.labs.apply.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labs.common.annotation.Excel;
import com.labs.common.core.domain.BaseEntity;

/**
 * 预约实验室对象 lab_schedule
 * 
 * @author santh
 * @date 2025-04-20
 */
public class LabSchedule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 排班记录ID */
    @Excel(name = "排班记录ID")
    private Long id;

    /** 实验室ID */
    @Excel(name = "实验室ID")
    private Long labId;

    /** 日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date date;

    /** 预约时间段编号（1: 8-10, 2: 10-12, 3: 14:30-16:30, 4: 16:30-18:30） */
    @Excel(name = "预约时间段编号", readConverterExp = "1=:,8=-10,,2=:,1=0-12,,3=:,1=4:30-16:30,,4=:,1=6:30-18:30")
    private Long timeSlot;

    /** 实验室状态 */
    @Excel(name = "实验室状态")
    private Integer isAvailable;

    /** 备注 */
    @Excel(name = "备注")
    private String note;



    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLabId(Long labId) 
    {
        this.labId = labId;
    }

    public Long getLabId() 
    {
        return labId;
    }

    public void setDate(Date date) 
    {
        this.date = date;
    }

    public Date getDate() 
    {
        return date;
    }

    public void setTimeSlot(Long timeSlot) 
    {
        this.timeSlot = timeSlot;
    }

    public Long getTimeSlot() 
    {
        return timeSlot;
    }

    public void setIsAvailable(Integer isAvailable) 
    {
        this.isAvailable = isAvailable;
    }

    public Integer getIsAvailable() 
    {
        return isAvailable;
    }

    public void setNote(String note) 
    {
        this.note = note;
    }

    public String getNote() 
    {
        return note;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("labId", getLabId())
            .append("date", getDate())
            .append("timeSlot", getTimeSlot())
            .append("isAvailable", getIsAvailable())
            .append("note", getNote())
            .toString();
    }
}
