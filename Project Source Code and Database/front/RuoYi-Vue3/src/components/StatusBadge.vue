<template>
  <div 
    class="status-badge" 
    :class="status" 
    @click="$emit('click')">
    <span v-if="status === 'available'">可预约</span>
    <span v-else-if="status === 'booked'">已预约</span>
  </div>
</template>

<script setup>
defineProps({
  status: {
    type: String,
    required: true,
    validator: value => ['available', 'booked'].includes(value)
  },
  time: String
})

defineEmits(['click'])
</script>

<style scoped>
.status-badge {
  padding: 6px 10px;
  border-radius: 4px;
  text-align: center;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &.available {
    background-color: #f0f9eb;
    color: #67c23a;
    &:hover {
      background-color: #e1f3d8;
    }
  }
  
  &.booked {
    background-color: #fef0f0;
    color: #f56c6c;
  }
}
</style>