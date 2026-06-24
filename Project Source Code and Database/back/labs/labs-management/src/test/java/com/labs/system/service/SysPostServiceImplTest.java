package com.labs.system.service;

import com.labs.common.constant.UserConstants;
import com.labs.common.exception.ServiceException;
import com.labs.system.domain.SysPost;
import com.labs.system.mapper.SysPostMapper;
import com.labs.system.mapper.SysUserPostMapper;
import com.labs.system.service.impl.SysPostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 系统管理-岗位管理 Service 单元测试（系统模块代表性覆盖）。
 * <p>
 * 被测类：{@link SysPostServiceImpl}，使用 Mockito 隔离 {@link SysPostMapper}、{@link SysUserPostMapper}。
 * 重点覆盖：岗位名称/编码唯一性校验的三种分支，以及“岗位已被用户分配则禁止删除”的业务规则。
 * <p>
 * 开放式实验室网上预约管理系统 —— 软件测试综合训练课程设计
 * 测试人员：雷清亮    指导教师：刘嘉
 *
 * @author 雷清亮
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("M5 系统管理(岗位) - SysPostServiceImpl 单元测试")
class SysPostServiceImplTest {

    @Mock
    private SysPostMapper postMapper;

    @Mock
    private SysUserPostMapper userPostMapper;

    @InjectMocks
    private SysPostServiceImpl postService;

    private SysPost newPost(Long id, String name, String code) {
        SysPost p = new SysPost();
        p.setPostId(id);
        p.setPostName(name);
        p.setPostCode(code);
        return p;
    }

    @Test
    @DisplayName("UT-POST-01 根据ID查询岗位")
    void testSelectPostById() {
        SysPost post = newPost(1L, "项目经理", "pm");
        when(postMapper.selectPostById(1L)).thenReturn(post);

        assertEquals("项目经理", postService.selectPostById(1L).getPostName());
    }

    @Test
    @DisplayName("UT-POST-02 查询岗位列表")
    void testSelectPostList() {
        when(postMapper.selectPostList(any(SysPost.class)))
                .thenReturn(Arrays.asList(newPost(1L, "a", "a"), newPost(2L, "b", "b")));

        assertEquals(2, postService.selectPostList(new SysPost()).size());
    }

    @Test
    @DisplayName("UT-POST-03 岗位名称唯一性-数据库中不存在(唯一)")
    void testCheckPostNameUnique_unique() {
        SysPost post = newPost(null, "测试岗", "test");
        when(postMapper.checkPostNameUnique("测试岗")).thenReturn(null);

        assertEquals(UserConstants.UNIQUE, postService.checkPostNameUnique(post));
    }

    @Test
    @DisplayName("UT-POST-04 岗位名称唯一性-存在同名不同ID(不唯一)")
    void testCheckPostNameUnique_notUnique() {
        SysPost post = newPost(1L, "测试岗", "test");
        when(postMapper.checkPostNameUnique("测试岗")).thenReturn(newPost(2L, "测试岗", "test2"));

        assertEquals(UserConstants.NOT_UNIQUE, postService.checkPostNameUnique(post));
    }

    @Test
    @DisplayName("UT-POST-05 岗位名称唯一性-同名且同ID即自身(唯一)")
    void testCheckPostNameUnique_self() {
        SysPost post = newPost(1L, "测试岗", "test");
        when(postMapper.checkPostNameUnique("测试岗")).thenReturn(newPost(1L, "测试岗", "test"));

        assertEquals(UserConstants.UNIQUE, postService.checkPostNameUnique(post));
    }

    @Test
    @DisplayName("UT-POST-06 岗位编码唯一性-存在同码不同ID(不唯一)")
    void testCheckPostCodeUnique_notUnique() {
        SysPost post = newPost(1L, "测试岗", "test");
        when(postMapper.checkPostCodeUnique("test")).thenReturn(newPost(9L, "其他", "test"));

        assertEquals(UserConstants.NOT_UNIQUE, postService.checkPostCodeUnique(post));
    }

    @Test
    @DisplayName("UT-POST-07 删除岗位-已分配用户时禁止删除并抛异常")
    void testDeletePostByIds_assigned() {
        Long[] ids = {1L};
        when(postMapper.selectPostById(1L)).thenReturn(newPost(1L, "项目经理", "pm"));
        when(userPostMapper.countUserPostById(1L)).thenReturn(2);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> postService.deletePostByIds(ids));
        assertTrue(ex.getMessage().contains("已分配"));
        verify(postMapper, never()).deletePostByIds(any());
    }

    @Test
    @DisplayName("UT-POST-08 删除岗位-未被分配时删除成功")
    void testDeletePostByIds_success() {
        Long[] ids = {1L};
        when(postMapper.selectPostById(1L)).thenReturn(newPost(1L, "项目经理", "pm"));
        when(userPostMapper.countUserPostById(1L)).thenReturn(0);
        when(postMapper.deletePostByIds(ids)).thenReturn(1);

        assertEquals(1, postService.deletePostByIds(ids));
    }

    @Test
    @DisplayName("UT-POST-09 新增/修改岗位")
    void testInsertUpdate() {
        SysPost post = newPost(null, "测试岗", "test");
        when(postMapper.insertPost(post)).thenReturn(1);
        when(postMapper.updatePost(post)).thenReturn(1);

        assertEquals(1, postService.insertPost(post));
        assertEquals(1, postService.updatePost(post));
    }
}
