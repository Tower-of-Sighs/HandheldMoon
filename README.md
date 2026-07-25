# HandheldMoon

HandheldMoon keeps each loader and Minecraft version in a separate,
independently buildable Gradle project.

## Layout

- `common/`: source and resources shared by compatible targets.
- `targets/fabric-26.1/`: Fabric-only entrypoints, metadata, dependencies, and wrapper.
- `targets/neoforge-1.21.1/`: NeoForge 1.21.1 version adapters, metadata, dependencies, and wrapper.
- `targets/neoforge-26.1/`: NeoForge-only entrypoints, metadata, dependencies, and wrapper.
- `buildSrc/`: the existing shared Gradle convention plugins.

Each target references `../../common`; no target includes another loader or
Minecraft version.

## Build

Build each target from its own directory. NeoForge 1.21.1 uses JDK 21; the
Minecraft 26.1 targets use JDK 25.

```powershell
.\scripts\build-target.ps1 -Target fabric-26.1
.\scripts\build-target.ps1 -Target neoforge-1.21.1
.\scripts\build-target.ps1 -Target neoforge-26.1
```

The root project exposes `buildFabric261`, `buildNeoForge1211`,
`buildNeoForge261`, and `buildCommon` as convenience tasks, but does not
combine targets into one artifact.
