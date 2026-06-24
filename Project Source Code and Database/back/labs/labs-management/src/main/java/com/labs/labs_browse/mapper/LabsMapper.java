package com.labs.labs_browse.mapper;

import java.util.List;
import com.labs.labs_browse.domain.Labs;

/**
 * 实验室资源浏览1Mapper接口
 * 
 * @author santh
 * @date 2025-04-10
 */
public interface LabsMapper 
{
    /**
     * 查询实验室资源浏览1
     * 
     * @param id 实验室资源浏览1主键
     * @return 实验室资源浏览1
     */
    public Labs selectLabsById(Long id);

    /**
     * 查询实验室资源浏览1列表
     * 
     * @param labs 实验室资源浏览1
     * @return 实验室资源浏览1集合
     */
    public List<Labs> selectLabsList(Labs labs);

    /**
     * 新增实验室资源浏览1
     * 
     * @param labs 实验室资源浏览1
     * @return 结果
     */
    public int insertLabs(Labs labs);

    /**
     * 修改实验室资源浏览1
     * 
     * @param labs 实验室资源浏览1
     * @return 结果
     */
    public int updateLabs(Labs labs);

    /**
     * 删除实验室资源浏览1
     * 
     * @param id 实验室资源浏览1主键
     * @return 结果
     */
    public int deleteLabsById(Long id);

    /**
     * 批量删除实验室资源浏览1
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLabsByIds(Long[] ids);
}
