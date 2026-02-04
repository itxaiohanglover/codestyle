<template>
  <a-card class="general-card" title="技术栈分布">
    <template #extra>
      <a-link>详情</a-link>
    </template>
    <Chart :option="chartOption" style="height: 280px" />
  </a-card>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { useChart } from '@/hooks'
import Chart from '@/components/Chart/index.vue'

use([PieChart, LegendComponent, TooltipComponent, CanvasRenderer])

const { chartOption } = useChart((isDark): EChartsOption => {
  return {
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      itemWidth: 8,
      itemHeight: 8,
      icon: 'circle',
      textStyle: {
        color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969'
      }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      label: { show: false },
      itemStyle: {
        borderColor: isDark ? '#232324' : '#fff',
        borderWidth: 1
      },
      data: [
        { value: 40, name: 'Java', itemStyle: { color: '#165DFF' } },
        { value: 25, name: 'Vue', itemStyle: { color: '#14C9C9' } },
        { value: 18, name: 'TypeScript', itemStyle: { color: '#722ED1' } },
        { value: 10, name: 'Python', itemStyle: { color: '#F7BA1E' } },
        { value: 7, name: '其他', itemStyle: { color: '#86909c' } }
      ]
    }]
  }
})
</script>

<style scoped lang="scss">
</style>
