import request from '@/utils/request'

// 查询审核预约列表
export function listReservation_review(query) {
  return request({
    url: '/reservation/reservation/list',
    method: 'get',
    params: query
  })
}

// 查询审核预约详细
export function getReservation_review(id) {
  return request({
    url: '/reservation/reservation/' + id,
    method: 'get'
  })
}

// 新增审核预约
export function addReservation_review(data) {
  return request({
    url: '/reservation/reservation',
    method: 'post',
    data: data
  })
}

// 修改审核预约
export function updateReservation_review(data) {
  return request({
    url: '/reservation/reservation',
    method: 'put',
    data: data
  })
}

// 删除审核预约
export function delReservation_review(id) {
  return request({
    url: '/reservation/reservation/' + id,
    method: 'delete'
  })
}
