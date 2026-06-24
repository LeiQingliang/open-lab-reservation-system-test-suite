import request from '@/utils/request'

// 查询实验室使用记录列表
export function listReservation(query) {
  return request({
    url: '/reservation/reservation/list',
    method: 'get',
    params: query
  })
}

// 查询实验室使用记录详细
export function getReservation(id) {
  return request({
    url: '/reservation/reservation/' + id,
    method: 'get'
  })
}


// 审核预约记录（新增的接口）
export function reviewReservation(data) {
  return request({
    url: '/reservation/reservation/review',
    method: 'put',
    data: data
  })
}

export function insertReservation(data) {
  return request({
    url: '/reservation/reservation/insert',
    method: 'post',
    data: data
  });
}