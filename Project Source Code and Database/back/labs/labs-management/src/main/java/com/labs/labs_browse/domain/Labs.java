package com.labs.labs_browse.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.labs.common.annotation.Excel;
import com.labs.common.core.domain.BaseEntity;

/**
 * 实验室资源浏览1对象 labs
 * 
 * @author santh
 * @date 2025-04-10
 */
public class Labs extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 实验室ID */
    private Long id;

    /** 实验室名称，如“软件工程实验室” */
    @Excel(name = "实验室名称，如“软件工程实验室”")
    private String labName;

    /** 实验室地址，如“教学楼A305” */
    @Excel(name = "实验室地址，如“教学楼A305”")
    private String location;

    /** 实验室可容纳人数 */
    @Excel(name = "实验室可容纳人数")
    private Long capacity;

    /** 实验室简介或备注 */
    @Excel(name = "实验室简介或备注")
    private String description;

    @Excel(name = "状态")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 实验室信息创建时间 */
    private Date createdAt;

    /** 实验室信息最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实验室信息最后更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setLabName(String labName) 
    {
        this.labName = labName;
    }

    public String getLabName() 
    {
        return labName;
    }
    public void setLocation(String location) 
    {
        this.location = location;
    }

    public String getLocation() 
    {
        return location;
    }
    public void setCapacity(Long capacity) 
    {
        this.capacity = capacity;
    }

    public Long getCapacity() 
    {
        return capacity;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }
    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("labName", getLabName())
            .append("location", getLocation())
            .append("capacity", getCapacity())
            .append("description", getDescription())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
