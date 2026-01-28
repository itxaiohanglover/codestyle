// 分片上传 API 封装
import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/system/multipart-upload'

/** @desc 初始化分片上传 */
export function initMultipartUpload(data: T.MultiPartUploadInitReq) {
  return http.post<T.MultiPartUploadInitResp>(`${BASE_URL}/init`, data)
}

/** @desc 上传分片 */
export function uploadPart(data: T.UploadPartReq, signal?: AbortSignal) {
  const formData = new FormData()
  formData.append('file', data.file)
  formData.append('uploadId', data.uploadId)
  formData.append('partNumber', String(data.partNumber))
  formData.append('path', data.path)
  return http.post<T.UploadPartResp>(`${BASE_URL}/part`, formData, { signal })
}

/** @desc 完成上传 */
export function completeMultipartUpload(params: T.CompleteMultipartUploadReq) {
  return http.get<string>(`${BASE_URL}/complete/${params.uploadId}`)
}

/** @desc 取消上传 */
export function cancelUpload(params: T.CancelUploadParams) {
  return http.get<void>(`${BASE_URL}/cancel/${params.uploadId}`)
}
