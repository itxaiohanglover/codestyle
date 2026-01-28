import http from '@/utils/http'

const BASE_URL = '/tenant/common'

/** @desc 根据域名查询租户 ID */
export function getTenantIdByDomain(domain: string) {
  return http.get<string>(`${BASE_URL}/id`, { domain })
}
