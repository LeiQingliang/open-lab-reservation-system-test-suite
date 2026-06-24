package com.labs.reservation.domain;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labs.common.annotation.Excel;
import com.labs.common.core.domain.BaseEntity;
import org.springframework.data.annotation.Transient;

/**
 * 实验室使用记录对象 reservation
 * 
 * @author santh
 * @date 2025-04-10
 */
public class Reservation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预约记录ID */
    @Excel(name = "预约记录ID")
    private Long id;

    /** 申请人ID，外键关联users表 */
    @Excel(name = "申请人ID，外键关联users表")
    private Long userId;

    /** 实验室ID，外键关联lab表 */
    @Excel(name = "实验室ID，外键关联lab表")
    private Long labId;

    /** 预约日期（年月日） */
    @Excel(name = "预约日期", readConverterExp = "年=月日")
    private Date reservationDate;

    /** 预约时间段编号（1: 8-10, 2: 10-12, 3: 14:30-16:30, 4: 16:30-18:30） */
    @Excel(name = "预约时间段编号", readConverterExp = "1=:,8=-10,,2=:,1=0-12,,3=:,1=4:30-16:30,,4=:,1=6:30-18:30")
    private Long timeSlot;

    /** 预约用途说明 */
    @Excel(name = "预约用途说明")
    private String purpose;

    /** 预约状态（0:待审核, 1:已通过, 2:已拒绝） */
    @Excel(name = "预约状态", readConverterExp = "0=:待审核,,1=:已通过,,2=:已拒绝")
    private Long status;

    /** 审核教师ID（users表，角色为教师） */
    @Excel(name = "审核教师ID", readConverterExp = "u=sers表，角色为教师")
    private Long teacherId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reviewTime;

    /** 审核意见/备注 */
    @Excel(name = "审核意见/备注")
    private String reviewNote;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    //多表查询
    @Excel(name = "申请人")
    private String nickName;

    @Excel(name = "实验室")
    private String labName;

    private String teacherNickName;

    // 替换原有的beginDate/endDate字段
    @TableField(exist = false)
    private String beginReservationDate;  // 与前端参数beginReservationDate对应

    @TableField(exist = false)
    private String endReservationDate;    // 与前端参数endReservationDate对应

    @Transient // 表示不持久化到数据库
    private Boolean autoRejectOthers;

    @Transient
    private List<Long> ids; // 用于批量操作的ID列表


    public Boolean getAutoRejectOthers() {
        return autoRejectOthers;
    }

    public void setAutoRejectOthers(Boolean autoRejectOthers) {
        this.autoRejectOthers = autoRejectOthers;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    // 修改getter/setter方法
    public String getBeginReservationDate() {
        return beginReservationDate;
    }

    public void setBeginReservationDate(String beginReservationDate) {
        this.beginReservationDate = beginReservationDate;
    }

    public String getEndReservationDate() {
        return endReservationDate;
    }

    public void setEndReservationDate(String endReservationDate) {
        this.endReservationDate = endReservationDate;
    }

    public String getTeacherNickName() {
        return teacherNickName;
    }

    public void setTeacherNickName(String teacherNickName) {
        this.teacherNickName = teacherNickName;
    }


    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setLabId(Long labId) 
    {
        this.labId = labId;
    }

    public Long getLabId() 
    {
        return labId;
    }
    public void setReservationDate(Date reservationDate) 
    {
        this.reservationDate = reservationDate;
    }

    public Date getReservationDate() 
    {
        return reservationDate;
    }
    public void setTimeSlot(Long timeSlot) 
    {
        this.timeSlot = timeSlot;
    }

    public Long getTimeSlot() 
    {
        return timeSlot;
    }
    public void setPurpose(String purpose) 
    {
        this.purpose = purpose;
    }

    public String getPurpose() 
    {
        return purpose;
    }
    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }
    public void setTeacherId(Long teacherId) 
    {
        this.teacherId = teacherId;
    }

    public Long getTeacherId() 
    {
        return teacherId;
    }
    public void setReviewTime(Date reviewTime) 
    {
        this.reviewTime = reviewTime;
    }

    public Date getReviewTime() 
    {
        return reviewTime;
    }
    public void setReviewNote(String reviewNote) 
    {
        this.reviewNote = reviewNote;
    }

    public String getReviewNote() 
    {
        return reviewNote;
    }
    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("labId", getLabId())
            .append("reservationDate", getReservationDate())
            .append("timeSlot", getTimeSlot())
            .append("purpose", getPurpose())
            .append("status", getStatus())
            .append("teacherId", getTeacherId())
            .append("reviewTime", getReviewTime())
            .append("reviewNote", getReviewNote())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
