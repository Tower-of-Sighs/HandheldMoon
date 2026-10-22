# HandheldMoon

HandheldMoon uses the `common + targets/<loader>-<minecraft-version>` layout of
[SighsTemple](https://github.com/Tower-of-Sighs/SighsTemple): one shared source
tree, and one independent Gradle project per loader/version pair.

## Layout

- `common/`: every source and resource that has **no confirmed loader/version
  difference**. It may reference Minecraft and loader types; it is not a
  platform-independent Java library. The core version is NeoForge 26.1, so the
  standalone build compiles against that classpath.
- `targets/fabric-26.1/`: Fabric entrypoints, platform SPI implementation,
  Trinkets compat, input mixins, metadata and resources.
- `targets/neoforge-26.1/`: NeoForge 26.1 entrypoints, platform SPI
  implementation, Curios compat, mixin config, metadata and resources.
- `targets/neoforge-1.21.1/`: the NeoForge 1.21.1 implementation. Its legacy
  rendering, item, screen, block-entity and mixin files are confirmed version
  differences, so they live here and the matching common paths are excluded in
  `prepareCommonSources`.

Each target attaches `../../common` as a composite build (IDE module) and
compiles the shared sources into its own source set with the
`prepareCommonSources` `Sync` task. Targets never depend on
`project(':common')`; a single published jar always targets exactly one loader
and one Minecraft version.

## SPI

Loader boundaries are exposed through `common/.../spi` and loaded with
`ServiceLoader`:

| SPI | Shared side | Target side |
| --- | --- | --- |
| `PlatformService` | `PlatformServices`, `HandheldMoon.init` | loader entrypoint, config directory, registry + networking implementation |
| `RegistryService` | registry declarations (`ModBlocks`, `ModItems`, ...) | `DeferredRegister` / `Registry.register` registration |
| `ClientPlatformService` | `AbstractClientPlatformService` (tick, screens, key codes, packet building) | packet transport, key-binding and menu-screen registration |
| `IAccessoryCompat` | `AccessoryCompat` facade | Curios / Trinkets integration |

`META-INF/services/<spi>` files stay in the target that implements them.

## Build

Each target is its own Gradle root project. Run the build from the target
directory:

```powershell
cd targets\neoforge-26.1
.\gradlew.bat clean build
```

| Target | Gradle JVM |
| --- | --- |
| `fabric-26.1` | JDK 25 |
| `neoforge-26.1` | JDK 25 |
| `neoforge-1.21.1` | JDK 21 (JDK 25 also works for Gradle; the toolchain resolves 21) |

The shared tree also builds and tests on its own:

```powershell
.\gradlew.bat -p common build
```

The root project exposes `buildCommon`, `buildFabric261`, `buildNeoForge1211`
and `buildNeoForge261` as convenience wrappers, but never combines targets into
one artifact.

## Version differences

- [Confirmed version differences](docs/version-differences/README.md)

For entity light `entityData` synchronization and persistence details, see the
[common development guide](common/DEVELOPMENT.md).
