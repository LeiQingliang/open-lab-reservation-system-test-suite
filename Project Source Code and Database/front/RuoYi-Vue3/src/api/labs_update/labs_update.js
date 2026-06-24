import request from '@/utils/request'

// 查询维护实验室信息列表
export function listLabs_update(query) {
  return request({
    url: '/labs_browse/labs_browse/list',
    method: 'get',
    params: query
  })
}

// 查询维护实验室信息详细
export function getLabs_update(id) {
  return request({
    url: '/labs_browse/labs_browse/' + id,
    method: 'get'
  })
}

// 新增维护实验室信息
export function addLabs_update(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'post',
    data: data
  })
}

// 修改维护实验室信息
export function updateLabs_update(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'put',
    data: data
  })
}

// 删除维护实验室信息
export function delLabs_update(id) {
  return request({
    url: '/labs_browse/labs_browse/' + id,
    method: 'delete'
  })
}
