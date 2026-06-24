package com.labs.apply.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.labs.apply.dto.PublishScheduleRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.labs.common.annotation.Log;
import com.labs.common.core.controller.BaseController;
import com.labs.common.core.domain.AjaxResult;
import com.labs.common.enums.BusinessType;
import com.labs.apply.domain.LabSchedule;
import com.labs.apply.service.ILabScheduleService;
import com.labs.common.utils.poi.ExcelUtil;
import com.labs.common.core.page.TableDataInfo;

/**
 * 预约实验室Controller
 * 
 * @author santh
 * @date 2025-04-20
 */
@RestController
@RequestMapping("/apply/apply")
public class LabScheduleController extends BaseController
{
    @Autowired
    private ILabScheduleService labScheduleService;

    /**
     * 查询预约实验室列表
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:list')")
    @GetMapping("/list")
    public TableDataInfo list(LabSchedule labSchedule)
    {
        startPage();
        List<LabSchedule> list = labScheduleService.selectLabScheduleList(labSchedule);
        return getDataTable(list);
    }

    /**
     * 导出预约实验室列表
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:export')")
    @Log(title = "预约实验室", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LabSchedule labSchedule)
    {
        List<LabSchedule> list = labScheduleService.selectLabScheduleList(labSchedule);
        ExcelUtil<LabSchedule> util = new ExcelUtil<LabSchedule>(LabSchedule.class);
        util.exportExcel(response, list, "预约实验室数据");
    }

    /**
     * 获取预约实验室详细信息
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(labScheduleService.selectLabScheduleById(id));
    }

    /**
     * 获取今天的预约实验室记录
     */
//    @PreAuthorize("@ss.hasPermi('apply:apply:list')")
    @GetMapping("/gettoday")
    public AjaxResult getTodayLabSchedules() {
        LocalDate today = LocalDate.now();
        List<LabSchedule> todaySchedules = labScheduleService.selectLabSchedulesByDate(today);
        return success(todaySchedules);
    }

    /**
     * 新增预约实验室
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:add')")
    @Log(title = "预约实验室", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LabSchedule labSchedule)
    {
        return toAjax(labScheduleService.insertLabSchedule(labSchedule));
    }

    /**
     * 修改预约实验室
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:edit')")
    @Log(title = "预约实验室", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LabSchedule labSchedule)
    {
        return toAjax(labScheduleService.updateLabSchedule(labSchedule));
    }

    /**
     * 删除预约实验室
     */
    @PreAuthorize("@ss.hasPermi('apply:apply:remove')")
    @Log(title = "预约实验室", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(labScheduleService.deleteLabScheduleByIds(ids));
    }


    /**
     * 发布实验室可预约时段
     */
    /**
     * 发布实验室可预约时段
     */
    @Log(title = "发布可预约时段", businessType = BusinessType.OTHER)
    @PostMapping("/publish")
    public AjaxResult publishAvailableSlots(@Valid @RequestBody PublishScheduleRequest request) {

        Date date = Date.from(
                request.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
        labScheduleService.publishAvailableSlots(
                date,
                request.getTimeSlots(),
                request.getLabIds()
        );
        return AjaxResult.success("发布成功");
    }

}
