# Forgotten Relics Plus

English | [简体中文](README.md)

An unofficial **Minecraft 1.21.1 / NeoForge** port of Forgotten Relics, bringing magical accessories, tomes and weapons into Thaumaturge's research system.

Solve research notes with complexity **2–8** to unlock relic recipes and discover teleportation, telekinesis, protection and weather control. **Omega Core** research and crafting require **Re:Avaritia** and `omega_core.enabled` (enabled by default). Set it to `false` in `config/forgotten_relics_plus-common.toml` to hide the research and remove its recipe; restart after changing it.

![Research tree](show.png)

## Installation

Requires **NeoForge 21.1.250+, Java 21** and these mods:

- Thaumaturge 1.0.2+
- Lithostitched 1.8.0+
- Botania 457-SNAPSHOT+
- Curios 9.0.0+

[Downloads](https://github.com/BeiWuCN/Forgotten-Relics-Plus/releases) · [Issues](https://github.com/BeiWuCN/Forgotten-Relics-Plus/issues) · [MCMod.cn](https://www.mcmod.cn/class/28217.html)

## Development

Use Java 21 and PowerShell 7. Prepare dependencies using the [dependency guide](libs/README.md), then run `./gradlew clean build`. After editing research, run `./gradlew runData` and commit the generated resources. Sync Gradle in IDEA to use the Client, Server and Data configurations.

## Credits and license

Gameplay behavior is based on [Forgotten Relics: RE](https://www.curseforge.com/minecraft/mc-mods/forgotten-relics-re) by **Nyonio (NNYYOONNIIOO) and contributors**, with the author's permission. Art assets are based on the original [Forgotten Relics](https://github.com/jss2a98aj/Forgotten-Relics) by **Integral (Extegral / VictorShadow)**.

Compared with its predecessors, Plus targets **Minecraft 1.21.1 / NeoForge**, integrates Thaumaturge's research and charging systems, adds research-note recipe gates and conditional Omega Core research, and reworks projectile targeting, motion and rendering.

Project code is licensed under [MIT](LICENSE); reference projects and original assets retain their respective licenses and credits.
