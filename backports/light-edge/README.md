# 光照边缘修复移植

源修复：`2b056cd`（`Try to fix smooth light map.`）。范围为 Minecraft 1.20.1、1.21.1、26.1 的 Fabric 和对应 Forge / NeoForge 版本。

## 当前分支

`targets/fabric-26.1`、`targets/neoforge-26.1` 和 `targets/neoforge-1.21.1` 使用 `common` 中的 `BeamEdgeLighting`。该类保留源修复的参数：横向支撑阈值 3.5、边缘衰减强度 0.9、相邻供光阈值 3.0、暗侧最低光照 1。

移植同时修正了两个边界情况：衰减后的动态光不能降低原有方块光照；邻居供光检查必须跨越区块分段边界。26.1 专用的世界取光接口和 lightmap 状态在版本适配层转换，保证 1.21.1 可以编译共用代码。

## 旧版本分支

三个补丁各包含相同的纯 Java 边缘算法、对应动态光引擎的 Mixin，以及客户端 Mixin 注册。

| 版本 | 基线分支 | 基线提交 | 补丁 | 本地工作分支 |
| --- | --- | --- | --- | --- |
| 1.20.1 Fabric | `origin/1.20.1` | `51cb0d2` | `fabric-1.20.1.patch` | `backport/light-edge-fabric-1.20.1` |
| 1.20.1 Forge | `origin/master` | `dd980c0` | `forge-1.20.1.patch` | `backport/light-edge-forge-1.20.1` |
| 1.21.1 Fabric | `origin/1.21.1` | `1269569` | `fabric-1.21.1.patch` | `backport/light-edge-fabric-1.21.1` |

本地已经在仓库旁的 `../HandheldMoon-backports/<版本>/` 建立对应 worktree 并应用改动。改动保留为未提交状态；补丁用于审阅和在其他检出目录重现。

Fabric 旧版在 LambDynamicLights 的 `DynamicLightingEngine.getDynamicLightLevel` 返回处处理；Forge 旧版在 SodiumDynamicLights 的同名查询返回处处理。原版渲染器和 Sodium / Embeddium 共用这些入口。邻居读取通过线程局部标志跳过二次过滤，`finally` 恢复标志；无附近光源时跳过额外采样。Forge 的邻居扫描沿用引擎的读锁保护光源集合。

对尚未应用补丁的独立检出目录，可以执行：

```powershell
git -C <旧版本目录> apply --check <当前仓库绝对路径>/backports/light-edge/fabric-1.20.1.patch
git -C <旧版本目录> apply <当前仓库绝对路径>/backports/light-edge/fabric-1.20.1.patch
```

按版本替换补丁文件名。已经应用的 worktree 不需要重复执行。

## 验证

2026-11-01 完成以下完整 `build` 验证：

| Minecraft | Fabric | Forge / NeoForge |
| --- | --- | --- |
| 1.20.1 | 通过，JDK 21 | Forge 通过，JDK 17 |
| 1.21.1 | 通过，JDK 21 | NeoForge 通过，JDK 21 |
| 26.1 | 通过，JDK 25 | NeoForge 通过，JDK 25 |

当前三个 target 的 `:common:test` 均通过，共用测试集包含 31 项测试，失败数为 0。三个旧版分支没有配置自动测试集，因此额外使用共用边缘算法的回归测试验证其 Java 17 兼容性。

六个可测试 JAR 集中在当前仓库的 `build/light-edge-ports/`，其中 `manifest.json` 记录目标和 SHA-256。收集时检查每个 JAR 包含 `BeamEdgeLighting.class`，旧版 JAR 还检查 `BeamEdgeLightingMixin.class` 和对应客户端 Mixin 注册。

共用回归测试 `BeamEdgeLightingTest` 覆盖光束内部亮度、边缘逐级衰减、跨坐标边界的对角供光、裙边范围、保留天空光及方块光下限。四项测试也独立使用 `--release 17` 编译并在 Java 17 上运行通过。

三个旧版适配类已分别对实际版本的 Minecraft、LambDynamicLights / SodiumDynamicLights 和 Mixin 依赖完成定向编译。Fabric 1.20.1 的依赖使用其实际 intermediary 发布包和官方映射进行转换。

旧版 Fabric 的构建脚本引用未跟踪的 `libs/*.jar`，新 worktree 需要补齐这些构建输入。此次本地构建从对应版本 TaCZ、Iris 和 Sodium Extra 的内嵌 JAR 提取匹配文件，并取得发布的 Trinkets 3.7.2 和 glsl-transformer 3.0.0-pre3；这些二进制依赖未包含在源码补丁中。

构建验证不能代替游戏内画面验证。需在每个版本检查原版与 Sodium / Embeddium 下的平地、墙面、光束近端、分段边界、火把与手电筒叠加，以及关闭动态光后的恢复情况。
