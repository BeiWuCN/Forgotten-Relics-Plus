# 失落遗物学：重现 — 高版本移植（Forgotten Relics: Unofficial）

**失落遗物学**（Forgotten Relics）的 **1.21.1 / NeoForge** 移植版。

失落遗物学是由 **Integral**（又名 Extegral / VictorShadow）创作、围绕**神秘时代（Thaumcraft）**与**植物魔法（Botania）**展开的附属模组，
以「发现并使用强力遗物」为核心。本仓库做的是把它搬到高版本（1.21.1 + NeoForge），
改动之处尽量沿用原版的数值、配方与研究结构，只做必要的 API 适配。

- MC百科条目：<https://www.mcmod.cn/class/28217.html>
- 1.12.2 移植版（本项目的直接参考与素材来源）：<https://github.com/NNYYOONNIIOO/Forgotten-Relics-RE>
- 1.7.10 原版：<https://github.com/jss2a98aj/Forgotten-Relics>
- 问题反馈：<https://github.com/gali2009/Forgotten-Relics-Unofficial/issues>

---

## 协议

**本项目使用 [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh)（署名—非商业性使用—相同方式共享 4.0 国际）协议。**

协议沿链条继承而来，各环节授权情况如下：

| 项目 | 版本 | 作者 | 协议 |
| --- | --- | --- | --- |
| Forgotten Relics（原版） | 1.7.10 | Integral / Extegral / VictorShadow | [WTFPL](https://github.com/jss2a98aj/Forgotten-Relics/blob/master/LICENSE) |
| Forgotten Relics RE（1.12.2 移植） | 1.12.2 | NNYYOONNIIOO 等 | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en) |
| **Forgotten Relics: Unofficial（本项目）** | 1.21.1 | beiwu、gali2009 等 | **[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh)** |

也就是说，在遵守协议的前提下你可以自由：

- **分享** —— 以任何媒介或格式复制、发行本模组；
- **演绎** —— 修改、转换或以本模组为基础进行创作。

但必须遵守以下条件：

- **署名** —— 必须给出适当的署名，提供协议链接，并**注明是否作出了修改**，
  且不得以任何方式暗示原作者为你或你的使用背书；
- **非商业性使用** —— **不得将本模组用于商业目的**（包括但不限于付费整合包、付费下载、以本模组为卖点收费的服务器）；
- **相同方式共享** —— 若你修改、转换或以本模组为基础进行创作，
  你的贡献必须基于**相同的协议**分发。

> 本模组是**非官方**移植，与原版作者及 1.12.2 移植版作者**无隶属关系**，也未获得其背书。
> 原版的美术素材、研究文本与设计版权归原作者所有。

---

## 版本号规则

版本号形如 **`1.<第几波饰品>.<第几个修复版本>`**：

- 第一位 `1` —— 大版本，对应 1.21.1 / NeoForge 这一条线；
- 第二位 —— **第几波饰品**。每完成一批新的遗物物品就 +1；
- 第三位 —— **修复版本**。每完成一批修复（哪怕是工程性修复）就 +1。

例如 `1.2.0` 表示：1.21.1 线上的**第 2 波饰品**，尚未有额外修复版本。

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

## 移植进度

原版共 **34** 件物品，目前已移植 **11** 件。进度按「若干个物品一波」推进。

### 已完成（第 1 波：6 件）

| 物品 | 中文名 | 类型 | 研究 |
| --- | --- | --- | --- |
| `mining_charm` | 挖掘之魅 | 饰品（护符） | ✅ |
| `advanced_mining_charm` | 挖掘之魅-空灵 | 饰品（护符） | ✅ |
| `dimensional_mirror` | 空间魔镜 | 物品 | ✅ |
| `superposition_ring` | 叠加之指 | 饰品（戒指） | ✅ |
| `weather_stone` | 符文天象石 | 物品 | ✅ |
| `xp_tome` | 永恒智慧之书 | 物品 | ✅ |

### 已完成（第 2 波：5 件）

| 物品 | 中文名 | 类型 | 研究 |
| --- | --- | --- | --- |
| `ancient_aegis` | 远古之庇护 | 饰品（腰带） | ✅ |
| `dark_sun_ring` | 七阳之戒 | 饰品（戒指） | ✅ |
| `deific_amulet` | 神圣护身符 | 饰品（护身符） | ✅ |
| `oblivion_amulet` | 湮灭护符 | 饰品（护身符） | ✅ |
| `terror_crown` | 恐惧之冠 | 护甲 + 饰品（头饰） | ✅ |

第 2 波同时引入了：
研究分类「失落遗物学」延续、共 12 个研究词条、11 个灌注配方；
自定义伤害类型（湮灭 / 超维）、物品数据组件（储存伤害、无敌帧冷却）；
以及用 **Curios `ICurioRenderer` + 烘焙模型层** 重写的恐惧之冠佩戴渲染
（取代 1.12.2 往 `RenderPlayer` 挂渲染层的做法）。

### 待移植（23 件）

`chaos_core`（混沌之核）、`shiny_stone`（日耀石）、`paradox`（悖论之刃）、
`oblivion_stone`（遗忘之石）、`arcanum`（浑浊之核）、`dormant_arcanum`（休眠浑浊之核）、
`omega_core`（欧米伽之核）、`false_justice`（虚伪审判）、
`tome_of_broken_fates`（破碎的命运巨著）、`tome_of_predestiny`（命运巨著）、
`nuclear_fury`（原子之怒）、`crimson_spell`（血腥咒书）、
`devourer_of_the_void`（虚空吞噬者之书）、`eldritch_spell`（邪术咒书）、
`tome_of_lunar_flares`（月耀咒书）、`tome_of_discord`（错位之典）、`soul_tome`（千魂号令之典）、
`apotheosis`（王之宝典）、`tome_of_primal_chaos`（元始混沌之书）、`thunderpeal`（霹雳咒书）、
`edict_of_banishment`（炼狱放逐咒书）、`void_grimoire`（深渊之魔书）、`discord_ring`（不和谐之戒）

> 剩余的书籍类物品大多需要自定义弹射物实体与渲染器，届时同样采用现代化的写法。

> 贴图、模型与语言键已随首次提交整批就位（`assets/` 下 34 套已齐），
> 因此后续每个物品只要补 Java 实现、配方与研究即可。

---

## 从源码构建

需要 **JDK 21**。

```bash
# Windows
set JAVA_HOME=D:\Java\Java21
gradlew.bat build

# Linux / macOS
export JAVA_HOME=/path/to/jdk-21
./gradlew build
```

产物位于 `build/libs/forgotten_relics-<版本>.jar`。

### 调试运行

仓库内已提交 `.run/` 下的共享运行配置（`runClient` / `runServer` / `runData`），
IDEA 打开项目后即可直接选用，无需重新生成。

也可直接用 Gradle：

```bash
gradlew.bat runClient
gradlew.bat runServer
gradlew.bat runData
```

> `.run/` 里 `-Dfml.modFolders` 的路径**必须加引号** —— 本项目路径含空格，
> 不加引号会导致 `Error: could not open 'F:\Deepseek'`。

---

## 目录结构

```
src/main/java/com/beiwu/forgottenrelics_re/
├── ForgottenRelics.java        # 模组主类
├── FRCommonEvents.java         # 公共事件
├── FRDamageEvents.java         # 伤害事件（减伤、转嫁、反弹、吸收）
├── client/                     # 客户端渲染（饰品佩戴渲染、模型层注册）
├── config/FRConfig.java        # 配置（对应原版 RelicsConfigHandler）
├── items/                      # 物品实现
├── registry/                   # 物品、护甲材质、数据组件、创造模式标签页注册
└── utils/                      # 工具类（冷却、饰品栏、音效、伤害类型等）

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
- **NNYYOONNIIOO** —— 1.12.2 移植版 Forgotten Relics RE，本项目的主要参考；
- **Thaumaturge** 团队 —— 高版本神秘时代的实现；
- **Botania**、**Curios** 团队 —— 前置模组。
