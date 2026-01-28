/** 租户 */
export interface TenantResp {
  id: string
  name: string
  code: string
  domain: string
  expireTime: string
  description: number
  status: string
  packageId: string
  createUser: string
  createTime: string
  updateUser: string
  updateTime: string
  createUserString: string
  updateUserString: string
  adminUsername: string
  packageName: string
}
export interface TenantQuery {
  description?: string
  packageId?: string
  status?: string
  sort: Array<string>
}
export interface TenantPageQuery extends TenantQuery, PageQuery {}

/** 租户套餐 */
export interface TenantPackageResp {
  id: string
  name: string
  sort: number
  menuCheckStrictly: string
  description: string
  status: string
  menuIds: []
  createUser: string
  createTime: string
  updateUser: string
  updateTime: string
  createUserString: string
  updateUserString: string
}
export interface TenantPackageQuery {
  description?: string
  status?: string
  sort: Array<string>
}
export interface TenantPackagePageQuery extends TenantPackageQuery, PageQuery {}
