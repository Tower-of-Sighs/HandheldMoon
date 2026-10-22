# 开发与迁移规范

本仓库使用 SighsTemple 的 `common + targets/<loader>-<minecraft-version>` 结构维护多个 Minecraft 加载器和版本。任何开发者或自动化代理在修改前都必须遵守本文件。

## 架构边界

```text
common/                         无确证版本差异的共享源码树与共享资源
targets/<loader>-<mc-version>/  一个独立的加载器 + Minecraft 版本工程
```

- `common` 放所有没有确证版本差异的代码，包括引用 Minecraft 与 loader 类型的那部分；依赖平台类型不是拆分理由，Java 版本也不是。核心版本是 NeoForge 26.1。
- `targets/*` 只放该 target 的入口、SPI 实现、加载器事件/网络/注册适配、metadata、Mixin 配置和版本专属资源。
- 每个 target 通过 `includeBuild("../../common")` 把 `common` 作为 composite build 导入 IDE（只让 IDE 把 `common` 当作独立可编辑模块，不产生 Gradle 项目依赖）；再用 source-set sharing 把共享源码编译进自己：`prepareCommonSources` 这个 `Sync` 任务把 `../../common/src/main/java` 同步到 `build/generated/common-main` 并加入 main 源码集，test 源码集加入 `../../common/src/test/`。target 不依赖 `project(":common")`。
- 版本差异的登记点是各 target `build.gradle.kts` 里 `prepareCommonSources` 的 `exclude` 块：某个 common 文件在该 target 上有确证差异时，在那里 `exclude '<路径>'`，并把该 target 自己的实现放进 target。
- 唯一能把代码从 `common` 拆到 `targets/<loader>-<mc-version>/` 的理由是：经阅读实际源码/API 确证该内容在不同 loader 或不同 Minecraft 版本之间存在差异。不得凭猜测拆分，也不得为了“代码好看”而拆分。
- `common` 不是 Gradle 子项目，而是可独立编译的共享源码树：`common/build.gradle.kts` 用 `java-library` + `net.neoforged.moddev`、toolchain 25，对着 `targets/neoforge-26.1` 相同的 NeoForge 26.1 类路径编译与测试（`gradlew -p common build`）。
- 不要用运行时版本判断、反射或同名 class 覆盖来兼容不同 target；不同 API 应由各 target 的适配器实现。
- 一个发布 jar 只对应一个 loader 与一个 Minecraft 版本，禁止 universal jar。

## SPI 约定

加载器差异统一通过 `common/.../spi` 暴露，实现由 `ServiceLoader` 注入：

| SPI | common 侧 | target 侧 |
| --- | --- | --- |
| `PlatformService` | `PlatformServices`、`HandheldMoon.init` | 加载器入口、配置目录、注册与网络实现 |
| `RegistryService` | 注册项声明（`ModBlocks`、`ModItems` 等） | `DeferredRegister` / `Registry.register` |
| `ClientPlatformService` | `AbstractClientPlatformService`（tick、界面、按键码、发包构造） | 发包传输、按键/菜单界面注册 |
| `IAccessoryCompat` | `AccessoryCompat` 门面 | Curios / Trinkets 集成 |

`META-INF/services/<spi>` 文件必须留在实现该 SPI 的 target 中。不要在 common 里新增 `if (loader == ...)` 分支。

## 文件与配置约定

| 内容 | 位置 | 规则 |
| --- | --- | --- |
| 共享 Java 代码 | `common/src/main/java/` | 放所有没有确证版本差异的代码，可引用 Minecraft 与 loader 类型；确证有差异的文件在该 target 的 `prepareCommonSources` 中 `exclude`。 |
| 共享资源 | `common/src/main/resources/` | 会自动合并到所有 target 的最终 jar。 |
| 目标专属资源 | `targets/<name>/src/main/resources/` | 仅放该版本/loader 专属资源，例如 Curios/Trinkets 数据、26.1 与 1.21.1 的模型和配方。 |
| Loader metadata | target 的 `src/main/resources/` | `fabric.mod.json` / `META-INF/neoforge.mods.toml` 保留在 target。 |
| Mixin 配置 | target 的 `src/main/resources/` | 一个 target 一个共享配置（`handheldmoon.mixins.json`）+ 加载器专属配置；common 不放 mixin json。 |
| Access widening | `common/accesstransformer.cfg` + target 的 `handheldmoon.classtweaker` | common 的 AT 只用于编译；26.1 target 各自持有生产用 classtweaker，1.21.1 持有自己的 `META-INF/accesstransformer.cfg`。 |
| 版本/loader 参数 | `targets/<name>/gradle.properties` | 不放在仓库根 `gradle.properties`。 |
| 共享模组信息 | 根 `gradle.properties` | 仅 `mod_*`、`version`、`group`、`java_version` 与 Gradle 运行参数。 |
| 发布配置 | 各 target 的 `build.gradle.kts` | Modrinth / CurseForge 任务，token 只从环境变量读取。 |
| 本地 jar 依赖 | 无 | 当前全部依赖通过 Maven 坐标解析；需要本地 jar 时先在 AGENTS 中登记再添加约定。 |

共享资源与 target 资源若有同路径文件，必须明确选择唯一归属；不要依赖覆盖顺序。加载器 metadata、Mixin 配置、access widener/transformer 和版本专属语言/模型/配方文件一律归 target。

## 日常开发

1. 先判断改动是 `common`、单个 target、多个 target，还是构建/发布配置。
2. 单 target 改动只修改对应 `targets/<name>/`；不因方便而改动其他版本。
3. 改动默认放入 `common`；只有经源码确证某段内容存在版本差异时，才把它移入对应 target，并在该 target 的 `prepareCommonSources` 里 `exclude` 对应 common 路径。
4. 改动资源、metadata、Mixin、注册、事件、网络或渲染时，除构建外必须做相应的运行验证。
5. 不提交 token、账号、密码、私有仓库凭据、IDE 运行缓存或 `build/` 输出。

### 构建命令

每个 target 是独立 Gradle 根工程，应在它自己的目录中构建。26.1 target 需要 JDK 25，1.21.1 target 的 toolchain 需要 JDK 21（Gradle 本身可用 21 或 25）：

```powershell
cd targets\neoforge-26.1
.\gradlew.bat clean build
```

共享源码树可独立构建与测试：

```powershell
.\gradlew.bat -p common build
```

根项目的 `buildCommon`、`buildFabric261`、`buildNeoForge1211`、`buildNeoForge261` 只是转发到对应目录的便捷任务。

### 发布

在对应 target 内运行 `publishToPlatformServices`（或 `modrinth` / `publishCurseForge`）可发布该 target 的 jar。token 只通过环境变量提供：

```powershell
$env.CURSEFORGE_TOKEN = '...'
$env.MODRINTH_TOKEN = '...'
```

发布前必须执行该 target 的 `clean build`，检查 jar 内的 metadata、共享 class、共享资源和版本范围。不要从根项目或错误 target 发布。

## 提交前检查

- `common` 只放没有确证版本差异的代码；有确证差异的文件已在对应 target 的 `prepareCommonSources` 中 `exclude` 并改为 target 自己的实现。
- 每个新增/修改的 target 使用正确 JDK 独立构建，`common` 单独 `build` 通过。
- 最终 jar 含正确 metadata、目标专属资源与共享 class/resources。
- 共享资源与 target 资源没有未登记的路径冲突。
- 发布相关改动不包含 token；项目 ID、版本类型、依赖关系和 changelog 已确认。
- README、版本差异文档和支持矩阵与实际 target 一致。
