import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/template/library'

export function listTemplate(query: T.TemplatePageQuery) {
  return http.get<PageRes<T.TemplateItem[]>>(`${BASE_URL}/list`, query)
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
