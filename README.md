# Forgotten Relics Plus

[English](README.en.md) | 简体中文

Forgotten Relics 的 **Minecraft 1.21.1 / NeoForge** 非官方移植版，加入魔法饰品、典籍与武器，并接入 Thaumaturge 研究体系。

解开复杂度为 **2～8** 的研究笔记，逐步解锁遗物配方，体验传送、念力、防护与天气控制等能力。欧米伽之核研究仅在安装 **Re:Avaritia** 时出现。

![研究树](show.png)

## 安装

需要 **NeoForge 21.1.250+、Java 21**，以及以下前置：

- Thaumaturge 1.0.0+
- Lithostitched 1.8.0+
- Botania 457-SNAPSHOT+
- Curios 9.0.0+

[下载](https://github.com/BeiWuCN/Forgotten-Relics-Plus/releases) · [问题反馈](https://github.com/BeiWuCN/Forgotten-Relics-Plus/issues) · [MC 百科](https://www.mcmod.cn/class/28217.html)

## 开发

使用 Java 21 和 PowerShell 7。按 [依赖说明](libs/README.md) 准备依赖后执行 `./gradlew clean build`。修改研究后执行 `./gradlew runData`，并提交生成资源。IDEA 同步 Gradle 后可使用 Client、Server、Data 配置。

## 来源与许可

原版由 **Integral / Extegral / VictorShadow** 创作。本项目针对新版本独立实现，素材与行为参照 [1.7.10 原版](https://github.com/jss2a98aj/Forgotten-Relics)，未使用 RE 版代码。本项目代码采用 [MIT](LICENSE) 协议；原版素材归原作者所有。
