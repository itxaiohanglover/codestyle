export interface ModeItem {
  title: string
  icon: string
  subtitle: string
  value?: string
  type: 'phone' | 'email' | 'wechat_open' | 'gitee' | 'github'
  jumpMode?: 'link' | 'modal'
  status: boolean
  statusString: string
}
