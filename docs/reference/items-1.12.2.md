# Forgotten Relics RE 1.12.2 逐物品移植规格清单

> 本文只依据反编译源码与资源文件的实际内容整理。所有数字均从代码中抄出；
> 无法从代码确认的标注「待确认」；代码中不存在的一律写「无」，不做推测。
>
> 源码根 `F:\Deepseek Harness\Forgotten Relics Unofficial\Forgotten-Relics-RE-main\src\main\java\com\forgottenrelics\forgotten_relics\`
> 资源根 `F:\Deepseek Harness\Forgotten Relics Unofficial\Forgotten-Relics-RE-main\src\main\resources\assets\forgotten_relics\`
>
> 模组元数据（Main.java:111）：`@Mod(modid="forgotten_relics", name="Forgotten Relics RE", version="0.7.0", acceptedMinecraftVersions="[1.12.2]")`
> **矛盾点**：mcmod.info 里的 version 是 `1.0.0`，与 `@Mod` 的 `0.7.0` 不一致。

---

## 0. 全局约定

| 项目 | 值 | 出处 |
|---|---|---|
| 物品注册名 | `forgotten_relics:<registryName>` | ItemModBase.java:36 |
| 创造标签页 | `tabForgottenRelics`（图标 = `CommonProxy.terrorCrown`） | Main.java:223-229 |
| 堆叠上限 | 下面逐项核对的 **34 个已注册物品全部为 1** | 各构造器 |
| 网络频道 | `RelicsChannel`（SimpleNetworkWrapper） | Main.java:130 |
| 饰品槽位数硬编码 | 部分代码写死 `i < 7` 或 `setStackInSlot(6, ...)` | ItemOmegaCore / ItemArcanum |

**物品总数**：`CommonProxy.registerItems()` 实际注册 **34** 个（CommonProxy.java:118-153）。
`items/` 目录下有 **35** 个具体物品类，多出的一个是 **`ItemDiscordRing`（未在任何地方注册或实例化）**，见文末附录。
另有 3 个抽象基类：`ItemModBase`、`ItemBaubleBase`、`ItemBaubleBaseModifier`。

**统一继承链**：

```
Item (vanilla)
 ├─ ItemModBase (abstract, implements IModelRegister[Botania])
 │   └─ ItemBaubleBase (abstract, implements IBauble, ICosmeticAttachable, IPhantomInkable)
 │       ├─ ItemBaubleBaseModifier (abstract, 属性修饰钩子 fillModifiers)
 │       │   ├─ ItemMiningCharm
 │       │   ├─ ItemAdvancedMiningCharm
 │       │   └─ ItemAncientAegis
 │       ├─ ItemArcanum / ItemDormantArcanum
 │       ├─ ItemDarkSunRing
 │       ├─ ItemDeificAmulet
 │       ├─ ItemOblivionAmulet
 │       ├─ ItemShinyStone
 │       ├─ ItemSuperpositionRing
 │       └─ ItemRingOfDiscord
 ├─ ItemSword  ── ItemParadox
 └─ ItemArmor  ── ItemTerrorCrown
```

**`ItemBaubleBase` 的共享行为（所有饰品必须复刻）**
- `onWornTick`：若 NBT `playerHashcode` ≠ 玩家 hashCode → 调 `onEquippedOrLoadedIntoWorld` 并写回 hash（ItemBaubleBase.java:181-186）。
- `onItemRightClick`：右键穿戴。遍历 `BaublesApi.getBaublesHandler(player)`，找第一个 `isItemValidForSlot` 的槽位写入，调 `onEquipped`，并发放 Botania 进度触发器 `botania:main/bauble_wear`（108-143 行）。
- 静态 `onDeath`（`@SubscribeEvent`，类级 `@Mod.EventBusSubscriber(modid="botania")`）：死亡且未开 keepInventory 且非旁观时遍历 Baubles 槽位，**但只对 `getRegistryName().getResourceDomain().equals("botania")` 的物品调 `onUnequipped`**（95-105 行）。
  **这是原代码的 bug**：本模组物品 domain 是 `forgotten_relics`，永远不命中，故本模组饰品的属性修饰在死亡时不会被移除。
- `addStringToTooltip` 会把 `&` 替换成 `§`（170 行）。
- NBT 键：装饰物 `cosmeticItem`、幻影墨水 `phantomInk`、UUID `baubleUUIDMost`/`baubleUUIDLeast`、玩家 hash `playerHashcode`（84-88 行）。
- 三个饰品基底类都实现 Botania 的 `IPhantomInkable` / `ICosmeticAttachable`，移植时若目标版本无 Botania，整套 API（`getCosmeticItem` / `setCosmeticItem` / `hasPhantomInk` / `setPhantomInk` / `getContainerItem`）都要找替代或删掉。

**统一附魔光效约定**：`hasEffect()` 返回 true 的物品 = Apotheosis, Crimson Spell, Eldritch Spell, Tome of Discord, Tome of Lunar Flares, Tome of Predestiny, Tome of Primal Chaos, Nuclear Fury, Thunderpeal, Edict of Banishment, Void Grimoire, Soul Tome, Devourer of the Void, Omega Core, False Justice；返回 false 的 = Terror Crown（显式 false）；其余按 NBT/状态。

**统一稀有度**：除 `ItemMiningCharm` = UNCOMMON 外，其余所有物品 `getRarity` 均为 EPIC。

---

## 1. 逐物品清单

> 顺序 = `CommonProxy.registerItems()` 的注册顺序。
> 「研究」字段格式：条目 key / 分类 / 要素需求 / 父研究 / meta / 坐标。所有条目分类均为 `FORGOTTEN_RELICS`。
> 「配方」全部为 Thaumcraft 灌注（`ThaumcraftApi.addInfusionCraftingRecipe`），除非另行注明。
### forgotten_relics:advanced_mining_charm  （Ethereal Mining Charm / 挖掘之魅-空灵）
- 类：`ItemAdvancedMiningCharm`，父类：`ItemBaubleBaseModifier` ← `ItemBaubleBase` ← `ItemModBase` ← `Item`
- 类型：饰品(Bauble)
- 堆叠上限：`this.maxStackSize = 1`（ItemAdvancedMiningCharm.java:45）
- 关键常量/数值：`getBaubleType = BaubleType.CHARM`；`getRarity = EPIC`；属性修饰目标 `EntityPlayer.REACH_DISTANCE`，operation = 0（加法），值 = `advancedMiningCharmReach`，修饰符不保存（`.setSaved(false)`）
- 配置项：`advancedMiningCharmBoost` / Generic Config / 默认 `3.0f`；`advancedMiningCharmReach` / Generic Config / 默认 `4.0f`（两者范围 0~1024）
- 行为：
  - **挖掘速度**（RelicsEventHandler.miningStuff，`PlayerEvent.BreakSpeed`，priority LOWEST）：玩家任一 Bauble 槽戴本物品 → `miningBoost += advancedMiningCharmBoost`；最终 `event.setNewSpeed(getNewSpeed() * miningBoost)`，初始 `miningBoost = 1.0f`。与 Mining Charm 同时佩戴会叠加（RelicsEventHandler.java:94-104）。
  - **触及距离**：`ItemBaubleBaseModifier.onEquippedOrLoadedIntoWorld` 里 `player.getAttributeMap().applyAttributeModifiers(...)`，`onUnequipped` 里 `removeAttributeModifiers(...)`（ItemBaubleBaseModifier.java:26-42）。
  - **工具提示**：`(int)(boost * 100.0f)` % 与 `reach` 格（54-55 行）。
- 配方：**有，灌注**。FRRecipes.java:65；研究名 `ADVANCED_MINING_CHARM`，instability **8**；要素 TOOL 250 / AURA 150 / CRYSTAL 185 / EXCHANGE 150 / MOTION 125 / MAGIC 140 / **TOOL 175**（TOOL 被重复 add，疑似原代码 bug，`AspectList.add` 对同 key 的覆盖行为**待确认**）；中心物品 = `CommonProxy.MiningCharm`；外围 12 个：`ItemsTC.visResonator`, `ItemsTC.crystalEssence`, `"ingotElvenElementium"`, `"elvenDragonstone"`, `"eternalLifeEssence"`, `ItemsTC.crystalEssence`, `ItemsTC.voidPick`, `ItemsTC.crystalEssence`, `"eternalLifeEssence"`, `"elvenDragonstone"`, `"ingotElvenElementium"`, `ItemsTC.crystalEssence`。
- 研究：条目 `ADVANCED_MINING_CHARM` / 分类 `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;1` + `THEORY;ARTIFICE;1`，`required_item = thaumcraft:void_pick`，`required_craft = forbidden_relics:mining_charm`（**原资源 modid 拼写错误，应为 forgotten_relics**）/ 父研究 `MINING_CHARM`、`!EternalLifeEssence`、`!MiningCharm`；meta `HIDDEN`；坐标 [3,-4]；第 2 阶段解锁配方 `forgotten_relics:advanced_mining_charm`。
- 贴图/模型：`models/item/advanced_mining_charm.json` → `forgotten_relics:items/advanced_mining_charm`；文件 `textures/items/advanced_mining_charm.png`

### forgotten_relics:mining_charm  （Mining Charm / 挖掘之魅）
- 类：`ItemMiningCharm`，父类：`ItemBaubleBaseModifier` ← `ItemBaubleBase` ← `ItemModBase` ← `Item`
- 类型：饰品(Bauble)
- 堆叠上限：`this.maxStackSize = 1`（ItemMiningCharm.java:45）
- 关键常量/数值：`getBaubleType = BaubleType.CHARM`；`getRarity = **UNCOMMON**`（全模组唯二的非 EPIC，另一个是它自己）；属性修饰目标 `EntityPlayer.REACH_DISTANCE`，operation 0，值 = `miningCharmReach`
- 配置项：`miningCharmBoost` / Generic Config / 默认 `1.0f`；`miningCharmReach` / Generic Config / 默认 `2.0f`
- 行为：
  - **挖掘速度**：同 Advanced Mining Charm，公式 `speed * (1.0 + miningCharmBoost)`，两者可叠加。
  - **触及距离**：同 Advanced Mining Charm，走属性修饰。
  - **工具提示**：`(int)(boost * 100.0f)` 与 `reach` 格。
- 配方：**有，灌注**。FRRecipes.java:63；研究名 `MINING_CHARM`，instability **1**；要素 TOOL 200 / MOTION 200 / METAL 175 / MAGIC 200；中心 = `vazkii.botania.common.item.ModItems.reachRing`；外围 8 个：`ItemsTC.elementalPick`, `crystal(EARTH)`, `ItemsTC.salisMundus`, `"ingotGold"`, 长效迅捷药水（`PotionTypes.LONG_SWIFTNESS`）, `crystal(EARTH)`, `ItemsTC.salisMundus`, `"ingotGold"`。
- 研究：条目 `MINING_CHARM` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;1` + `THEORY;ARTIFICE;1`，`required_item = thaumcraft:thaumium_pick` / 父 `GENERICTHEORY`、`INFUSION`；meta `ROUND`；坐标 [2,-2]。
- 贴图/模型：`models/item/mining_charm.json` → `forgotten_relics:items/mining_charm`

### forgotten_relics:ancient_aegis  （Ancient Aegis / 远古之庇护）
- 类：`ItemAncientAegis`，父类：`ItemBaubleBaseModifier` ← `ItemBaubleBase` ← `ItemModBase` ← `Item`
- 类型：饰品(Bauble)
- 堆叠上限：1（继承 `ItemBaubleBase` 的 `setMaxStackSize(1)`）
- 关键常量/数值：`getBaubleType = BaubleType.**BELT**`（不是 RING，尽管其研究父节点是戒指）；`getRarity = EPIC`；属性修饰目标 `SharedMonsterAttributes.KNOCKBACK_RESISTANCE`，operation 0，值 = `ancientAegisKnockbackResistance`；**代伤比例硬编码**：转移 `amount * 0.4f`，原受伤者保留 `amount * 0.6f`；**代伤搜索半径硬编码 32**
- 配置项：`ancientAegisDamageReduction` / Generic Config / 默认 `0.25f`；`ancientAegisHealAmount` / 默认 `1.0f`；`ancientAegisHealInterval` / 默认 `20`；`ancientAegisKnockbackResistance` / 默认 `1.0f`
- 行为：
  - **自我回血**（`onWornTick`，ItemAncientAegis.java:66-71）：服务端且 `entity.ticksExisted % ancientAegisHealInterval == 0` 且当前血量 < 最大血量 → `entity.heal(ancientAegisHealAmount)`。
  - **减伤**（RelicsEventHandler.onEntityHurt，228-230 行）：玩家戴着 Aegis 且伤害非绝对型 → `event.setAmount(amount * (1.0f - ancientAegisDamageReduction))`。
  - **代伤**（RelicsEventHandler.onEntityHurt，224-227 行）：受伤玩家**没有**戴 Aegis 时，调 `SuperpositionHandler.findPlayerWithBauble(world, 32, AncientAegis, player)` 在 **32** 格内随机挑一个戴 Aegis 的玩家，让其承担 `amount * 0.4f`，原受伤者减为 `amount * 0.6f`。注意 0.4 + 0.6 = 1.0，是**伤害转移**不是额外减伤。
- 配方：**有，灌注**。FRRecipes.java:66；研究名 `ANCIENT_AEGIS`，instability **5**；要素 PROTECT 175 / EXCHANGE 125 / LIFE 100 / MAGIC 30 / METAL 20；中心 = `new ItemStack(ItemsTC.baubles, 1, 6)`；外围 8 个：`"elvenDragonstone"`, 强力治疗药水（`PotionTypes.STRONG_HEALING`）, `"ingotGold"`, `ItemsTC.fabric`, `ModItems.knockbackBelt`, `ItemsTC.fabric`, `"ingotGold"`, 强力治疗药水。
- 研究：条目 `ANCIENT_AEGIS` / `FORGOTTEN_RELICS` / 要素 `THEORY;ARTIFIICE;1` + `THEORY;INFUSION;1` + `THEORY;ELDRITCH;1`（**`ARTIFIICE` 是原资源里的拼写错误，应为 `ARTIFICE`**），`required_craft = botania:knockbackbelt`，`warp = 2` / 父 `RING_OF_SUPERPOSITION`、`!DragonStone`；meta `HIDDEN`；坐标 [4,2]。
- 贴图/模型：`models/item/ancient_aegis.json` → `forgotten_relics:items/ancient_aegis`

### forgotten_relics:chaos_core  （Chaos Core / 混沌之核）
- 类：`ItemChaosCore`，父类：`Item`（直接继承），implements `IWarpingGear`
- 类型：普通物品
- 堆叠上限：`this.maxStackSize = 1`（ItemChaosCore.java:53）
- 关键常量/数值：触发概率硬编码 `2.08E-4`（= 0.000208，**未使用配置里的 `chaosCorePotionChance`**）；药水 ID 随机 `1 + (int)(Math.random() * 21.0)` → 1~21，若结果 == 6 或 7 则改成 **20**；持续时间硬编码 `100 + (int)(Math.random() * 2400.0)`（100~2500 tick）；等级硬编码 `(int)(Math.random() * 3.0)`（amplifier 0~2）；`getWarp = RelicsConfigHandler.chaosCoreWarp`；**攻击转移概率硬编码 0.45 / 受击转移 0.42 / 自伤概率 0.15 / 范围 ±16**；**受击伤害倍率 `Math.random() * 2.0`（0~2 倍）**
- 配置项：`chaosCoreWarp` / Chaos Core / 默认 `2`；`chaosCorePotionChance` 默认 `2.08E-4f`、`chaosCorePotionDurationMin` 默认 `100`、`chaosCorePotionDurationMax` 默认 `2500`、`chaosCorePotionLevelMin` 默认 `0`、`chaosCorePotionLevelMax` 默认 `3` —— **这 5 个配置项在代码里只被声明和赋值，没有任何引用，全部被硬编码值取代**
- 行为：
  - **onUpdate（背包/手持 tick）**（ItemChaosCore.java:59-67）：服务端且 `Math.random() <= 2.08E-4` → 给**持有者本人**（`entity` 被强转 `EntityLivingBase`）加一个随机原版药水效果，amplifier 0~2，时长 100~2500 tick，不显示粒子、不显示图标。
  - **攻击时转移**（RelicsEventHandler.onEntityAttacked，123-135 行）：攻击者主背包里有 Chaos Core 且 `Math.random() < 0.45` → 在被打者周围 **16** 格内随机挑一个实体，转移 `amount * (Math.random() * 2.0)` 的伤害；另有 **15%** 概率改为打攻击者自己；然后 `event.setCanceled(true)`。
  - **受击时转移**（同方法 136-143 行）：被打者是玩家且背包有 Chaos Core 且 `Math.random() < 0.42` → 把 `amount * (Math.random() * 2.0)` 转给周围 16 格内随机实体并取消。
  - **受击伤害随机化**（onEntityHurt，214-216 行）：背包有 Chaos Core → `event.setAmount(amount * (float)(Math.random() * 2.0))`。
- 配方：**有，灌注**。FRRecipes.java:69；研究名 `CHAOS_CORE`，instability **10**；要素 ENTROPY 100 / ORDER 100 / EXCHANGE 200 / MAGIC 125；中心 = `ItemsTC.visResonator`；外围 8 个：`"elvenDragonstone"`, `"elvenPixieDust"`, `"ingotVoid"`, `"elvenPixieDust"`, `ItemsTC.alumentum`, `"elvenPixieDust"`, `"ingotVoid"`, `"elvenPixieDust"`。
- 研究：条目 `CHAOS_CORE` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;1` + `THEORY;ELDRITCH;1`，`warp = 2` / 父 `OBLIVION_STONE`、`!DragonStone`、`!PixieDust`、`!VoidIngot`；meta `ROUND`；坐标 [5,-4]。
- 贴图/模型：`models/item/chaos_core.json` → `forgotten_relics:items/chaos_core`

### forgotten_relics:dark_sun_ring  （Ring of The Seven Suns / 七阳之戒）
- 类：`ItemDarkSunRing`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`, `IRechargable`
- 类型：饰品(Bauble)
- 堆叠上限：1
- 关键常量/数值：`getBaubleType = BaubleType.RING`；`getRarity = EPIC`；`getMaxCharge = darkSunRingMaxCharge`；**免伤伤害类型列表只在 `Main.init` 静态块里硬编码 3 项**：`DamageSource.LAVA.damageType`、`DamageSource.IN_FIRE.damageType`、`DamageSource.ON_FIRE.damageType`（Main.java:208-210）；**huatResistantTime 重置值硬编码 20**；**大伤害判定阈值硬编码 100.0f**；**随机加伤概率硬编码 0.25**
- 配置项：`darkSunRingMaxCharge` / Vis / 默认 `500`；`darkSunRingDamageCap` / Generic Config / 默认 `100.0f`；`darkSunRingDeflectChance` / Generic Config / 默认 `0.2f`；`darkSunRingHealLimit` / Generic Config / 默认 `false`
- 行为：
  - **免伤 + 回血**（RelicsEventHandler.onEntityAttacked，152-161 行）：伤害类型在 `Main.darkRingDamageNegations` 中时取消伤害并回血。若 `darkSunRingHealLimit == true`，则仅在 `hurtResistantTime == 0` 时回血并将其设为 **20**。
  - **反弹**（162-166 行）：非上述伤害类型、来源实体存在、`Math.random() <= darkSunRingDeflectChance`（默认 0.2）且 `hurtResistantTime == 0` → 设 `hurtResistantTime = 20`，对**攻击者**造成同样数值的伤害（用同一个 DamageSource），取消原伤害。
  - **大伤害免疫**（onEntityHurt，217-220 行）：伤害 > **100.0f**（硬编码，非配置）且伤害非绝对型且戴戒指 → `SuperpositionHandler.sendNotification(player, 2)`（对应 `notification.overdamage_block`）并取消伤害。
  - **随机加伤**（221-223 行）：戴戒指且 `Math.random() <= 0.25`（硬编码）且伤害非绝对型 → `event.setAmount(amount + amount * (float)Math.random())`。
  - **onWornTick**：仅 `if (entity.isBurning()) entity.extinguish();`（自带灭火）。
  - **工具提示**：`(int)darkSunRingDamageCap` 与 `(int)(darkSunRingDeflectChance * 100.0f)` %（ItemDarkSunRing.java:56,59）。
- 配方：**有，灌注**。FRRecipes.java:67；研究名 `DARK_SUN_RING`，instability **8**；要素 FIRE 200 / PROTECT 185 / EXCHANGE 140 / DARKNESS 150 / MAGIC 100；中心 = `new ItemStack(ItemsTC.baubles, 1, 5)`；外围 10 个：`ModItems.superLavaPendant`, `"ingotElvenElementium"`, `Items.BLAZE_ROD`, `new ItemStack(BlocksTC.cinderpearl)`, `ItemsTC.voidSeed`, `"nitor"`, `ItemsTC.voidSeed`, `cinderpearl`, `BLAZE_ROD`, `"ingotElvenElementium"`。
- 研究：条目 `DARK_SUN_RING` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;INFUSION;1` + `THEORY;ELDRITCH;1`，`warp = 3` / 父 `RING_OF_SUPERPOSITION`、`!BloodPendant`、`!EternalLifeEssence`、`!BlazeRod`；meta `HIDDEN`；坐标 [6,1]。
- 贴图/模型：`models/item/dark_sun_ring.json` → `forgotten_relics:items/dark_sun_ring`
- **待确认/矛盾**：配置项 `darkSunRingDamageCap`（默认 100.0f）在代码里**只用于 tooltip 显示**（ItemDarkSunRing.java:56），真正的大伤害判定用的是硬编码 `100.0f`（RelicsEventHandler.java:217）。两者数值恰好相同，但**移植时不要把 cap 当成可调阈值**。

### forgotten_relics:shiny_stone  （Shiny Stone / 日耀石）
- 类：`ItemShinyStone`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`
- 类型：饰品(Bauble)
- 堆叠上限：1
- 关键常量/数值：`getBaubleType = BaubleType.CHARM`；`getRarity = EPIC`；**静止判定用精确 double 相等**（`entity.posX == LastX && posY == LastY && posZ == LastZ`）；`healCheckrate = (int)(shinyStoneCheckrate / 4.0)`（默认 4/4 = **1**）；回血间隔 HealRate 1 → `ticksExisted % (10 * rate)`，2 → `% (5 * rate)`，3 → `% (2 * rate)`，4 → `% (1 * rate)`；**粒子数初值硬编码 3**，随档位递减到 0
- 配置项：`shinyStoneCheckrate` / Generic Config / 默认 `4`（tick）；`shinyStoneStillThreshold2` / 默认 `40`；`shinyStoneStillThreshold3` / 默认 `80`；`shinyStoneStillThreshold4` / 默认 `200`；`shinyStoneStillIncrement` / 默认 `4.0f`；`shinyStoneHealAmount` / 默认 `1.0f`
- 行为：
  - **onWornTick**（ItemShinyStone.java:88-133，服务端）：每 `shinyStoneCheckrate` tick 比较当前位置与 NBT 中的 `LastX/LastY/LastZ`：
    - 位置完全没变 → `Static += shinyStoneStillIncrement`，并按 Static 越过 `StillThreshold2/3/4` 把 `HealRate` 设为 1/2/3/4；同时生成 `EntityShinyEnergy` 粒子（`for (counter = particleNumber; counter <= 3; counter++)`，粒子数从 3 递减到 0）。
    - 位置变了 → `Static = 0`、`HealRate = 0`。
  - 按 HealRate 用上面的间隔调用 `entity.heal(shinyStoneHealAmount)`。
  - **注意**：静止判定块与 HealRate 回血块**不在同一个 else 分支里**，回血块每 tick 都会执行。
- 配方：**有，灌注**。FRRecipes.java:71；研究名 `SHINY_STONE`，instability **8**；要素 LIFE 375 / TRAP 100 / EXCHANGE 100 / MAGIC 100 / CRYSTAL 200；中心 = 字符串 `"elvenDragonstone"`（**Botania 的矿石词典条目**）；外围 8 个：`ItemsTC.visResonator`, `"ingotGold"`, `"eternalLifeEssence"`, `"nitor"`, 附魔金苹果, `"nitor"`, `"eternalLifeEssence"`, `"ingotGold"`。
- 研究：条目 `SHINY_STONE` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;INFUSION;1`，`required_item = thaumcraft:verdant_charm` + `minecraft:golden_apple;1;1` / 父 `DEIFIC_AMULET`、`!DragonStone`、`!EternalLifeEssence`、`!EnchantedGoldenApple`；meta `HIDDEN`；坐标 [4,-6]。
- 贴图/模型：`models/item/shiny_stone.json` → `forgotten_relics:items/shiny_stone`

### forgotten_relics:terror_crown  （Crown of Terror / 恐惧之冠）
- 类：`ItemTerrorCrown`，父类：`ItemArmor`（材质 = `ItemArmor.ArmorMaterial.GOLD`），implements `IWarpingGear`, `IGoggles`, `IRevealer`, `IBauble`
- 类型：头盔(Armor) **且同时是饰品(Bauble HEAD)**
- 堆叠上限：1（ItemArmor 默认）
- 关键常量/数值：
  - 构造：`super(mat, 0, type)`，mat = `ArmorMaterial.GOLD`，renderIndex **0**，type = `EntityEquipmentSlot.HEAD`（CommonProxy.java:80）
  - `setMaxDamage(1000)`（ItemTerrorCrown.java:81）；`getItemEnchantability() = 0`；`hasEffect() = false`（显式）
  - 护甲贴图 = `forgotten_relics:textures/armor/crown_prs.png`（164-166 行）；渲染层 `LayerCrown` 也用同一张贴图（LayerCrown.java:28）
  - 可修复材料 = 金锭（168-170 行）
  - `canEquip` 限制：头部槽已经是本物品时不能重复戴（89-91 行）
  - 扫描目标用 `EntityUtils.getPointedEntity(world, player, 0.0, terrorCrownScanRange, 3.0f, false)`——**expand 参数硬编码 3.0f**
  - **Blindness amplifier 硬编码 2**（= 等级 III）；Wither **仅当目标当前没有 Wither 时才加**
  - `getWarp = terrorCrownWarp`
  - `ModelCrown`：贴图 64x32；盒体 8x3x8；戴头盔时 Y = **-12**，否则 **-11**；潜行时整体 Y 偏移 **0.2**（ModelCrown.java:30-42）
- 配置项：`terrorCrownWarp` / 默认 `3`；`terrorCrownHavocRange` / 默认 `24`；`terrorCrownScanRange` / 默认 `32.0f`；`terrorCrownBlindnessDuration` / 默认 `100`（amplifier 硬编码 2）；`terrorCrownWitherDuration` / 默认 `40`、`terrorCrownWitherLevel` / 默认 `0`；`terrorCrownNauseaDuration` / 默认 `100`、`terrorCrownNauseaLevel` / 默认 `1`；`terrorCrownSlownessDuration` / 默认 `30`、`terrorCrownSlownessLevel` / 默认 `1`；`terrorCrownWeaknessDuration` / 默认 `80`、`terrorCrownWeaknessLevel` / 默认 `2`；`terrorCrownManaCost` / 默认 `200`（Botania Mana）
- 行为：
  - **onArmorTick（同时被 onWornTick 转发调用）**（97-116 行）：
    1) `SuperpositionHandler.cryHavoc(world, player, terrorCrownHavocRange)` → 用 Botania `SubTileHeiseiDream.brainwashEntity(args, 8.2, Predicates.instanceOf(IMob.class), true, false, false)` 让半径内的怪互相攻击。
    2) `this.onUpdate(itemStack, world, player, 0, false)`。
    3) 服务端：对视线指向的 LivingBase 施加 Blindness(`terrorCrownBlindnessDuration`, **2**)、Wither(`terrorCrownWitherDuration`, `terrorCrownWitherLevel`)（**仅当目标当前没有 Wither**）、Nausea、Slowness、Weakness。整个过程包在 try/catch 里。
  - **onUpdate**（127-138 行）：移除物品上的 `ench` NBT（禁止附魔）；服务端且耐久 > 0 且 `ManaItemHandler.requestManaExact(stack, player, terrorCrownManaCost, true)` 成功 → 耐久 **-1**（用 Botania 魔力修耐久）。
  - **IGoggles / IRevealer**：`showIngamePopups = true`、`showNodes = true`（172-178 行）—— 用于显示灵气节点/揭示。
  - **getBaubleType = BaubleType.HEAD**（180-182 行）。
- 配方：**有，灌注**。FRRecipes.java:60；研究名 `TERROR_CROWN`，instability **10**；要素 ELDRITCH 75 / MOTION 75 / MAGIC 175 / DARKNESS 30 / VOID 30；中心 = `ItemsTC.goggles`；外围 8 个：`Items.NETHER_STAR`, `"runePrideB"`, `"ingotGold"`, `"eternalLifeEssence"`, `Items.ENDER_EYE`, `"eternalLifeEssence"`, `"ingotGold"`, `"runeWrathB"`。
- 研究：条目 `TERROR_CROWN` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;1` + `THEORY;ARTIFICE;1`，`required_item = botania:divacharm` + `botania:manaresource;1;5` / 父 `EXPERIENCE_TOME`、`!NetherStar`、`!VoidSeerCharm`；meta `HIDDEN`；坐标 [-7,-4]。
- 贴图/模型：`models/item/terror_crown.json` → `forgotten_relics:items/terror_crown`；armor 贴图 `textures/armor/crown_prs.png`；`textures/armor/crown_black.png` **未被任何代码引用**；渲染层 `ClientProxy.addRenderLayers` → `addLayersToSkin` 给 `default` 与 `slim` 两种皮肤都加了 `LayerCrown`（ClientProxy.java:117-126）。

### forgotten_relics:paradox  （The Paradox / 悖论之刃）
- 类：`ItemParadox`，父类：`ItemSword`，implements `IWarpingGear`
- 类型：普通物品（武器/剑）
- 堆叠上限：1（ItemSword 默认）
- 关键常量/数值：
  - 工具材质 `RelicsMaterialHandler.materialParadoxicalStuff` = `EnumHelper.addToolMaterial("PARADOXICALSTUFF", 4, 3000, 16.0f, -4.0f, 100)`（harvestLevel **4**, durability **3000**, efficiency **16.0f**, **damage -4.0f**, enchantability **100**）。RelicsMaterialHandler.java:14 —— **注意 damage 是负的**
  - 伤害拆分：`currentDamage = Math.random() * paradoxDamageCap`；对目标造成 `currentDamage`，对**自己**造成 `paradoxDamageCap - currentDamage`（两者都用 `DamageSource.causePlayerDamage(player)`）
  - `getWarp = paradoxWarp`
- 配置项：`paradoxDamageCap` / Damage Values / 默认 `200.0f`；`paradoxRepairRate` / 默认 `20`（tick）；`paradoxRepairAmount` / 默认 `1`；`paradoxWarp` / 默认 `8`
- 行为：
  - **onLeftClickEntity**（57-62 行）：返回 **true**（取消原版攻击），自己计算随机伤害分配给目标和自己，伤害的期望总和恒为 `paradoxDamageCap`。
  - **onUpdate**（82-86 行）：服务端且已掉耐久且 `entity.ticksExisted % paradoxRepairRate == 0` → `stack.damageItem(-paradoxRepairAmount, (EntityLivingBase)entity)`（自动修复）。
  - **工具提示劫持**（RelicsEventHandler.onTooltip，107-116 行）：把 tooltip 里含 `attribute.name.generic.attackDamage` 或 `Attack Damage` 的那一行**整行替换**为 `" " + I18n("item.ItemParadoxDamage_1.lore") + (int)paradoxDamageCap + I18n("item.ItemParadoxDamage_2.lore")`。
- 配方：**有，灌注**。FRRecipes.java:70；研究名 `PARADOX`，instability **32**（全模组最高）；要素 AIR 250 / FIRE 250 / WATER 250 / EARTH 250 / ORDER 250 / ENTROPY 250 / VOID 185 / AVERSION 125 / MAGIC 100 / EXCHANGE 140；中心 = `ItemsTC.voidSword`；外围 8 个：`chaosCore`, `crystal(AIR)`, `crystal(FIRE)`, `crystal(WATER)`, `pearlAny`, `crystal(EARTH)`, `crystal(ORDER)`, `crystal(ENTROPY)`。
- 研究：条目 `PARADOX` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;6` + `THEORY;INFUSION;6` + `THEORY;ELDRITCH;6`，`required_craft = forgotten_relics:chaos_core`，`warp = 5` / 父 `RING_OF_SUPERPOSITION`、`!ChaosCore`；meta `HIDDEN` + `HEX`；坐标 [7,-2]。
- 贴图/模型：`models/item/paradox.json` → `forgotten_relics:items/paradox`
### forgotten_relics:deific_amulet  （Deific Amulet / 神圣护身符）
- 类：`ItemDeificAmulet`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`, `IRechargable`
- 类型：饰品(Bauble)
- 堆叠上限：`this.setMaxStackSize(1)`（ItemDeificAmulet.java:61）
- 关键常量/数值：`getBaubleType = BaubleType.AMULET`；`getRarity = EPIC`；`getMaxCharge = deificAmuletMaxCharge`；NBT 键 `ICooldown`；**无敌延长触发条件硬编码 `hurtResistantTime > 10`**
- 配置项：`deificAmuletMaxCharge` / Vis / 默认 `200`；`deificAmuletEffectImmunity` / Generic Config / 默认 `true`；`deificAmuletOnlyNegatesDebuffs` / Generic Config / 默认 `false`；`deificAmuletInvincibility` / Generic Config / 默认 `true`；`deificAmuletInvincibilityExtension` / 默认 `40`；`deificAmuletInvincibilityCooldown` / 默认 `32`；`deificAmuletFireDuration` / 默认 `300`；`deificAmuletFireVisCost` / 默认 `10`；`deificAmuletVisCost`（字段名 `deificAmuletVisMult`）/ Vis Costs / 默认 `1.0f`
- 行为：**onWornTick（服务端）**（100-131 行），四步：
  1) `deificAmuletEffectImmunity` 为真时：若 `deificAmuletOnlyNegatesDebuffs` → 遍历药水效果列表的副本，只移除 `id.isBadEffect()` 为真的；否则 `entity.clearActivePotions()` 全清（含正面）。
  2) 若正在燃烧 → `entity.extinguish()`。
  3) 若 `entity.getAir() == 0`（溺水）且 `RechargeHelper.consumeCharge(stack, entity, (int)(deificAmuletFireVisCost * deificAmuletVisMult))` 成功 → `entity.setFire(deificAmuletFireDuration)`。**注意逻辑：花 Vis 的结果是「点燃自己」而不是免溺水**，移植时需原样复刻或标注为可疑设计。
  4) `deificAmuletInvincibility` 为真时：若 `ICooldown == 0` 且 `entity.hurtResistantTime > 10` → 把 `hurtResistantTime = deificAmuletInvincibilityExtension` 并设 `ICooldown = deificAmuletInvincibilityCooldown`；每 tick 若 `ICooldown > 0` 则 -1。
- 配方：**有，灌注**。FRRecipes.java:68；研究名 `DEIFIC_AMULET`，instability **4**；要素 MAN 125 / LIGHT 165 / AURA 200 / MAGIC 100 / LIFE 30 / EXCHANGE 20；中心 = `new ItemStack(ItemsTC.baubles, 1, 4)`；外围 8 个：`ModItems.lavaPendant`, `"eternalLifeEssence"`, `"elvenPixieDust"`, `"eternalLifeEssence"`, `ItemsTC.visResonator`, `"eternalLifeEssence"`, `"elvenPixieDust"`, `"eternalLifeEssence"`。
- 研究：条目 `DEIFIC_AMULET` / `FORGOTTEN_RELICS` / 要素 `THEORY;ARTIFICE;2` + `THEORY;INFUSION;2` / 父 `MINING_CHARM`、`!EternalLifeEssence`、`!PixieDust`；meta `HIDDEN`；坐标 [2,-5]。
- 贴图/模型：`models/item/deific_amulet.json` → `forgotten_relics:items/deific_amulet`

### forgotten_relics:xp_tome  （Tome of Ageless Wisdom / 永恒智慧之书）
- 类：`ItemXPTome`，父类：`Item`
- 类型：普通物品
- 堆叠上限：1
- 关键常量/数值：NBT 键 `IsActive`（默认 **false**）、`AbsorptionMode`（默认 **true**）、`XPStored`（默认 0）；`isFull3D() = false`；`hasEffect = IsActive`；`getRarity = EPIC`；常量 `TAG_ABSORPTION = "AbsorptionMode"`；等级换算用 Botania `ExperienceHelper.getLevelForExperience`
- 配置项：`xpTomeTransferRate` / XP Tome / 默认 `5`
- 行为：
  - **onUpdate**（服务端且 `IsActive` 为真，ItemXPTome.java:94-126）：
    - `AbsorptionMode == true`（吸收）：玩家 XP ≥ rate → 抽取 rate 存进 `XPStored`；XP 落在 (0, rate) 之间 → 全部抽干存入。
    - `AbsorptionMode == false`（释放）：`XPStored >= rate` → 扣 rate 还给玩家；`XPStored` 落在 (0, rate) 之间 → 全部还给玩家并清零。
    - 有动作时调 `player.inventoryContainer.detectAndSendChanges()`。
  - **onItemRightClick**（128-147 行）：不潜行 → 切换 `AbsorptionMode`（播 `ENTITY_PLAYER_LEVELUP`）；潜行 → 切换 `IsActive`（播 Thaumcraft `SoundsTC.fly`）。最后 `player.setActiveHand(hand)` 并返回 `super.onItemRightClick`。
  - 工具提示显示当前模式 + `XPStored` + 对应等级。
- 配方：**有，灌注**。FRRecipes.java:73；研究名 `EXPERIENCE_TOME`，instability **4**；要素 SOUL 125 / MIND 150 / EXCHANGE 30 / MAGIC 100；中心 = `new ItemStack(Items.WRITABLE_BOOK)`；外围 8 个：`BlocksTC.jarBrain`, `ItemsTC.salisMundus`, `ItemsTC.amber`, `ItemsTC.salisMundus`, `"nitor"`, `ItemsTC.salisMundus`, `ItemsTC.amber`, `ItemsTC.salisMundus`。
- 研究：条目 `EXPERIENCE_TOME` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;ARTIFICE;1` + `THEORY;INFUSION;1`，`required_item = thaumcraft:curio;1;1` + `minecraft:writable_book` / 父 `WEATHER_STONE`、`JARBRAIN`；坐标 [-5,-2]。
- 贴图/模型：`models/item/xp_tome.json` → `forgotten_relics:items/xp_tome`

### forgotten_relics:weather_stone  （Runic Stone / 符文天象石）
- 类：`ItemWeatherStone`，父类：`Item`，implements `IRechargable`
- 类型：普通物品
- 堆叠上限：1
- 关键常量/数值：`getVisCost() = (int)(weatherStoneVisCost * weatherStoneVisMult)`；`getMaxCharge = weatherStoneMaxCharge`；使用条件 `world.isRaining()`；施放时把 `rainTime` 设为 `24000 + (int)(Math.random() * 976000.0)`（24 秒 ~ 1000 秒）；`getItemUseAction = BOW`，use duration = `weatherStoneChannelDuration`；生成 **25** 个 Botania wisp 粒子；**`weatherStoneVisMult` 在静态块里先被初始化成 `1.0f`（RelicsConfigHandler.java:513），随后又被配置覆盖**
- 配置项：`weatherStoneVisCost` / Weather Stone / 默认 `25`；**同名键冲突**：`weatherStoneVisCost` 同时出现在 Vis Costs 分类（默认 `1.0f`，赋值给字段 `weatherStoneVisMult`）—— 见「公共数值」与风险点；`weatherStoneMaxCharge` / Vis / 默认 `100`；`weatherStoneChannelDuration` / 默认 `60`；`weatherStoneCooldown` / 默认 `100`
- 行为：
  - **onItemRightClick**：只有 `world.isRaining()` 且不在冷却中才 `setActiveHand(hand)`。
  - **onUsingTick**（ItemWeatherStone.java:104-123）：`count == 1`（引导结束）且仍在降雨且扣 Vis 成功 → 生成 25 个 wisp 粒子、播放 `ModSounds.altarCraft`、`getWorldInfo().setRaining(false)`、重设 `rainTime`、`SuperpositionHandler.setCasted(player, weatherStoneCooldown, false)`。
  - 工具提示显示 `getVisCost()`（ItemWeatherStone.java:82）。
- 配方：**有，灌注**。FRRecipes.java:62；研究名 `WEATHER_STONE`，instability **4**；要素 MOTION 65 / AIR 85 / WATER 35 / EXCHANGE 30 / ENERGY 75；中心 = `BlocksTC.stoneArcane`；外围 8 个：`"eternalLifeEssence"`, `Items.GHAST_TEAR`, `"runeAirB"`, `new ItemStack(ItemsTC.celestialNotes, 1, 0)`, `"nitor"`, `new ItemStack(ItemsTC.celestialNotes, 1, 5)`, `"runeAirB"`, `Items.GHAST_TEAR`。
- 研究：条目 `WEATHER_STONE` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;ARTIFICE;1` + `THEORY;INFUSION;1`，`required_craft = botania:teruterubozu` / 父 `GENERICTHEORY`、`INFUSION`；坐标 [-3,0]。
- 贴图/模型：`models/item/weather_stone.json` → `forgotten_relics:items/weather_stone`

### forgotten_relics:superposition_ring  （Ring of Superposition / 叠加之指）
- 类：`ItemSuperpositionRing`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`
- 类型：饰品(Bauble)
- 堆叠上限：1
- 关键常量/数值：`getBaubleType = BaubleType.RING`；`getRarity = EPIC`；交换触发：`entity.ticksExisted % superpositionRingCheckInterval == 0` 且 `Math.random() <= superpositionRingSwapChance`；**伤害分摊比例硬编码** `percent = 0.12 + Math.random() * 0.62`（0.12~0.74）；每个被分摊者承受 `splitAmount / superpositioned.size()`；**位置互换用 `transferPlayerToDimension` + `getDefaultTeleporter`，可跨维度**
- 配置项：`superpositionRingSwapChance` / Superposition Ring / 默认 `0.025f`；`superpositionRingCheckInterval` / Superposition Ring / 默认 `600`（tick）
- 行为：
  - **onWornTick**（ItemSuperpositionRing.java:87-108，服务端）：按间隔随机触发，从所有戴 Superposition Ring 的在线玩家中随机挑一个（排除自己），**跨维度传送**后互换两者坐标；每次交换播放 `ENTITY_ENDERMEN_TELEPORT`。
  - **伤害分摊**（RelicsEventHandler.onEntityHurt，231-253 行）：伤害来源不是 `DamageSourceSuperposition` / `DamageSourceSuperpositionDefined`、玩家戴着戒指、事件未取消 → 构造 `DamageSourceSuperpositionDefined(source.getTrueSource())` 或 `DamageSourceSuperposition()`，继承原来源的 `isUnblockable()` / `isDamageAbsolute()` 与 `damageType`，把 `amount * percent` 平均分给其他所有戴戒指的玩家，并从自己的伤害里扣掉。
  - `SuperpositionHandler.getBaubleOwnersList` 返回的是**当前服务器所有在线且戴戒指的玩家**（不限制距离/维度），因此分摊可跨世界。
- 配方：**有，灌注**。FRRecipes.java:61；研究名 `RING_OF_SUPERPOSITION`，instability **4**；要素 ELDRITCH 125 / EXCHANGE 100 / MOTION 75 / DARKNESS 75 / PROTECT 30；中心 = `new ItemStack(ItemsTC.baubles, 1, 1)`；外围 8 个：`Items.ENDER_EYE`, `new ItemStack(ModItems.manaResource, 1, 15)`, `ItemsTC.voidSeed`, `ItemsTC.salisMundus`, `"gemEmerald"`, `ItemsTC.salisMundus`, `ItemsTC.voidSeed`, `manaResource:15`。
- 研究：条目 `RING_OF_SUPERPOSITION` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;ARTIFICE;1` + `THEORY;INFUSION;1` / 父 `GENERICTHEORY`、`INFUSION`；坐标 [3,0]。
- 贴图/模型：`models/item/superposition_ring.json` → `forgotten_relics:items/superposition_ring`

### forgotten_relics:oblivion_stone  （Keystone of The Oblivion / 遗忘之石）
- 类：`ItemOblivionStone`，父类：`Item`，implements `IWarpingGear`
- 类型：普通物品（用 item damage 当模式状态机）
- 堆叠上限：1；`setMaxDamage(0)`（ItemOblivionStone.java:61-62）
- 关键常量/数值：
  - **模式编码**：`damage < 100` = 启用，mode = `damage % 100`（0/1/2）；`damage >= 100` = 停用（mode = `damage - 100`）
  - 右键（不潜行）循环 0→1→2→0；潜行右键在 (0/1/2) 与 (+100) 之间切换（113-114 行的 `setItemDamage` 逻辑）
  - `onUpdate` 每 **10** tick 执行一次（`entity.ticksExisted % 10 == 0`），且只在**非停用**（damage < 100）且**有 NBT** 时执行
  - 注册模型时循环 `i = 0 .. 104`（ClientProxy 调 `oblivionStone.registerModels()`，内部按 metadata 批量注册）
  - NBT 键：`SupersolidID`（int[] item id）、`SupersolidMetaID`（int[] meta，**-1 表示忽略 meta**，只有不可损坏物品才记实际 damage）
- 配置项：`oblivionStoneHardCap` / Generic Config / 默认 `64`（NBT 列表硬上限，配方中添加时判定）；`oblivionStoneSoftCap` / Generic Config / 默认 `28`（**仅用于 tooltip 显示截断，代码中搜不到其它引用**）
- 行为：
  - **onItemRightClick**（72-95 行）：切换模式（见上），播放 `ENTITY_EXPERIENCE_ORB_PICKUP`。
  - **onUpdate**（97-111 行）：每 10 tick 调 `consumeStuff(player, SupersolidID, SupersolidMetaID, mode)`。
  - **consumeStuff（静态）**（119 行起）：把玩家主背包所有物品做成 `stackMap`（跳过空槽与本物品），按模式：
    - mode **0 Total Absorption**：把记录过的物品**全部清空**。
    - mode **1 Last Stack**：每种记录物品**只留 1 组**，多余的从**最小堆叠数**的槽开始删（`Collections.min(stackSizeMultimap.keySet())`）。
    - mode **2 Excess Absorption**：只有背包**满了**（`filledStacks >= player.inventory.mainInventory.size()`）才消费，且只消费到出现空位为止。
  - **特殊工作台配方**（`RecipeOblivionStone`，注册名 `forge:oblivion_stone`）：1 个遗忘之石 + 1 个其它物品 → 把该物品 id/meta 写进 NBT 列表；可损坏物品 meta 记 **-1**，否则记实际 damage；已达 `oblivionStoneHardCap` 则返回空。只有遗忘之石没有其它物品 → 返回一个**全新**的遗忘之石（清空列表）。`getRecipeSize() = 10`，`canFit()` 恒 true。
- 配方：**两套**
  1. **灌注** FRRecipes.java:75；研究名 `OBLIVION_STONE`，instability **4**；要素 VOID 75 / DARKNESS 75 / ENTROPY 60 / EXCHANGE 30 / MAGIC 30；中心 = `ItemsTC.charmVoidseer`；外围 8 个：`Items.ENDER_EYE`, `ItemsTC.voidSeed`, `"ingotVoid"`, `ItemsTC.voidSeed`, `"nitor"`, `ItemsTC.voidSeed`, `"ingotVoid"`, `ItemsTC.voidSeed`。
  2. **普通工作台配方** `RecipeOblivionStone`（注册名 `forge:oblivion_stone`）。
- 研究：条目 `OBLIVION_STONE` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;3` + `THEORY;ELDRITCH;3`，`warp = 5` / 父 `RING_OF_SUPERPOSITION`、`VOIDSEERPEARL`；meta `SPIKY` + `HEX`；坐标 [4,-2]。
- 贴图/模型：`models/item/oblivion_stone.json` → `forgotten_relics:items/oblivion_stone`；模型注册覆盖 metadata 0~104。

### forgotten_relics:oblivion_amulet  （Amulet of The Oblivion / 湮灭护符）
- 类：`ItemOblivionAmulet`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`, `IWarpingGear`, `IRechargable`
- 类型：饰品(Bauble)
- 堆叠上限：`this.maxStackSize = 1`（ItemOblivionAmulet.java:62）
- 关键常量/数值：`getBaubleType = BaubleType.AMULET`；`getRarity = EPIC`；`getMaxCharge = oblivionAmuletMaxCharge`；NBT 键 `IDamageStored`（float）；**Vis 吸收换算系数硬编码 8.0f**：`(int)(event.getAmount() * 8.0f * oblivionAmuletVisMult)`；**药水四档硬编码**：`omega = Math.random()`，≤0.25→**4**，≤0.5→**15**，≤0.75→**18**，否则→**20**；**硬编码读取 Baubles 槽位 0**（`getBaublesHandler(player).getStackInSlot(0)`），不是查找护符实际所在槽
- 配置项：`oblivionAmuletMaxCharge` / Vis / 默认 `400`；`oblivionAmuletVisCost`（字段 `oblivionAmuletVisMult`）/ Vis Costs / 默认 `1.0f`；`oblivionAmuletDamageReleaseChance` / 默认 `0.0008f`；`oblivionAmuletDamageCap` / 默认 `100.0f`；`oblivionAmuletHighDamageReductionChance` / 默认 `0.9f`；`oblivionAmuletPotionChance` / 默认 `0.0004f`；`oblivionAmuletPotionDurationMin` / 默认 `100`；`oblivionAmuletPotionDurationMax` / 默认 `2100`；`oblivionAmuletPotionLevelMin` / 默认 `0`；`oblivionAmuletPotionLevelMax` / 默认 `3`；`oblivionAmuletWarp` / 默认 `4`
- 行为：
  - **伤害吸收**（RelicsEventHandler.onEntityHurt，254-258 行）：`RechargeHelper.consumeCharge(BaublesApi.getBaublesHandler(player).getStackInSlot(0), player, (int)(event.getAmount() * 8.0f * oblivionAmuletVisMult))` 成功、玩家戴着护符、伤害非绝对型 → 把 `event.getAmount()` 累加进 NBT `IDamageStored` 并 `event.setCanceled(true)`。
  - **onWornTick**（98-130 行，服务端）：
    - 若 `Math.random() <= oblivionAmuletDamageReleaseChance`（默认 0.0008）且 `IDamageStored > 0` → 取 `getDamage = IDamageStored * Math.random()`；若 `getDamage > oblivionAmuletDamageCap` 且 `Math.random() <= oblivionAmuletHighDamageReductionChance` → `getDamage = oblivionAmuletDamageCap * Math.random()`；扣减 `IDamageStored` 并用 `DamageSourceOblivion` **打自己**。
    - **else if** `Math.random() <= oblivionAmuletPotionChance`（默认 0.0004）→ 按上面四档给一个随机原版药水效果，时长为 `[Min, Max]` 随机，等级为 `[LevelMin, LevelMax]` 随机。
  - `getWarp = oblivionAmuletWarp`。
  - 工具提示显示 `round(IDamageStored * 100) / 100`。
- 配方：**有，灌注**。FRRecipes.java:72；研究名 `OBLIVION_AMULET`，instability **16**；要素 DEATH 250 / EXCHANGE 175 / VOID 300 / DARKNESS 100 / ELDRITCH 100 / MAGIC 125 / FIRE 200；中心 = `new ItemStack(ModItems.bloodPendant, 1, 0)`；外围 8 个：`Items.NETHER_STAR`, `Items.BLAZE_POWDER`, `"ingotVoid"`, `Items.BLAZE_POWDER`, `ItemsTC.charmVoidseer`, `Items.BLAZE_POWDER`, `"ingotVoid"`, `Items.BLAZE_POWDER`。
- 研究：条目 `OBLIVION_AMULET` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;3` + `THEORY;ELDRITCH;3`，`warp = 3` / 父 `EXPERIENCE_TOME`、`VOIDSEERPEARL`；meta `SPIKY`；坐标 [-3,-4]。
- 贴图/模型：`models/item/oblivion_amulet.json` → `forgotten_relics:items/oblivion_amulet`

### forgotten_relics:dimensional_mirror  （Dimensional Mirror / 空间魔镜）
- 类：`ItemDimensionalMirror`，父类：`Item`，implements `IRechargable`
- 类型：普通物品
- 堆叠上限：`this.maxStackSize = 1`（ItemDimensionalMirror.java:76）
- 关键常量/数值：NBT 键 `IStoredX` / `IStoredY` / `IStoredZ`（int）、`IDimensionID`（int，默认 0）；`getItemUseAction = BOW`；use duration = `dimensionalMirrorChannelDuration`；`onUsingTick` 在 `count == 1` 时执行传送；每 tick 生成 **4** 个 `PORTAL` 粒子（`for counter = 0; counter <= 3`）；传送落点 = 存储值 + **0.5**；传送后撒 **129** 个 `PORTAL` 粒子；`getMaxCharge = dimensionalMirrorMaxCharge`；`hasEffect() = stack.hasTagCompound()`；`isFull3D() = false`
- 配置项：`dimensionalMirrorMaxCharge` / Vis / 默认 `100`；`dimensionalMirrorChannelDuration` / 默认 `80`；`interdimensionalMirror` / Generic Config / 默认 `true`（允许跨维度）
- 行为：
  - **onItemRightClick**（152-173 行）：
    - 已存储 且 不潜行 → 若 `interdimensionalMirror == false` 且当前维度 ≠ 存储维度 → `PASS`；若当前维度 == **1（末地）** 且存储维度 ≠ 1 → `PASS`；否则 `setActiveHand` 开始引导。
    - **潜行** → 把 `(int)posX/Y/Z` 与 `player.dimension` 写入 NBT，播放 Thaumcraft `SoundsTC.jar`，`setActiveHand`。
  - **onUsingTick**（128-150 行）：`count == 1` 时生成粒子、`SuperpositionHandler.imposeBurst(..., 1.25f)`；若维度不同用 `ExtradimensionalTeleporter`（继承 vanilla `Teleporter`，构造时带目标坐标）跨维度，否则直接 `setPosition`；播 `ENTITY_ENDERMEN_TELEPORT` 并撒粒子。
- 配方：**有，灌注**。FRRecipes.java:64；研究名 `DIMENSIONAL_MIRROR`，instability **6**；要素 MOTION 150 / DARKNESS 20 / MAGIC 100 / VOID 200 / ELDRITCH 10；中心 = `ItemsTC.handMirror`；外围 8 个：`"eternalLifeEssence"`, `Blocks.GLOWSTONE`, `"elvenDragonstone"`, `manaResource:15`, `ItemsTC.focus2`, `manaResource:15`, `"elvenDragonstone"`, `Blocks.GLOWSTONE`。
- 研究：条目 `DIMENSIONAL_MIRROR` / `FORGOTTEN_RELICS` / 要素 `THEORY;INFUSION;1` + `THEORY;ARTIFICE;1`，`required_craft = thaumcraft:hand_mirror` / 父 `GENERICTHEORY`、`MIRRORHAND`；meta `SPIKY`；坐标 [0,-3]。
- 贴图/模型：`models/item/dimensional_mirror.json` → `forgotten_relics:items/dimensional_mirror`

### forgotten_relics:arcanum  （Nebulous Core / 浑浊之核）
- 类：`ItemArcanum`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`, `IVisDiscountGear`, `IRechargable`
- 类型：饰品(Bauble)
- 堆叠上限：`this.maxStackSize = 1`（ItemArcanum.java:70）
- 关键常量/数值：`getBaubleType = BaubleType.CHARM`；`getRarity = EPIC`；`getMaxCharge = arcanumMaxCharge`；**充能概率硬编码 `0.025 * arcanumGenRate`**；每次充能 **1** Vis；**退化时写死 Baubles 槽位 6**（`baubles.setStackInSlot(6, new ItemStack(arcanum))`，ItemDormantArcanum.java:97）；`getVisDiscount = (int)arcanumVisDiscount`；NBT 键 `ILifetime`
- 配置项：`arcanumMaxCharge` / Vis / 默认 `500`；`arcanumGenRate` / Generic Config / 默认 `1.0f`；`arcanumVisDiscount` / 默认 `35.0f`；`arcanumTeleportChance` / 默认 `0.000208f`；`arcanumTeleportRange` / 默认 `32`；`arcanumDormantTransformChance` / 默认 `0.000027f`；`arcanumDormantLifeMin` / 默认 `12`；`arcanumDormantLifeMax` / 默认 `72`；`dormantArcanumMaxCharge` / Vis / 默认 `300`；`dormantArcanumVisCostPerTick` / 默认 `3`（**代码未引用**）；`dormantArcanumVisMult` / Vis Costs / 默认 `1.0f`
- 行为（**onWornTick**，ItemArcanum.java:104-140，服务端）：
  1) `Math.random() <= 0.025 * arcanumGenRate` → 依次尝试给主背包前 `InventoryPlayer.getHotbarSize()` 格、Baubles 全部槽、护甲槽调 `RechargeHelper.rechargeItem(world, stack, pos, player, 1)`，充到第一个成功为止（每次只充 1 点，每 tick 最多 1 件）。
  2) `Math.random() <= arcanumTeleportChance` → 循环最多 `arcanumTeleportRange` 次调用 `SuperpositionHandler.validTeleportRandomly(entity, world, arcanumTeleportRange)` 随机传送。
  3) 否则 `Math.random() <= arcanumDormantTransformChance` → 把自己替换成 Dormant Nebulous Core，寿命 = `(int)((arcanumDormantLifeMin + Math.random() * (arcanumDormantLifeMax - arcanumDormantLifeMin)) * dormantArcanumVisMult)`，写进 **Baubles 槽位 6**。
- 配方：**有，灌注**。FRRecipes.java:74；研究名 `FR_ARCANUM`，instability **12**；要素 AURA 80 / MAGIC 500 / VOID 125 / ELDRITCH 175 / DARKNESS 150 / MOTION 100 / EXCHANGE 185；中心 = `pearlAny`（`ItemsTC.primordialPearl` 任意 meta 0~7）；外围 12 个：`new ItemStack(ItemsTC.amuletVis, 1, 1)`, `ItemsTC.crystalEssence`, `"eternalLifeEssence"`, `"ingotThaumium"`, `manaResource:15`, `ItemsTC.crystalEssence`, **`CommonProxy.superpositionRing`**, `ItemsTC.crystalEssence`, `manaResource:15`, `"ingotThaumium"`, `"eternalLifeEssence"`, `ItemsTC.crystalEssence`。
- 研究：条目 `FR_ARCANUM` / `FORGOTTEN_RELICS` / 要素 `THEORY;AUROMANCY;1` + `THEORY;ARTIFICE;2` + `THEORY;INFUSION;2`，`required_craft = thaumcraft:amulet_vis;1;1` / 父 `DEIFIC_AMULET`、`VISAMULET`、`!SuperpositionRing`、`!ThaumiumIngot`；meta `HIDDEN` + `SPIKY`；坐标 [0,-6]。
- 贴图/模型：`models/item/arcanum.json` → `forgotten_relics:items/**nebulous_core**`（**注册名是 `arcanum`，贴图名是 `nebulous_core`，两者不同**）；文件 `textures/items/nebulous_core.png`

### forgotten_relics:dormant_arcanum  （Dormant Nebulous Core / 休眠浑浊之核）
- 类：`ItemDormantArcanum`，父类：`ItemBaubleBase` ← `ItemModBase` ← `Item`，implements `IBauble`, `IRechargable`
- 类型：饰品(Bauble)
- 堆叠上限：1
- 关键常量/数值：`getBaubleType = BaubleType.CHARM`；`getRarity = EPIC`；`getMaxCharge = dormantArcanumMaxCharge`；**Vis 消耗硬编码 3/tick**（`consumeCharge(itemstack, entity, 3)`），配置项 `dormantArcanumVisCostPerTick`（默认 3）**未被引用**；**寿命耗尽时写死 Baubles 槽位 6**（`baubles.setStackInSlot(6, new ItemStack(arcanum))`）；NBT 键 `ILifetime`；tooltip 显示 `ILifetime * 2` + 单位
- 配置项：`dormantArcanumMaxCharge` / Vis / 默认 `300`；`dormantArcanumVisCostPerTick` / Dormant Arcanum / 默认 `3`（**代码未引用**）；`dormantArcanumVisMult` / Vis Costs / 默认 `1.0f`（在 `ItemArcanum` 退化时使用）
- 行为（**onWornTick**，ItemDormantArcanum.java:88-100，服务端且有 NBT）：若 `ILifetime > 0` → 尝试扣 **3** Vis，成功则 `ILifetime -= 1`；否则（寿命 ≤ 0）→ 把自己替换回 `CommonProxy.arcanum`，写进 **Baubles 槽位 6**。
- 配方：无（只能由 `ItemArcanum` 退化产生）
- 研究：无独立研究条目（不在 baubles.json / spellbooks.json 中）
- 贴图/模型：`models/item/dormant_arcanum.json` → `forgotten_relics:items/nebulous_core_dormant`
### forgotten_relics:omega_core  （The Omega Core / 欧米伽之核）
- 类：`ItemOmegaCore`，父类：`Item`
- 类型：普通物品（被动效果载体，无主动使用）
- 堆叠上限：1
- 关键常量/数值：`getRarity = EPIC`；`hasEffect = true`（恒定附魔光效）；**补 Vis 量硬编码 9999**（`RechargeHelper.rechargeItemBlindly(stack, player, 9999)`）；**遍历 Baubles 槽位上界硬编码 7**（`for (int i = 0; i < 7; i++)`，ItemOmegaCore.java:72 附近）；**死亡保护后血量硬编码 1.0f**
- 配置项：**无**（没有任何配置项引用它）
- 行为：
  - **onUpdate**（55-63 行，服务端）：每 tick 调 `fillAllVis(player)`。**不检查 `isSelected`**，即放在背包里也生效。
  - **fillAllVis**（65-111 行）：遍历**主背包**（排除自己）、**护甲栏**、**副手栏**，以及 **Baubles 槽 0~6**；对所有 `instanceof IRechargable` 且 `RechargeHelper.getCharge(stack) >= 0` 的物品调 `rechargeItemBlindly(..., 9999)`（直接充满）。
  - **死亡保护**（RelicsEventHandler.onPlayerDeath，267-281 行，priority HIGHEST）：玩家主背包或副手栏里有 Omega Core → `event.setCanceled(true)` 并 `player.setHealth(1.0f)`，然后 **return**。**不消耗物品，等于只要持有就永久免死**。
---

---

## 5. 公共数值

### 5.1 配置项默认值总表

全部来自 `RelicsConfigHandler.configDisposition`（233-510 行）。「是否被引用」= 除 `RelicsConfigHandler.java` 外是否有代码读取该字段（实测结果）。

| 分类 | 键 | 字段 | 默认值 | 范围 | 是否被引用（引用点） |
|---|---|---|---|---|---|
| Justice Handler Overrides | `justiceHandlerOverrides` | `forgottenKnowledgeOverrides` | `exampleOverrides` 数组 | — | **UNUSED** |
| Justice Handler Overrides | `justiceOverridingEnabled` | `forgottenKnowledgeOverridingEnabled` | `false` | — | **UNUSED** |
| Generic Config | `deificAmuletOnlyNegatesDebuffs` | 同名 | `false` | — | 是（ItemDeificAmulet:71,73 附近） |
| Generic Config | `altTelekinesisAlgorithm` | 同名 | `false` | — | **UNUSED** |
| Generic Config | `guardianAntiAbuseRadius` | 同名 | `16.0f` | 0~1024 | **UNUSED** |
| Generic Config | `guardianNotificationRadius` | 同名 | `64.0f` | -32768~32768 | **UNUSED** |
| Generic Config | `oblivionStoneHardCap` | 同名 | `64` | 0~2048 | 是（RecipeOblivionStone） |
| Generic Config | `oblivionStoneSoftCap` | 同名 | `28` | 0~2048 | 仅 tooltip（ItemOblivionStone） |
| Generic Config | `memesEnabled` | 同名 | `false` | — | **UNUSED** |
| Generic Config | `updateNotificationsEnabled` | 同名 | `true` | — | **UNUSED** |
| Generic Config | `voidGrimoireEnabled` | 同名 | `true` | — | 是（ItemVoidGrimoire:116 附近） |
| Generic Config | `outerLandsCheckrate` | 同名 | `20` | 1~1024000 | **UNUSED** |
| Generic Config | `outerLandsAntiAbuseEnabled` | 同名 | `true` | — | **UNUSED** |
| Generic Config | `outerLandsAntiAbuseDamage` | 同名 | 待确认（未在 233-510 行看到赋值） | — | **UNUSED** |
| Generic Config | `revelationModifier` | 同名 | `1.0f` | 0.001~32 | **UNUSED**（仅用于算下面两个） |
| Generic Config | `telekinesisOnPlayers` | 同名 | `true` | — | **UNUSED** |
| Generic Config | `fateTomeCooldownMIN` | 同名 | `30` | 0~32768 | **仅 tooltip** |
| Generic Config | `fateTomeCooldownMAX` | 同名 | `90` | 0~32768 | **仅 tooltip** |
| Generic Config | `advancedMiningCharmReach` | 同名 | `4.0f` | 0~32 | 是 |
| Generic Config | `miningCharmReach` | 同名 | `2.0f` | 0~32 | 是 |
| Generic Config | `advancedMiningCharmBoost` | 同名 | `3.0f` | 0~32000 | 是 |
| Generic Config | `miningCharmBoost` | 同名 | `1.0f` | 0~32000 | 是 |
| Generic Config | `nebulousCoreDodgeChance` | 同名 | `0.4f` | 0~1 | 是（RelicsEventHandler:144） |
| Generic Config | `ancientAegisDamageReduction` | 同名 | `0.25f` | 0~1 | 是 |
| Generic Config | `deificAmuletEffectImmunity` | 同名 | `true` | — | 是 |
| Generic Config | `deificAmuletInvincibility` | 同名 | `true` | — | 是 |
| Generic Config | `darkSunRingDeflectChance` | 同名 | `0.2f` | 0~1 | 是 |
| Generic Config | `darkSunRingDamageCap` | 同名 | `100.0f` | 0~32768 | **仅 tooltip**（真判定硬编码 100.0f） |
| Generic Config | `darkSunRingHealLimit` | 同名 | `false` | — | 是 |
| Generic Config | `interdimensionalMirror` | 同名 | `true` | — | 是（ItemDimensionalMirror:157） |
| Generic Config | `shinyStoneCheckrate` | 同名 | `4` | 1~2048 | 是（ItemShinyStone:92,124） |
| Generic Config | `obeliskDrainerVisGen` | `obeliskDrainerVisMult` | `1.0f` | 0~32000 | **UNUSED** |
| Generic Config | `arcanumGenRate` | 同名 | `1.0f` | 0~32000 | 是（ItemArcanum:107） |
| Generic Config | `soulTomeDivisor` | 同名 | `10.0f` | 0~∞ | **UNUSED**（类内硬编码 `SOUL_DAMAGE_DIVISOR = 10.0F`） |
| Generic Config | `falseJusticeEnabled` | 同名 | `true` | — | **UNUSED** |
| Generic Config | `chaosTomeExplosionBlockDamage` | 同名 | `false` | — | 是（EntityPrimalOrb:313） |
| Generic Config | `overthrowerAffectsPlayers` | 同名 | `false` | — | 是（ItemEdictOfBanishment / ItemVoidGrimoire） |
| Generic Config | `primalDiscordComboEnabled` | 同名 | `true` | — | 是（ItemTomeOfPrimalChaos:154） |
| Generic Config | `outerLands...` / `guardian...` | — | — | — | 见上 |
| Thaumcraft Overrides | `notificationDelay` | 同名 | `2000` | 0~32768 | **UNUSED** |
| Thaumcraft Overrides | `runicRechargeSpeed` | 同名 | `750` | 0~32768 | **UNUSED** |
| Thaumcraft Overrides | `runicRechargeDelay` | 同名 | `40` | 0~32768 | **UNUSED** |
| Thaumcraft Overrides | `runicCost` | 同名 | `10` | 0~32768 | **UNUSED** |
| Damage Values | `damageThunderpealDirect` | 同名 | `24.0f` | 0~32000 | 是 |
| Damage Values | `damageThunderpealBolt` | 同名 | `16.0f` | 0~32000 | 是 |
| Damage Values | `damageApotheosisDirect` | 同名 | `100.0f` | 0~32000 | **仅 tooltip**（实体硬编码 100.0F） |
| Damage Values | `damageApotheosisImpact` | 同名 | `75.0f` | 0~32000 | **仅 tooltip**（实体硬编码 75.0F） |
| Damage Values | `damageLunarFlareDirect` | 同名 | `72.0f` | 0~32000 | **UNUSED**（实体硬编码 72.0F） |
| Damage Values | `damageLunarFlareImpact` | 同名 | `40.0f` | 0~32000 | **UNUSED**（实体硬编码 40.0F） |
| Damage Values | `paradoxDamageCap` | 同名 | `200.0f` | 0~32000 | 是（ItemParadox + tooltip） |
| Damage Values | `telekinesisTomeDamageMIN` | 同名 | `16.0f` | 0~32000 | **仅 tooltip**（硬编码 16.0F） |
| Damage Values | `telekinesisTomeDamageMAX` | 同名 | `40.0f` | 0~32000 | **仅 tooltip**（硬编码 24.0F 随机幅度） |
| Damage Values | `nuclearFuryDamageMIN` | 同名 | `24.0f` | 0~32000 | **仅 tooltip**（实体硬编码 24.0F+rand*8） |
| Damage Values | `nuclearFuryDamageMAX` | 同名 | `32.0f` | 0~32000 | **仅 tooltip** |
| Damage Values | `crimsonSpellDamageMIN` | 同名 | `42.0f` | 0~32000 | **仅 tooltip**（实体硬编码 42.0F） |
| Damage Values | `crimsonSpellDamageMAX` | 同名 | `100.0f` | 0~32000 | **仅 tooltip**（实体硬编码 100.0F） |
| Damage Values | `chaosTomeDamageCap` | 同名 | `100.0f` | 0~32000 | **是**（EntityPrimalOrb:307） |
| Damage Values | `eldritchSpellDamage` | 同名 | `32.5f` | 0~32000 | **仅 tooltip**（实体硬编码 32.5F） |
| Damage Values | `eldritchSpellDamageEx` | 同名 | `100.0f` | 0~32000 | **UNUSED**（实体硬编码 ×2.0 = 65.0F，与 100 不符） |
| Vis Costs | `apotheosisVisMult` | 同名 | `1.0f` | 0~1024 | 是 |
| Vis Costs | `chaosTomeVisMult` | 同名 | `1.0f` | 0~1024 | 仅 tooltip |
| Vis Costs | `crimsonSpellVisMult` | 同名 | `1.0f` | 0~1024 | 是（ItemCrimsonSpell:136 附近） |
| Vis Costs | `deificAmuletVisCost` | `deificAmuletVisMult` | `1.0f` | 0~1024 | 是（ItemDeificAmulet:107） |
| Vis Costs | `discordTomeVisCost` | `discordTomeVisMult` | `1.0f` | 0~1024 | 是（ItemTomeOfDiscord:124） |
| Vis Costs | `dormantArcanumVisMult` | 同名 | `1.0f` | 0~1024 | 是（ItemDormantArcanum:96） |
| Vis Costs | `eldritchSpellVisCost` | `eldritchSpellVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `fateTomeVisCost` | `fateTomeVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `lunarFlaresVisCost` | `lunarFlaresVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `nuclearFuryVisCost` | `nuclearFuryVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `oblivionAmuletVisCost` | `oblivionAmuletVisMult` | `1.0f` | 0~1024 | 是（RelicsEventHandler:254） |
| Vis Costs | `soulTomeVisCost` | `soulTomeVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `telekinesisTomeVisCost` | `telekinesisTomeVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `weatherStoneVisCost` | `weatherStoneVisMult` | `1.0f` | 0~1024 | 是（ItemWeatherStone:63） |
| Vis Costs | `thunderpealVisCost` | `thunderpealVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `overthrowerVisCost` | `overthrowerVisMult` | `1.0f` | 0~1024 | 是 |
| Vis Costs | `voidGrimoireVisMult` | 同名 | `1.0f` | 0~1024 | 是 |

**各物品独立分类的默认值（摘要）**：

| 分类 | 键 = 默认值 |
|---|---|
| Paradox | `paradoxRepairRate=20`、`paradoxRepairAmount=1`、`paradoxWarp=8` |
| Ancient Aegis | `ancientAegisHealAmount=1.0f`、`ancientAegisHealInterval=20`、`ancientAegisKnockbackResistance=1.0f` |
| Tome of Discord | `discordTomeVisCost=6`、`discordTomeTeleportRange=128`、`discordTomeDiscordRange=1024`、`discordTomeShiftRange=16`、`discordTomeCooldown=20` |
| Void Grimoire | `voidGrimoireChannelDuration=100`、`voidGrimoireSearchRange=64`、`voidGrimoireLevitateSpeed=0.03f`、`voidGrimoireLevitateSlowDuration=30`、`voidGrimoireLevitateSlowLevel=100` |
| Edict of Banishment | `overthrowerChannelDuration=150`、`overthrowerSearchRange=64`、`overthrowerNetherRange=1000`、`overthrowerLevitateSlowDuration=30`、`overthrowerLevitateSlowLevel=2` |
| Tome of Lunar Flares | `lunarFlaresVisCost=20`、`lunarFlaresRayTraceRange=128` |
| Apotheosis | `apotheosisVisCost=7` |
| Nuclear Fury | `nuclearFuryVisCostPerSecond=5`、`nuclearFuryClearRange=32` |
| Soul Tome | `soulTomeVisCostPerTick=1`、`soulTomeSearchRange=20`、`soulTomeKnockbackRange=4`、`soulTomeMaxDamage=20.0f`、`soulTomeMinDamage=1.0f`、`soulTomeWarmupTicks=20` |
| Eldritch Spell | `eldritchSpellVisCost=4`、`eldritchSpellCooldown=20` |
| Crimson Spell | `crimsonSpellVisCost=8`、`crimsonSpellSearchRange=3.0f`、`crimsonSpellCooldown=30` |
| Thunderpeal | `thunderpealVisCost=2`、`thunderpealCooldown=30` |
| Tome of Predestiny | `telekinesisVisCostPerTick=1`、`telekinesisLightningVisCost=3`、`telekinesisThrowVisCost=3`、`telekinesisReach=7.5f`、`telekinesisMoveForce=0.66666f`、`telekinesisCloseThreshold=1.5f`、`telekinesisFarThreshold=8.0f`、`telekinesisTargetExpireTicks=5`、`telekinesisLeftClickRange=16`、`telekinesisThrowCooldown=40`、`telekinesisLightningCount=4`、`telekinesisLightningChainTargets=3`、`telekinesisLightningCooldown=10` |
| Tome of Primal Chaos | `chaosTomeHomingChance=35`（百分比） |
| XP Tome | `xpTomeTransferRate=5` |
| Weather Stone | `weatherStoneVisCost=25`、`weatherStoneChannelDuration=60`、`weatherStoneCooldown=100` |
| Tome of Broken Fates | `fateTomeVisCost=100`、`fateTomeMultiHeldChance=0.000016f`、`fateTomeDamage=40000.0f`、`fateTomeExplosionRadius=16.0f`、`fateTomeBigExplosionRadius=100.0f`、`fateTomeBuffChance=0.75f` |
| Terror Crown | `terrorCrownHavocRange=24`、`terrorCrownScanRange=32.0f`、`terrorCrownBlindnessDuration=100`、`terrorCrownWitherDuration=40`、`terrorCrownWitherLevel=0`、`terrorCrownNauseaDuration=100`、`terrorCrownNauseaLevel=1`、`terrorCrownSlownessDuration=30`、`terrorCrownSlownessLevel=1`、`terrorCrownWeaknessDuration=80`、`terrorCrownWeaknessLevel=2`、`terrorCrownManaCost=200`、`terrorCrownWarp=3` |
| Superposition Ring | `superpositionRingSwapChance=0.025f`、`superpositionRingCheckInterval=600` |
| Shiny Stone | `shinyStoneStillThreshold2=40`、`3=80`、`4=200`、`shinyStoneStillIncrement=4.0f`、`shinyStoneHealAmount=1.0f` |
| Oblivion Amulet | `oblivionAmuletDamageReleaseChance=0.0008f`、`oblivionAmuletDamageCap=100.0f`、`oblivionAmuletHighDamageReductionChance=0.9f`、`oblivionAmuletPotionChance=0.0004f`、`oblivionAmuletPotionDurationMin=100`、`Max=2100`、`oblivionAmuletPotionLevelMin=0`、`Max=3`、`oblivionAmuletWarp=4` |
| Devourer of The Void | `devourerRange=16`、`devourerMinRiftSize=50`、`devourerDrainRate=1` |
| Deific Amulet | `deificAmuletFireDuration=300`、`deificAmuletFireVisCost=10`、`deificAmuletInvincibilityExtension=40`、`deificAmuletInvincibilityCooldown=32` |
| Chaos Core | `chaosCoreWarp=2`、`chaosCorePotionChance=0.000208f`（**UNUSED**）、`chaosCorePotionDurationMin=100`（UNUSED）、`Max=2500`（UNUSED）、`chaosCorePotionLevelMin=0`（UNUSED）、`Max=3`（UNUSED） |
| Arcanum | `arcanumTeleportChance=0.000208f`、`arcanumTeleportRange=32`、`arcanumDormantTransformChance=0.000027f`、`arcanumDormantLifeMin=12`、`arcanumDormantLifeMax=72`、`arcanumVisDiscount=35.0f` |
| Dimensional Mirror | `dimensionalMirrorChannelDuration=80` |
| Dormant Arcanum | `dormantArcanumVisCostPerTick=3`（**UNUSED**） |

**Vis 最大充能（分类 `Vis`）**：`apotheosisMaxCharge=400`、`crimsonSpellMaxCharge=200`、`darkSunRingMaxCharge=500`、`deificAmuletMaxCharge=200`、`devourerMaxCharge=300`、`discordTomeMaxCharge=100`、`eldritchSpellMaxCharge=100`、`lunarFlaresMaxCharge=500`、`nuclearFuryMaxCharge=500`、`oblivionAmuletMaxCharge=400`、`overthrowerMaxCharge=100`、`soulTomeMaxCharge=1000`、`thunderpealMaxCharge=100`、`weatherStoneMaxCharge=100`、`fateTomeMaxCharge=600`、`voidGrimoireMaxCharge=100`、`primalChaosMaxCharge=100`、`predestinyMaxCharge=100`、`dormantArcanumMaxCharge=300`、`arcanumMaxCharge=500`、`dimensionalMirrorMaxCharge=100`。

**键名与字段名不一致（移植时最容易踩的坑，逐条列出）**：

| 配置 key | 实际字段 |
|---|---|
| `obeliskDrainerVisGen` | `obeliskDrainerVisMult` |
| `deificAmuletVisCost` | `deificAmuletVisMult` |
| `discordTomeVisCost` | `discordTomeVisMult` |
| `eldritchSpellVisCost` | `eldritchSpellVisMult` |
| `fateTomeVisCost` | `fateTomeVisMult` |
| `lunarFlaresVisCost` | `lunarFlaresVisMult` |
| `nuclearFuryVisCost` | `nuclearFuryVisMult` |
| `oblivionAmuletVisCost` | `oblivionAmuletVisMult` |
| `soulTomeVisCost` | `soulTomeVisMult` |
| `telekinesisTomeVisCost` | `telekinesisTomeVisMult` |
| `weatherStoneVisCost` | `weatherStoneVisMult`（**同名 key 在 Weather Stone 分类里又出现一次，默认 25**） |
| `thunderpealVisCost` | `thunderpealVisMult` |
| `overthrowerVisCost` | `overthrowerVisMult` |

**同名 key 冲突（两个不同分类用了同一个 key 名，后加载的覆盖先加载的）**：
- `weatherStoneVisCost`：分类 `Weather Stone`（默认 25，读进 `weatherStoneVisCost` 字段）与分类 `Vis Costs`（默认 1.0f，读进 `weatherStoneVisMult` 字段）。两个字段都存在，**不冲突**，但配置界面会出现两个同名键，容易误改。
- `crimsonSpellVisCost`：分类 `Crimson Spell`（默认 8）+ 分类 `Vis Costs`（默认 1.0f）—— 同上，两字段。
- `eldritchSpellVisCost`、`discordTomeVisCost`、`lunarFlaresVisCost`、`thunderpealVisCost`、`apotheosisVisCost`、`fateTomeVisCost`、`nuclearFuryVisCost`、`oblivionAmuletVisCost`、`soulTomeVisCost`、`deificAmuletVisCost`、`overthrowerVisCost` —— 全部都是「一个 int 基础消耗字段 + 一个 float 倍率字段共用同一个 key 字符串」。

**派生值（非配置项，由配置算出）**：
- `researchInspectionFrequency = (int)(600.0f / revelationModifier)`（第 508 行）
- `knowledgeChance = 0.1 * revelationModifier`（第 509 行）
- 两者都 **UNUSED**（对应「被遗忘的知识」触发系统整套缺失）。

**静态初始化**（512-515 行）：`weatherStoneVisMult = 1.0f`；`exampleOverrides` 有 3 条示例，格式为 `ResearchKey[modid:itemname:meta, ...]`，示例里用的 modid 是 `Thaumcraft` / `Botania` / `ForgottenRelicsRE`（**与实际 modid 不符，是从 1.7.10 老版本抄来的**）。

### 5.2 全部 `%s` 占位符与填充来源

`en_us.lang` / `zh_cn.lang` 中共 **38 行**含占位符。逐条对应代码填充：

| 行 | lang key | 模板 | 填什么（代码处） |
|---|---|---|---|
| 5 | `death.attack.trueDamage` | `%1$s ... %2$s` | 原版死亡消息机制，`%1$s` = 死者名，`%2$s` = 攻击者名 |
| 6 | `death.attack.trueDamageUndef` | `%1$s` | 死者名 |
| 7 | `death.attack.attackOblivion` | `%1$s` | 死者名 |
| 8 | `death.attack.attackFate` | `%1$s` | 死者名 |
| 9 | `death.attack.attackLightning` | `%2$s ... %1$s` | 原版机制 |
| 10 | `death.attack.attackDarkMatter` | `%1$s` | 死者名 |
| 11 | `death.attack.forgottenMagic` | `%1$s ... %2$s` | 原版机制 |
| 99 | `item.ItemDarkSunRing4.lore` | `%s%%` | `(int)(darkSunRingDeflectChance * 100.0f)`（ItemDarkSunRing:59） |
| 164 | `item.FateTome5.lore` | `%1$s-%2$s` | `fateTomeCooldownMIN`、`fateTomeCooldownMAX`（ItemTomeOfBrokenFates:50）**仅显示，实际冷却硬编码** |
| 169 | `item.FateTomeVisCost.lore` | `%s` | 被 5 处复用：`fateTomeVisCost`、`crimsonSpellVisCost`、`eldritchSpellVisCost`、`lunarFlaresVisCost`、`thunderpealVisCost`、`discordTomeVisCost` |
| 180 | `item.PredestinyTome8.lore` | `%1$s-%2$s` | `telekinesisTomeDamageMIN`、`MAX`（ItemTomeOfPredestiny:136）**仅显示** |
| 183 | `item.PredestinyTomeVisCost.lore` | `%s` | `telekinesisVisCostPerTick` |
| 184 | `item.PredestinyTomeLightningCost.lore` | `%s` | `telekinesisLightningVisCost` |
| 187 | `item.ItemAncientAegis1.lore` | `%s%%` | `(int)(ancientAegisDamageReduction * 100.0f)` |
| 196 | `item.NuclearFury3.lore` | `%1$s-%2$s` | `nuclearFuryDamageMIN`、`MAX` **仅显示** |
| 197 | `item.NuclearFuryVisCost.lore` | `%s` | `nuclearFuryVisCostPerSecond`；**另被 Apotheosis:76（apotheosisVisCost）、Edict:103（`(int)(VIS_PER_TICK*20)`=4）、VoidGrimoire:97（同 4）、SoulTome:84（soulTomeVisCostPerTick）复用** |
| 200 | `item.ItemMiningCharm1.lore` | `%s%%` | `(int)(miningCharmBoost * 100.0f)` |
| 201 | `item.ItemMiningCharm2.lore` | `%s` | `miningCharmReach` |
| 204 | `item.ItemAdvancedMiningCharm1.lore` | `%s%%` | `(int)(advancedMiningCharmBoost * 100.0f)` |
| 205 | `item.ItemAdvancedMiningCharm2.lore` | `%s` | `advancedMiningCharmReach` |
| 213 | `item.ItemCrimsonSpell6.lore` | `%1$s-%2$s` | `crimsonSpellDamageMIN`、`MAX` **仅显示** |
| 217 | `item.ItemDevourerOfTheVoid2.lore` | `%s` | `devourerRange` |
| 219 | `item.ItemDevourerOfTheVoid4.lore` | `%s` | `devourerDrainRate` |
| 222 | `item.ItemDevourerOfTheVoidStatus.lore` | `%s` | 开/关状态字符串（`item.ItemDevourerOfTheVoidOn/Off.lore`） |
| 231 | `item.ItemEldritchSpell5.lore` | `%s` | `(int)eldritchSpellDamage` **仅显示，实际 32.5F 硬编码** |
| 248 | `item.ItemTeleportationTome2.lore` | `%s` | `discordTomeTeleportRange`（**注意：默认 128，但 tooltip 不随戒指切换到 1024**） |
| 251 | `item.ItemTeleportationTome5.lore` | `%s` | `discordTomeShiftRange` |
| 257 | `item.LunarFlares3.lore` | `%s` | 类内常量 `IMPACT_DAMAGE`（**不走配置**） |
| 259 | `item.LunarFlares4.lore` | `%s` | 类内常量 `DIRECT_DAMAGE`（**不走配置**） |
| 262 | `item.ItemSoulTome1.lore` | `%s` | `soulTomeSearchRange` |
| 268 | `item.ItemSoulTome7.lore` | `%s` | `soulTomeKnockbackRange` |
| 285 | `item.ItemWeatherStone2.lore` | `%s` | `this.getVisCost()` = `(int)(weatherStoneVisCost * weatherStoneVisMult)` |
| 291 | `item.ItemApotheosis4.lore` | `%s` | `(int)damageApotheosisDirect` **仅显示** |
| 292 | `item.ItemApotheosis5.lore` | `%s` | `(int)damageApotheosisImpact` **仅显示** |
| 300 | `item.ItemChaosTome5.lore` | `%s` | `(int)chaosTomeDamageCap`（**这个是真生效的**） |
| 324 | `item.ItemThunderpeal3.lore` | `%s` | `(int)damageThunderpealDirect`（真生效） |
| 326 | `item.ItemThunderpeal5.lore` | `%s` | `(int)damageThunderpealBolt`（真生效） |
| 331 | `item.ItemTerrorCrown1.lore` | `%s` | `terrorCrownHavocRange` |

**无占位符但拼接数字的 key**（`I18n.format(...) + 数字`）：
- `item.ItemDimensionalMirror` 系列（`MirrorX/Y/Z/Dimension.lore`）接 `ItemNBTHelper.getInt`。
- `item.ItemDormantArcanum2.lore` 前接 `ILifetime * 2`。
- `item.ItemXPTome9.lore` 前接 `XPStored` + 单位 + `ExperienceHelper.getLevelForExperience(XPStored)` + 等级。
- `item.OblivionStoneMode.lore` + `item.OblivionMode{0,1,2}.lore` / `item.OblivionStoneDeactivated.lore`。
- `item.ItemParadoxDamage_1.lore` + `(int)paradoxDamageCap` + `item.ItemParadoxDamage_2.lore`（由 `RelicsEventHandler:112` 拼）。
- `item.FRCode1..15.lore`：`ItemTomeOfBrokenFates:67` 用 `Math.random()*15+1` 随机选一条彩蛋文本。
- `item.FRSeconds.lore` / `item.ItemDormantArcanum` 的单位。
