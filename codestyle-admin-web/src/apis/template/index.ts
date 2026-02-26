import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/template/library'

export function listTemplate(query: T.TemplatePageQuery) {
  return http.get<PageRes<T.TemplateItem[]>>(`${BASE_URL}/list`, query)
}

export function listFavoriteTemplates(query: T.TemplatePageQuery) {
  return http.get<PageRes<T.TemplateItem[]>>(`${BASE_URL}/favorites`, query)
}

export function listMyTemplates(query: T.TemplatePageQuery) {
  return http.get<PageRes<T.TemplateItem[]>>(`${BASE_URL}/mine`, query)
}

export function toggleVisibility(id: number) {
  return http.put(`${BASE_URL}/${id}/visibility`)
}

export function toggleFavorite(id: number) {
  return http.put<boolean>(`${BASE_URL}/${id}/favorite`)
}

export function getTemplateDetail(id: number) {
  return http.get<T.TemplateDetail>(`${BASE_URL}/${id}`)
}

export function recordDownload(id: number) {
  return http.post(`${BASE_URL}/${id}/download`)
}

export function addTemplate(req: T.TemplateReq) {
  return http.post<number>(`${BASE_URL}`, req)
}

export function updateTemplate(id: number, req: T.TemplateReq) {
  return http.put(`${BASE_URL}/${id}`, req)
}

export function deleteTemplate(ids: number | number[]) {
  return http.del(`${BASE_URL}`, Array.isArray(ids) ? ids : [ids])
}

export function listVersions(groupId: string, artifactId: string) {
  return http.get<T.TemplateItem[]>(`${BASE_URL}/versions`, { groupId, artifactId })
}

const FILE_URL = '/open-api/template'

export function uploadTemplateZip(file: File, groupId?: string, artifactId?: string, version?: string) {
  const formData = new FormData()
  formData.append('file', file)
  const params = new URLSearchParams()
  if (groupId) params.append('groupId', groupId)
  if (artifactId) params.append('artifactId', artifactId)
  if (version) params.append('version', version)
  const qs = params.toString()
  return http.post<T.TemplateUploadResp>(`${FILE_URL}/upload${qs ? `?${qs}` : ''}`, formData)
}

export function listTemplateFiles(groupId: string, artifactId: string, version: string) {
  return http.get<T.FileTreeNode[]>(`${FILE_URL}/files`, { groupId, artifactId, version })
}

export function readTemplateFileContent(groupId: string, artifactId: string, version: string, filePath: string) {
  return http.get<string>(`${FILE_URL}/file-content`, { groupId, artifactId, version, filePath })
}
