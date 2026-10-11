# Forgotten Relics Plus 1.7.1

## 1.21.1 / NeoForge

This update adapts Forgotten Relics Plus to Thaumaturge 1.0.0 and adds research notes to every crafting research entry. Thaumaturge 1.0.0 or newer and Lithostitched 1.8.0 or newer are required, alongside Botania and Curios.

### Additions

- Require a solved and learned research note before the next page reveals a relic's crafting recipe. All 35 crafting research entries use themed aspect anchors and complexity levels from 2 to 8. Existing research prerequisites and item requirements still apply; the dormant core shares the Arcanum research.
- Add independent Omega Core research with complexity 8. Its research and infusion recipe load only when Re:Avaritia is installed.
- Add a configurable interval for the Terror Crown's area debuffs, defaulting to 5 ticks. Gaze effects and mana repair retain their existing tick frequency.

### Fixes

- Adapt rechargeable relics to Thaumaturge's charge profile components. Preserve charge consumption, recharging and HUD display, and update carried stacks to the configured capacity.
- Preserve the Terror Crown's revealing ability through Thaumaturge's goggles component.
- Tick telekinesis timers once per player and clear cached state on logout, player replacement and server shutdown.
- Validate Dimensional Mirror coordinates and dimension identifiers, and clear invalid bindings without removing unrelated item data.
- Replace stale IDEA launch paths with shared Gradle run configurations that work after moving the project.

### Changes

- Reduce repeated entity queries and random-selection work in Thunderpeal chain attacks while preserving damage behavior.
- Reuse carried-item scans for defensive damage handling and remove duplicate behavior dispatch.
- Update the project name to Forgotten Relics Plus and rename its Java package to `com.beiwu.forgottenrelicsplus`.
- Require Lithostitched for the current Thaumaturge world generation system. TerraBlender is no longer a required dependency.

### Other (internal refactors, build, documentation)

- Generate the research category and all 37 research entries through Thaumaturge's datagen interfaces.
- Pin Thaumaturge 1.0.0 to upstream 1.21.1 revision `b4670aea`. Pin development JEI to 19.36.0.360 to match the upstream integration.
- Split research definitions into focused providers and remove source comments.
- Lock dependency versions and SHA-256 hashes, add dependency setup tooling, and verify research references, recipe gates, dependency cycles and release JAR resources.
- Add regression checks for mirror bindings, target timers and random sampling, plus datagen checks for 21 rechargeable relics, the crown's revealing component and 35 research note gates across 32 seeds each.
- Document research note difficulty and provide shared Client, Server and Data configurations for IDEA.

---

# Forgotten Relics Plus 1.7.1

## 1.21.1 / NeoForge

本次更新将 Forgotten Relics Plus 适配至 Thaumaturge 1.0.0，并为全部合成研究加入研究笔记。需要 Thaumaturge 1.0.0 及以上、Lithostitched 1.8.0 及以上，以及 Botania 和 Curios。

### 新增

- 全部 35 个合成研究都需要先解开并学习研究笔记，再解锁下一页的合成方式。笔记采用对应主题的要素锚点，复杂度为 2～8。原有前置研究和物品要求继续生效；休眠核心共享浑浊之核的研究。
- 为欧米伽之核增加复杂度 8 的独立研究。研究与注魔配方仅在安装 Re:Avaritia 时加载。
- 增加恐惧之冠范围减益效果的执行间隔配置，默认每 5 tick 执行一次。凝视效果和魔力修复保持原有频率。

### 修复

- 将可充能遗物适配至 Thaumaturge 的充能数据组件，保留充能、消耗和 HUD 显示，并按配置更新玩家携带物品的容量。
- 通过 Thaumaturge 的揭示组件保留恐惧之冠的揭示能力。
- 念力计时器按玩家每 tick 更新一次，并在退出、玩家实体替换和服务端停止时清理缓存。
- 校验次元魔镜的坐标和维度标识，清除无效绑定时保留其他物品数据。
- 将 IDEA 中失效的旧目录启动路径替换为共享 Gradle 运行配置，移动项目后仍可使用。

### 调整

- 减少雷鸣连锁攻击中重复的实体查询与随机选择开销，保留原有伤害行为。
- 在防御伤害处理中复用携带物品扫描结果，避免重复分发同一行为。
- 将项目名称统一为 Forgotten Relics Plus，Java 包名调整为 `com.beiwu.forgottenrelicsplus`。
- 配合新版 Thaumaturge 的世界生成系统使用 Lithostitched，不再要求安装 TerraBlender。

### 其他（内部重构、构建、文档）

- 通过 Thaumaturge 的 datagen 接口生成研究分类及全部 37 个研究条目。
- 将 Thaumaturge 1.0.0 固定至上游 1.21.1 分支提交 `b4670aea`；开发环境 JEI 固定为 19.36.0.360，与上游兼容接口保持一致。
- 将研究定义拆分为职责明确的生成器，并移除源码注释。
- 锁定依赖版本与 SHA-256，增加依赖安装工具，并校验研究引用、配方解锁条件、循环依赖及发布 JAR 资源。
- 增加魔镜绑定、目标计时器和随机采样的回归检查；datagen 同时检查 21 件充能遗物、恐惧之冠揭示组件，以及 35 个研究笔记解锁条件，每个研究使用 32 个随机种子验证。
- 补充研究笔记难度文档，并提供 IDEA 共享 Client、Server、Data 配置。
