import request from '@/utils/request'

// 查询实验室资源管理列表
export function listLabs_resource_manage(query) {
  return request({
    url: '/labs_browse/labs_browse/list',
    method: 'get',
    params: query
  })
}

// 查询实验室资源管理详细
export function getLabs_resource_manage(id) {
  return request({
    url: '/labs_browse/labs_browse/' + id,
    method: 'get'
  })
}

// 新增实验室资源管理
export function addLabs_resource_manage(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'post',
    data: data
  })
}

// 修改实验室资源管理
export function updateLabs_resource_manage(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'put',
    data: data
  })
}

// 删除实验室资源管理
export function delLabs_resource_manage(id) {
  return request({
    url: '/labs_browse/labs_browse/' + id,
    method: 'delete'
  })
}
