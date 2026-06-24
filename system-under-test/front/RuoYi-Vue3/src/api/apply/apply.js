import request from '@/utils/request'

// 查询预约实验室列表
export function listApply(query) {
  return request({
    url: '/apply/apply/list',
    method: 'get',
    params: query
  })
}

// 查询预约实验室详细
export function getApply(id) {
  return request({
    url: '/apply/apply/' + id,
    method: 'get'
  })
}

// 新增预约实验室
export function addApply(data) {
  return request({
    url: '/apply/apply',
    method: 'post',
    data: data
  })
}

// 修改预约实验室
export function updateApply(data) {
  return request({
    url: '/apply/apply',
    method: 'put',
    data: data
  })
}

// 删除预约实验室
export function delApply(id) {
  return request({
    url: '/apply/apply/' + id,
    method: 'delete'
  })
}
