<template>
  <div class="app-container home">
    <!-- 紧凑型时间日期显示 -->
    <el-row :gutter="20" class="datetime-row">
      <el-col :span="24">
        <div class="compact-datetime-display">
          <div class="time-section">
            <div class="time">{{ currentTime }}</div>
          </div>
          <div class="date-section">
            <span class="date">{{ formattedDate }}</span>
            <span class="weekday">{{ weekday }}</span>
            <span class="week-number">第{{ weekNumber }}周</span>
          </div>
          <div class="holiday-section">
            <span class="holiday-text">距离{{ nextHoliday.name }}还有</span>
            <span class="holiday-days">{{ nextHoliday.days }}天</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧列 - 实验室状态 (70%) -->
      <el-col :span="17">
        <el-card class="lab-status">
          <template #header>
            <div class="clearfix">
              <span>实验室状态</span>
            </div>
          </template>
          <el-table :data="labStatus" style="width: 100%">
            <el-table-column prop="labName" label="实验室名称" width="180" />
            <el-table-column label="8:00-10:00" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.timeSlots[0] === '可预约' ? 'success' : 'warning'">
                  {{ scope.row.timeSlots[0] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="10:00-12:00" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.timeSlots[1] === '可预约' ? 'success' : 'warning'">
                  {{ scope.row.timeSlots[1] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="14:30-16:30" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.timeSlots[2] === '可预约' ? 'success' : 'warning'">
                  {{ scope.row.timeSlots[2] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="16:30-18:30" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.timeSlots[3] === '可预约' ? 'success' : 'warning'">
                  {{ scope.row.timeSlots[3] }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧列 - 通知公告 (30%) -->
      <el-col :span="7">
        <el-card class="announcements">
          <template #header>
            <div class="clearfix">
              <span>通知公告</span>
            </div>
          </template>
          <el-scrollbar height="400px">
            <el-timeline>
              <el-timeline-item v-for="(announcement, index) in announcements" :key="index"
                :timestamp="announcement.time" placement="top">
                <el-card shadow="hover">
                  <h4>{{ announcement.title }}</h4>
                  <p>{{ announcement.content }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// 当前时间和日期
const currentTime = ref('')
const currentDate = ref(new Date())
const currentSeconds = computed(() => new Date().getSeconds())
const weekdayMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

// 节假日数据（示例数据，可按需修改）
const holidays = ref([
  { name: '元旦', date: '2024-01-01' },
  { name: '春节', date: '2024-02-10' },
  { name: '清明节', date: '2024-04-04' },
  { name: '劳动节', date: '2024-05-01' },
  { name: '端午节', date: '2024-06-10' },
  { name: '中秋节', date: '2024-09-17' },
  { name: '国庆节', date: '2024-10-01' }
])

// 计算下一个节假日
const nextHoliday = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // 找到下一个节假日
  const upcoming = holidays.value.map(holiday => {
    const holidayDate = new Date(holiday.date)
    const timeDiff = holidayDate - today
    const daysDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24))
    return { ...holiday, days: daysDiff }
  }).filter(h => h.days >= 0)
    .sort((a, b) => a.days - b.days)[0]

  // 如果没有找到（所有节日都已过去），则显示元旦
  return upcoming || {
    name: '元旦',
    days: Math.ceil((new Date(today.getFullYear() + 1, 0, 1) - today) / (1000 * 60 * 60 * 24))
  }
})

// 计算周数
const weekNumber = computed(() => {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  date.setDate(date.getDate() + 3 - (date.getDay() + 6) % 7)
  const week1 = new Date(date.getFullYear(), 0, 4)
  return 1 + Math.round(((date - week1) / 86400000 - 3 + (week1.getDay() + 6) % 7) / 7)
})

// 格式化日期
const formattedDate = computed(() => {
  return currentDate.value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
})

// 获取当前星期
const weekday = computed(() => {
  return weekdayMap[currentDate.value.getDay()]
})

// 更新时间函数
const updateTime = () => {
  const now = new Date()
  currentDate.value = now
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 组件挂载时启动定时器
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

// 组件卸载前清除定时器
let timer = null
onBeforeUnmount(() => {
  clearInterval(timer)
})

// 实验室状态数据
const labStatus = ref([
  {
    labName: '人工智能实验室',
    timeSlots: ['可预约', '已预约', '可预约', '已预约']
  },
  {
    labName: '量子计算实验室',
    timeSlots: ['已预约', '已预约', '可预约', '可预约']
  },
  {
    labName: '生物化学实验室',
    timeSlots: ['可预约', '可预约', '可预约', '已预约']
  },
  {
    labName: '材料科学实验室',
    timeSlots: ['已预约', '已预约', '可预约', '可预约']
  },
  {
    labName: '环境工程实验室',
    timeSlots: ['可预约', '可预约', '可预约', '可预约']
  },
  {
    labName: '机械工程实验室',
    timeSlots: ['已预约', '已预约', '已预约', '已预约']
  }
])

// 通知公告数据
const announcements = ref([
  {
    time: '2023-11-15',
    title: '实验室安全培训通知',
    content: '本周五下午2点在201会议室举行实验室安全培训，所有实验室负责人必须参加。'
  },
  {
    time: '2023-11-10',
    title: '新设备使用说明',
    content: '量子计算实验室新增设备已安装完毕，使用前请阅读操作手册并预约培训。'
  },
  {
    time: '2023-11-05',
    title: '寒假实验室开放安排',
    content: '寒假期间实验室开放时间为每周一至周五9:00-17:00，需提前一天预约。'
  },
  {
    time: '2023-10-28',
    title: '实验室管理系统升级',
    content: '系统将于本周六凌晨0:00-6:00进行升级维护，期间无法进行预约操作。'
  }
])
</script>

<style scoped lang="scss">
.home {

  /* 紧凑型时间日期显示 */
  .compact-datetime-display {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: #f8fafc;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    border: 1px solid #ebeef5;

    /* 时间区域 */
    .time-section {
      display: flex;
      align-items: center;

      .time {
        font-size: 28px;
        font-weight: bold;
        color: #409EFF;
        margin-right: 10px;
        min-width: 100px;
      }

      /* 秒数指示器（3个点） */
      .seconds-indicator {
        display: flex;
        gap: 6px;

        span {
          display: inline-block;
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: #ddd;
          transition: all 0.3s;

          &.active {
            background: #409EFF;
            transform: scale(1.3);
          }
        }
      }
    }

    /* 日期区域 */
    .date-section {
      display: flex;
      align-items: center;
      font-size: 14px;
      color: #666;

      .date {
        margin-right: 8px;
      }

      .weekday {
        margin-right: 12px;
        color: #409EFF;
        font-weight: 500;
      }

      .week-number {
        background: rgba(64, 158, 255, 0.1);
        padding: 2px 8px;
        border-radius: 10px;
      }
    }

    /* 节假日区域 */
    .holiday-section {
      display: flex;
      align-items: center;
      font-size: 14px;

      .holiday-text {
        color: #666;
        margin-right: 6px;
      }

      .holiday-days {
        color: #F56C6C;
        font-weight: bold;
        background: rgba(245, 108, 108, 0.1);
        padding: 2px 8px;
        border-radius: 10px;
      }
    }
  }

  /* 实验室状态卡片 */
  .lab-status {
    margin-bottom: 20px;

    :deep(.el-card__header) {
      background: #f5f7fa;
      font-weight: bold;
      font-size: 16px;
    }

    .el-tag {
      font-size: 13px;
      padding: 0 8px;
      height: 24px;
      line-height: 24px;
    }
  }

  /* 通知公告卡片 */
  .announcements {
    margin-bottom: 20px;
    height: 100%;

    :deep(.el-card__header) {
      background: #f5f7fa;
      font-weight: bold;
      font-size: 16px;
    }

    :deep(.el-timeline) {
      padding-left: 10px;
    }

    h4 {
      margin-top: 0;
      font-size: 14px;
    }

    p {
      margin-bottom: 0;
      color: #606266;
      font-size: 12px;
    }
  }
}
</style>