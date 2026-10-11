# Forgotten Relics Plus

English | [简体中文](README.md)

An unofficial **Minecraft 1.21.1 / NeoForge** port of Forgotten Relics, bringing magical accessories, tomes and weapons into Thaumaturge's research system.

Solve research notes with complexity **2–8** to unlock relic recipes and discover teleportation, telekinesis, protection and weather control. **Omega Core** research appears only with **Re:Avaritia** installed.

![Research tree](show.png)

## Installation

Requires **NeoForge 21.1.250+, Java 21** and these mods:

- Thaumaturge 1.0.0+
- Lithostitched 1.8.0+
- Botania 457-SNAPSHOT+
- Curios 9.0.0+

[Downloads](https://github.com/BeiWuCN/Forgotten-Relics-Plus/releases) · [Issues](https://github.com/BeiWuCN/Forgotten-Relics-Plus/issues) · [MCMod.cn](https://www.mcmod.cn/class/28217.html)

## Development

Use Java 21 and PowerShell 7. Prepare dependencies using the [dependency guide](libs/README.md), then run `./gradlew clean build`. After editing research, run `./gradlew runData` and commit the generated resources. Sync Gradle in IDEA to use the Client, Server and Data configurations.

## Credits and license

Originally created by **Integral / Extegral / VictorShadow**. This port is independently implemented for modern APIs, with assets and behavior based on the [1.7.10 original](https://github.com/jss2a98aj/Forgotten-Relics). It does not use RE code. Project code is licensed under [MIT](LICENSE); original assets belong to their creators.
