/// <reference types="vite/client" />

// 这告诉 TypeScript 如何处理 *.vue 文件
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 你可能还会看到或需要这个，如果你的项目使用 Vite
// /// <reference types="vite/client" />
// 如果你的项目是用 `npm create vue@latest` 并选择了 TypeScript 创建的，
// `vite-env.d.ts` 文件通常已经包含这个 reference 和上面的 declare module。
