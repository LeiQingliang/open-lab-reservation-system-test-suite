import request from '@/utils/request'

// 查询预约记录列表
export function listReservation_records(query) {
  return request({
    url: '/reservation/reservation/list',
    method: 'get',
    params: query
  })
}

// 查询预约记录详细
export function getReservation_records(id) {
  return request({
    url: '/reservation/reservation/list/' + id,
    method: 'get'
  })
}

// 新增预约记录
export function addReservation_records(data) {
  return request({
    url: '/reservation/reservation/list',
    method: 'post',
    data: data
  })
}

// 修改预约记录
export function updateReservation_records(data) {
  return request({
    url: '/reservation/reservation/list',
    method: 'put',
    data: data
  })
}

// 删除预约记录
export function delReservation_records(id) {
  return request({
    url: '/reservation/reservation/list' + id,
    method: 'delete'
  })
}
