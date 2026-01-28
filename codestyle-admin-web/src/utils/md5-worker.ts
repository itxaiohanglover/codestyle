import SparkMD5 from 'spark-md5'

// 确保在 Web Worker 环境中运行
if (typeof globalThis !== 'undefined') {
  // 监听来自主线程的消息
  globalThis.addEventListener('message', (event) => {
    const { file, taskId, blockSize, chunkSize } = event.data

    if (file && taskId && blockSize && chunkSize) {
      calculateFileMd5Optimized(file, taskId, blockSize, chunkSize)
    } else {
      globalThis.postMessage({
        type: 'error',
        taskId: taskId || 'unknown',
        error: 'Missing required parameters: file, taskId, blockSize, chunkSize',
      })
    }
  })
}

function calculateFileMd5Optimized(file: File, taskId: string, blockSize: number, chunkSize: number) {
  const totalSize = file.size
  const blocks = Math.ceil(totalSize / blockSize)
  const blockHashes: string[] = Array.from({ length: blocks })
  let processedBytes = 0
  let processedBlocks = 0

  const maxConcurrency = Math.max(2, navigator.hardwareConcurrency || 2)
  let activeWorkers = 0
  let nextBlockIndex = 0

  function processBlock(blockIndex: number): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        const start = blockIndex * blockSize
        const end = Math.min(start + blockSize, totalSize)
        const block = file.slice(start, end)

        const spark = new SparkMD5.ArrayBuffer()
        const chunks = Math.ceil(block.size / chunkSize)
        let currentChunk = 0
        const reader = new FileReader()

        reader.onload = function (e: ProgressEvent<FileReader>) {
          try {
            if (e.target?.result) {
              spark.append(e.target.result as ArrayBuffer)
              processedBytes += (e.target.result as ArrayBuffer).byteLength

              globalThis.postMessage({
                type: 'progress',
                taskId,
                progress: processedBytes / totalSize,
                processedBytes,
                totalSize,
              })

              currentChunk++
              if (currentChunk < chunks) {
                loadNextChunk()
              } else {
                blockHashes[blockIndex] = spark.end()
                processedBlocks++
                resolve()
              }
            } else {
              reject(new Error('FileReader result is null'))
            }
          } catch (error) {
            reject(error)
          }
        }

        reader.onerror = function (e: ProgressEvent<FileReader>) {
          console.error(`[Worker] 文件读取错误:`, e)
          globalThis.postMessage({
            type: 'error',
            taskId,
            error: e,
          })
          reject(new Error('FileReader error'))
        }

        function loadNextChunk() {
          try {
            const chunkStart = currentChunk * chunkSize
            const chunkEnd = Math.min(chunkStart + chunkSize, block.size)
            const blob = block.slice(chunkStart, chunkEnd)
            reader.readAsArrayBuffer(blob)
          } catch (error) {
            reject(error)
          }
        }

        loadNextChunk()
      } catch (error) {
        reject(error)
      }
    })
  }

  function scheduleBlocks() {
    while (activeWorkers < maxConcurrency && nextBlockIndex < blocks) {
      const currentIndex = nextBlockIndex++
      activeWorkers++
      processBlock(currentIndex).then(() => {
        activeWorkers--
        if (processedBlocks >= blocks) {
          // 所有块完成，计算最终 MD5
          try {
            const finalSpark = new SparkMD5.ArrayBuffer()
            blockHashes.forEach((hash) => {
              const hashBuffer = new TextEncoder().encode(hash)
              finalSpark.append(hashBuffer.buffer.slice(hashBuffer.byteOffset, hashBuffer.byteOffset + hashBuffer.byteLength))
            })
            const finalMd5 = finalSpark.end()

            globalThis.postMessage({
              type: 'complete',
              taskId,
              md5: finalMd5,
            })
          } catch (error) {
            console.error(`[Worker] 计算最终 MD5 时出错:`, error)
            globalThis.postMessage({
              type: 'error',
              taskId,
              error,
            })
          }
        } else {
          // 继续调度
          scheduleBlocks()
        }
      }).catch((error) => {
        activeWorkers--
        console.error(`[Worker] 处理块时出错:`, error)
        globalThis.postMessage({
          type: 'error',
          taskId,
          error,
        })
      })
    }
  }

  // 启动调度器
  scheduleBlocks()
}
