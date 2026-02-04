<template>
  <a-card class="general-card" title="调用趋势">
    <template #extra>
      <a-radio-group v-model="timeRange" type="button" size="small">
        <a-radio value="today">今日</a-radio>
        <a-radio value="week">本周</a-radio>
        <a-radio value="month">本月</a-radio>
      </a-radio-group>
    </template>
    <Chart :key="timeRange" :option="chartOption" style="height: 280px" />
  </a-card>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { useChart } from '@/hooks'
import Chart from '@/components/Chart/index.vue'

use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const timeRange = ref('today')

// 不同时间范围的数据配置
const chartDataMap = {
  today: {
    xAxis: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    codeGen: [120, 80, 150, 320, 450, 380, 200],
    knowledge: [80, 60, 120, 260, 350, 300, 150],
    mcp: [60, 40, 80, 180, 250, 200, 100]
  },
  week: {
    xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    codeGen: [856, 1024, 980, 1150, 1320, 680, 420],
    knowledge: [620, 780, 850, 920, 1050, 520, 380],
    mcp: [420, 560, 620, 680, 780, 350, 280]
  },
  month: {
    xAxis: ['第1周', '第2周', '第3周', '第4周'],
    codeGen: [5680, 6240, 7120, 6850],
    knowledge: [4120, 4680, 5240, 4920],
    mcp: [2860, 3240, 3680, 3420]
  }
}

const currentData = computed(() => chartDataMap[timeRange.value as keyof typeof chartDataMap])

const { chartOption } = useChart((isDark): EChartsOption => {
  return {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['代码生成', '知识库查询', 'MCP调用'],
      bottom: 0,
      textStyle: {
        color: isDark ? 'rgba(255,255,255,0.7)' : '#4E5969'
      }
    },
    grid: { left: 40, right: 20, top: 10, bottom: 40 },
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    xAxis: {
      type: 'category',
      data: currentData.value.xAxis,
      axisLine: { lineStyle: { color: isDark ? '#333' : '#e8e8e8' } },
      axisLabel: { color: isDark ? 'rgba(255,255,255,0.7)' : '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: isDark ? '#333' : '#f0f0f0' } },
      axisLabel: { color: isDark ? 'rgba(255,255,255,0.7)' : '#999' }
    },
    series: [
      {
        name: '代码生成',
        type: 'line',
        smooth: true,
        data: currentData.value.codeGen,
        lineStyle: { width: 2 },
        showSymbol: false,
        itemStyle: { color: '#165DFF' },
        animationDelay: (idx: number) => idx * 50
      },
      {
        name: '知识库查询',
        type: 'line',
        smooth: true,
        data: currentData.value.knowledge,
        lineStyle: { width: 2 },
        showSymbol: false,
        itemStyle: { color: '#14C9C9' },
        animationDelay: (idx: number) => idx * 50 + 100
      },
      {
        name: 'MCP调用',
        type: 'line',
        smooth: true,
        data: currentData.value.mcp,
        lineStyle: { width: 2 },
        showSymbol: false,
        itemStyle: { color: '#722ED1' },
        animationDelay: (idx: number) => idx * 50 + 200
      }
    ]
  }
})
</script>

<style scoped lang="scss">
</style>
