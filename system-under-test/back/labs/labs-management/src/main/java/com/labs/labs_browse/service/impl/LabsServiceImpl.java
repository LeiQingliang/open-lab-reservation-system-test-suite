package com.labs.labs_browse.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.labs.labs_browse.mapper.LabsMapper;
import com.labs.labs_browse.domain.Labs;
import com.labs.labs_browse.service.ILabsService;

/**
 * 实验室信息管理1Service业务层处理
 * 
 * @author santh
 * @date 2025-04-10
 */
@Service
public class LabsServiceImpl implements ILabsService 
{
    @Autowired
    private LabsMapper labsMapper;

    /**
     * 实验室信息管理
     * 
     * @param id 实验室信息主键
     * @return 实验室信息
     */
    @Override
    public Labs selectLabsById(Long id)
    {
        return labsMapper.selectLabsById(id);
    }

    /**
     * 实验室信息列表
     * 
     * @param labs 实验室信息
     * @return 实验室信息
     */
    @Override
    public List<Labs> selectLabsList(Labs labs)
    {
        return labsMapper.selectLabsList(labs);
    }

    /**
     * 新增实验室
     * 
     * @param labs 实验室信息
     * @return 结果
     */
    @Override
    public int insertLabs(Labs labs)
    {
        return labsMapper.insertLabs(labs);
    }

    /**
     * 修改实验室信息
     * 
     * @param labs 实验室信息管理
     * @return 结果
     */
    @Override
    public int updateLabs(Labs labs)
    {
        return labsMapper.updateLabs(labs);
    }

    /**
     * 批量删除实验室
     * 
     * @param ids 需要删除的实验室信息主键
     * @return 结果
     */
    @Override
    public int deleteLabsByIds(Long[] ids)
    {
        return labsMapper.deleteLabsByIds(ids);
    }

    /**
     * 删除实验室信息
     * 
     * @param id 实验室主键
     * @return 结果
     */
    @Override
    public int deleteLabsById(Long id)
    {
        return labsMapper.deleteLabsById(id);
    }
}
