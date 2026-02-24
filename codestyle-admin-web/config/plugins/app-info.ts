import boxen from 'boxen'
import picocolors from 'picocolors'
import type { Plugin } from 'vite'

export default function appInfo(): Plugin {
  return {
    name: 'appInfo',
    apply: 'serve',
    async buildStart() {
      const { bold, green, cyan, bgGreen, underline } = picocolors
      // eslint-disable-next-line no-console
      console.log(
        boxen(
          `${bold(green(`${bgGreen(' CodeStyle Admin v1.0.0 ')}`))}\n${cyan('让历史代码活起来，让 AI 写的更对味！')}\n${cyan('官网：')}${underline('https://codestyle.top')}\n${cyan('文档：')}${underline('https://codestyle.top/docs')}`,
          {
            padding: 1,
            margin: 1,
            borderStyle: 'double',
            textAlignment: 'center',
          },
        ),
      )
    },
  }
}
