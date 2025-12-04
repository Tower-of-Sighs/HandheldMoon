# Handheld Moon

This mod adds several large-area lighting items such as flashlights and mini moons.

### Main Features

The mod currently contains two core items: **FullMoon** and **MoonlightLamp**.

The lighting features are very easy to understand — a few pictures say it all:

* (First-person using the MoonlightLamp)
* (Other players using the MoonlightLamp)
* (Placed MoonlightLamp)
* (Comparison: FullMoon, MoonlightLamp, torch)

In the fourth image the item in the player’s hand is the **FullMoon**. It provides a lighting radius of **18**, and its falloff is specially tuned so it only noticeably darkens very close to the edge. Think of it as a mini moon!

The **FullMoon** can be used to craft the **MoonlightLamp**. Although the latter limits the lighting angle, it increases the illumination distance to **32**!

Note: this mod depends on **dynamic lighting** and builds on vanilla lighting behavior. The lighting shown above uses the vanilla lightmap, so it generally doesn’t cause compatibility issues.

On top of vanilla lighting, the **MoonlightLamp** adds a shader filter to make up for vanilla lighting’s lack of brightness and to give the lit area a slightly overexposed look.

Use right-click to toggle the MoonlightLamp on/off, or use the control hotkey (default **V**) to switch. While holding the control hotkey you can use the mouse wheel to adjust the shader strength. Mouse left-click toggles vanilla lighting, and mouse right-click toggles the global third-person beam.

What is the third-person beam? When other players in your view use a **MoonlightLamp**, a very noticeable beam is emitted to make up for vanilla lighting’s limited expressiveness.

If you place a **MoonlightLamp** on the ground it will also emit a beam and act as a “searchlight.” You can right-click to toggle it, or sneak + use the mouse wheel to change the beam’s direction. Aiming the crosshair at a side causes horizontal rotation; aiming at the top or bottom causes vertical rotation.

(Spooky!)

### Mod Integrations

The mod already has planned integrations with several other mods such as TaCZ, Superb Warfare, and Sona Survival 101.

TaCZ integration is already underway — when both mods are installed two additional attachments become available for the MoonlightLamp: a “muzzle” version and a “laser” version.

An accessory slot integration has also been added: a new **shoulder accessory** slot specifically for holding flashlights, allowing quick toggling via the control hotkey.

More integrations with other mods are actively in progress.

It’s worth noting Valkyrie and Create are planned as well, but “moving blocks” are rich in design possibilities and won’t be expected for quite some time.

### Compatibility

Most importantly, this mod’s changes to vanilla lighting and shaders are fully compatible with shader packs, but the third-person beam may fail to render or appear as a laser under some shader packs — please toggle it off if that happens.

(「Master Spark」)

Performance-wise, thanks to Mojang’s vanilla lighting optimizations since **1.20**, this mod’s overhead is modest. The shader component has the biggest FPS impact; if you want maximum performance you can set the shader intensity to **0** (turn it off).

If you encounter other compatibility issues, please report them.

### Future Plans

* Configurable lighting range and angle
* Colored lighting
* More lighting items
* More mod integrations
* Allow beams to emit from body parts other than the face