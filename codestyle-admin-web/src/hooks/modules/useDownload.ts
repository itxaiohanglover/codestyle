import { Message, Notification } from '@arco-design/web-vue'

/**
 * @description 接收数据流生成 blob，创建链接，下载文件
 * @param {Function} api 导出表格的api方法 (必传)
 * @param {string} fileName 导出的文件名 (可选，例如：导出数据.xlsx，默认从响应头或时间戳生成)
 * @param {string} fileType 导出的文件格式 (默认为.xlsx)
 * @param {boolean} isNotify 是否显示导出提示消息 (默认为 false)
 * @returns {Promise<void>} 无返回值
 */
interface NavigatorWithMsSaveOrOpenBlob extends Navigator {
  msSaveOrOpenBlob: (blob: Blob, fileName: string) => void
}
export const useDownload = async (api: () => Promise<any>, fileName = '', fileType = '.xlsx', isNotify = false) => {
  try {
    const res = await api()
    if (!fileName) {
      if (res.headers['content-disposition']) {
        // 从响应头提取文件名
        fileName = decodeURI(res.headers['content-disposition'].split(';')[1].split('=')[1])
      } else {
        // 时间戳生成
        fileName = new Date().getTime() + fileType
      }
    }

    if (isNotify && !res?.code) {
      Notification.warning({
        title: '温馨提示',
        content: '如果数据庞大会导致下载缓慢哦，请您耐心等待！',
      })
    }
    // 验证响应数据
    if (res.status !== 200 || !res.data || !(res.data instanceof Blob)) {
      Message.error('导出失败：无效的响应数据')
      return
    }
    const blob = new Blob([res.data])
    // 兼容 IE/Edge 浏览器
    if ('msSaveOrOpenBlob' in navigator) {
      return (navigator as unknown as NavigatorWithMsSaveOrOpenBlob).msSaveOrOpenBlob(blob, fileName)
    }
    // 创建下载链接并触发下载
    const blobUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.style.display = 'none'
    link.download = fileName
    link.href = blobUrl
    document.body.appendChild(link)
    link.click()
    // 清理资源
    document.body.removeChild(link)
    window.URL.revokeObjectURL(blobUrl)
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : '未知错误'
    Message.error(`下载失败: ${errorMsg}`)
  }
}
