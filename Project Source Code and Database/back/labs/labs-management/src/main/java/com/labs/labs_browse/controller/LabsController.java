package com.labs.labs_browse.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.labs.common.annotation.Anonymous;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.labs.common.annotation.Log;
import com.labs.common.core.controller.BaseController;
import com.labs.common.core.domain.AjaxResult;
import com.labs.common.enums.BusinessType;
import com.labs.labs_browse.domain.Labs;
import com.labs.labs_browse.service.ILabsService;
import com.labs.common.utils.poi.ExcelUtil;
import com.labs.common.core.page.TableDataInfo;

/**
 * 实验室信息Controller
 * 
 * @author santh
 * @date 2025-04-10
 */
@RestController
@RequestMapping("/labs_browse/labs_browse")
public class LabsController extends BaseController
{
    @Autowired
    private ILabsService labsService;

    /**
     * 查询实验室列表
     */
//    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:list')")
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(Labs labs)
    {
        startPage();
        List<Labs> list = labsService.selectLabsList(labs);
        return getDataTable(list);
    }

    /**
     * 导出实验室列表
     */
    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:export')")
    @Log(title = "实验室信息管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Labs labs)
    {
        List<Labs> list = labsService.selectLabsList(labs);
        ExcelUtil<Labs> util = new ExcelUtil<Labs>(Labs.class);
        util.exportExcel(response, list, "实验室资源浏览1数据");
    }

    /**
     * 获取实验室详细信息
     */
    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(labsService.selectLabsById(id));
    }

    /**
     * 新增实验室
     */
    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:add')")
    @Log(title = "实验室信息管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Labs labs)
    {
        return toAjax(labsService.insertLabs(labs));
    }

    /**
     * 修改实验室
     */
    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:edit')")
    @Log(title = "实验室信息管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Labs labs)
    {
        return toAjax(labsService.updateLabs(labs));
    }

    /**
     * 删除实验室
     */
    @PreAuthorize("@ss.hasPermi('labs_browse:labs_browse:remove')")
    @Log(title = "实验室信息管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(labsService.deleteLabsByIds(ids));
    }
}
