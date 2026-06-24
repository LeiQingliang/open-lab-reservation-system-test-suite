import request from '@/utils/request'

// 发布实验室可预约时段
export function publishLabTimeSlots(data) {
  return request({
    url: '/apply/apply/publish',
    method: 'post',
    data: data
  })
}

// 获取今天的排班表
export function getLabSchedules() {
  return request({
    url: '/apply/apply/gettoday',
    method: 'get',
    data: data
  })
}