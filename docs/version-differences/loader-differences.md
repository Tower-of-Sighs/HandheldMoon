# Fabric 与 NeoForge 的加载器差异

26.1 的两个 target 共享同一份 Minecraft API，差别只在加载器边界。这些边界全部通过 SPI 或 target 内实现隔离，common 不出现加载器判断。

| 主题 | Fabric 26.1 | NeoForge 26.1 | 归属 |
| --- | --- | --- | --- |
| 入口 | `ModInitializer` / `ClientModInitializer` | `@Mod` 构造器 + `@EventBusSubscriber` | target |
| 配置目录 | `FabricLoader.getInstance().getConfigDir()` | `FMLPaths.CONFIGDIR` | target（`PlatformService`） |
| 注册 | `Registry.register(BuiltInRegistries.*, ...)` | `DeferredRegister` + mod event bus | target（`RegistryService`） |
| 方块实体工厂 | `FabricBlockEntityTypeBuilder` | `new BlockEntityType<>(supplier, Set.of(block))` | target（`RegistryService`） |
| 网络 | `PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking` | `RegisterPayloadHandlersEvent` + `ServerboundCustomPayloadPacket` | target（`PlatformService` / `ClientPlatformService.send`） |
| 按键注册 | `KeyMappingHelper.registerKeyMapping` | `RegisterKeyMappingsEvent` | target |
| 菜单界面 | `MenuScreens.register` | `RegisterMenuScreensEvent` | target |
| 饰品位 | Trinkets（`TrinketRenderer` / `TrinketSlotAccess`） | Curios（`ICurioRenderer` / `SlotContext`） | target（`IAccessoryCompat` 实现） |
| 客户端输入 | `KeyboardHandler` / `MouseHandler` Mixin | `InputEvent.Key` / `InputEvent.MouseButton` | target |
| 资源标识符辅助 | `ConditionalItemModelProperties.ID_MAPPER.put` | `RegisterConditionalItemModelPropertyEvent` | target |
| Access widening | `handheldmoon.classtweaker`（loom `accessWidenerPath`） | `handheldmoon.classtweaker` + 构建期 `accessTransformers` | target / `common/accesstransformer.cfg` |
| Metadata | `fabric.mod.json` | `META-INF/neoforge.mods.toml` | target |
| Mixin 配置 | `handheldmoon.mixins.json` + `handheldmoon.fabric.mixins.json` | `handheldmoon.mixins.json` | target |

## 共享部分

以下内容在两个 26.1 target 上逐行等价（已用源码 diff 确认，仅格式差异），因此放在 `common`：

- `api/raycone/impl/RayConeRendererImpl.java`
- `block/FullMoonBlockEntity.java`、`block/MoonlightLampBlockEntity.java`
- `client/renderer/*`、`client/renderer/item/*`、`client/screen/*`
- `entity/FullMoonEntity.java`
- `event/handler/{EffectManager, ShaderEventHandler}.java`
- `item/MoonlightLampItem.java`
- `mixin/client/*`、`mixin/dynamiclight/*`
- `registry/ModKeyBindings.java`
- `util/IrisCompat.java`

Fabric 只额外保留两个输入 Mixin（`KeyboardHandlerMixin`、`MouseHandlerMixin`）和 Trinkets 渲染器；NeoForge 只额外保留 Curios 兼容和入口/事件类。
