# 失落遗物学：重现 — 高版本移植（Forgotten Relics: Unofficial）

[English](README.en.md) | **简体中文**

**失落遗物学**（Forgotten Relics）的 **1.21.1 / NeoForge** 移植版。

失落遗物学是由 **Integral**（又名 Extegral / VictorShadow）创作、围绕**神秘时代（Thaumcraft）**与**植物魔法（Botania）**展开的附属模组，
以「发现并使用强力遗物」为核心。本仓库做的是把它搬到高版本（1.21.1 + NeoForge），
数值、配方与研究结构尽量沿用 1.7.10 原版，代码则按 1.21.1 的 API 与习惯**重新实现**，不做逐行搬运。

- MC百科条目：<https://www.mcmod.cn/class/28217.html>
- 1.7.10 原版（**唯一的行为参照与素材来源**）：<https://github.com/jss2a98aj/Forgotten-Relics>
- 1.12.2 移植版（仅作行为对照，见下文「代码来源」）：<https://github.com/NNYYOONNIIOO/Forgotten-Relics-RE>
- 问题反馈：<https://github.com/beiwucn/Forgotten-Relics-Unofficial/issues>

---

## 协议

**本项目使用 [MIT](https://opensource.org/license/mit) 协议。**

各环节授权情况如下：

| 项目 | 版本 | 作者 | 协议 |
| --- | --- | --- | --- |
| Forgotten Relics（原版） | 1.7.10 | Integral / Extegral / VictorShadow | [WTFPL](https://github.com/jss2a98aj/Forgotten-Relics/blob/master/LICENSE) |
| Forgotten Relics RE（1.12.2 移植） | 1.12.2 | NNYYOONNIIOO 等 | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en)（**本项目未使用其代码**） |
| **Forgotten Relics: Unofficial（本项目）** | 1.21.1 | beiwu、gali2009 等 | **[MIT](https://opensource.org/license/mit)** |

### 代码来源

本项目的**唯一行为参照是 1.7.10 原版**（WTFPL，不附加任何条件），全部代码针对
1.21.1 / NeoForge 的 API 独立实现，**不含 1.12.2 移植版（RE）的代码**。
RE 版采用 CC BY-NC-SA 4.0，参考其代码会一并继承「禁止商用」与「必须同协议共享」两项限制，
这正是本项目改用 MIT 的原因。RE 版只用于对照行为表现；物品纹理与文本直接取自 1.7.10 原版。

### 你可以自由地

- 用于**任何用途，包括商业用途**（付费整合包、收费服务器等）；
- 修改、再发布、闭源分发，甚至换用别的协议；
- 唯一的义务是保留版权声明与协议文本。

### 注意

- MIT 只覆盖**本项目的代码与文档**。Minecraft 本体、以及 Thaumaturge、Curios、Botania 等前置
  各有自己的协议，请分别遵守。
- 本模组是**非官方**移植，与原版作者及 1.12.2 移植版作者**无隶属关系**，也未获得其背书。
- 原版的美术素材、研究文本与游戏设计的著作权归原作者所有。

---

## 运行环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.250 及以上 |
| Java | 21 |

### 前置模组

均为**必装**：

| 模组 | 版本要求 | 说明 |
| --- | --- | --- |
| [Thaumaturge](https://www.curseforge.com/minecraft/mc-mods/thaumaturge) | 0.4.0+ | 高版本的神秘时代，提供奥术合成、要素（Aspects）、研究系统 |
| [Botania](https://www.curseforge.com/minecraft/mc-mods/botania) | 457+ | 植物魔法，提供部分材料与法杖 API |
| [Curios](https://www.curseforge.com/minecraft/mc-mods/curios) | 9.0.0+ | 饰品栏（对应 1.12.2 的 Baubles） |
| [TerraBlender](https://www.curseforge.com/minecraft/mc-mods/terrablender) | 4.1.0.0+ | Thaumaturge 的前置，这里跟着声明 |

> 说明：Botania 的版本号必须写成 `457-SNAPSHOT` 才匹配。jar 内部声明的版本是 `457-SNAPSHOT`，
> 按 Maven 规则排在 `457` **之下**，所以写 `457` 反而会被判定为「版本不满足」。

---

## 目录结构

```
src/main/java/com/beiwu/forgottenrelics_re/
├── ForgottenRelics.java        # 模组主类
├── FRCommonEvents.java         # 行为派发器：不认识具体物品，只把事件转给实现了行为接口的物品
├── api/                        # 行为接口（佩戴 tick、受击、挖掘速度、替人承伤、可充能）
├── client/                     # 客户端渲染与键位（饰品佩戴渲染、模型层注册）
├── config/FRConfig.java        # 配置（对应原版 RelicsConfigHandler）
├── items/                      # 物品实现，各自实现自己需要的行为接口
├── network/                    # 网络载荷
├── registry/                   # 物品、护甲材质、数据组件、创造模式标签页注册
└── utils/                      # 工具类（冷却、佩戴物遍历、音效、伤害类型等）

src/main/resources/
├── assets/forgotten_relics/    # 贴图、模型、语言文件
│   └── textures/models/armor/  # 护甲层贴图
├── data/forgotten_relics/
│   ├── damage_type/            # 自定义伤害类型
│   ├── recipe/infusion/        # 灌注配方
│   └── thaumaturge/            # 研究分类与研究词条
└── data/curios/tags/item/      # 饰品栏位归属
```

**代码注释统一使用中文。**

---

## 致谢

- **Integral**（Extegral / VictorShadow）—— 原版 Forgotten Relics 的作者；
- **NNYYOONNIIOO** —— 1.12.2 移植版 Forgotten Relics RE，本项目借其对照行为表现；
- **Thaumaturge** 团队 —— 高版本神秘时代的实现；
- **Botania**、**Curios** 团队 —— 前置模组。
