import request from '@/utils/request'

// 查询实验室资源浏览1列表
export function listLabs_browse(query) {
  return request({
    url: '/labs_browse/labs_browse/list',
    method: 'get',
    params:query
  })
}

// 查询实验室资源浏览1详细
export function getLabs_browse(id) {
  return request({
    url: '/labs_browse/labs_browse/' + id,
    method: 'get'
  })
}

// 新增实验室资源浏览1
export function addLabs_browse(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'post',
    data: data
  })
}

// 修改实验室资源浏览1
export function updateLabs_browse(data) {
  return request({
    url: '/labs_browse/labs_browse',
    method: 'put',
    data: data
  })
}

// 删除实验室资源浏览1
export function delLabs_browse(id) {
  return request({
    url: '/labs/labs_browse/' + id,
    method: 'delete'
  })
}
