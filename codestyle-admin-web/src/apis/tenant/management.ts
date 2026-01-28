import type * as T from './type'
import http from '@/utils/http'

export type * from './type'

const BASE_URL = '/tenant/management'

/** @desc 查询租户列表 */
export function listTenant(query: T.TenantPageQuery) {
  return http.get<PageRes<T.TenantResp[]>>(`${BASE_URL}`, query)
}

/** @desc 查询租户详情 */
export function getTenant(id: string) {
  return http.get<T.TenantResp>(`${BASE_URL}/${id}`)
}

/** @desc 新增租户 */
export function addTenant(data: any) {
  return http.post(`${BASE_URL}`, data)
}

/** @desc 修改租户 */
export function updateTenant(data: any, id: string) {
  return http.put(`${BASE_URL}/${id}`, data)
}

/** @desc 删除租户 */
export function deleteTenant(id: string) {
  return http.del(`${BASE_URL}/${id}`)
}

/** @desc 修改租户管理员密码 */
export const updateTenantAdminUserPwd = (data: any, id: string) => {
  return http.put(`${BASE_URL}/${id}/admin/pwd`, data)
}
