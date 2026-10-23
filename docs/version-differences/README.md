# 版本差异记录

本目录记录经阅读实际源码/API 确证的差异。只有登记在这里的差异才允许把代码拆到 `targets/`；未登记的差异应优先在 `common` 用 SPI 适配。

- [26.1 与 1.21.1 的 Minecraft API 差异](26.1-vs-1.21.1.md)
- [26.1 与 1.20.1 的 Minecraft API 差异](26.1-vs-1.20.1.md)
- [Fabric 与 NeoForge 的加载器差异](loader-differences.md)

差异的登记方式：

1. 在 `common` 中保留共享实现（核心版本是 NeoForge 26.1）。
2. 在对应 target 的 `prepareCommonSources` 里 `exclude` 该 common 路径。
3. 把该 target 自己的实现放进 target 源码集。
4. 在本目录补充一条记录，说明源码证据和影响范围。
