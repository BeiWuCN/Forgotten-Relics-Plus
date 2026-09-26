# Thaumaturge 1.21.1 × Curios 1.21.1 API 参考（Forgotten Relics 非官方移植专用）

> 本文档为 **只读调研产物**，不含模组代码。所有签名均从**源码**或**已构建 jar 的字节码/资源**中实际确认。
> 未确认的内容一律标注「**待确认**」，不凭记忆补全。

---

## 0. 文档约定

### 0.1 数据来源

| 来源 | 路径 | 说明 |
|---|---|---|
| Thaumaturge 源码 | `F:/Deepseek Harness/Forgotten Relics Unofficial/Thaumaturge-1.21.1/src/main/java/com/leclowndu93150/thaumaturge/` | 1483 个 `.java`，**完整源码** |
| Thaumaturge 源码资源 | `F:/Deepseek Harness/Forgotten Relics Unofficial/Thaumaturge-1.21.1/src/main/resources/` | 仅 432 个 JSON，**不含** datagen 产物 |
| Thaumaturge 成品 jar | `E:/Modpacks/versions/Thaumcraft 1.21.1/mods/thaumaturge-1.21.1-NeoForge-BETA-0.4.4.jar` | 6480 个条目；**datagen 产物（研究/分类/扫描/配方/伤害类型/curios entities）只在这里**，源码里没有 |
| Curios 源码 | `F:/Deepseek Harness/Forgotten Relics Unofficial/Curios-1.21.1/` | 9.5.1+1.21.1，common + neoforge multiloader |
| Botania 1.21.1 | `E:/Modpacks/versions/Thaumcraft 1.21.1/mods/botania-neoforge-1.21.1-457-*.jar`（解包到 `F:/Deepseek Harness/Forgotten Relics Unofficial/build/botania-jar/`） | **无源码**，只有字节码 + 资源 |
| 1.12.2 原版附属 | `F:/Deepseek Harness/Forgotten Relics Unofficial/Forgotten-Relics-RE-main/` | 反编译源码，用于确定「1.12.2 依赖了什么」 |
| 1.21.1 同类附属范例 | `F:/Deepseek Harness/Thaumic Energistics CE/ThaumicEnergistics/src/main/java/thaumicenergistics_ce/` | **已在用这些 API 的真实代码**，范例最可靠 |

### 0.2 两个关键前提（先读）

1. **源码树 ≠ 发行内容。** Thaumaturge 仓库里 `src/main/resources` **只有** `data/thaumaturge/thaumaturge/research_entry/*.json`（182 个）和 `data/thaumaturge/data_maps/item/base_aspects.json`、`data/thaumaturge/neoforge/biome_modifier`。
   以下内容**只存在于构建产物 jar**，是 datagen 生成的：
   - `data/thaumaturge/thaumaturge/aspect/*.json`、`research_category/*.json`、`scan/*.json`、`blueprint/*.json`、`pech_trade/*.json`
   - `data/thaumaturge/recipe/**`、`data/thaumaturge/damage_type/*.json`、`data/thaumaturge/curios/entities/players.json`、`data/thaumaturge/tags/**`
   - 也就是说：**附属要抄格式，必须看 jar**（或看 `com/leclowndu93150/thaumaturge/data/` 下的 datagen 类，共 45 个）。
2. **路径书写**：本文档统一用正斜杠（`F:/...`），Windows 下等价可读。

### 0.3 置信度标记

- 无标记 = 直接读到源码/jar 内容。
- 「**待确认**」 = 未在本机源码或字节码中直接确认。
- 「**推论**」 = 由多处间接证据推出的结论。

---

## 1. 模组入口与注册

### 1.1 模组 ID 与工具类

**`com.leclowndu93150.thaumaturge.TCIds`**
源码：`Thaumaturge-1.21.1/src/main/java/com/leclowndu93150/thaumaturge/TCIds.java`（16 行）

    package com.leclowndu93150.thaumaturge;

    public final class TCIds {
        public static final String MODID = "thaumaturge";
        public static final String CURIOS = "curios";
        public static final String IRIS = "iris";
        public static final String DISTANT_HORIZONS = "distanthorizons";

        private TCIds() {}

        public static ResourceLocation rl(String path) {
            return ResourceLocation.fromNamespaceAndPath(MODID, path);
        }
    }

### 1.2 主类

**`com.leclowndu93150.thaumaturge.Thaumaturge`**
源码：`.../thaumaturge/Thaumaturge.java`（117 行）

    @Mod(TCIds.MODID)
    public final class Thaumaturge {
        public static final Logger LOGGER = LoggerFactory.getLogger(TCIds.MODID);

        public Thaumaturge(IEventBus modBus, ModContainer container) { ... }
    }

构造器里做的事情（附属照抄这个模式即可）：

    TCFluidTypes.register(modBus);  TCFluids.register(modBus);   TCBlocks.register(modBus);
    TCMaterials.register(modBus);   TCItems.register(modBus);    TCFeatures.register(modBus);
    TCStructures.register(modBus);  TCBlockEntities.register(modBus); TCEntities.register(modBus);
    TCMenus.register(modBus);       TCRecipeTypes.register(modBus);   TCRecipeSerializers.register(modBus);
    TCDataComponents.register(modBus); TCCreativeTabs.register(modBus); TCParticles.register(modBus);
    TCSounds.register(modBus);      TCAttachments.register(modBus);   TCDamageTypes.register(modBus);
    TCMobEffects.register(modBus);  TCAttributes.register(modBus);    TCChunkGenerators.register(modBus);
    TCBiomeModifierSerializers.register(modBus); TCPlacementModifiers.register(modBus);
    TCGolemTraits.register(modBus); TCFocusElements.register(modBus); TCGolemParts.register(modBus);
    TCWandParts.register(modBus);   TCSeals.register(modBus);         TCEntityDataSerializers.register(modBus);

    container.registerConfig(ModConfig.Type.COMMON, ThaumaturgeCommonConfig.SPEC);
    container.registerConfig(ModConfig.Type.CLIENT, ThaumaturgeClientConfig.SPEC);
    container.registerConfig(ModConfig.Type.SERVER, ThaumaturgeServerConfig.SPEC);

    if (ModList.get().isLoaded(TCIds.CURIOS)) ThaumaturgeCuriosCompat.init(modBus);

**关键**：所有 API 门面（`*Access` / `*Helper` / `*Api`）都在这里 `bind(...)`。
**附属绝对不要调用任何 `bind(...)`**——它们大多是一次性绑定，二次调用抛 `IllegalStateException`。

### 1.3 注册表全清单

全部位于 `com.leclowndu93150.thaumaturge.registry`（41 个类，源码目录 `.../thaumaturge/registry/`）：

    TCAttachments  TCAttributes  TCBiomeModifierSerializers  TCBiomeTags  TCBlockEntities
    TCBlockFamilies  TCBlocks  TCBlockTags  TCChunkGenerators  TCColorParticleType
    TCCreativeTabs  TCDamageTypes  TCDataComponents  TCDataMaps  TCDatapackRegistries
    TCEffectTags  TCEntities  TCEntityDataSerializers  TCEntityTags  TCFeatures
    TCFluids  TCFluidTypes  TCFocusElements  TCGolemAccessories  TCGolemParts
    TCGolemTraits  TCItems  TCItemTags  TCLootTables  TCMenus  TCMobEffects
    TCParticles  TCParticleType  TCPlacementModifiers  TCRecipeSerializers  TCRecipeTypes
    TCSeals  TCSounds  TCSoundTypes  TCStructures  TCWandParts

**注意**：`registry/TCDamageTypes.java` 是**空壳**（`register(IEventBus)` 方法体为空），
真正的伤害类型常量在 `api/damagesource/TCDamageTypes.java`，见 §8。

### 1.4 DeferredRegister 用法（NeoForge 1.21.1 现代写法）

Thaumaturge 用的**不是**老式 `DeferredRegister.create(Registries.ITEM, MODID)`，而是类型化工厂：

| 注册表 | 声明（真实代码） | 源码 |
|---|---|---|
| 物品 | `public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TCIds.MODID);` | `registry/TCItems.java:106` |
| 方块 | `public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TCIds.MODID);` | `registry/TCBlocks.java:150` |
| 数据组件 | `public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TCIds.MODID);` | `registry/TCDataComponents.java:38` |
| 实体 | `public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TCIds.MODID);` | `registry/TCEntities.java:27` |
| 配方类型 | `public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TCIds.MODID);` | `registry/TCRecipeTypes.java:16` |
| 创造标签 | `public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TCIds.MODID);` | `registry/TCCreativeTabs.java:31` |
| 属性 | `public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, TCIds.MODID);` | `registry/TCAttributes.java:22` |

**物品注册的三种真实写法**（`registry/TCItems.java`）：

    // 1) 普通物品
    public static final DeferredItem<AmuletVisItem> AMULET_VIS = ITEMS.registerItem(
            "amulet_vis", AmuletVisItem::new, new Item.Properties().stacksTo(1));

    // 2) 方块物品（自动用方块的注册路径）
    public static final DeferredItem<BlockItem> CRUCIBLE = ITEMS.registerSimpleBlockItem(TCBlocks.CRUCIBLE);

    // 3) 自定义方块物品 + 构造器（文件末尾的真实辅助方法，可直接抄）
    public static <T extends BlockItem> DeferredItem<BlockItem> registerSimpleBlockItem(
            Holder<Block> block, BiFunction<Block, Item.Properties, T> constructor) {
        return ITEMS.registerItem(
                block.unwrapKey().orElseThrow().location().getPath(),
                p -> constructor.apply(block.value(), p),
                new Item.Properties());
    }

    public static void register(IEventBus modBus) { ITEMS.register(modBus); }

### 1.5 创造模式标签

**`com.leclowndu93150.thaumaturge.registry.TCCreativeTabs`**
源码：`.../registry/TCCreativeTabs.java`（530 行）

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TCIds.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THAUMATURGE =
            CREATIVE_MODE_TABS.register("thaumaturge", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumaturge"))
                    .icon(() -> new ItemStack(TCItems.THAUMONOMICON.get()))
                    .displayItems((parameters, output) -> { output.accept(...); ... })
                    .build());

    public static void register(IEventBus modBus) { CREATIVE_MODE_TABS.register(modBus); }

真实辅助方法（用于「tag 非空才显示」的条目）：

    private static void addIfTag(CreativeModeTab.ItemDisplayParameters parameters,
                                 CreativeModeTab.Output output, TagKey<Item> tag, Item item)

### 1.6 最小可用示例：附属注册一个物品 + 一个创造标签

    package com.forgottenrelics.forgotten_relics.registry;

    public final class FRItems {
        public static final DeferredRegister.Items ITEMS =
                DeferredRegister.createItems(ForgottenRelics.MOD_ID);

        public static final DeferredItem<Item> RELIC =
                ITEMS.registerItem("relic", Item::new, new Item.Properties().rarity(Rarity.EPIC));

        public static void register(IEventBus modBus) { ITEMS.register(modBus); }
    }

    public final class FRCreativeTabs {
        public static final DeferredRegister<CreativeModeTab> TABS =
                DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgottenRelics.MOD_ID);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
                TABS.register("main", () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.forgotten_relics"))
                        .icon(() -> new ItemStack(FRItems.RELIC.get()))
                        .displayItems((p, out) -> out.accept(FRItems.RELIC.get()))
                        .build());

        public static void register(IEventBus modBus) { TABS.register(modBus); }
    }

> 现有工程 `Forgotten-Relics-Unofficial/src/main/java/com/forgottenrelics/forgotten_relics/`
> 已经在用这个模式（`ForgottenRelics.java`、`registry/FRItems.java`、`registry/FRCreativeTabs.java`）。

---

## 2. 要素 (Aspect)

### 2.1 结论：**数据包注册，不是代码注册**

要素是 **DataPack Registry**，注册键 `thaumaturge:aspect`。
注册发生在 `registry/TCDatapackRegistries.java`：

    @EventBusSubscriber(modid = TCIds.MODID)
    public final class TCDatapackRegistries {
        @SubscribeEvent
        public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(IAspect.REGISTRY_KEY, Aspect.CODEC, Aspect.CODEC);
            event.dataPackRegistry(Blueprint.REGISTRY_KEY, Blueprint.CODEC, Blueprint.CODEC);
            event.dataPackRegistry(IResearchCategory.REGISTRY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
            event.dataPackRegistry(IResearchEntry.REGISTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
            event.dataPackRegistry(ScanEntry.REGISTRY_KEY, ScanEntry.CODEC, ScanEntry.CODEC);
            event.dataPackRegistry(PechTradeTable.REGISTRY_KEY, PechTradeTable.CODEC);
        }
    }

**JSON 路径**：`data/<namespace>/thaumaturge/aspect/<tag>.json`
（注意是二级目录 `thaumaturge/aspect`，即 `RegistryKey = thaumaturge:aspect`）

### 2.2 `IAspect` 接口

**`com.leclowndu93150.thaumaturge.api.aspect.IAspect`**
源码：`api/aspect/IAspect.java`（88 行）

    public interface IAspect {
        ResourceKey<Registry<IAspect>> REGISTRY_KEY =
                ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "aspect"));

        String tag();
        int color();                                   // 0xRRGGBB
        List<Holder<IAspect>> components();            // 空 = primal；否则恰好 2 个
        Optional<String> chatColor();
        ResourceLocation texture();
        int blend();

        default boolean isPrimal() { return components().isEmpty(); }
    }

### 2.3 实现类与 JSON 字段

**`com.leclowndu93150.thaumaturge.content.aspect.Aspect`**（record）
源码：`content/aspect/Aspect.java`（77 行）

    public record Aspect(String tag, int color, List<Holder<IAspect>> components,
                         Optional<String> chatColor, ResourceLocation texture, int blend)
            implements IAspect {
        public static final int DEFAULT_BLEND = 1;
        public static final int CONTRAST_BLEND = 771;
    }

Codec 字段（`Aspect.DIRECT_CODEC`，逐字段核实）：

| JSON 字段 | 类型 | 必填 | 默认 | 含义 |
|---|---|---|---|---|
| `tag` | string | **必填** | — | 要素标识（= 注册路径） |
| `color` | int | **必填** | — | 打包 RGB，如 `16777086` |
| `components` | aspect id 数组 | 可选 | `[]` | **必须 0 或 2 个**，其它数量报错 |
| `chat_color` | string | 可选 | 空 | 旧版聊天色码（不含 `§`），如 `"e"` |
| `texture` | ResourceLocation | 可选 | `thaumaturge:textures/aspects/<tag>.png` | 图标贴图 |
| `blend` | int | 可选 | `1` | OpenGL blend；熵系用 `771` |

校验（`Aspect.validate`）：`components.size()` 必须是 0 或 2，否则 `DataResult.error("Aspect '<tag>' must have 0 or 2 components, has N")`。

**从 jar 实测的真实 JSON**（`build/tcjar/data/thaumaturge/thaumaturge/aspect/`）：

`aer.json`（primal）:

    {
      "chat_color": "e",
      "color": 16777086,
      "tag": "aer"
    }

`ignis.json`（primal）:

    {
      "chat_color": "c",
      "color": 16734721,
      "tag": "ignis"
    }

`motus.json`（compound，Aer + Ordo）:

    {
      "color": 13487604,
      "components": [
        "thaumaturge:aer",
        "thaumaturge:ordo"
      ],
      "tag": "motus"
    }

其它实测样例：`auram = praecantatio + aer`(16761087)、`vitium = perditio + praecantatio`(8388736)、
`gelum = ignis + perditio`(14811135)、`praecantatio = potentia + aer`(13566207)。

### 2.4 如何取得要素

**`com.leclowndu93150.thaumaturge.api.aspect.Aspects`**（静态解析门面）
源码：`api/aspect/Aspects.java`（66 行）

    public static @Nullable Holder<IAspect> resolve(HolderLookup.Provider registries, ResourceKey<IAspect> key)
    public static @Nullable Holder<IAspect> resolve(@Nullable Level level, ResourceKey<IAspect> key)
    public static @Nullable Holder<IAspect> resolve(Registry<IAspect> registry, ResourceKey<IAspect> key)

三者注册表不可用或键不存在时**返回 null**，不抛异常。

最小示例（真实范例来自 `ThaumicEnergistics CE`）：

    var aspect = Aspects.resolve(level.registryAccess(),
            ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.parse("thaumaturge:aer")));
    if (aspect != null) { int rgb = aspect.value().color(); }

### 2.5 内置要素常量

**`com.leclowndu93150.thaumaturge.api.aspect.TCAspects`**
源码：`api/aspect/TCAspects.java`（72 行）

**全部常量都是 `ResourceKey<IAspect>`**（不是 `IAspect`），可安全写在静态初始化里。
命名空间固定 `thaumaturge`。

    AER("aer")  TERRA("terra")  IGNIS("ignis")  AQUA("aqua")  ORDO("ordo")  PERDITIO("perditio")
    VACUOS  LUX  MOTUS  GELUM  VITREUS  METALLUM  VICTUS  MORTUUS  POTENTIA  PERMUTATIO
    PRAECANTATIO  AURAM  ALKIMIA  VITIUM  TENEBRAE  ALIENIS  VOLATUS  HERBA  INSTRUMENTUM
    FABRICO  MACHINA  VINCULUM  SPIRITUS  COGNITIO  SENSUS  AVERSIO  PRAEMUNIO  DESIDERIUM
    EXANIMIS  BESTIA  HUMANUS

    /** 六大源质，规范显示顺序：aer, ignis, aqua, terra, ordo, perditio。
     *  该顺序被法杖 vis 池与奥术工作台水晶槽使用。 */
    public static final List<ResourceKey<IAspect>> PRIMALS = List.of(AER, IGNIS, AQUA, TERRA, ORDO, PERDITIO);

> jar 里实际存在的 aspect 文件（来自 `data/thaumaturge/thaumaturge/aspect/`）：
> aer, alienis, alkimia, aqua, auram, aversio, bestia, cognitio, desiderium, exanimis, fabrico,
> gelum, herba, humanus, ignis, instrumentum, lux, machina, metallum, mortuus, motus, ordo,
> perditio, permutatio, potentia, praecantatio, praemunio, sensus, spiritus, tenebrae, terra,
> vacuos, victus, vinculum, vitium, vitreus, volatus（37 个）。

### 2.6 `AspectList` 与 `AspectInstance`

**`api.aspect.AspectInstance`**（record，源码 `api/aspect/AspectInstance.java`，54 行）

    public record AspectInstance(Holder<IAspect> aspect, int amount) {
        public static final Codec<AspectInstance> CODEC;
        public static final StreamCodec<RegistryFriendlyByteBuf, AspectInstance> STREAM_CODEC;
        public AspectInstance withAmount(int newAmount);
    }

- JSON 形如 `{"aspect": "thaumaturge:aer", "amount": 5}`，`amount` 默认 `1`，范围 `[1, MAX_INT]`。
- 构造时 `amount < 1` 抛 `IllegalArgumentException`。

**`api.aspect.AspectList`**（不可变有序列表，源码 `api/aspect/AspectList.java`，335 行）

    public static final AspectList EMPTY;
    public static final Codec<AspectList> CODEC;            // JSON 数组 of AspectInstance
    public static final Codec<AspectList> NON_EMPTY_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC;

    public static AspectList ofEntries(List<AspectInstance> entries);   // 重复要素合并求和
    public static AspectList of(AspectInstance... entries);
    public List<AspectInstance> entries();
    public int size();
    // 其余查询/变换方法：add / remove / without / reduce / merge /
    // amountOf(...) / totalAmount / sortedByTag / sortedByAmount / isEmpty ...

真实用法（`ThaumicEnergistics CE`）：

    AspectList composition = AspectIndexAccess.of(source);        // 从 block entity 拿要素
    AspectList composition = AspectIndexAccess.of(stack);         // 从 ItemStack 拿要素
    AspectList list = AspectList.EMPTY.add(aspect, amount);       // 逐步累加
    for (AspectInstance entry : list.sortedByAmount().reversed()) { ... }

### 2.7 物品/方块的要素注册：**NeoForge DataMap**（不是代码）

**`com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps`**
源码：`api/aspect/AspectDataMaps.java`（43 行）

    public static final DataMapType<Item, AspectList> BASE_ASPECTS = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "base_aspects"),
            Registries.ITEM, AspectList.CODEC).synced(AspectList.CODEC, false).build();

    public static final DataMapType<EntityType<?>, AspectList> ENTITY_ASPECTS = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "entity_aspects"),
            Registries.ENTITY_TYPE, AspectList.CODEC).synced(AspectList.CODEC, false).build();

**JSON 路径**：
- 物品：`data/<namespace>/data_maps/item/base_aspects.json`
- 实体：`data/<namespace>/data_maps/entity_type/entity_aspects.json`（**实体侧路径待确认**，仅确认了 `DataMapType` 注册键为 `thaumaturge:entity_aspects`）

**从 jar 实测**的 `data/thaumaturge/data_maps/item/base_aspects.json` 结构（顶层是 NeoForge 标准 data map 包装）：

    {
      "replace": false,
      "values": {
        "minecraft:hopper": [
          { "aspect": "thaumaturge:machina",   "amount": 5 },
          { "aspect": "thaumaturge:permutatio","amount": 10 },
          { "aspect": "thaumaturge:vacuos",    "amount": 5 }
        ],
        "minecraft:ice": [
          { "aspect": "thaumaturge:gelum", "amount": 20 }
        ],
        "minecraft:honeycomb": [
          { "aspect": "thaumaturge:bestia", "amount": 5 },
          { "aspect": "thaumaturge:desiderium", "amount": 2 }
        ]
      }
    }

### 2.8 运行时读取物品要素

**`api.aspect.AspectIndexAccess`**（源码 `api/aspect/AspectIndexAccess.java`，73 行）
底层 `api.aspect.IAspectIndex`：

    public static IAspectIndex index();
    public static AspectList of(Item item);
    public static AspectList of(ItemStack stack);   // 会考虑 per-stack 组件覆盖

- 索引在每次服务器资源重载时重建，并同步给客户端，**双端都可直接调**。
- 索引 = data map 声明的 base aspects + 由配方推导（`IAspectRecipeContributor`）。
- 索引未建立时返回 `AspectList.EMPTY`，不抛异常；但 `index()` 在 `bind` 之前会抛。

### 2.9 给要素补充「配方推导」的扩展点

**`api.aspect.RegisterAspectContributorsEvent`**（`IModBusEvent`，源码 39 行）

    public void register(IAspectRecipeContributor contributor);
    public List<IAspectRecipeContributor> contributors();

**`api.aspect.IAspectRecipeContributor`**（源码 52 行）

    default void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {}
    Optional<AspectList> derive(Item item, RecipeManager recipes,
                                HolderLookup.Provider registries, IAspectIndex partial);

契约：必须确定性、不得依赖世界状态；返回 `Optional.empty()` 表示弃权。
内建贡献者顺序：crucible → infusion → crafting（crafting walker 也处理奥术工作台配方）。

用法：

    modBus.addListener((RegisterAspectContributorsEvent e) -> e.register(new MyContributor()));

### 2.10 要素能力（方块容器）

**`api.aspect.AspectCapabilities`**（源码 23 行）

    public static final BlockCapability<IAspectContainer, Direction> CONTAINER =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath("thaumaturge", "aspect_container"),
                    IAspectContainer.class);

**`api.aspect.IAspectContainer`**（源码 70 行）

    AspectList getAspects();
    void setAspects(AspectList aspects);
    boolean doesContainerAccept(Holder<IAspect> aspect);
    int addToContainer(Holder<IAspect> aspect, int amount);      // 返回没加进去的量
    boolean takeFromContainer(Holder<IAspect> aspect, int amount);
    boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount);
    int containerContains(Holder<IAspect> aspect);

**`api.aspect.IAspectSource extends IAspectContainer`**：额外 `boolean isBlocked();`

注册范例（真实代码，`ThaumicEnergistics CE/ThaumicEnergistics.java:348`）：

    event.registerBlockEntity(
            com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities.CONTAINER,
            ModBlockEntities.INFUSION_PROVIDER.get(),
            (blockEntity, context) -> (com.leclowndu93150.thaumaturge.api.aspect.IAspectSource) blockEntity);

> 源码注释明确：灌注的源发现会以 **null side** 查询该能力，实现必须支持 null context。

### 2.11 要素文本工具

**`api.aspect.AspectComponents`**（源码 123 行）

    public static MutableComponent name(Holder<IAspect> aspect);       // 未发现时显示占位符
    public static MutableComponent shortName(Holder<IAspect> aspect);
    public static MutableComponent trueName(Holder<IAspect> aspect);   // 无视发现状态
    public static MutableComponent description(Holder<IAspect> aspect);
    public static MutableComponent help(Holder<IAspect> aspect);
    public static MutableComponent composition(Holder<IAspect> aspect);

翻译键规则：`aspect.<namespace>.<tag>`、`.desc`、`.help`
（namespace 从注册 id 推导，所以附属自己的要素无需额外配置）。

**`api.aspect.AspectChipsTooltip`**（源码 15 行）

    public record AspectChipsTooltip(AspectList aspects) implements TooltipComponent {}

这是**服务端通用**的 tooltip 载体；Thaumaturge 自己在
`client/tooltip/AspectTooltipEvents.java` 里通过
`RenderTooltipEvent.GatherComponents` 注入：

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null
                || !KnowledgeAccess.of(mc.player)
                        .isResearchKnown(ScanKeys.item(event.getItemStack().getItem()))) return;
        AspectList aspects = AspectIndexAccess.index().of(event.getItemStack());
        if (aspects.isEmpty()) return;
        event.getTooltipElements().add(Either.right(new AspectChipsTooltip(aspects)));
    }

**结论**：只要物品有 base_aspects 且玩家已扫描过它，**要素 chip 会自动出现在 tooltip 上，附属不用自己写渲染**。

---

## 3. 灵气（Aura）与 Vis

### 3.1 结论：1.21.1 没有「玩家背包 Vis」这个概念

1.12.2 的 `RechargeHelper.getVisFromInventory(player)` / `IRechargable` 计费模型在 1.21.1 **被拆成三条互不相通的通路**：

| 通路 | 1.21.1 API | 存储位置 | 单位 |
|---|---|---|---|
| **区块灵气** | `api.aura.AuraHelper` + `api.aura.IAuraChunk` | 每个 Chunk 的 `Attachment`（`TCAttachments.AURA`） | vis = 1.0f |
| **法杖储能** | `api.wands.WandAccess` + `WandVis`（数据组件 `thaumaturge:wand_vis`） | 物品数据组件 | **centivis**，100 centivis = 1 vis |
| **物品充能** | `api.items.RechargeAccess` + `IRechargable`（数据组件 `thaumaturge:charge`） | 物品数据组件 | 整数 charge |

**没有任何 API 能「从玩家背包里抽 vis」。** 1.12.2 那句 `RechargeHelper.getVisFromInventory(player)` 在 1.21.1 **无对应物**（见 §12）。

### 3.2 `api.aura.AuraHelper` —— 完整静态方法表（逐字核对）

源码：`api/aura/AuraHelper.java`。**全部 public static，addon 直接调；只有 `bind(...)` 例外。**

    public static float getVis(Level level, BlockPos pos);
    public static float getFlux(Level level, BlockPos pos);
    public static int   getAuraBase(Level level, BlockPos pos);
    public static float getTotalAura(Level level, BlockPos pos);

    public static float getFluxSaturation(Level level, BlockPos pos);
    public static float capacityRemaining(Level level, BlockPos pos);
    public static boolean canAcceptVis(Level level, BlockPos pos, float amount);
    public static boolean shouldPreserveAura(Level level, BlockPos pos);

    public static float addVis(Level level, BlockPos pos, float amount);
    public static float addFlux(Level level, BlockPos pos, float amount);
    public static float drainVis(Level level, BlockPos pos, float amount, boolean simulate);
    public static float drainVis(Level level, BlockPos pos, float amount);
    public static float drainFlux(Level level, BlockPos pos, float amount, boolean simulate);
    public static float drainFlux(Level level, BlockPos pos, float amount);
    public static void  polluteAura(Level level, BlockPos pos, float amount);

    public static @Nullable IAuraChunk of(Level level, BlockPos pos);       // 拿底层 chunk 对象
    // bind(Bindings) 为 Thaumaturge 内部初始化，addon 禁止调用

**语义要点**：
- `addVis` / `addFlux` **返回实际成功加入的量**（受容量上限限制），不是「请求量」。
- `drainVis` / `drainFlux` **返回实际抽到的量**；带 `simulate` 的重载只探测不修改。
- `shouldPreserveAura`：判断「附近有灵气保护装置/研究台」，为 true 时 `RechargeAccess.rechargeItem` **直接中止**（见 §3.5）。
- `polluteAura` = 加 flux，语义上是「污染」。

### 3.3 `api.aura.IAuraChunk` —— 区块底层表示

源码：`api/aura/IAuraChunk.java`。

    public interface IAuraChunk {
        short getBase();          // 该区块的基础灵气上限（按生物群系生成）
        float getVis();           // 当前 vis，钳制在 [0, 500]
        float getFlux();          // 当前 flux，钳制在 [0, 32766]
        ChunkPos getChunkPos();
    }

> 注意 `getBase()` 返回 **short**，而 `getVis()`/`getFlux()` 是 **float**。
> vis 硬上限 500，flux 硬上限 32766。

**最小可运行示例（读当前区块灵气）**：

    // 服务端：任意 Level + BlockPos
    float vis  = AuraHelper.getVis(level, pos);
    float flux = AuraHelper.getFlux(level, pos);
    int   base = AuraHelper.getAuraBase(level, pos);

    // 消耗 5 vis（不够就抽多少算多少）
    float drained = AuraHelper.drainVis(level, pos, 5.0F, false);
    if (drained < 5.0F) { /* 灵气不足，回滚或失败 */ }

    // 释放 3 vis 到环境（返回真正加入的量）
    float added = AuraHelper.addVis(level, pos, 3.0F);

### 3.4 法杖 Vis：`WandVis` / `WandAccess`

**`api.wands.WandVis`**（57 行，不可变值对象）：

    public static final WandVis EMPTY;
    public static final Codec<WandVis> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, WandVis> STREAM_CODEC;

    public int amount(ResourceKey<IAspect> aspect);          // 单位：centivis
    public WandVis with(ResourceKey<IAspect> aspect, int centivis);
    public boolean isEmpty();
    // 构造：public WandVis(Map<ResourceKey<IAspect>, Integer> centivis)

**100 centivis = 1 vis。** 只记录 **primal 要素**（aer/ignis/terra/aqua/ordo/perditio）。

**`api.wands.WandAccess`**（86 行）：

    public static WandVis getAllVis(ItemStack wand);
    public static int getVis(ItemStack wand, ResourceKey<IAspect> aspect);   // centivis
    public static ItemStack withVis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis);
    // bind(Supplier<DataComponentType<WandVis>>) 内部初始化，addon 禁止调用

**`content.wands.WandVisHelper`**（`content/wands/WandVisHelper.java`）—— 17 个 public static，
把 centivis 换算成 vis、按折扣扣费、跨多根法杖轮询等。
**待确认**：完整逐字签名未逐行抄录；addon 若只需读/写数值，直接用 `WandAccess` 即可。

### 3.5 物品充能：`RechargeAccess` / `IRechargable`

**`api.items.RechargeAccess`**（150 行）：

    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, Player player, int amount);
    public static float rechargeItemBlindly(ItemStack stack, LivingEntity holder, int amount);
    public static int   getCharge(ItemStack stack);                        // 非 IRechargable 返回 -1
    public static float getChargePercentage(ItemStack stack, LivingEntity holder);
    public static boolean consumeCharge(ItemStack stack, LivingEntity holder, int amount);
    // bind(...) 内部初始化，addon 禁止调用

**`api.items.IRechargable`**：

    public interface IRechargable {
        int getMaxCharge(ItemStack stack, LivingEntity holder);
        ChargeDisplay showInHud(ItemStack stack, LivingEntity holder);
        enum ChargeDisplay { NEVER, NORMAL, PERIODIC }
    }

**关键行为**：`RechargeAccess.rechargeItem(Level, ItemStack, BlockPos, Player, int) -> float`
会**先**检查 `AuraHelper.shouldPreserveAura(level, pos)`，为 true 则**直接返回、不充电**。
它从区块 aura 抽 vis 写入物品的 `thaumaturge:charge` 组件。

**这正好替代 1.12.2 的 `RechargeHelper.rechargeItem`**：旧版从「玩家背包 vis」抽，新版从「区块 aura」抽。

**完整示例（给自己的饰品加充能）**：

    public final class MyCharmItem extends Item implements IRechargable {
        private static final int MAX_CHARGE = 500;

        @Override public int getMaxCharge(ItemStack stack, LivingEntity holder) { return MAX_CHARGE; }

        @Override public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
            return ChargeDisplay.NORMAL;      // 自动在 HUD 上显示 vis 条
        }

        /** 每 tick 被 curio 的 curioTick 调用（见 §10.6）。 */
        public void wornTick(ItemStack stack, LivingEntity wearer) {
            if (!(wearer instanceof Player player)) return;
            if (RechargeAccess.consumeCharge(stack, player, 10)) { /* 消耗 10 charge 做点什么 */ }
            RechargeAccess.rechargeItem(wearer.level(), stack, wearer.blockPosition(), player, 5);
        }
    }

> 实现 `IRechargable` 后，**tooltip 上的「充能 X / Y」一行会自动出现**（由 `client.TCTooltipEvents` 注入，见 §7.3.2），
> 不用自己写 `appendHoverText`。

### 3.6 Vis 中继（Vis Relay）—— 给机器抽 aura 用

源码：`api/vis/VisRelaySources.java`、`IVisRelaySource.java`。

    // api.vis.VisRelaySources
    public static void register(VisRelaySourceContext context, IVisRelaySource source);
    // bind(...) 内部初始化，addon 禁止调用

    // api.vis.IVisRelaySource
    public interface IVisRelaySource {
        Reservation reserve(/* ... */);
        interface Reservation {
            int amount();
            boolean commit();
            void close();
        }
    }

**用途**：让自动化机器（如 Thaumic Energistics 的 vis 接口）向 aura 申请 vis 预留。
典型模式是 `reserve` 探测可用量、`commit` 真正扣减、`close` 释放未使用部分。

---

## 4. 研究（Research）

### 4.1 注册方式：**全部是数据包 JSON，没有任何 Java 注册 API**

`registry/TCDatapackRegistries.java`（`@EventBusSubscriber(modid = TCIds.MODID)`）：

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(IAspect.REGISTRY_KEY, Aspect.CODEC, Aspect.CODEC);
        event.dataPackRegistry(Blueprint.REGISTRY_KEY, Blueprint.CODEC, Blueprint.CODEC);
        event.dataPackRegistry(IResearchCategory.REGISTRY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
        event.dataPackRegistry(IResearchEntry.REGISTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
        event.dataPackRegistry(ScanEntry.REGISTRY_KEY, ScanEntry.CODEC, ScanEntry.CODEC);
        event.dataPackRegistry(PechTradeTable.REGISTRY_KEY, PechTradeTable.CODEC);
    }

注册表 key 决定了目录（NeoForge 规则：`data/<namespace>/<registryNamespace>/<registryPath>/`）：

| 内容 | REGISTRY_KEY | 完整 JSON 目录 |
|---|---|---|
| 要素 | `thaumaturge:aspect` | `data/<ns>/thaumaturge/aspect/<id>.json` |
| 研究分类 | `thaumaturge:research_category` | `data/<ns>/thaumaturge/research_category/<id>.json` |
| 研究条目 | `thaumaturge:research_entry` | `data/<ns>/thaumaturge/research_entry/<id>.json` |
| 扫描 | `thaumaturge:scan` | `data/<ns>/thaumaturge/scan/<id>.json` |
| 蓝图 | `thaumaturge:blueprint` | `data/<ns>/thaumaturge/blueprint/<id>.json` |
| 佩奇交易 | `thaumaturge:pech_trade` | `data/<ns>/thaumaturge/pech_trade/<id>.json` |

**实测反证**：全仓库 grep `IResearchEntry.REGISTRY_KEY` 只出现在读取侧（browser / manager / command / JEI），
`new ResearchEntry(` 只在 `content/research/ResearchEntry.java` 的 codec 内部。
grep `ResearchEntryBuilder|IResearchEntryBuilder|registerResearch|addResearchEntry|ResearchRegistry`
在 `api/research` 下 **0 命中**。→ **没有代码注册路径。**

**注**：`ResearchEntryMeta.AUTOUNLOCK` 与 `ResearchManager.applyAutoUnlock` 是**运行时解锁**，不是注册。

### 4.2 研究条目 `ResearchEntry` 字段全表

源码：`content/research/ResearchEntry.java`（codec 逐字）：

    public record ResearchEntry(
            Holder<IResearchCategory> category, String nameKey,
            Set<ResearchParent> parents, Set<ResourceLocation> siblings,
            int column, int row, List<IResearchStage> stages,
            Set<ResearchEntryMeta> meta, List<ResearchIcon> icons,
            List<ResearchAddendum> addenda, AspectList noteAspects, int complexity)
            implements IResearchEntry {

        public static final Codec<ResearchEntry> DIRECT_CODEC = RecordCodecBuilder.<ResearchEntry>create(instance ->
            instance.group(
                RegistryFixedCodec.create(IResearchCategory.REGISTRY_KEY).fieldOf("category"),
                Codec.STRING.fieldOf("name"),
                ResearchParent.CODEC.listOf().optionalFieldOf("parents", List.of()),
                ResourceLocation.CODEC.listOf().optionalFieldOf("siblings", List.of()),
                Codec.INT.fieldOf("column"),
                Codec.INT.fieldOf("row"),
                ResearchStage.CODEC.listOf().fieldOf("stages"),
                ResearchEntryMeta.CODEC.listOf().optionalFieldOf("meta", List.of()),
                ResearchIcon.CODEC.listOf().optionalFieldOf("icons", List.of()),
                ResearchAddendum.CODEC.listOf().optionalFieldOf("addenda", List.of()),
                AspectList.CODEC.optionalFieldOf("note_aspects", AspectList.EMPTY),
                Codec.intRange(1, 3).optionalFieldOf("complexity", 1))
            .apply(instance, ResearchEntry::create))
            .validate(ResearchEntry::validate);   // stages 为空 -> "must declare at least one stage"

| JSON 字段 | 类型 | 必填 | 默认 | 含义 |
|---|---|---|---|---|
| `category` | ResourceLocation（指向 `thaumaturge:research_category`） | **是** | — | 所属分类，如 `"thaumaturge:basics"` |
| `name` | String | **是** | — | 标题翻译键（**字面 key**，不是译文） |
| `parents` | List<String> | 否 | `[]` | 前置条目，字符串语法见 §4.3 |
| `siblings` | List<ResourceLocation> | 否 | `[]` | 装饰性连线，不影响进度 |
| `column` | int | **是** | — | 分类网格列 |
| `row` | int | **是** | — | 分类网格行 |
| `stages` | List<Stage> | **是** | — | 阶段列表，**不能为空** |
| `meta` | List<枚举> | 否 | `[]` | 见 §4.6 |
| `icons` | List<String> | 否 | `[]` | 见 §4.5 |
| `addenda` | List<Addendum> | 否 | `[]` | 完成后的附加页 |
| `note_aspects` | List<AspectInstance> | 否 | `[]` | 研究笔记谜题的要素锚点；数量 = 从要素池支付的观察门槛点数 |
| `complexity` | int **1..3** | 否 | `1` | 笔记谜题复杂度，越界 codec 直接报错 |

### 4.3 `ResearchParent` —— **是字符串，不是对象**

源码：`api/research/ResearchParent.java`

    public record ResearchParent(ResourceLocation id, int stage, boolean inherit)
    public static final Codec<ResearchParent> CODEC =
            Codec.STRING.comapFlatMap(ResearchParent::parse, ResearchParent::serialize);

    private String serialize() {
        return (inherit ? "~" : "") + id + (stage > 0 ? "@" + stage : "");
    }

| 写法 | 含义 |
|---|---|
| `"thaumaturge:metallurgy"` | 要求父条目**完全完成** |
| `"thaumaturge:metallurgy@3"` | 要求父条目达到 **stage 3**（1-based；`@1` = 只要开始） |
| `"~thaumaturge:gotthaumonomicon"` | 仍然要求进度，但 Thaumonomicon **不画连线** |

判定逻辑：`isSatisfiedBy(IPlayerKnowledge)` —— 完成父条目直接通过，
否则 `stage > 0 && knowledge.isResearchKnown(id, stage - 1)`。

**真实用法已核实**：`unlock_artifice.json:6` = `"thaumaturge:metallurgy@2"`；
`nodes.json:5` = `"~thaumaturge:gotthaumonomicon"`。

### 4.4 `ResearchStage` 字段全表

源码：`content/research/ResearchStage.java`

    public record ResearchStage(
            String textKey, List<ResourceLocation> recipes, Optional<ResearchConstruct> construct,
            List<ResearchRequirement> obtain, List<ResearchRequirement> craft,
            List<KnowledgeReward> knowledge, List<KnowledgeReward> requiredKnowledge,
            List<ResourceLocation> requiredResearch, int warp) implements IResearchStage {

        public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("text"),
                ResourceLocation.CODEC.listOf().optionalFieldOf("recipes", List.of()),
                ResearchConstruct.CODEC.optionalFieldOf("construct"),
                ResearchRequirement.CODEC.listOf().optionalFieldOf("obtain", List.of()),
                ResearchRequirement.CODEC.listOf().optionalFieldOf("craft", List.of()),
                KnowledgeReward.CODEC.listOf().optionalFieldOf("knowledge", List.of()),
                KnowledgeReward.CODEC.listOf().optionalFieldOf("required_knowledge", List.of()),
                ResourceLocation.CODEC.listOf().optionalFieldOf("required_research", List.of()),
                Codec.INT.optionalFieldOf("warp", 0))
            .apply(instance, ResearchStage::new));
    }

| 字段 | 类型 | 默认 | 含义 |
|---|---|---|---|
| `text` | String | **必填** | 阶段正文翻译键 |
| `recipes` | List<ResourceLocation> | `[]` | UI 面板展示的配方 id（如 `thaumaturge:arcane_workbench/thaumometer`） |
| `construct` | object | 无 | 多方块结构图，见 §4.7 |
| `obtain` | List<ResearchRequirement> | `[]` | 必须**收集**的物品 |
| `craft` | List<ResearchRequirement> | `[]` | 必须**合成**的物品 |
| `knowledge` | List<KnowledgeReward> | `[]` | 本阶段完成时**发放**的要素知识 |
| `required_knowledge` | List<KnowledgeReward> | `[]` | 进入下一阶段**需要并消耗**的要素知识 |
| `required_research` | List<ResourceLocation> | `[]` | 必须已完成的其它研究条目 |
| `warp` | int | `0` | 阶段完成时施加的扭曲值 |

**`ResearchRequirement`**（`api/research/ResearchRequirement.java`）：

    public record ResearchRequirement(HolderSet<Item> items, DataComponentPatch components, int amount)
    // JSON: { "items": <id | [ids] | "#tag">, "components": {...} 可选, "amount": <int> }

- `items` **必填**，接受单个 id、id 数组、或 `#tag`。
- `components` 可选（`DataComponentPatch`）：候选堆叠必须**全部**匹配；
  **附魔组件按下界匹配**（要 Fortune I，任意等级 Fortune 都算）。
- `amount` 必填，聚合计数下界。

**`KnowledgeReward`**（`api/research/KnowledgeReward.java`）：

    public record KnowledgeReward(KnowledgeType type, Holder<IResearchCategory> category, int amount)
    // JSON: { "type": "theory"|"observation", "category": "...", "amount": <int> }

`KnowledgeType`：`theory`（progression 32，缩写 T）、`observation`（progression 16，缩写 O）。
**可见等级 = 原始计数 / progression。**

### 4.5 `ResearchIcon` —— 也是**字符串**

解析规则（`ResearchIcon.parse`）：

| 写法 | 解释 |
|---|---|
| `"focus:thaumaturge:xxx"` | focus 元素图标 |
| `"thaumaturge:textures/research/r_wisp.png"` | **path 以 `.png` 结尾 → 纹理路径** |
| `"thaumaturge:thaumonomicon"` | 物品 id |

多个 icon 会被浏览器轮播；为空时回退到首个阶段物品需求的图标。

### 4.6 `ResearchEntryMeta` 全部取值

源码：`api/research/ResearchEntryMeta.java`（enum + Codec）

| JSON 值 | 含义 |
|---|---|
| `"round"` | 圆形边框 |
| `"spiky"` | 尖刺边框（eldritch 用） |
| `"reverse"` | 连线反向绘制 |
| `"hidden"` | 未解锁前在网格中隐藏 |
| `"autounlock"` | 加载时**自动加入**玩家记录 |
| `"hex"` | 六边形边框 |

### 4.7 `ResearchConstruct`（多方块结构图）

    public record ResearchConstruct(int xSize, int ySize, int zSize, List<String> cells, AspectList cost)
    // JSON: x_size / y_size / z_size / cells / cost
    // validate: cells.size() == xSize*ySize*zSize，否则 codec 报错

- `cells` **顶层优先（top layer first）**，层内 x-major。
- 每格：物品 id `"minecraft:glass"` / 标签 `"#minecraft:wooden_slabs"` / 空串 `""` 表示留空。
- `cost` 是激活结构的 vis，可省。

### 4.8 `research_category` 怎么注册

同样**不是代码注册，是数据包**：`data/<ns>/thaumaturge/research_category/<id>.json`。

`content/research/ResearchCategory.java`：

    public record ResearchCategory(
            Optional<ResourceLocation> requiredResearch, AspectList formula,
            ResourceLocation icon, ResourceLocation background,
            Optional<ResourceLocation> overlayBackground, int index) implements IResearchCategory {

        public static final Codec<ResearchCategory> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("required_research"),
                AspectList.CODEC.fieldOf("formula"),
                ResourceLocation.CODEC.fieldOf("icon"),
                ResourceLocation.CODEC.fieldOf("background"),
                ResourceLocation.CODEC.optionalFieldOf("overlay_background"),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0))
            .apply(instance, ResearchCategory::new));
    }

| 字段 | 必填 | 默认 | 含义 |
|---|---|---|---|
| `required_research` | 否 | 无 | 解锁该分类标签页需要完成的研究条目 |
| `formula` | **是** | — | 要素加权公式 |
| `icon` | **是** | — | 分类标签图标纹理 |
| `background` | **是** | — | 条目网格背景纹理 |
| `overlay_background` | 否 | 无 | 叠加的第二层背景 |
| `index` | 否 | `0` | 排序，越小越靠前，**非负** |

公式数学（`IResearchCategory.applyFormula`）：

    total += modifier * modifier * aspects.amountOf(entry.aspect()) * (entry.amount() / 10.0);
    return Mth.ceil(total > 0 ? Math.sqrt(total) : 0);

**内置分类常量**（`api/research/TCResearchCategories.java`）—— **只有 ResourceKey，没有注册方法**：

    BASICS / AUROMANCY / ALCHEMY / ARTIFICE / INFUSION / GOLEMANCY / ELDRITCH
    // 全部为 ResourceKey<IResearchCategory>，key(path) = thaumaturge:<path>

> 这些 key 只能用于**代码里读取**。**附属要加自己的分类，只能写 JSON。**

分类名的翻译键约定（`CategoryComponents.translationKey`）：

    "research_category." + key.location().getNamespace() + "." + key.location().getPath()

即 `thaumicenergistics_ce:thaumicenergistics_ce` → `research_category.thaumicenergistics_ce.thaumicenergistics_ce`。
另有 `CategoryComponents.name(key)` / `.emphasised(key)`。

### 4.9 扫描（Scan）：数据驱动 + 代码驱动两条路

#### 4.9.1 数据驱动：`ScanEntry`

`api/research/scan/ScanEntry.java`，目录 `data/<ns>/thaumaturge/scan/<id>.json`：

    public record ScanEntry(ResourceLocation key, Optional<HolderSet<Block>> blocks,
                            Optional<HolderSet<Item>> items, Optional<HolderSet<EntityType<?>>> entities)

    public static final ResourceKey<Registry<ScanEntry>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "scan"));

- `key`：扫描成功后授予的研究 key。
- `blocks` / `items` / `entities`：可选 HolderSet，**接受单个字符串、数组、或 `#tag`**。
- **方块的 item 形态自动匹配**（列了 block 就同时覆盖手持/掉落物，不用再单列 item）。
- **物品实体按内部 stack 匹配。**
- **不需要任何注册调用**：`ScanningManager` 每次扫描遍历全部已加载条目。

**jar 实测原文**：

    // scan/oreamber.json
    { "blocks": "thaumaturge:ore_amber", "key": "thaumaturge:scanned/oreamber" }

    // scan/plants.json
    { "blocks": "#thaumaturge:magical_plants", "key": "thaumaturge:plants" }

    // scan/cultist.json
    { "entities": ["thaumaturge:cultist_knight", "thaumaturge:cultist_cleric"], "key": "..." }

#### 4.9.2 代码驱动：`IScanThing` + `ScanningManager`

`api/research/scan/IScanThing.java`：

    public interface IScanThing {
        boolean checkThing(Player player, @Nullable Object target);
        @Nullable ResourceLocation getResearchKey(Player player, @Nullable Object target);
        default void onSuccess(Player player, @Nullable Object target) {}
        default @Nullable Component scanFailure(Player player, @Nullable Object target) { return null; }
    }

`target` 只可能是四种：`Entity` / `ItemStack` / `BlockPos`（在玩家 level 内）/ `null`（扫天空）。

`api/research/scan/ScanningManager.java` 公开静态方法：

    public static void addScannableThing(IScanThing thing);
    public static void scanTheThing(Player player, @Nullable Object target);
    public static boolean isThingStillScannable(Player player, @Nullable Object target);
    public static ItemStack getItemFromParms(Player player, @Nullable Object target);
    public static AspectList itemAspects(ItemStack stack);
    public static AspectList entityAspects(Entity entity);
    public static boolean progressResearch(Player player, ResourceLocation research);
    public static boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount);
    // bind(Bindings) 是 Thaumaturge 自己 init 时调的，addon 绝不能调

**注册时机**（照抄 `content/research/scan/ScanBootstrap.java`）：

    @EventBusSubscriber(modid = "你的modid")
    public final class YourScanBootstrap {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                ScanningManager.addScannableThing(
                        new ScanItem(TCIds.rl("scanned/item/myitem"), MY_ITEM.get()));
            });
        }
    }

#### 4.9.3 现成的 `IScanThing` 实现类（都能直接 `new`）

全部位于 `com.leclowndu93150.thaumaturge.api.research.scan`：

| 类 | 构造签名 |
|---|---|
| `ScanItem` | `ScanItem(ResourceLocation research, ItemLike item)` |
| `ScanItemTag` | `ScanItemTag(ResourceLocation research, TagKey<Item> tag)` |
| `ScanBlock` | `ScanBlock(ResourceLocation research, Block... blocks)`（自动为每个 block 的 item 形态挂一个 `ScanItem`） |
| `ScanBlockTag` | `ScanBlockTag(ResourceLocation research, TagKey<Block> tag)` |
| `ScanBlockState` | `ScanBlockState(ResourceLocation research, BlockState blockState, boolean item)`（精确 state，`==` 比较） |
| `ScanEntity` | `ScanEntity(ResourceLocation, EntityType<?>)` / `(ResourceLocation, Class<?>, boolean inheritedClasses)` / `(ResourceLocation, Class<?>, boolean, Predicate<Entity>)` |
| `ScanAspect` | `ScanAspect(ResourceLocation research, Holder<IAspect> aspect)`（命中给 basics/auromancy/alchemy 各 +1 observation） |

#### 4.9.4 `ScanKeys` —— 规范命名（**namespace 永远是 `thaumaturge`**）

`api/research/scan/ScanKeys.java`：

    public static ResourceLocation item(Item item);                       // thaumaturge:scanned/item/<ns>/<path>
    public static ResourceLocation entity(EntityType<?> type);            // thaumaturge:scanned/entity/<ns>/<path>
    public static ResourceLocation aspect(ResourceKey<IAspect> aspect);   // thaumaturge:scanned/aspect/<ns>/<path>
    public static ResourceLocation enchantment(ResourceLocation ench);    // thaumaturge:scanned/enchantment/<ns>/<path>
    public static ResourceLocation effect(ResourceLocation effect);       // thaumaturge:scanned/effect/<ns>/<path>
    public static ResourceLocation celestial(int worldDay, String body);  // thaumaturge:scanned/celestial/<day>/<body>
    public static String celestialDayPrefix(int worldDay);
    public static boolean isCelestial(ResourceLocation research);

> **附属注意**：key 的 namespace 永远是 `thaumaturge`，不是你的 modid。
> 想在 Thaumonomicon 里对应上，就要在 `data/thaumaturge/thaumaturge/research_entry/`（**thaumaturge 命名空间！**）
> 放同名条目；否则只能当 flag 用。

#### 4.9.5 scan 如何「挂」到研究条目上 —— key 同名即绑定

`ScanningManager.progressResearch(player, key)` → `content/research/scan/ScanBindings.java:22-49`（逐字）：

    boolean hasEntry = serverPlayer.registryAccess()
            .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
            .get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research))
            .isPresent();
    PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(serverPlayer);
    if (hasEntry) {
        if (!knowledge.isResearchKnown(research)) {
            if (!ResearchManager.unlock(serverPlayer, research)) return false;   // 被 parentsSatisfied 拦住
            ResearchManager.advanceStage(serverPlayer, research);
            return true;
        }
        return ResearchManager.advanceStage(serverPlayer, research);
    }
    // 没有对应条目 -> 只当一个 bare flag 记下来并标记完成
    if (knowledge.isResearchKnown(research)) return false;
    knowledge.addResearch(research);
    knowledge.markComplete(research);
    knowledge.sync(serverPlayer);
    return true;

结论：

1. `key` 若等于某个 research_entry 的 id → 解锁/推进它，但**父条目必须已满足**，否则扫描**毫无反应**。
2. 若没有同名条目 → 只写玩家记录并 markComplete，可被
   `KnowledgeAccess.of(player).isResearchKnown(key)` 查询（Thaumic Energistics 就靠这个判断「扫描过没有」）。
3. 任一匹配项返回非 null 的 `scanFailure` → **整次扫描失败**。

### 4.10 玩家研究进度读写 API

**`api.capability.IPlayerKnowledge`**（完整方法签名，逐条抄自源码）：

    void clear();
    ResearchStatus researchStatus(ResourceLocation research);        // UNKNOWN / COMPLETE / IN_PROGRESS
    boolean isResearchComplete(ResourceLocation research);
    boolean isResearchKnown(ResourceLocation research);
    boolean isResearchKnown(ResourceLocation research, int minimumStage);
    int researchStage(ResourceLocation research);                     // 未知返回 -1
    boolean addResearch(ResourceLocation research);
    boolean setResearchStage(ResourceLocation research, int stage);   // stage 必须 > 0
    boolean removeResearch(ResourceLocation research);
    Set<ResourceLocation> researchList();
    boolean setResearchFlag(ResourceLocation research, ResearchFlag flag);
    boolean clearResearchFlag(ResourceLocation research, ResearchFlag flag);
    boolean hasResearchFlag(ResourceLocation research, ResearchFlag flag);
    boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount);
    int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);   // = raw / progression
    int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);
    void sync(ServerPlayer player);                                   // 改完必须调，否则客户端看不到

> **待确认**：`markComplete(ResourceLocation)` 的精确签名 —— 只在 `ScanBindings.java:46` 见到调用，
> 已读到的接口片段（1-172 行）里没有它的声明（很可能是 `boolean markComplete(ResourceLocation)`）。

**`api.capability.KnowledgeAccess`**：

    public static IPlayerKnowledge of(Player player);   // 唯一给 addon 的入口
    // bind(Function<Player, IPlayerKnowledge>) 是 Thaumaturge init 调用的，addon 禁止调用

`Thaumaturge.java:91`：`KnowledgeAccess.bind(player -> player.getData(TCAttachments.KNOWLEDGE));`

| 枚举 | 取值 |
|---|---|
| `KnowledgeType` | `THEORY("theory","T",32)`、`OBSERVATION("observation","O",16)`；`getSerializedName()` / `abbreviation()` / `progression()` |
| `ResearchFlag` | `PAGE("page")`、`RESEARCH("research")`、`POPUP("popup")` |
| `ResearchStatus` | `UNKNOWN`、`COMPLETE`、`IN_PROGRESS` |

**`api.research.pool.AspectPoolAccess`**（要素池 —— 研究笔记/合成用）：

    public static boolean isDiscovered(Player player, Holder<IAspect> aspect);
    public static int amount(Player player, Holder<IAspect> aspect);
    public static boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect);
    public static int grant(ServerPlayer player, Holder<IAspect> aspect, int amount);
    public static void grantAll(ServerPlayer player, AspectList aspects);
    public static boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount);
    public static boolean canAfford(Player player, AspectList cost);
    public static boolean spendAll(ServerPlayer player, AspectList cost);
    public static void refund(ServerPlayer player, Holder<IAspect> aspect, int amount);

**`content.research.ResearchManager`**（不是 api 包，但 `public static` 可用，推进研究最常用）：

    public static ResourceLocation craftedKey(ResourceLocation item);   // thaumaturge:crafted/<ns>/<path>
    public static boolean unlock(ServerPlayer player, ResourceLocation research);
    public static boolean advanceStage(ServerPlayer player, ResourceLocation research);
    public static boolean advanceStage(ServerPlayer player, ResourceLocation research, boolean checkRequisites);
    public static boolean setStage(ServerPlayer player, ResourceLocation research, int stage);
    public static boolean complete(ServerPlayer player, ResourceLocation research);
    public static boolean gainKnowledge(ServerPlayer player, KnowledgeType type, Holder<IResearchCategory> category, int amount);
    public static void applyAutoUnlock(ServerPlayer player);
    public static boolean doesPassGate(Player player, @Nullable ResearchGate gate);
    public static IPlayerKnowledge of(ServerPlayer player);
    public static boolean parentsSatisfied(IPlayerKnowledge knowledge, IResearchEntry entry);
    public static boolean stageRequirementsMet(/* 参数表待确认 */);
    public static boolean isCraftSatisfied(Player player, IPlayerKnowledge knowledge, ResearchRequirement req);
    public static int countMatching(Player player, ResearchRequirement req);
    public static void applyWarp(ServerPlayer player, int amount);

**重要行为**：`unlock()` 会先 `NeoForge.EVENT_BUS.post(new ResearchEvent.Unlocked(...))` 并检查 `isCanceled()`，
且 `if (entry != null && !parentsSatisfied(knowledge, entry)) return false;`
—— 父条目没满足就解锁不了。

**`api.research.ResearchEvent`**（基类 `abstract class ResearchEvent extends Event implements ICancellableEvent`，
`player()` 可读；取消即阻止写记录）：

| 子类 | 关键 getter |
|---|---|
| `ResearchEvent.Unlocked(Player, ResourceLocation)` | `research()` |
| `ResearchEvent.StageAdvanced(Player, ResourceLocation, int previousStage, int newStage)` | — |
| `ResearchEvent.Completed(Player, ResourceLocation)` | — |
| `ResearchEvent.KnowledgeGained(Player, KnowledgeType, Holder<IResearchCategory>, int amount)` | — |

**Bindings 内部接口禁令**：`KnowledgeAccess` / `AspectPoolAccess` / `ScanningManager` /
`AspectKnowledgeAccess` / `ArcaneCraftCost` / `AuraHelper` / `WandAccess` 的 `bind(...)`
**只能 Thaumaturge 调**（`Thaumaturge.java:91-114` 是全部绑定点），
addon 一个都不能碰，重复调用抛 `IllegalStateException`。

### 4.11 完整可粘贴的研究 JSON

#### 分类：`data/<ns>/thaumaturge/research_category/<ns>.json`

    {
      "background": "yourmod:textures/research/research_background.png",
      "overlay_background": "thaumaturge:textures/gui/gui_research_back_over.png",
      "icon": "yourmod:textures/research/tab_icon.png",
      "index": 8,
      "required_research": "thaumaturge:unlock_infusion",
      "formula": [
        { "aspect": "thaumaturge:praecantatio", "amount": 20 },
        { "aspect": "thaumaturge:machina", "amount": 10 },
        { "aspect": "thaumaturge:fabrico", "amount": 10 }
      ]
    }

#### 条目：`data/<ns>/thaumaturge/research_entry/my_first_relic.json`

    {
      "category": "yourmod:yourmod",
      "name": "research.yourmod.my_first_relic.title",
      "parents": [
        "~thaumaturge:gotthaumonomicon",
        "thaumaturge:infusion@2"
      ],
      "siblings": [],
      "column": 0,
      "row": 0,
      "meta": ["round", "spiky"],
      "icons": [
        "yourmod:relic_core",
        "yourmod:textures/research/relic_core_alt.png"
      ],
      "stages": [
        {
          "text": "research.yourmod.my_first_relic.stage_0",
          "obtain": [
            { "items": ["minecraft:diamond"], "amount": 1 }
          ],
          "required_knowledge": [
            { "type": "observation", "category": "yourmod:yourmod", "amount": 1 }
          ]
        },
        {
          "text": "research.yourmod.my_first_relic.stage_1",
          "recipes": ["yourmod:arcane_workbench/relic_core"],
          "craft": [
            { "items": ["yourmod:relic_core"], "amount": 1 }
          ],
          "required_research": ["yourmod:my_first_relic"],
          "warp": 1
        },
        {
          "text": "research.yourmod.my_first_relic.stage_2",
          "construct": {
            "x_size": 1,
            "y_size": 2,
            "z_size": 1,
            "cells": ["yourmod:relic_core", "minecraft:stone"],
            "cost": [
              { "aspect": "thaumaturge:praecantatio", "amount": 50 }
            ]
          },
          "knowledge": [
            { "type": "theory", "category": "yourmod:yourmod", "amount": 16 }
          ]
        }
      ],
      "addenda": [
        {
          "text": "research.yourmod.my_first_relic.addendum_0",
          "recipes": ["yourmod:infusion/relic_core_awakened"],
          "required_research": ["yourmod:my_first_relic"]
        }
      ],
      "note_aspects": [
        { "aspect": "thaumaturge:praecantatio", "amount": 4 },
        { "aspect": "thaumaturge:fabrico", "amount": 3 },
        { "aspect": "thaumaturge:machina", "amount": 2 }
      ],
      "complexity": 3
    }

#### 扫描：`data/thaumaturge/thaumaturge/scan/yourmod_relic.json`

    {
      "key": "yourmod:my_first_relic",
      "blocks": ["yourmod:relic_core"]
    }

> 注意路径第一段是 `data/thaumaturge/`（**registry 的 namespace**），只有第二段之后才是你的 ns。

### 4.12 真实附属范例：Thaumic Energistics CE

**条目**（`data/thaumicenergistics_ce/thaumaturge/research_entry/arcane_assembler.json`，原文）：

    {
      "category": "thaumicenergistics_ce:thaumicenergistics_ce",
      "name": "tc.research_name.ARCANEASSEMBLER",
      "parents": ["thaumicenergistics_ce:essentia_terminal", "thaumicenergistics_ce:digisentia"],
      "column": 2, "row": 0,
      "meta": ["round", "hidden"],
      "icons": ["thaumicenergistics_ce:arcane_assembler"],
      "stages": [
        { "text": "tc.research_text.ARCANEASSEMBLER.stage.1",
          "required_knowledge": [{ "type": "observation", "category": "thaumicenergistics_ce:thaumicenergistics_ce", "amount": 2 }],
          "craft": [{ "items": ["thaumicenergistics_ce:essentia_terminal"], "amount": 1 }],
          "required_research": ["thaumicenergistics_ce:essentia_terminal", "thaumicenergistics_ce:digisentia"] },
        { "text": "tc.research_text.ARCANEASSEMBLER.stage.2",
          "recipes": ["thaumicenergistics_ce:arcane_assembler"] }
      ],
      "complexity": 3
    }

**分类**（`research_category/thaumicenergistics_ce.json`）用 `"index": 7` +
`"required_research": "thaumaturge:unlock_infusion"` 把自己挂在原版解锁之后
—— **这就是附属加分类的标准姿势**。

**运行时读取注册表**（`research/ResearchSelfTest.java:44-72`）：

    HolderLookup.RegistryLookup<IResearchCategory> categories;
    HolderLookup.RegistryLookup<IResearchEntry> entries;
    try {
        categories = registries.lookupOrThrow(IResearchCategory.REGISTRY_KEY);
        entries    = registries.lookupOrThrow(IResearchEntry.REGISTRY_KEY);
    } catch (IllegalStateException e) { /* Thaumaturge 不存在 */ }

    IResearchCategory category = categories.get(CATEGORY_KEY).map(Holder.Reference::value).orElse(null);
    for (Holder.Reference<IResearchEntry> holder : entries.listElements().toList()) { /* ... */ }

**用扫描 key 当「物品是否扫描过」的开关**（`menu/MenuDistillationEncoder.java:232,257`）：

    import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
    import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
    import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;

    if (itemScanned && AspectPoolAccess.isDiscovered(owner, aspects.get(i))) { ... }

    private boolean sourceIsScanned(ItemStack source) {
        return !source.isEmpty()
                && KnowledgeAccess.of(owner).isResearchKnown(ScanKeys.item(source.getItem()));
    }

**手工构造 `ResearchGate` 并检查**（`arcane/ThEArcanePattern.java:196-201,315`；
`blockentity/BlockEntityKnowledgeInscriber.java:374`）：

    import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;

    public Optional<ResearchGate> gate() {
        if (research == null) return Optional.empty();
        return Optional.of(new ResearchGate(research, Optional.ofNullable(researchStage), false));
    }
    // 读取现成配方上的门槛：
    ResearchGate gate = recipe.researchGate().orElse(null);
    // 判定：
    return pattern.gate().map(g -> ResearchGate.passes(player, g)).orElse(true);

---

## 5. 奥术合成与灌注注魔

### 5.1 注册表 id 的两个层次（**最容易踩的坑**）

**`registry/TCRecipeTypes.java`**（RecipeType，即 `getAllRecipesFor` 用的 key）：

    DUST_TRIGGER         = RECIPE_TYPES.register("dust_trigger", ...);
    INFUSION             = RECIPE_TYPES.register("infusion", ...);
    INFUSION_ENCHANTMENT = RECIPE_TYPES.register("infusion_enchantment", ...);
    RUNIC_AUGMENT        = RECIPE_TYPES.register("runic_augment", ...);
    CRUCIBLE             = RECIPE_TYPES.register("crucible", ...);
    ARCANE               = RECIPE_TYPES.register("arcane_workbench", ...);   // 注意：不是 "arcane"

**`registry/TCRecipeSerializers.java`**（serializer，即 JSON 里的 `"type"`）：

| 常量 | 注册 id（= JSON `"type"`） | 对应类 |
|---|---|---|
| `DUST_TRIGGER_SIMPLE` | `thaumaturge:dust_trigger_simple` | `DustTriggerSimpleRecipe` |
| `DUST_TRIGGER_TAG` | `thaumaturge:dust_trigger_tag` | `DustTriggerTagRecipe` |
| `DUST_TRIGGER_MULTIBLOCK` | `thaumaturge:dust_trigger_multiblock` | `DustTriggerMultiblockRecipe` |
| `INFUSION` | `thaumaturge:infusion` | `InfusionRecipe` |
| `INFUSION_ENCHANTMENT` | `thaumaturge:infusion_enchantment` | `InfusionEnchantmentRecipe` |
| `RUNIC_AUGMENT` | `thaumaturge:runic_augment` | `InfusionRunicAugmentRecipe` |
| `CRUCIBLE` | `thaumaturge:crucible` | `CrucibleRecipe` |
| `ARCANE_SHAPED` | `thaumaturge:arcane_workbench_shaped` | `ArcaneShapedCraftingRecipe` |
| `ARCANE_SHAPELESS` | `thaumaturge:arcane_workbench_shapeless` | `ArcaneShapelessCraftingRecipe` |
| `SALIS_MUNDUS` | `thaumaturge:salis_mundus` | `SalisMundusRecipe` |

> **RecipeType id ≠ serializer id。** 遍历配方时用前者，写 JSON 时用后者。

**`content/recipe/SimpleRecipeSerializer.java`**（Thaumaturge 自己的薄包装）：

    public final class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
        public SimpleRecipeSerializer(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec);
        public MapCodec<T> codec();
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
    }

两个 register 都在 `Thaumaturge.java:65-66` 调用。

### 5.2 `ResearchGate` —— 所有配方共用的研究门槛

`api/recipe/ResearchGate.java`：

    public record ResearchGate(ResourceLocation entry, Optional<Integer> stage, boolean negate)
    // JSON: { "entry": "...", "stage": <int 可选>, "negate": <bool 默认 false> }

判定（`ResearchManager.doesPassGate`）：

    boolean known = gate.stage().isPresent()
            ? knowledge.isResearchKnown(gate.entry(), gate.stage().get())
            : knowledge.isResearchComplete(gate.entry());
    return gate.negate() != known;

→ `negate: true` = 「**没**研究过才允许」。

`api.recipe.ResearchStatus`（与 `api.capability.ResearchStatus` **不是同一个类**）：
`ResearchStatus.evaluate(player, Optional<ResearchGate>)` 返回
`NOT_REQUIRED / UNLOCKED / LOCKED / INVALID`，`permitsCrafting()` 只有前两者为 true。

### 5.3 奥术工作台 shaped（`thaumaturge:arcane_workbench_shaped`）

**路径**：`data/<ns>/recipe/<任意子目录>/<id>.json`（Thaumaturge 自己用 `recipe/arcane_workbench/`）。

**接口** `api/recipe/IArcaneRecipe.java`：

    public interface IArcaneRecipe extends ResearchGated, Recipe<IArcaneCraftingInput> {
        int getBaseVis();
        AspectList getCrystals();
    }

**Codec**（`content/recipe/workbench/ArcaneShapedCraftingRecipe.java:26-35`）：

    RecordCodecBuilder.mapCodec((i) -> i.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(o -> o.group),
        ExtraCodecs.POSITIVE_INT.fieldOf("vis").forGetter(o -> o.vis),
        ResearchGate.CODEC.optionalFieldOf("research").forGetter(o -> o.gate),
        ArcaneCraftingRecipe.PRIMAL_ASPECTS_CODEC.fieldOf("crystals").forGetter(o -> o.aspects),
        ArcaneShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),   // key + pattern
        ItemStack.CODEC.fieldOf("result").forGetter(o -> o.result))
      .apply(i, ArcaneShapedCraftingRecipe::new))

**`PRIMAL_ASPECTS_CODEC` 的硬约束**（`ArcaneCraftingRecipe.java:21-40`）：

- 必须是 **primal 要素**（`thaumaturge:aer/ignis/terra/aqua/ordo/perditio`），非 primal **直接 codec 报错**；
- 单个要素 amount **<= 64**；
- 列表 0..6 项。

| 字段 | 必填 | 说明 |
|---|---|---|
| `type` | **是** | `"thaumaturge:arcane_workbench_shaped"` |
| `group` | 否 | 默认 `""` |
| `vis` | **是** | 正整数，基础 vis 消耗 |
| `research` | 否 | `ResearchGate` 对象 |
| `crystals` | **是** | **字段必填**（可以是空数组 `[]`），primal 要素数组 |
| `key` | **是** | `{ "G": {"item": "..."} / {"tag": "..."} / {"type":"neoforge:components", ...} }` |
| `pattern` | **是** | 字符串数组，最多 3 行、每行最多 3 字符、空格为空洞 |
| `result` | **是** | ItemStack 格式 `{"id": ..., "count": n, "components": {...}}` |

**可直接粘贴（jar 内 `recipe/arcane_workbench/thaumometer.json` 原文）**：

    {
      "type": "thaumaturge:arcane_workbench_shaped",
      "crystals": [
        { "aspect": "thaumaturge:aer" },
        { "aspect": "thaumaturge:ignis" },
        { "aspect": "thaumaturge:terra" },
        { "aspect": "thaumaturge:aqua" },
        { "aspect": "thaumaturge:ordo" },
        { "aspect": "thaumaturge:perditio" }
      ],
      "key": {
        "G": { "tag": "c:ingots/gold" },
        "P": { "tag": "c:glass_panes" }
      },
      "pattern": [ " G ", "GPG", " G " ],
      "research": { "entry": "thaumaturge:first_steps", "stage": 1 },
      "result": { "count": 1, "id": "thaumaturge:thaumometer" },
      "vis": 20
    }

> `crystals` 里的 `amount` 可省（`AspectInstance.CODEC` 用 `optionalFieldOf("amount", 1)`），
> **1 单位 = 1 颗要素水晶**。

**带组件过滤的 key**（TE `recipe/vis_interface.json` 真实用例，指定某要素的水晶）：

    "C": {
      "type": "neoforge:components",
      "items": "thaumaturge:essentia_crystal",
      "components": { "thaumaturge:crystal_aspect": { "aspect": "thaumaturge:auram" } }
    }

**护目镜的真实配方（jar 内 `recipe/arcane_workbench/goggles_revealing.json`）**：

    {
      "type": "thaumaturge:arcane_workbench_shaped",
      "crystals": [],
      "key": {
        "B": { "tag": "c:ingots/brass" },
        "L": { "tag": "c:leathers" },
        "M": { "item": "thaumaturge:thaumometer" }
      },
      "pattern": [ "LBL", "L L", "MBM" ],
      "research": { "entry": "thaumaturge:unlock_artifice" },
      "result": { "count": 1, "id": "thaumaturge:goggles_revealing" },
      "vis": 50
    }

### 5.4 奥术工作台 shapeless（`thaumaturge:arcane_workbench_shapeless`）

**Codec**（`ArcaneShapelessCraftingRecipe.java:28-40`）：

    i.group(
      Codec.STRING.optionalFieldOf("group", ""),
      ExtraCodecs.POSITIVE_INT.fieldOf("vis"),
      ResearchGate.CODEC.optionalFieldOf("research"),
      ArcaneCraftingRecipe.PRIMAL_ASPECTS_CODEC.fieldOf("crystals"),
      ItemStack.CODEC.fieldOf("result"),
      Ingredient.CODEC.listOf(1, maxHeight * maxWidth).fieldOf("ingredients"))

字段同 shaped，但把 `key` + `pattern` 换成 **`ingredients`**（Ingredient 数组，**1..9 项，必填不能空**）。

### 5.5 灌注注魔（`thaumaturge:infusion`）

**路径**：`data/<ns>/recipe/infusion/<id>.json`。

**接口** `api/recipe/IInfusionRecipe.java`：

    public interface IInfusionRecipe extends ResearchGated {
        Ingredient catalyst();
        List<Ingredient> components();
        AspectList aspects();
        int instability();
        ItemStack resultItem();
        default List<ItemStack> matchComponents(List<ItemStack> available);  // 顺序无关，需完全匹配
    }

**Codec**（`content/infusion/InfusionRecipe.java:28-43`）：

    RecordCodecBuilder.<InfusionRecipe>mapCodec(i -> i.group(
          Ingredient.CODEC.fieldOf("catalyst"),
          Ingredient.CODEC.listOf(1, 64).fieldOf("components"),
          AspectList.NON_EMPTY_CODEC.fieldOf("aspects"),
          Codec.intRange(0, 100).optionalFieldOf("instability", 0),
          ItemStack.CODEC.optionalFieldOf("result"),
          DataComponentPatch.CODEC.optionalFieldOf("catalyst_patch"),
          ResearchGate.CODEC.optionalFieldOf("research"))
      .apply(i, InfusionRecipe::new))
      .validate(recipe -> recipe.result.isPresent() == recipe.catalystPatch.isPresent()
            ? DataResult.error(() -> "Infusion recipe needs exactly one of result or catalyst_patch")
            : DataResult.success(recipe));

| JSON 字段 | 必填 | 约束 | 含义 |
|---|---|---|---|
| `type` | **是** | `"thaumaturge:infusion"` | |
| `catalyst` | **是** | Ingredient | 中央基座上的催化剂 |
| `components` | **是** | **1..64 项**，不能空 | 外围基座材料，顺序无关、一次性消耗 |
| `aspects` | **是** | **非空** | 从附近要素容器抽取的要素总量 |
| `instability` | 否 | **0..100**，默认 0 | 风险；越大越不稳 |
| `result` | 视情况 | 与 `catalyst_patch` **恰好二选一** | 直接产出 |
| `catalyst_patch` | 视情况 | 与 `result` 恰好二选一 | 对催化剂施加组件补丁（如注入附魔） |
| `research` | 否 | ResearchGate | 研究门槛 |

> **实测反例**：`result` 和 `catalyst_patch` 同时给或同时不给，codec 直接报
> `"Infusion recipe needs exactly one of result or catalyst_patch"`。

**完整可粘贴（jar 内 `recipe/infusion/elemental_pickaxe.json` 原文）**：

    {
      "type": "thaumaturge:infusion",
      "aspects": [
        { "amount": 30, "aspect": "thaumaturge:ignis" },
        { "amount": 30, "aspect": "thaumaturge:metallum" },
        { "amount": 30, "aspect": "thaumaturge:sensus" }
      ],
      "catalyst": { "item": "thaumaturge:thaumium_pickaxe" },
      "components": [
        { "item": "thaumaturge:crystal_ignis" },
        { "item": "thaumaturge:crystal_ignis" },
        { "tag": "c:nuggets/quartz" },
        { "tag": "c:planks/greatwood" }
      ],
      "instability": 1,
      "research": { "entry": "thaumaturge:elemental_tools" },
      "result": {
        "components": {
          "thaumaturge:infusion_enchantments": { "refining": 1, "sounding": 2 }
        },
        "count": 1,
        "id": "thaumaturge:elemental_pickaxe"
      }
    }

> `result.components.thaumaturge:infusion_enchantments` 是 Thaumaturge 的注魔附魔数据组件，
> JSON 形态为 `Map<InfusionEnchantment, Integer>`。

**「给已有装备加能力」的官方范例**（jar 内 `recipe/infusion/fortress_helm_goggles.json`）：
用 `catalyst_patch` 给要塞头盔打上 `thaumaturge:goggles_upgrade` 组件，使其获得护目镜功能。
相应地 `VoidRobeArmorItem` / `FortressArmorItem` 直接 `implements IGoggles, IRevealer`。

### 5.6 运行的三个入口 API

#### 5.6.1 `api/recipe/ArcaneCraftingTransaction.java` —— 奥术合成服务端总入口

    public static Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input);
    public static Inspection inspect(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input);
    public static Result commit(ArcaneWorkbenchContext context, ServerPlayer player,
                                IArcaneCraftingInput input, IArcaneCraftingStore store);

    public record Result(boolean successful, boolean committed, Failure failure,
                         ItemStack output, List<ItemStack> remainders, @Nullable ArcaneCraftCost cost) {
        public AspectList crystals();
        public @Nullable ArcanePaymentSummary paymentSummary();
    }
    public enum Failure { NONE, NOT_SERVER_THREAD, INVALID_CONTEXT, NO_RECIPE,
                          RESEARCH_LOCKED, INGREDIENTS_CHANGED, PAYMENT_UNAVAILABLE }

配套接口：

    // api/recipe/IArcaneWorkbench.java
    public interface IArcaneWorkbench {
        AspectList availableCrystals();
        ItemStack wandStack();
    }

    // api/recipe/IArcaneCraftingStore.java
    public interface IArcaneCraftingStore {
        @Nullable Reservation reserve(List<ItemStack> gridSnapshot);
        interface Reservation extends AutoCloseable {
            boolean isValid();
            void commit(ItemStack output, List<ItemStack> remainders, AspectList crystals);
            void close();
        }
    }

    // api/recipe/ArcaneWorkbenchContext.java
    public record ArcaneWorkbenchContext(ServerLevel level, UUID hostIdentity, @Nullable BlockPos position,
                                         @Nullable UUID owner, UUID actingPlayer, Kind kind) {
        public static ArcaneWorkbenchContext placed(ServerPlayer, BlockPos, UUID hostIdentity, @Nullable UUID owner);
        public static ArcaneWorkbenchContext virtual(ServerPlayer, UUID hostIdentity, @Nullable UUID owner);
        public Optional<BlockPos> blockPosition();
        public Optional<UUID> ownerIdentity();
        public enum Kind { PLACED, VIRTUAL }
    }

    // api/recipe/ArcaneCraftCost.java
    public record ArcaneCraftCost(boolean paidFromWand, Map<ResourceKey<IAspect>, Integer> wandCentivis,
                                  AspectList crystalsNeeded, int auraVis, boolean affordable) {
        public static ArcaneCraftCost of(IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player);
    }

**费用三级来源**（`ArcaneCraftCost` 注释）：**法杖 vis → 要素水晶 → 方块 aura 缓冲**。

**加自己的 vis 来源**（`api/recipe/IWorkbenchVisSource.java` 与 `IWorkbenchAuraSource.java`）：

    public final class RegisterWorkbenchVisSourcesEvent extends Event implements IModBusEvent {
        public void register(IWorkbenchVisSource source);
        public List<IWorkbenchVisSource> sources();
    }
    public final class RegisterWorkbenchAuraSourcesEvent extends Event implements IModBusEvent {
        public void register(IWorkbenchAuraSource source);
        public List<IWorkbenchAuraSource> sources();
    }
    @FunctionalInterface public interface IWorkbenchVisSource {
        int supply(Player player, IArcaneWorkbench workbench, Holder<IAspect> aspect, int need, boolean simulate);
        default int supply(ArcaneWorkbenchContext ctx, Player p, IArcaneWorkbench wb,
                           Holder<IAspect> aspect, int need, boolean simulate) {
            return supply(p, wb, aspect, need, simulate);
        }
    }
    @FunctionalInterface public interface IWorkbenchAuraSource {
        int supply(ArcaneWorkbenchContext context, Player player, IArcaneWorkbench workbench,
                   int need, boolean simulate);
    }

正确用法（`Thaumaturge.java:96-102`）：new 事件 → `visSourcesEvent.register(...)` → `modBus.post(visSourcesEvent)`
→ `WorkbenchPayment.registerSources(visSourcesEvent.sources())`。

#### 5.6.2 `api/recipe/InfusionCraftingTransaction.java` —— 灌注注魔（给自动化机器用）

    public static Inspection inspect(InfusionMatrixContext context, ServerPlayer player,
                                     ItemStack catalyst, List<ItemStack> components);
    public static Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceLocation expectedRecipeId);

    // Failure: NONE, NOT_SERVER_THREAD, INVALID_CONTEXT, INVALID_INPUT, MATRIX_UNAVAILABLE,
    //          MATRIX_INACTIVE, MATRIX_BUSY, NO_RECIPE, RESEARCH_LOCKED, CATALYST_CHANGED,
    //          COMPONENTS_CHANGED, NATIVE_INFUSION_FAILED
    // Inspection: successful, failure, recipeId, requiredAspects, instability, researchStatus,
    //             output, catalystAfter, componentRemainders, exactOutput

`InfusionMatrixContext(ServerLevel level, BlockPos position, UUID actingPlayer)`。
另：`content/infusion/BlockEntityInfusionMatrix.STABILITY_CAP = 25.0F`。

#### 5.6.3 完整集成范例（TE `menu/slot/ArcaneCraftingResultSlot.java`，逐字）

    // 预览（不消耗任何东西）
    var result = ArcaneCraftingTransaction.preview(workbenchContext(), serverPlayer, input);
    lastFailure = result.failure();
    if (!result.successful()) { /* 记录原因 */ }
    setDisplayedCraftingOutput(result.successful() ? result.output() : ItemStack.EMPTY);
    ownerMenu.sendCraftCost(result.successful() ? result.cost() : null);

    // 提交（真正扣费）
    var store = new NetworkArcaneCraftingStore(storage, energySource, actionSource);
    var result = ArcaneCraftingTransaction.commit(workbenchContext(), server, input, store);
    if (!result.successful() || !result.committed()) { break; }
    settleRemainders(result.remainders(), who);
    consumeCrystals(result.cost());          // crystalsNeeded 是法杖付不掉、需要扣水晶的那部分
    ItemStack output = result.output().copy();

自定义 store（`arcane/NetworkArcaneCraftingStore.java`）实现 `IArcaneCraftingStore`：
`reserve` 只检查+预留、`commit` 才真正搬物品，并有 `isValid() == false` 的「空预留」单例。

### 5.7 坩埚（`thaumaturge:crucible`）

**Codec**（`content/recipe/crucible/CrucibleRecipe.java:22-27`）：

    i.group(Ingredient.CODEC.fieldOf("catalyst"),
            AspectList.NON_EMPTY_CODEC.fieldOf("aspects"),
            ItemStack.CODEC.fieldOf("result"),
            ResearchGate.CODEC.optionalFieldOf("research"))

**jar 内 `recipe/crucible/alumentum.json` 原文**：

    {
      "type": "thaumaturge:crucible",
      "aspects": [
        { "amount": 10, "aspect": "thaumaturge:ignis" },
        { "amount": 10, "aspect": "thaumaturge:potentia" },
        { "amount": 5,  "aspect": "thaumaturge:perditio" }
      ],
      "catalyst": { "tag": "minecraft:coals" },
      "research": { "entry": "thaumaturge:alumentum" },
      "result": { "count": 1, "id": "thaumaturge:alumentum" }
    }

### 5.8 粉尘触发（dustTrigger）—— 三个 serializer

**接口** `api/recipe/DustTrigger.java`：

    public interface DustTrigger extends Recipe<DustTriggerInput>, ResearchGated {
        default List<BlockPos> sparkle(Level, Player, BlockPos, DustTriggerPlacement) { return List.of(pos); }
        default @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) { return null; }
        default boolean isMultiblock() { return false; }
        void execute(DustTriggerInput input, Player player,
                     @Nullable DustTriggerPlacement placement, Direction useFace);
    }

| `type` | 字段 | 源文件 |
|---|---|---|
| `thaumaturge:dust_trigger_simple` | `target`（方块 id）、`result`（ItemStack）、`research`（可选） | `DustTriggerSimpleRecipe.java:28-32` |
| `thaumaturge:dust_trigger_tag` | `target_tag`（方块 tag）、`result`、`research` | `DustTriggerTagRecipe.java:29-33` |
| `thaumaturge:dust_trigger_multiblock` | `blueprint`（ResourceLocation，指向 Blueprint 注册表）、`result`、`research` | `DustTriggerMultiblockRecipe.java:37-41` |

**jar 内原文**：

    // recipe/dust_trigger/infusion_altar.json
    {
      "type": "thaumaturge:dust_trigger_multiblock",
      "blueprint": "thaumaturge:infusion_altar",
      "research": { "entry": "thaumaturge:infusion" },
      "result": { "count": 1, "id": "thaumaturge:infusion_matrix" }
    }

    // recipe/dust_trigger/cauldron_to_crucible.json
    {
      "type": "thaumaturge:dust_trigger_simple",
      "research": { "entry": "thaumaturge:unlock_alchemy", "stage": 0 },
      "result": { "count": 1, "id": "thaumaturge:crucible" },
      "target": "minecraft:cauldron"
    }

### 5.9 datagen 侧（Java data provider）—— 供参考

生成器：`data/recipe/TCRecipeProvider.java`（3558 行，`extends RecipeProvider`），
注册点 `data/TCDataGenerators.java:78` `event.createProvider(TCRecipeProvider::new);`
门槛助手（`TCRecipeProvider.java:92-98`）：

    private static ResearchGate gate(String path) { return new ResearchGate(TCIds.rl(path), Optional.empty(), false); }
    private static ResearchGate gate(String path, int stage) { return new ResearchGate(TCIds.rl(path), Optional.of(stage), false); }

**`data/recipe/builders/workbench/ArcaneWorkbenchShapedRecipeBuilder.java`**：

    public ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory category, ItemStack result,
            HolderGetter<Item> items, HolderGetter<IAspect> aspects, int vis);
    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, TagKey<Item> tag);
    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, ItemLike item);
    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, Ingredient ingredient);
    public ArcaneWorkbenchShapedRecipeBuilder pattern(String row);

基类 `ArcaneWorkbenchRecipeBuilder` 提供 `.gate(ResearchGate)`、
`.aspect(ResourceKey<IAspect>, int)`（加到 crystals）、`.unlockedBy(...)`、`.save(output[, id])`，
默认 id 带前缀 `arcane_workbench/`。

**`data/recipe/builders/InfusionRecipeBuilder.java`**：

    public InfusionRecipeBuilder(HolderGetter<IAspect> aspectsGetter, RecipeCategory category,
                                 ItemStack result, Ingredient catalyst);
    public InfusionRecipeBuilder component(Ingredient ingredient);
    public InfusionRecipeBuilder aspect(ResourceKey<IAspect> aspect, int amount);   // amount 必须 > 0
    public InfusionRecipeBuilder instability(int instability);
    public InfusionRecipeBuilder catalystPatch(DataComponentPatch catalystPatch);
    public InfusionRecipeBuilder gate(ResearchGate gate);

**`data/recipe/builders/CrucibleRecipeBuilder.java`**：

    public CrucibleRecipeBuilder(HolderGetter<IAspect> aspectsGetter, RecipeCategory category,
                                 ItemStack result, Ingredient catalyst);
    public CrucibleRecipeBuilder aspect(ResourceKey<IAspect> aspect);
    public CrucibleRecipeBuilder aspect(ResourceKey<IAspect> aspect, int amount);
    public CrucibleRecipeBuilder aspect(Holder<IAspect> aspect);
    public CrucibleRecipeBuilder aspect(Holder<IAspect> aspect, int amount);
    public CrucibleRecipeBuilder aspect(AspectInstance instance);
    public CrucibleRecipeBuilder gate(ResearchGate gate);

> **给附属的建议**：这些 builder 在 `com.leclowndu93150.thaumaturge.data.recipe.builders.*`，
> **不是 api 包**，跨 mod 依赖有风险。**手写 JSON 更稳**（Thaumic Energistics CE 就是手写）。

### 5.10 遍历已有配方（TE `blockentity/BlockEntityInfusionMonitor.java:664-672`）

    for (var holder : level.getRecipeManager().getAllRecipesFor(TCRecipeTypes.INFUSION.get())) {
        InfusionRecipe recipe = holder.value();
        if (recipe.catalyst().test(catalyst)) { /* ... */ }
    }

> 注意这里用的是 **RecipeType**（`TCRecipeTypes.INFUSION`），不是 serializer。

---

## 6. 装备与护目镜 (Goggles of Revealing)

### 6.1 Thaumaturge 自己的护目镜实现（真实源码全文）

**`com.leclowndu93150.thaumaturge.content.item.equipment.GogglesItem`**
源码：`.../content/item/equipment/GogglesItem.java`（60 行）

    package com.leclowndu93150.thaumaturge.content.item.equipment;

    public final class GogglesItem extends ArmorItem implements IGoggles, IRevealer, IVisDiscountGear {
        public GogglesItem(Properties properties) {
            super(TCMaterials.ARMOR_GOGGLES, Type.HELMET, properties);
        }

        @Override public EquipmentSlot getEquipmentSlot() { return EquipmentSlot.HEAD; }
        @Override public Holder<SoundEvent> getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            return swapWithEquipmentSlot(this, level, player, hand);
        }
        @Override public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) { return true; }
        @Override public boolean showNodes(ItemStack stack, LivingEntity holder) { return true; }
        @Override public int getVisDiscount(ItemStack stack) { return 5; }
        @Override public int getEnchantmentValue() { return TCItems.GOGGLES_ENCHANTMENT_VALUE; }
    }

**注册**（`registry/TCItems.java:973-993`，逐字）：

    public static final int GOGGLES_ENCHANTMENT_VALUE = 25;
    public static final ResourceLocation GOGGLES_REVEALING_ID = TCIds.rl("goggles_revealing");

    public static final DeferredItem<GogglesItem> GOGGLES_REVEALING = ITEMS.registerItem(
            "goggles_revealing", GogglesItem::new,
            new Item.Properties().stacksTo(1).durability(GOGGLES_DURABILITY).rarity(Rarity.RARE)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ARMOR,
                                 new AttributeModifier(GOGGLES_REVEALING_ID, 1, AttributeModifier.Operation.ADD_VALUE),
                                 EquipmentSlotGroup.HEAD)
                            .add(TCAttributes.VIS_DISCOUNT, /* ... 同上 ... */)
                            .build()));

**注册名**：`thaumaturge:goggles_revealing`

### 6.2 装备能力接口（`api/items/`，逐字）

    // api.items.IGoggles —— 戴上后显示方块上方的浮空文字（世界内 popup）
    public interface IGoggles { boolean showIngamePopups(ItemStack stack, LivingEntity wearer); }

    // api.items.IRevealer —— 戴上后可看见灵气节点
    public interface IRevealer { boolean showNodes(ItemStack stack, LivingEntity holder); }

    // api.items.IVisDiscountGear —— 提供 vis 消耗折扣（整数百分点）
    public interface IVisDiscountGear {
        int getVisDiscount(ItemStack stack);
        default EquipmentSlotGroup getAppliedSlot(ItemStack stack) {
            if (stack.getItem() instanceof Equipable equipable)
                return EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
            return EquipmentSlotGroup.ANY;
        }
    }

    // api.items.IWarpingGear —— 穿戴时施加 warp
    public interface IWarpingGear { int getWarp(ItemStack stack, LivingEntity wearer); }

    // api.items.IChanneledItem —— 「持续引导」类物品（工具/法杖）
    public interface IChanneledItem { default boolean releasesOnScreenOpen(ItemStack stack) { return true; } }

    // api.items.IScribeTools —— 纯标记（可作研究台书写工具）
    public interface IScribeTools {}

    // api.items.IGogglesDisplayExtended —— 方块侧提供浮空文字（BlockEntity 实现）
    public interface IGogglesDisplayExtended {
        Component[] getIGogglesText();
        default Vec3 getIGogglesTextOffset() { return Vec3.ZERO; }
    }

    // api.items.IRechargable
    public interface IRechargable {
        int getMaxCharge(ItemStack stack, LivingEntity holder);
        ChargeDisplay showInHud(ItemStack stack, LivingEntity holder);
        enum ChargeDisplay { NEVER, NORMAL, PERIODIC }
    }

    // api.items.IArchitect —— 范围挖掘（元素工具）
    public interface IArchitect {
        @Nullable HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster);
        boolean useBlockHighlight(ItemStack stack);
        List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player);
        boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis);
        enum EnumAxis { X, Y, Z }
    }

**静态访问器 `api.items.GogglesAccess`**（源码 131 行）——**附属直接调，不要 bind**：

    public static boolean wearsGoggles(LivingEntity entity);
    public static boolean revealsNodes(LivingEntity entity);
    public static int     totalVisDiscount(Player player);

    // 内部：bind(Supplier<Holder<Attribute>>) / bindCurios(Curios)，javadoc 明说 addons must not call

**`IGogglesDisplayExtended` 真实用例**（`content/essentia/tube/BlockEntityTubeFilter.java`）：

    public final class BlockEntityTubeFilter extends BlockEntityTube
            implements IAspectQuery, IGogglesDisplayExtended {
        @Override public Component[] getIGogglesText() { /* ... */ }
    }

### 6.3 护目镜的 Curios 集成（真实源码）

**`com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat`**
源码：`.../compat/curio/ThaumaturgeCuriosCompat.java`（242 行）

    public static void init(IEventBus modBus) {
        GogglesAccess.bindCurios(new GogglesAccess.Curios() {
            @Override public boolean wearsGoggles(LivingEntity entity) { return checkForGoggles(entity); }
            @Override public boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate) {
                return ThaumaturgeCuriosCompat.anyCurioMatches(entity, predicate);
            }
        });
        modBus.addListener(ThaumaturgeCuriosCompat::registerCurio);
        modBus.addListener(ThaumaturgeCuriosCompat::onClientSetup);
        NeoForge.EVENT_BUS.addListener(ThaumaturgeCuriosCompat::onItemAttributeModifier);
    }

    private static void registerCurio(RegisterCapabilitiesEvent event) {
        registerPlain(event,
                TCItems.GOGGLES_REVEALING.get(), TCItems.AMULET_MUNDANE.get(), TCItems.RING_MUNDANE.get(),
                TCItems.GIRDLE_MUNDANE.get(), TCItems.RING_APPRENTICE.get(), TCItems.AMULET_FANCY.get(),
                TCItems.RING_FANCY.get(), TCItems.GIRDLE_FANCY.get(), TCItems.CHARM_UNDYING.get(),
                TCItems.CLOUD_RING.get(), TCItems.CURIOSITY_BAND.get(), TCItems.FOCUS_POUCH.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.AMULET_VIS.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.AMULET_VIS_CRAFTED.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.VERDANT_CHARM.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.VOIDSEER_CHARM.get());
    }

    /** 普通饰品：无 tick，只需一个 ICurio 壳。 */
    private static void registerPlain(RegisterCapabilitiesEvent event, Item... items) {
        for (Item item : items) {
            event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> (ICurio) () -> stack, item);
        }
    }

    /** 需要 tick / 卸下钩子的饰品。 */
    private static ICurio tickingCurio(ItemStack stack) {
        return new ICurio() {
            @Override public ItemStack getStack() { return stack; }
            @Override public void curioTick(SlotContext slotContext) {
                LivingEntity wearer = slotContext.entity();
                if (stack.getItem() instanceof AmuletVisItem amulet) amulet.wornTick(stack, wearer);
                else if (stack.getItem() instanceof VerdantCharmItem charm) charm.wornTick(stack, wearer);
                else if (stack.getItem() instanceof VoidseerCharmItem charm) charm.wornTick(stack, wearer);
            }
            @Override public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                if (stack.getItem() instanceof VoidseerCharmItem && !newStack.is(stack.getItem())) {
                    VoidseerCharmItem.clearDiscount(slotContext.entity());
                }
            }
        };
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(TCItems.GOGGLES_REVEALING.get(), GoggleCurioRenderer::new);
        CuriosRendererRegistry.register(TCItems.CURIOSITY_BAND.get(), CuriosityBandCurioRenderer::new);
    }

**护目镜检测（真实代码，可直接照抄）**：

    public static boolean checkForGoggles(LivingEntity entity) {
        if (entity == null) return false;
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) return false;
        Optional<SlotResult> slotOpt = invOpt.get().findFirstCurio(stack -> stack.getItem() instanceof IGoggles);
        if (slotOpt.isEmpty()) return false;
        ItemStack stack = slotOpt.get().stack();
        if (stack.isEmpty() || !(stack.getItem() instanceof IGoggles g)) return false;
        return g.showIngamePopups(stack, entity);
    }

**vis 折扣属性注入（真实代码）**：

    private static void onItemAttributeModifier(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IVisDiscountGear gear) {
            float contribution = (float) gear.getVisDiscount(stack) / 100;
            if (contribution != 0) {
                event.addModifier(
                        TCAttributes.VIS_DISCOUNT,
                        new AttributeModifier(BuiltInRegistries.ITEM.getKey(stack.getItem()),
                                contribution, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

### 6.4 槽位分配：**数据包 JSON，不是代码**

Thaumaturge 用 datagen 生成的 `data/thaumaturge/curios/entities/players.json`（**从 jar 实测原文**）：

    {
      "entities": [
        "minecraft:player"
      ],
      "slots": [
        "head",
        "necklace",
        "ring",
        "belt",
        "charm"
      ]
    }

生成它的类（源码 `compat/curio/data/TCCurioProvider.java`，20 行，全文）：

    public class TCCurioProvider extends CuriosDataProvider {
        public TCCurioProvider(PackOutput output, ExistingFileHelper fileHelper,
                               CompletableFuture<HolderLookup.Provider> registries) {
            super(TCIds.MODID, output, fileHelper, registries);
        }
        @Override
        public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
            createEntities("players").addPlayer().addSlots("head", "necklace", "ring", "belt", "charm");
        }
    }

**物品属于哪个槽**由 item tag 决定。Thaumaturge 在
`data/tag/TCItemTagsProvider.java` 里用 `top.theillusivec4.curios.api.CuriosTags` 的常量写 tag：

    import top.theillusivec4.curios.api.CuriosTags;
    // ...
    tag(CuriosTags.HEAD).add(TCItems.GOGGLES_REVEALING.get(), /* ... */);

**附属需要自己写的三份 JSON**（放自己命名空间下，见 §10.3）。

### 6.5 护目镜研究/配方（jar 实测）

奥术工作台配方 `data/thaumaturge/recipe/arcane_workbench/goggles_revealing.json`：

    {
      "type": "thaumaturge:arcane_workbench_shaped",
      "crystals": [],
      "key": {
        "B": { "tag": "c:ingots/brass" },
        "L": { "tag": "c:leathers" },
        "M": { "item": "thaumaturge:thaumometer" }
      },
      "pattern": [ "LBL", "L L", "MBM" ],
      "research": { "entry": "thaumaturge:unlock_artifice" },
      "result": { "count": 1, "id": "thaumaturge:goggles_revealing" },
      "vis": 50
    }

灌注升级（`fortress_helm_goggles.json`）：给要塞头盔打上 `thaumaturge:goggles_upgrade` 组件，
使其获得护目镜功能。**这是「用 catalyst_patch 给已有装备加能力」的官方范例**。

相应地，`VoidRobeArmorItem` 与 `FortressArmorItem` 直接 implements `IGoggles, IRevealer`，
并在 tooltip 里判断组件：

    // content/equipment/FortressArmorItem.java:46
    if (hasGoggles(stack)) {
        tooltip.add(Component.translatable("item.thaumaturge.goggles_revealing")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

---

## 7. 物品基类与工具类

### 7.1 结论：**Thaumaturge 没有通用物品基类**

实测（全量 grep）：**不存在** `TCItem` / `ThaumaturgeItem` / `BaseItem`。
全模组唯一的抽象物品基类是：

**`content.recipe.dust.ItemMultiblockPlacer`**（源码 165 行）
`public abstract class ItemMultiblockPlacer extends BlockItem`

    protected ItemMultiblockPlacer(Block block, Properties properties);
    protected abstract ResourceKey<Blueprint> blueprint();
    @Override public InteractionResult place(BlockPlaceContext context);

其余物品都直接继承原版 `Item` / `BlockItem` / `SwordItem` / `ArmorItem`，
能力**靠接口组合**。

### 7.2 常见物品基类与实现范例（真实摘录）

| 类 | 继承/实现 | 源码 |
|---|---|---|
| `content.item.CausalityCollapserItem` | `extends Item` | `content/item/CausalityCollapserItem.java` |
| `content.item.PhialItem` | `extends Item implements IEssentiaContainerItem` | `content/item/PhialItem.java` |
| `content.item.LabelItem` | `extends Item implements IEssentiaContainerItem` | `content/item/LabelItem.java` |
| `content.item.ThaumometerItem` | `extends Item` | `content/item/ThaumometerItem.java` |
| `content.item.ScribingToolsItem` | `extends Item implements IScribeTools` | 10 行 |
| `content.equipment.RobeArmorItem` | `extends ArmorItem implements IVisDiscountGear` | `content/equipment/` |
| `content.equipment.VoidRobeArmorItem` | `extends ArmorItem implements IVisDiscountGear, IWarpingGear, IGoggles, IRevealer` | 同上 |
| `content.equipment.FortressArmorItem` | `extends ArmorItem implements IGoggles, IRevealer` | 同上 |
| `content.equipment.VoidSwordItem` | `extends SwordItem implements IWarpingGear` | 同上 |
| `content.equipment.ElementalSwordItem` | `extends SwordItem implements IChanneledItem` | 同上 |
| `content.equipment.ElementalShovelItem` | `extends ShovelItem implements IArchitect` | 同上 |
| `content.equipment.TravellerBootsItem` | `extends ArmorItem implements IRechargable` | 同上 |
| `content.equipment.bauble.VerdantCharmItem` | `extends Item implements IRechargable` | `content/equipment/bauble/` |
| `content.equipment.bauble.TrinketItem` | `extends Item implements IVisDiscountGear` | 同上 |
| `content.equipment.bauble.VoidseerCharmItem` | `extends Item implements IVisDiscountGear, IWarpingGear` | 同上 |
| `content.misc.ItemCurio` | `extends Item`（研究笔记） | `content/misc/ItemCurio.java` |

**`IRechargable` 真实实现范例**（`content/equipment/bauble/VerdantCharmItem.java`）：

    public final class VerdantCharmItem extends Item implements IRechargable {
        private static final int MAX_CHARGE = 200;
        @Override public int getMaxCharge(ItemStack stack, LivingEntity holder) { return MAX_CHARGE; }
        @Override public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) { return ChargeDisplay.NORMAL; }
        public void wornTick(ItemStack stack, LivingEntity wearer) {
            if (/* ... */ && RechargeAccess.consumeCharge(stack, player, WITHER_COST)) { /* ... */ }
        }
    }

### 7.3 tooltip 工具

#### 7.3.1 `appendHoverText` 标准签名（1.21.1）

两种写法在源码里都存在，**完全等价**：

    // 写法 A（content/item 下多数类）
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag)

    // 写法 B（content/equipment 下多数类，TooltipContext 已单独 import）
    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag)

真实例子（`content/equipment/ElementalSwordItem.java:40`）：

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.elemental_sword.toggle")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

#### 7.3.2 `client/TCTooltipEvents`（`content` 侧物品的自动附加行）

源码 `client/TCTooltipEvents.java`（81 行），`@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)`，
`public static void onItemTooltip(ItemTooltipEvent event)`。

**关键**：它对外来物品 **自动加行**，只要实现对应接口 / 带对应组件：

| 条件 | 加的行 |
|---|---|
| `InfusionEnchantmentHelper.get(stack)` 非空 | `enchantment.thaumaturge.<name>` |
| `InfusionRunicAugmentRecipe.charge(stack) > 0` | `tooltip.thaumaturge.runic_charge` |
| `WarpHelper.getFinalWarp(stack, player) > 0` | `item.thaumaturge.warping` |
| `stack.getItem() instanceof IRechargable` | `tooltip.thaumaturge.charge`（充能 X/Y，AQUA 色） |
| 是灌注稳定器方块（`IInfusionStabiliser` 或 `TCBlockTags.INFUSION_STABILISERS`）且已解锁 `thaumaturge:unlock_infusion` | 稳定器提示行 |

**含义**：附属物品只要 implements `IRechargable` / `IWarpingGear`，
**充能与 warp 的 tooltip 行会自动出现**，不用自己写。

#### 7.3.3 `client/screen/tooltip/` 工具类（全部实读）

**`client.screen.tooltip.DeferredTooltip`**（45 行，全静态，在 render 里延迟绘制 tooltip）：

    public static void set(List<Component> tooltipLines, int mouseX, int mouseY);
    public static void set(Component line, int mouseX, int mouseY);
    public static void setItem(ItemStack itemStack, int mouseX, int mouseY);
    public static void render(GuiGraphics graphics, Font font);

**`client.screen.tooltip.HalfScaleTooltipLine`**（37 行，`implements ClientTooltipComponent`）：

    public static final String PREFIX = "@@";    // 行首带 @@ 的行会被半尺寸渲染
    public HalfScaleTooltipLine(FormattedCharSequence text);
    public int getWidth(Font font);
    public int getHeight();

**`client.screen.tooltip.TCTooltipRenderer`**（53 行）：

    public static void render(GuiGraphics graphics, Font font, List<Component> lines, int x, int y);
    // 普通行按 200 宽换行；@@ 行按 400 宽换行 + 0.5 缩放

**`client.screen.TCTooltips`**（151 行）—— 研究界面文案工厂，全部 `public static`：
`categoryName`、`categoryPercent`、`knowledgeLabel`、`need(String)`、`cardUnknown`、
`noInkLine0/1`、`noPaperLine0`、`returnLabel`、`beginResearch`、
`researchStage(int,int)`、`researchMissing`、`researchNew`、`pageNew`、
`entryNameGold(Component)`、`entryHover(...)`、`prereqEntryName(ResourceLocation)`；
嵌套 `public enum EntryStatus { CAN_UNLOCK, MISSING_PREREQ, COMPLETE }`。

**`content.wands.WandTooltips`**（90 行）：

    public static Component primalColor(HolderLookup.@Nullable Provider, ResourceKey<IAspect>);
    public static Component primalName(HolderLookup.@Nullable Provider, ResourceKey<IAspect>);
    public static Component costSummary(HolderLookup.@Nullable Provider, Map<ResourceKey<IAspect>, Integer>);
    public static Component capCostSummary(HolderLookup.@Nullable Provider, WandCap);

### 7.4 要素显示工具

#### 7.4.1 `api.aspect.AspectComponents`（见 §2.11）

#### 7.4.2 `client.render.aspect.AspectTagRenderer`（源码 318 行，屏幕内绘制）

    public enum BlendMode { ALPHA, ADDITIVE }

    public static void render(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect);
    public static void renderUnknown(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect);
    public static void renderMaskedChip(GuiGraphics graphics, int x, int y,
                                        Holder<IAspect> aspect, AspectKnowledge knowledge);
    public static void renderUnknownChip(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect);
    public static void renderMissingChip(GuiGraphics graphics, int x, int y);
    public static void render(GuiGraphics graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount);
    public static void render(GuiGraphics graphics, Font font, int x, int y, Holder<IAspect> aspect,
                              float amount, int bonus, float alpha, boolean bw);
    public static void render(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect,
                              float amount, int bonus, float alpha, boolean bw);
    public static void render(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect,
                              float amount, int bonus, double z);
    public static void render(GuiGraphics graphics, int x, int y, Holder<IAspect> aspect,
                              float amount, int bonus, double z, BlendMode blend, float alpha);
    public static void render(GuiGraphics graphics, Font font, double x, double y, Holder<IAspect> aspect,
                              float amount, int bonus, double z, BlendMode blend, float alpha, boolean bw);
    public static int colorOf(IAspect aspect, float alpha, boolean bw);

`amount == 0` 只画图标不画数字；`bonus` 是右上角加成角标；`bw = true` 表示未解锁的灰度渲染。
未知图标常量：`TCIds.rl("textures/aspects/_unknown.png")`。

#### 7.4.3 `client.render.aspect.AspectTagWorldRenderer`（源码 259 行，世界内绘制）

    public static void renderTagCloud(PoseStack poseStack, Minecraft mc, double x, double y, double z,
            AspectList aspects, @Nullable Direction dir, float tagScale, float alpha);
    public static void renderTagCloud(PoseStack poseStack, Minecraft mc, double x, double y, double z,
            AspectList aspects, @Nullable Direction dir, float tagScale, float alpha,
            Predicate<Holder<IAspect>> discovered);
    public static void renderBillboard(PoseStack, MultiBufferSource, Holder<IAspect>,
            float scale, float alpha, boolean bw, int packedLight);
    public static void renderBillboardAdditive(PoseStack, MultiBufferSource, Holder<IAspect>,
            float scale, float alpha, boolean bw, int packedLight);
    public static void renderBillboard(PoseStack, MultiBufferSource, Holder<IAspect>,
            float scale, float alpha, boolean bw, int packedLight, AspectTagRenderer.BlendMode blend);
    public static void renderQuad(PoseStack, VertexConsumer, Holder<IAspect>, float alpha, boolean bw, int packedLight);
    public static void renderQuad(PoseStack.Pose, VertexConsumer, Holder<IAspect>, float alpha, boolean bw, int packedLight);
    public static void renderMissingQuad(PoseStack, VertexConsumer, float alpha, int packedLight);

### 7.5 物品栏工具 `api.items.InvHelper`（源码 579 行）

嵌套类型：

    public static final class InvFilter {
        public static final InvFilter STRICT = new InvFilter(false, false, false, false);
        public InvFilter(boolean ignoreDamage, boolean ignoreComponents, boolean useTags, boolean useMod);
        public InvFilter setRelaxedComponents();     // 返回 this
    }
    public record FilterMatch(ItemStack stack, int sizeLimit) {}
    public static final InvFilter BASE_TAGS = new InvFilter(false, false, true, false);

公开方法（逐字，含多行参数）：

    public static @Nullable IItemHandler getItemHandlerAt(Level level, BlockPos pos, @Nullable Direction side);
    public static ItemStack insertStack(@Nullable IItemHandler handler, ItemStack stack, boolean simulate);
    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate);
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack);
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack);
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack);
    public static int countTotalItemsIn(@Nullable IItemHandler inventory, ItemStack stack, InvFilter filter);
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter);
    public static boolean areItemStacksEqual(ItemStack first, ItemStack second, InvFilter filter);
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> filterStacks, boolean blacklist, IItemHandler inv, InvFilter filter, boolean leaveOne);
    public static boolean matchesFilters(
            List<ItemStack> filterStacks, boolean blacklist, ItemStack stack, InvFilter filter);
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> filterStacks, List<Integer> filterSizes, boolean blacklist,
            List<ItemStack> candidates, InvFilter filter);
    public static FilterMatch findFirstMatchFromFilterWithSize(
            List<ItemStack> filterStacks, List<Integer> filterSizes, boolean blacklist,
            List<ItemStack> candidates, InvFilter filter);
    public static ItemStack copyLimitedStack(ItemStack stack, int limit);
    public static ItemStack removeStackFrom(
            Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter, boolean simulate);
    public static ItemStack removeStackFrom(
            @Nullable IItemHandler inventory, ItemStack stack, InvFilter filter, boolean simulate);
    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out);
    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out, boolean smart);
    public static int countStackInWorld(Level level, BlockPos pos, ItemStack stack, double range, InvFilter filter);
    public static void dropItemAtEntity(Level level, ItemStack stack, Entity entity);
    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack stack);
    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack stack);
    public static boolean isPlayerCarryingAmount(Player player, ItemStack stack, boolean useTags);
    public static boolean consumePlayerItem(Player player, ItemStack stack, boolean skipCheck, boolean useTags);
    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(
            Level level, BlockPos pos, Player player, boolean simulate, ItemStack... items);

> **注意（先例缺口）**：`ThaumicEnergistics CE` 里 **InvHelper 使用次数为 0**（它走 AE2 自己的存储）。
> 所以该类**只有 Thaumaturge 自身使用**作证，附属用属于「可用但无先例」。

### 7.6 数据组件注册表 `registry.TCDataComponents`（源码 226 行）

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TCIds.MODID);
    public static void register(IEventBus modBus) { DATA_COMPONENTS.register(modBus); }

统一写法：
`DATA_COMPONENTS.registerComponentType("名字", builder -> builder.persistent(CODEC).networkSynchronized(STREAM_CODEC))`

与 vis / charge 直接相关的四个（逐字）：

    // thaumaturge:charge —— 通用「充能」值，由 RechargeAccess 绑定使用
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            DATA_COMPONENTS.registerComponentType("charge",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                                      .networkSynchronized(ByteBufCodecs.VAR_INT));

    // thaumaturge:runic_charge —— 符文强化层数（InfusionRunicAugmentRecipe 用）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNIC_CHARGE =
            DATA_COMPONENTS.registerComponentType("runic_charge",
                    builder -> builder.persistent(ExtraCodecs.POSITIVE_INT)
                                      .networkSynchronized(ByteBufCodecs.VAR_INT));

    // thaumaturge:wand_vis —— 法杖内各源质 vis
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandVis>> WAND_VIS =
            DATA_COMPONENTS.registerComponentType("wand_vis",
                    builder -> builder.persistent(LegacyIds.WAND_VIS_CODEC)
                                      .networkSynchronized(WandVis.STREAM_CODEC));

    // thaumaturge:energy —— 另一套通用能量（与 charge 并存）
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.registerComponentType("energy",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                                      .networkSynchronized(ByteBufCodecs.VAR_INT));

其余组件（同文件）：`ASPECTS`(AspectList)、`ESSENTIA_CONTENTS`(EssentiaList)、
`ASPECT_FILTER`(ResourceKey<IAspect>)、`CELESTIAL_BODY`(CelestialBody)、`CRYSTAL_ASPECT`(AspectInstance)、
`FOCUS_PACKAGE`、`SOCKETED_FOCUS`、`CASTER_AREA`、`PICKED_BLOCK`、`GRAPPLE_LOADED`(Boolean)、
`INFUSION_ENCHANTMENTS`、`STACK_WARP`("warp", Integer)、`TOOL_ORIENTATION`、`GOLEM_PROPERTIES`、
`GOLEM_XP`、`STORED_XP`、`VERDANT_TYPE`、`FORTRESS_MASK`、`GOGGLES_UPGRADE`(Unit)、
`POUCH_CONTENTS`、`MIRROR_LINK`、`ARCANE_KEY_LINK`、`NOTE_COMPLETE`、`WHIRLWIND_DISABLED`、
`RESEARCH_NOTE`、`NODE_DATA`、`WAND_PARTS`、`SHARE_BINDING`、`LINK_BINDING`。

### 7.7 `api.wands.WandAccess` 完整签名

    public static WandVis getAllVis(ItemStack wand);
    public static int getVis(ItemStack wand, ResourceKey<IAspect> aspect);
    public static ItemStack withVis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis);
    // bind(Supplier<DataComponentType<WandVis>>) 为内部初始化，addon 勿调用

---

## 8. 伤害与实体

### 8.1 三个同名不同包的类（最容易踩的坑）

| 完整类名 | 文件 | 作用 |
|---|---|---|
| `api.damagesource.TCDamageTypes` | `api/damagesource/TCDamageTypes.java`（32 行） | **公开 API**：5 个 `ResourceKey<DamageType>` 常量 |
| `api.damagesource.TCDamageSources` | `api/damagesource/TCDamageSources.java`（70 行） | **公开 API**：构造 `DamageSource` 的静态工厂 |
| `registry.TCDamageTypes` | `registry/TCDamageTypes.java`（9 行） | **空壳，别用**：`register(IEventBus)` 方法体为空 |

### 8.2 完整签名

    // com.leclowndu93150.thaumaturge.api.damagesource.TCDamageTypes
    public final class TCDamageTypes {
        public static final ResourceKey<DamageType> TAINT      = key("taint");
        public static final ResourceKey<DamageType> TENTACLE   = key("tentacle");
        public static final ResourceKey<DamageType> SWARM      = key("swarm");
        public static final ResourceKey<DamageType> DISSOLVE   = key("dissolve");
        public static final ResourceKey<DamageType> FOCUS_FIRE = key("focus_fire");
        // private static ResourceKey<DamageType> key(String path)
        //   -> ResourceKey.create(Registries.DAMAGE_TYPE,
        //        ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path))
    }

    // com.leclowndu93150.thaumaturge.api.damagesource.TCDamageSources
    public final class TCDamageSources {
        public static DamageSource taint(Level level);
        public static DamageSource tentacle(Level level, @Nullable Entity attacker);
        public static DamageSource swarm(Level level, @Nullable Entity attacker);
        public static DamageSource dissolve(Level level);
        public static DamageSource focusFire(Level level, @Nullable Entity direct, @Nullable Entity caster);
    }

**设计要点**（类 javadoc 原文）：伤害行为（穿甲、女巫抵抗、magic 标记等）
**不是**靠 `DamageSource` 子类，而是靠 `data/thaumaturge/tags/damage_type/` 下的标签接线，由原版在结算时读取。

### 8.3 数据包路径与 JSON 原文（**已从 jar 实测**）

**JSON 路径**：`data/<ns>/damage_type/<id>.json`

从 jar 实测 `data/thaumaturge/damage_type/taint.json`：

    {
      "exhaustion": 0.0,
      "message_id": "thaumaturge.taint",
      "scaling": "when_caused_by_living_non_player"
    }

（`message_id` 决定死亡消息翻译键；`dedupe` 未在此文件出现。）

**datagen 源码**（`data/damagetype/TCDamageTypeBootstrap.java`）：

    context.register(TCDamageTypes.TAINT,
        new DamageType("thaumaturge.taint", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
    context.register(TCDamageTypes.TENTACLE,
        new DamageType("thaumaturge.tentacle", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
    context.register(TCDamageTypes.SWARM,
        new DamageType("thaumaturge.swarm", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
    context.register(TCDamageTypes.DISSOLVE,
        new DamageType("thaumaturge.dissolve", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
    context.register(TCDamageTypes.FOCUS_FIRE,
        new DamageType("thaumaturge.focus_fire", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.BURNING));

**标签接线**（`data/tag/TCDamageTypeTagsProvider.java`，33 行）：

    public static final TagKey<DamageType> IS_MAGIC =
            TagKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "is_magic"));

    tag(DamageTypeTags.BYPASSES_ARMOR).add(TCDamageTypes.TAINT, TCDamageTypes.DISSOLVE);
    tag(DamageTypeTags.BYPASSES_SHIELD).add(TCDamageTypes.TAINT);
    tag(DamageTypeTags.WITCH_RESISTANT_TO).add(TCDamageTypes.TAINT);
    tag(DamageTypeTags.WITHER_IMMUNE_TO).add(TCDamageTypes.TAINT);
    tag(IS_MAGIC).add(TCDamageTypes.TAINT);
    tag(DamageTypeTags.IS_FIRE).add(TCDamageTypes.FOCUS_FIRE);
    tag(DamageTypeTags.IS_PROJECTILE).add(TCDamageTypes.FOCUS_FIRE);

### 8.4 如何造成一次自定义伤害（源码里全部 3 处真实用例，照抄即可）

1) **腐化伤害**（`content/taint/effect/FluxTaintEffect.java:37`）：

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplification) {
        if (!(mob.level() instanceof ServerLevel level)) return true;
        if (mob instanceof ITaintedMob) { mob.heal(HEAL); return true; }
        if (!mob.getType().is(EntityTypeTags.UNDEAD)) {
            mob.hurt(TCDamageSources.taint(level), DAMAGE);      // DAMAGE = 1.0F
        }
        return true;
    }

2) **溶解伤害**（`content/alchemy/LiquidDeathFluid.java:31`）：

    entity.hurt(TCDamageSources.dissolve(level), damageFor(amount));

配套的接收侧判定（`content/alchemy/LiquidDeathEvents.java:26`）：

    if (!(entity.level() instanceof ServerLevel level) || !event.getSource().is(TCDamageTypes.DISSOLVE)) { ... }

→ `DamageSource.is(ResourceKey<DamageType>)` **可直接吃 `TCDamageTypes.*` 常量**。

3) **法术火焰**（`content/focus/effect/FocusEffectFire.java:82`）：

    struck.hurt(TCDamageSources.focusFire(level, struck, ctx.caster()), damage);

### 8.5 新增自己的伤害类型（附属做法）

不要改 Thaumaturge 的类。在自己模组里：

    // 常量
    ResourceKey<DamageType> MY_TYPE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("forgotten_relics", "my_damage"));

    // datagen（或手写 data/forgotten_relics/damage_type/my_damage.json）
    new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, bootstrap);

    // 运行时构造
    Holder<DamageType> holder = level.registryAccess()
            .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(MY_TYPE);
    DamageSource source = new DamageSource(holder, directEntity, causingEntity);

### 8.6 实体注册

**注册表**（`registry/TCEntities.java`，448 行）：

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TCIds.MODID);

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(
            String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITIES.register(name, () -> builderSupplier.get().build(TCIds.MODID + ":" + name));
    }

    public static void register(IEventBus modBus) { ENTITIES.register(modBus); }

**跟投射物有关的真实注册摘录**：

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFocusProjectile>> FOCUS_PROJECTILE = register(
            "focus_projectile",
            () -> EntityType.Builder.<EntityFocusProjectile>of(EntityFocusProjectile::new, MobCategory.MISC)
                    .sized(0.15F, 0.15F).clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemDart>> GOLEM_DART = register(
            "golem_dart",
            () -> EntityType.Builder.<EntityGolemDart>of(EntityGolemDart::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(4).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownAlumentum>> ALUMENTUM = register(
            "thrown_alumentum",
            () -> EntityType.Builder.<ThrownAlumentum>of(ThrownAlumentum::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGrapple>> GRAPPLE = register(
            "grapple",
            () -> EntityType.Builder.<EntityGrapple>of(EntityGrapple::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(8).updateInterval(3));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchOrb>> ELDRITCH_ORB = register(
            "eldritch_orb",
            () -> EntityType.Builder.<EntityEldritchOrb>of(EntityEldritchOrb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

### 8.7 投射物实体类范例

> **纠正**：`content/entity/projectile/` 目录下**只有 `EntityGrapple.java` 一个文件**（已实测）。
> 其余投射物散布在 `content/entity/`（EntityGolemDart、EntityGolemOrb、EntityEldritchOrb、
> EntityFocusProjectile、EntityFocusMine、EntityFocusCloud、EntityBottleTaint、EntityCausalityCollapser）、
> `content/misc/alumentum/ThrownAlumentum.java`、`content/wands/EntityAspectOrb.java`。

#### (a) `ThrowableProjectile`：`content/entity/projectile/EntityGrapple.java`（161 行）

    public final class EntityGrapple extends ThrowableProjectile {
        private static final EntityDataAccessor<Boolean> PULLING =
                SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);

        // 必须有：反序列化构造
        public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level) { super(type, level); }

        // 实际使用的构造
        public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level,
                             LivingEntity thrower, InteractionHand hand) {
            super(type, thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ(), level);
            setOwner(thrower);
            entityData.set(MAIN_HAND, hand == InteractionHand.MAIN_HAND);
        }

        @Override protected void defineSynchedData(SynchedEntityData.Builder entityData) {
            entityData.define(PULLING, false);
            entityData.define(MAIN_HAND, true);
        }
        @Override protected double getDefaultGravity() { return isPulling() ? 0.0 : FALLING_GRAVITY; }
        @Override public void tick() { /* ... */ }
        @Override protected void onHitBlock(BlockHitResult hit) { /* ... */ }
    }

> 该类**没有**覆写 `getAddEntityPacket()`——1.21.1 里已由 NeoForge 默认实现；
> 需要额外 spawn 数据时改用 `IEntityWithComplexSpawn`。

#### (b) 最短投射物：`content/entity/EntityGolemDart.java`（25 行）

    public final class EntityGolemDart extends AbstractArrow {
        public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) { super(type, level); }
        public EntityGolemDart(Level level, LivingEntity owner) {
            super(TCEntities.GOLEM_DART.get(), owner, level, new ItemStack(Items.ARROW), null);
            this.pickup = Pickup.DISALLOWED;
        }
        @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }
    }

#### (c) `onHit(HitResult)`：`content/entity/EntityEldritchOrb.java`（69 行）

    public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level level) { super(type, level); }
    public EntityEldritchOrb(Level level, LivingEntity shooter) {
        super(TCEntities.ELDRITCH_ORB.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - EYE_OFFSET, shooter.getZ());
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder entityData) {}   // 可为空
    @Override protected double getDefaultGravity() { return 0.0; }
    @Override protected void onHit(HitResult result) {
        if (!(this.level() instanceof ServerLevel server) || !(this.getOwner() instanceof LivingEntity owner)) return;
        float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * DAMAGE_FACTOR;
        DamageSource source = this.damageSources().indirectMagic(this, owner);
        for (Entity entity : this.level().getEntities(owner, this.getBoundingBox().inflate(BLAST_RANGE))) { ... }
        this.discard();
    }

→ `onHit(HitResult)` 是总入口；`onHitBlock(BlockHitResult)` / `onHitEntity(EntityHitResult)` 是子分支。

#### (d) `ThrowableItemProjectile`：`content/misc/alumentum/ThrownAlumentum.java`（84 行）

    public ThrownAlumentum(EntityType<? extends ThrownAlumentum> type, Level level) { super(type, level); }
    public ThrownAlumentum(Level level, LivingEntity mob, ItemStack itemStack) {
        super(TCEntities.ALUMENTUM.get(), mob, level); this.setItem(itemStack);
    }
    public ThrownAlumentum(Level level, double x, double y, double z, ItemStack itemStack) {
        super(TCEntities.ALUMENTUM.get(), x, y, z, level); this.setItem(itemStack);
    }
    @Override public void shoot(double xd, double yd, double zd, float pow, float uncertainty) {
        super.shoot(xd, yd, zd, 0.75F, uncertainty);
    }
    @Override protected void onHit(HitResult hitResult) {
        /* ... */ level().explode(this, x, y, z, 1.1F, Level.ExplosionInteraction.TNT); this.discard();
    }
    protected Item getDefaultItem() { return Items.EGG; }

**发射它的物品写法**（`content/item/CausalityCollapserItem.java`，同一套路）：

    if (level instanceof ServerLevel serverLevel) {
        EntityCausalityCollapser projectile = new EntityCausalityCollapser(serverLevel, player, stack);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SHOOT_POWER, 2.0F);
        serverLevel.addFreshEntity(projectile);
    }

#### (e) 需要额外 spawn 数据：`IEntityWithComplexSpawn`

实现者：EntityFocusProjectile、EntityFocusMine、EntityFocusCloud、EntitySpellBat、
EntityFallingTaint、EntityFollowingItem（源码实测）。

NeoForge 接口：

    public interface IEntityWithComplexSpawn {
        void writeSpawnData(RegistryFriendlyByteBuf buffer);
        void readSpawnData(RegistryFriendlyByteBuf additionalData);
    }

真实实现（`content/entity/EntityFocusProjectile.java`）：

    @Override public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        FocusPackages.write(buffer, this.focusPackage);
    }
    @Override public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.focusPackage = FocusPackages.read(additionalData);
    }

### 8.8 从零注册一个自定义实体的完整步骤

1. **DeferredRegister**：`DeferredRegister.create(Registries.ENTITY_TYPE, "你的modid")` + `register(modBus)`。
2. **EntityType.Builder**：`.sized(w,h)`、`.eyeHeight(f)`、`.fireImmune()`、
   `.clientTrackingRange(n)`、`.updateInterval(n)`、`.spawnDimensionsScale(f)`；最后 `.build("modid:name")`。
3. **实体类**：至少两个构造 `(EntityType<? extends X>, Level)` 与 gameplay 构造；
   `defineSynchedData`；有状态时 `addAdditionalSaveData` / `readAdditionalSaveData`。
4. **属性（仅 Mob）**：实体类里 `public static AttributeSupplier.Builder createAttributes()`，例如
   `content/entity/WispEntity.java:69`：

       public static AttributeSupplier.Builder createAttributes() {
           return Monster.createMonsterAttributes()
                   .add(Attributes.MAX_HEALTH, 22.0)
                   .add(Attributes.ATTACK_DAMAGE, 3.0)
                   .add(Attributes.FOLLOW_RANGE, 16.0)
                   .add(Attributes.FLYING_SPEED, 0.1);
       }

   在 `EntityAttributeCreationEvent` 上注册（`content/entity/TCEntityEvents.java:110`）：

       @EventBusSubscriber(modid = TCIds.MODID)
       public final class TCEntityEvents {
           @SubscribeEvent
           public static void onAttributes(EntityAttributeCreationEvent event) {
               event.put(TCEntities.WISP.get(), WispEntity.createAttributes().build());
           }
           @SubscribeEvent
           public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
               event.register(TCEntities.WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                       Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WispEntity::checkWispSpawnRules,
                       RegisterSpawnPlacementsEvent.Operation.REPLACE);
           }
       }

5. **渲染器 + 客户端注册**：`client/entity/TCEntityRenderers.java`（164 行），
   `@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)`

       @SubscribeEvent
       public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
           event.registerEntityRenderer(TCEntities.GRAPPLE.get(), GrappleRenderer::new);
           event.registerEntityRenderer(TCEntities.ALUMENTUM.get(), EmptyEntityRenderer::new);
           event.registerEntityRenderer(TCEntities.FOCUS_PROJECTILE.get(), FocusProjectileRenderer::new);
           event.registerEntityRenderer(TCEntities.CAUSALITY_COLLAPSER.get(), NoModelRenderer::new);
       }

     模型层用 `EntityRenderersEvent.RegisterLayerDefinitions`：
     `event.registerLayerDefinition(TCModelLayers.GRAPPLER, GrapplerModel::createLayer);`

6. **（可选）刷怪蛋**：`registry/TCItems.java:603` 的 `registerSpawnEgg(...)`，
   用 NeoForge `DeferredSpawnEggItem`；基色常量 `SPAWN_EGG_BASE = 0x51436C`、
   `SPAWN_EGG_HIGHLIGHT = 0xB399CF`。

---

## 9. 网络包

### 9.1 结论：**就是 NeoForge 原生 `CustomPacketPayload` 方案，无自研框架**

证据：
1. `network/TCPayloads.java` 唯一注册入口是
   `@SubscribeEvent public static void register(RegisterPayloadHandlersEvent event)`，
   两个类型都来自 `net.neoforged.neoforge.network.*`。
2. 每个 payload 都 `implements net.minecraft.network.protocol.common.custom.CustomPacketPayload`（原版类），
   各自持有 `Type<T> TYPE` 与 `StreamCodec<RegistryFriendlyByteBuf, T>`。
3. 发送侧全部走 `net.neoforged.neoforge.network.PacketDistributor`（源码内 69 处调用）。
4. handler 签名是 `(T payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx)`。

### 9.2 `TCPayloads`

**`com.leclowndu93150.thaumaturge.network.TCPayloads`**（202 行）

    @EventBusSubscriber(modid = TCIds.MODID)
    public final class TCPayloads {
        private static final String VERSION = "2";

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar(VERSION);
            registrar.playToClient(TYPE, STREAM_CODEC, handler);
            registrar.playToServer(TYPE, STREAM_CODEC, handler);
            // ... 共 44 条
        }
    }

> `RegisterPayloadHandlersEvent implements IModBusEvent`，所以 `@EventBusSubscriber` 会自动挂到 mod bus。
> 附属要自己建一个同样形态的注册类（`@EventBusSubscriber(modid = 自己的 modid)`），**不要改 Thaumaturge 的类**。

**NeoForge 的 registrar 签名**（`PayloadRegistrar`）：

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> reader,
            IPayloadHandler<T> handler);
    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(/* 同上 */);
    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(/* ... */);

### 9.3 最小完整示例（Clientbound）

**`network/ClientboundWispZapPayload.java`（24 行，全文）**

    package com.leclowndu93150.thaumaturge.network;

    import com.leclowndu93150.thaumaturge.TCIds;
    import net.minecraft.network.RegistryFriendlyByteBuf;
    import net.minecraft.network.codec.ByteBufCodecs;
    import net.minecraft.network.codec.StreamCodec;
    import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

    public record ClientboundWispZapPayload(int sourceId, int targetId) implements CustomPacketPayload {
        public static final Type<ClientboundWispZapPayload> TYPE = new Type<>(TCIds.rl("wisp_zap"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWispZapPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ClientboundWispZapPayload::sourceId,
                        ByteBufCodecs.VAR_INT, ClientboundWispZapPayload::targetId,
                        ClientboundWispZapPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

**无字段 payload 用 `StreamCodec.unit`**（`ClientboundOpenThaumonomiconPayload.java`）：

    public static final ClientboundOpenThaumonomiconPayload INSTANCE = new ClientboundOpenThaumonomiconPayload();
    public static final Type<ClientboundOpenThaumonomiconPayload> TYPE = new Type<>(TCIds.rl("open_thaumonomicon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenThaumonomiconPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

### 9.4 最小完整示例（Serverbound + handler）

**`network/ServerboundAdvanceStagePayload.java` + `ServerboundAdvanceStageHandler.java`**

    public record ServerboundAdvanceStagePayload(ResourceLocation research) implements CustomPacketPayload {
        public static final Type<ServerboundAdvanceStagePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "advance_stage"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAdvanceStagePayload> STREAM_CODEC =
                StreamCodec.composite(ResourceLocation.STREAM_CODEC,
                        ServerboundAdvanceStagePayload::research,
                        ServerboundAdvanceStagePayload::new);

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public final class ServerboundAdvanceStageHandler {
        public static void handle(ServerboundAdvanceStagePayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ResearchManager.advanceStage(player, payload.research());
                }
            });
        }
    }

**handler 的三种真实放置方式**：
1. payload 类里自带静态 `handle`（服务端常见），注册处 `ServerboundCloudJumpPayload::handle`；
2. 独立的 server handler 类（`network/ServerboundRequestAuraChunkHandler.java` 等 6 个）；
3. **client handler 放 `client/network/` 下**（避免服务端加载客户端类），
   注册处写 lambda `(payload, context) -> WispZapClientHandler.handle(payload, context)`；
   handler 内部 `ctx.enqueueWork(() -> { ... })` 切回主线程。

### 9.5 发送工具：`PacketDistributor`

NeoForge 真实签名：

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded,
            double x, double y, double z, double radius, CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads);
    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos,
            CustomPacketPayload payload, CustomPacketPayload... payloads);

**源码里的真实调用**：

    // 客户端 → 服务端
    PacketDistributor.sendToServer(new ServerboundFocusChangePayload(CasterManager.REMOVE_FOCUS));
    PacketDistributor.sendToServer(new ServerboundRequestAuraChunkPayload(pos.x, pos.z));
    PacketDistributor.sendToServer(new ServerboundCloudJumpPayload());

    // 服务端 → 客户端
    PacketDistributor.sendToPlayer(player, new ClientboundAuraSnapshotPayload(pos.x, pos.z, base, vis, flux));
    PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload);
    PacketDistributor.sendToPlayersInDimension(level, ClientboundSealPayload.remove(pos));
    PacketDistributor.sendToPlayersNear(level, null, from.x, from.y, from.z, DEFAULT_RADIUS, payload);
    PacketDistributor.sendToAllPlayers(new ClientboundAspectIndexPayload(index));

**`IPayloadContext` 可用成员**：
`Player player()`（serverbound = 发送者 ServerPlayer；clientbound = 接收者 LocalPlayer）、
`CompletableFuture<Void> enqueueWork(Runnable)`、`<T> CompletableFuture<T> enqueueWork(Supplier<T>)`、
`ICommonPacketListener listener()`、`Connection connection()`、`void reply(CustomPacketPayload)`、
`void disconnect(Component)`、`ConnectionProtocol protocol()`。

### 9.6 现成可复用的特效 payload（写特效不用自己造）

| 类 | 说明 |
|---|---|
| `network/effect/ClientboundSpawnParticlePayload` | `(ParticleOptions options, double x,y,z, vx,vy,vz)` + 便捷构造 `(options,x,y,z)` |
| `network/effect/ClientboundStreamEffectPayload`（157 行） | 工厂：`arc(...)`、`bolt(...)`、`beam(...)`、`essentia(...)`、`bore(...)`、`voidStream(...)`；`hasFlag(byte)`；`FLAG_REVERSE=1`、`FLAG_WITH_SOURCE=2`；配套 `StreamEffectKind` |
| `network/effect/ClientboundFocusImpactPayload` | `NO_CASTER = -1` |
| `network/effect/ClientboundInfusionSourcePayload` / `ClientboundBoreDigPayload` | — |

---

## 10. Curios 1.21.1 API

> 本节来源：`F:/Deepseek Harness/Forgotten Relics Unofficial/Curios-1.21.1/`
> 版本 `9.5.1+1.21.1`，仅 NeoForge（common + neoforge multiloader，无 Fabric/Forge 分支）。

### 10.0 先决结论（最易踩坑）

1. **1.21.1 没有 `ICuriosApi` 接口，也没有 `CuriosApi.getInstance()`。**
   `top.theillusivec4.curios.api.CuriosApi` 是 `public final class`，**全静态方法**。
   全源码 + 成品 jar 搜 `getInstance` / `getApi(` → **0 命中**。
2. **`ICurioItem` 可直接 implement 到 `Item` 上**，Curios 自动挂载 `ICurio` 能力；
   也可在 `RegisterCapabilitiesEvent` 里 `evt.registerItem(CuriosCapability.ITEM, ...)` 手工提供。
3. **物品能进哪个槽由 item tag 决定**：`data/curios/tags/item/<slotId>.json` 或 `data/curios/tags/item/curio.json`。
4. **玩家默认槽位不在 Curios 仓库**。Curios 内置数据只注册 10 个「槽类型」，不给任何实体分配槽位。
   要稳，自己写 `data/<你的ns>/curios/entities/*.json`。

### 10.1 主入口与能力常量

| 项 | 完整类名 |
|---|---|
| API 主入口 | `top.theillusivec4.curios.api.CuriosApi` |
| 模组主类 | `top.theillusivec4.curios.Curios` |
| 常量 | `top.theillusivec4.curios.CuriosConstants`（`MOD_ID = "curios"`） |

**`CuriosApi` 关键静态方法（现行，未弃用）**：

    public static void registerCurio(Item item, ICurioItem curio);

    public static Optional<ISlotType> getSlot(String id, Level level);
    public static Optional<ISlotType> getSlot(String id, boolean isClient);
    public static Map<String, ISlotType> getSlots(Level level);
    public static Map<String, ISlotType> getSlots(boolean isClient);
    public static Map<String, ISlotType> getPlayerSlots(Level level);
    public static Map<String, ISlotType> getPlayerSlots(boolean isClient);
    public static Map<String, ISlotType> getPlayerSlots(Player player);
    public static Map<String, ISlotType> getEntitySlots(LivingEntity livingEntity);
    public static Map<String, ISlotType> getEntitySlots(EntityType<?> type, Level level);
    public static Map<String, ISlotType> getEntitySlots(EntityType<?> type, boolean isClient);
    public static Map<String, ISlotType> getItemStackSlots(ItemStack stack, Level level);
    public static Map<String, ISlotType> getItemStackSlots(ItemStack stack, boolean isClient);
    public static Map<String, ISlotType> getItemStackSlots(ItemStack stack, LivingEntity livingEntity);

    public static Optional<ICurio> getCurio(ItemStack stack);
    public static Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity livingEntity);

    public static boolean isStackValid(SlotContext slotContext, ItemStack stack);
    public static Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack);

    public static void addSlotModifier(Multimap<Holder<Attribute>, AttributeModifier> map,
            String identifier, ResourceLocation id, double amount, AttributeModifier.Operation operation);
    public static void addSlotModifier(ItemStack stack, String identifier, ResourceLocation id,
            double amount, AttributeModifier.Operation operation, String slot);
    public static ItemAttributeModifiers withSlotModifier(ItemAttributeModifiers itemAttributeModifiers,
            String identifier, ResourceLocation id, double amount,
            AttributeModifier.Operation operation, EquipmentSlotGroup slotGroup);
    public static void addModifier(ItemStack stack, Holder<Attribute> attribute, ResourceLocation id,
            double amount, AttributeModifier.Operation operation, String slot);

    public static void registerCurioPredicate(ResourceLocation resourceLocation, Predicate<SlotResult> predicate);
    public static Optional<Predicate<SlotResult>> getCurioPredicate(ResourceLocation resourceLocation);
    public static Map<ResourceLocation, Predicate<SlotResult>> getCurioPredicates();
    public static boolean testCurioPredicates(Set<ResourceLocation> predicates, SlotResult slotResult);

    public static ResourceLocation getSlotId(SlotContext slotContext);
    public static void broadcastCurioBreakEvent(SlotContext slotContext);

**已弃用（1.22 计划移除）**：`getSlot(String)`、`getSlots()`、`getEntitySlots(EntityType)`、
`getPlayerSlots()`、`getItemStackSlots(ItemStack)`、`getSlotIcon(String)`、
`setCuriosHelper/getCuriosHelper`、`setSlotHelper/getSlotHelper`、`setIconHelper/getIconHelper`。

> `CuriosApi` 方法体全是 `apiError(); return 默认值;`，真实现在
> `top.theillusivec4.curios.mixin.CuriosImplMixinHooks`，由 `MixinCuriosApi` 注入替换。

**`top.theillusivec4.curios.api.CuriosCapability`**：

    public static final ResourceLocation ID_INVENTORY    = ResourceLocation.fromNamespaceAndPath("curios", "inventory");
    public static final ResourceLocation ID_ITEM_HANDLER = ResourceLocation.fromNamespaceAndPath("curios", "item_handler");
    public static final ResourceLocation ID_ITEM         = ResourceLocation.fromNamespaceAndPath("curios", "item");

    public static final EntityCapability<ICuriosItemHandler, Void> INVENTORY =
            EntityCapability.createVoid(ID_INVENTORY, ICuriosItemHandler.class);
    public static final EntityCapability<IItemHandler, Void> ITEM_HANDLER =
            EntityCapability.createVoid(ID_ITEM_HANDLER, IItemHandler.class);
    public static final ItemCapability<ICurio, Void> ITEM =
            ItemCapability.createVoid(ID_ITEM, ICurio.class);

### 10.2 把物品注册成饰品

#### 10.2.1 方式 A（推荐）：物品 implements `ICurioItem`

**`top.theillusivec4.curios.api.type.capability.ICurioItem`**（390 行）
所有方法都有 default 实现，**空实现即可编译通过**：

    public interface ICurioItem {
        ICurio defaultInstance = () -> ItemStack.EMPTY;
        default boolean hasCurioCapability(ItemStack stack)

        default void curioTick(SlotContext slotContext, ItemStack stack)               // 每 tick，CS+SS
        default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
        default void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
        default boolean canEquip(SlotContext slotContext, ItemStack stack)
        default boolean canUnequip(SlotContext slotContext, ItemStack stack)
        default List<Component> getSlotsTooltip(List<Component> tooltips,
                Item.TooltipContext context, ItemStack stack)
        default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation id, ItemStack stack)
        default void onEquipFromUse(SlotContext slotContext, ItemStack stack)
        @Nonnull default ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack)
        default boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)       // 默认 false
        default void curioBreak(SlotContext slotContext, ItemStack stack)
        default boolean canSync(SlotContext slotContext, ItemStack stack)              // 默认 false
        @Nonnull default CompoundTag writeSyncData(SlotContext slotContext, ItemStack stack)
        default void readSyncData(SlotContext slotContext, CompoundTag compound, ItemStack stack)
        @Nonnull default DropRule getDropRule(SlotContext slotContext, DamageSource source,
                boolean recentlyHit, ItemStack stack)
        default List<Component> getAttributesTooltip(List<Component> tooltips,
                Item.TooltipContext context, ItemStack stack)
        default int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack)
        default int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext, ItemStack stack)
        default boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack)
        default boolean canWalkOnPowderedSnow(SlotContext slotContext, ItemStack stack)
        default boolean isEnderMask(SlotContext slotContext, EnderMan enderMan, ItemStack stack)
    }

#### 10.2.2 方式 B：`RegisterCapabilitiesEvent` 手工注册

Curios 自己在 `Curios.java:151-165` 对**所有物品**做了兜底注册：

    evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> {
        Item it = stack.getItem();
        ICurioItem curioItem = CuriosImplMixinHooks.getCurioFromRegistry(item).orElse(null);
        if (curioItem == null && it instanceof ICurioItem itemCurio) curioItem = itemCurio;
        if (curioItem != null && curioItem.hasCurioCapability(stack))
            return new ItemizedCurioCapability(curioItem, stack);
        return null;
    }, item);

**优先级**：`CuriosApi.registerCurio` 注册的实例优先于物品自身 implements。
Thaumaturge 就是显式 `event.registerItem(...)` 提供自定义 `ICurio`（见 §6.3）。

#### 10.2.3 低层接口 `ICurio`

**`top.theillusivec4.curios.api.type.capability.ICurio`**

    public interface ICurio {
        ItemStack getStack();                                   // 唯一抽象方法
        default void curioTick(SlotContext slotContext)
        default void onEquip(SlotContext slotContext, ItemStack prevStack)
        default void onUnequip(SlotContext slotContext, ItemStack newStack)
        default boolean canEquip(SlotContext slotContext)
        default boolean canUnequip(SlotContext slotContext)
        default Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation id)
        default void onEquipFromUse(SlotContext slotContext)
        @Nonnull default SoundInfo getEquipSound(SlotContext slotContext)
        default boolean canEquipFromUse(SlotContext slotContext)
        default void curioBreak(SlotContext slotContext)
        default boolean canSync(SlotContext slotContext)
        @Nonnull default CompoundTag writeSyncData(SlotContext slotContext)
        default void readSyncData(SlotContext slotContext, CompoundTag compound)
        @Nonnull default DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit)
        default List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context)
        default int getFortuneLevel(SlotContext slotContext, @Nullable LootContext lootContext)
        default int getLootingLevel(SlotContext slotContext, @Nullable LootContext lootContext)
        default boolean makesPiglinsNeutral(SlotContext slotContext)
        default boolean canWalkOnPowderedSnow(SlotContext slotContext)
        default boolean isEnderMask(SlotContext slotContext, EnderMan enderMan)

        enum DropRule { DEFAULT, ALWAYS_DROP, ALWAYS_KEEP, DESTROY }
        record SoundInfo(SoundEvent soundEvent, float volume, float pitch) { }
        static void playBreakAnimation(ItemStack stack, LivingEntity livingEntity)
    }

**对照**：`ICurio` 无 stack 参数；`ICurioItem` 多一个 `ItemStack stack` 末参；其余语义一致。

**关键 record**：

    package top.theillusivec4.curios.api;
    public record SlotContext(String identifier, LivingEntity entity, int index,
                              boolean cosmetic, boolean visible) {}
    public record SlotResult(SlotContext slotContext, ItemStack stack) {}

### 10.3 槽位：数据包定义

#### 槽类型定义

**路径**：`data/<namespace>/curios/slots/<slotId>.json`
（`CuriosSlotManager` 第 73 行 `super(GSON, "curios/slots")`）

| JSON 字段 | 类型 | 默认 | 含义 |
|---|---|---|---|
| `replace` | bool | `false` | 替换而非合并 |
| `order` | int | `1000` | 排序权重，越小越靠前（**1.21.1 无 `priority` 字段，已改名 `order`**） |
| `size` | int | `1` | 槽位数；<0 抛异常 |
| `operation` | `"SET"/"ADD"/"REMOVE"` | `"SET"` | 与已有 size 的合并方式 |
| `use_native_gui` | bool | `true` | 是否显示在 Curios GUI |
| `add_cosmetic` | bool | `false` | 是否附装饰槽 |
| `render_toggle` | bool | `true` | 是否允许切换渲染 |
| `icon` | ResourceLocation | `curios:slot/empty_curio_slot` | 图标 |
| `drop_rule` | `DEFAULT/ALWAYS_DROP/ALWAYS_KEEP/DESTROY` | `DEFAULT` | 死亡掉落规则 |
| `validators` | string[] | `["curios:tag"]` | 槽位有效性谓词 ID |
| `neoforge:conditions` | 条件数组 | 无 | 数据包条件 |

**合并语义**：`order` 取最小（除非 `replace:true`）；`size` 按 `operation` 合并后 `Math.max(size,0)`；
`use_native_gui`/`render_toggle` 逻辑与；`add_cosmetic` 逻辑或；`drop_rule` 后加载覆盖。

**Curios 内置 10 个槽类型**（`common/src/main/resources/data/curios/curios/slots/*.json`，**均无 `size` 字段 → 默认 1**）：

| 文件（= slotId） | order | icon |
|---|---|---|
| `curio.json` | 20 | `curios:slot/empty_curio_slot` |
| `head.json` | 40 | `curios:slot/empty_head_slot` |
| `necklace.json` | 60 | `curios:slot/empty_necklace_slot` |
| `back.json` | 80 | `curios:slot/empty_back_slot` |
| `body.json` | 100 | `curios:slot/empty_body_slot` |
| `bracelet.json` | 120 | `curios:slot/empty_bracelet_slot` |
| `hands.json` | 140 | `curios:slot/empty_hands_slot` |
| `ring.json` | 160 | `curios:slot/empty_ring_slot` |
| `belt.json` | 180 | `curios:slot/empty_belt_slot` |
| `charm.json` | 200 | `curios:slot/empty_charm_slot` |

除 `curio.json` 外其余 9 个带 `"validators": ["curios:tag"]`。`ring.json` 原文：

    { "order": 160, "icon": "curios:slot/empty_ring_slot", "validators": ["curios:tag"] }

#### 物品属于哪个槽（真正的「注册饰品」）

**路径**：`data/<namespace>/tags/item/<slotId>.json`

Curios 硬编码了 3 个内置 validator（`CuriosImplMixinHooks.java:309-324`）：

    registerCurioPredicate(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "all"),  (slotResult) -> true);
    registerCurioPredicate(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "none"), (slotResult) -> false);
    registerCurioPredicate(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "tag"), (slotResult) -> {
        String id = slotResult.slotContext().identifier();
        TagKey<Item> tag1 = ItemTags.create(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, id));
        TagKey<Item> tag2 = ItemTags.create(ResourceLocation.fromNamespaceAndPath(CuriosApi.MODID, "curio"));
        ItemStack stack = slotResult.stack();
        return stack.is(tag1) || stack.is(tag2);
    });

→ **标签 ID 固定是 `curios:<slotId>` / `curios:curio`，目录 namespace 必须是 `curios`。**

实测样例（`neoforge/src/test/resources/data/curios/tags/item/ring.json`）：

    { "replace": false, "values": ["curiostest:ring", "minecraft:leather_helmet"] }

**兜底规则**（`CuriosImplMixinHooks.isStackValid`，第 135-179 行）：
1. 该 stack 在当前实体上可用槽集合非空 → `id.equals("curio") || slots.contains(id) || slots.contains("curio")`
2. 否则若目标是 `"curio"` → 依次检查 ①带任意 `curios` 命名空间 tag ②遍历所有槽 validators ③`CuriosApi.getCurio(stack).isPresent()`
3. 否则 `false`

#### 哪些实体拥有哪些槽

**路径**：`data/<namespace>/curios/entities/<任意名>.json`
（`CuriosEntityManager` 第 68 行 `super(GSON, "curios/entities")`）

| 字段 | 类型 | 含义 |
|---|---|---|
| `replace` | bool | 替换该实体现有槽集合（默认 false = 合并） |
| `entities` | string[] | 实体 ID；支持 `#namespace:tag` 实体标签 |
| `slots` | string[] | 槽位 ID，须已注册，否则只打 error 日志并跳过 |
| `neoforge:conditions` | 条件数组 | 数据包条件 |

**实测核对**：Curios 成品 jar 的 `data/` 下**只有** `data/curios/curios/slots/*.json`，
**没有任何 `data/curios/curios/entities/*.json`**。
「玩家默认槽位」由 Curios 附带的默认数据包（不在本仓库）或整合包提供。
**不要假设，自己写 entities 数据。**

### 10.4 槽位标识符与 tag 常量

**没有 `SlotType` 枚举常量类。** 槽标识符就是纯字符串
（`"ring"`、`"necklace"`、`"belt"`、`"charm"`、`"head"`、`"back"`、`"body"`、`"bracelet"`、`"hands"`、`"curio"`），
由 datapack 键名派生。

**`top.theillusivec4.curios.api.CuriosTags`**：

    public static final TagKey<Item> BACK     = createItemTag("back");
    public static final TagKey<Item> BELT     = createItemTag("belt");
    public static final TagKey<Item> BODY     = createItemTag("body");
    public static final TagKey<Item> BRACELET = createItemTag("bracelet");
    public static final TagKey<Item> CHARM    = createItemTag("charm");
    public static final TagKey<Item> CURIO    = createItemTag("curio");
    public static final TagKey<Item> HANDS    = createItemTag("hands");
    public static final TagKey<Item> HEAD     = createItemTag("head");
    public static final TagKey<Item> NECKLACE = createItemTag("necklace");
    public static final TagKey<Item> RING     = createItemTag("ring");

    public static TagKey<Item> createItemTag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", id));
    }

**`top.theillusivec4.curios.api.type.ISlotType`**：

    public interface ISlotType extends Comparable<ISlotType> {
        String getIdentifier();
        ResourceLocation getIcon();
        int getOrder();
        int getSize();
        boolean useNativeGui();
        boolean hasCosmetic();
        boolean canToggleRendering();
        ICurio.DropRule getDropRule();
        default Set<ResourceLocation> getValidators();
        default CompoundTag writeNbt();
        @Deprecated(forRemoval = true) default boolean isLocked();
        @Deprecated(since="1.20.1", forRemoval=true) default int getPriority();
        @Deprecated(since="1.20.1", forRemoval=true) default boolean isVisible();
    }

**`SlotTypePreset`（已弃用，forRemoval 1.22）**：

    HEAD("head",40) NECKLACE("necklace",60) BACK("back",80) BODY("body",100)
    BRACELET("bracelet",120) HANDS("hands",140) RING("ring",160) BELT("belt",180)
    CHARM("charm",200) CURIO("curio",20)

### 10.5 遍历玩家身上所有饰品

**`top.theillusivec4.curios.api.type.capability.ICuriosItemHandler`**（实现 `CurioInventoryCapability`）

    Map<String, ICurioStacksHandler> getCurios();
    void setCurios(Map<String, ICurioStacksHandler> map);
    int getSlots();
    default int getVisibleSlots();
    void reset();
    Optional<ICurioStacksHandler> getStacksHandler(String identifier);
    IItemHandlerModifiable getEquippedCurios();
    void setEquippedCurio(String identifier, int index, ItemStack stack);

    default boolean isEquipped(Item item);
    default boolean isEquipped(Predicate<ItemStack> filter);
    default boolean isSlotActive(String identifier, int index);
    default void setSlotActive(String identifier, int index, boolean active);
    default void setSlotsActive(String identifier, boolean active);

    Optional<SlotResult> findFirstCurio(Item item);
    Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter);
    Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter, String cacheKey);
    default Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter,
            boolean includeInactive, String cacheKey);

    List<SlotResult> findCurios(Item item);
    List<SlotResult> findCurios(Predicate<ItemStack> filter);
    default List<SlotResult> findCurios(Predicate<ItemStack> filter,
            boolean includeInactive, String cacheKey);
    List<SlotResult> findCurios(String... identifiers);
    default List<SlotResult> findCurios(boolean includeInactive, String... identifiers);

    Optional<SlotResult> findCurio(String identifier, int index);
    default Optional<SlotResult> findCurio(String identifier, int index, boolean includeInactive);

    LivingEntity getWearer();
    int getFortuneLevel(@Nullable LootContext lootContext);
    int getLootingLevel(@Nullable LootContext lootContext);
    ListTag saveInventory(boolean clear);
    void loadInventory(ListTag data);

    default void addTransientSlotModifier(String slot, ResourceLocation id,
            double amount, AttributeModifier.Operation operation);
    default void addPermanentSlotModifier(String slot, ResourceLocation id,
            double amount, AttributeModifier.Operation operation);
    default void removeSlotModifier(String slot, ResourceLocation id);
    void clearSlotModifiers();
    Multimap<String, AttributeModifier> getModifiers();
    void clearCachedSlotModifiers();

> **缓存陷阱**：只有 cacheKey 非空时才查缓存（`CurioInventoryCapability` 第 167、227 行
> `if (!cacheKey.isEmpty())`），缓存按 `gameTime` 计。`findFirstCurio(Predicate)` 传的是 `""`（不缓存）。

**`top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler`**：

    IDynamicStackHandler getStacks();
    IDynamicStackHandler getCosmeticStacks();
    NonNullList<Boolean> getRenders();
    default NonNullList<Boolean> getActiveStates();
    default boolean canToggleRendering();
    default ICurio.DropRule getDropRule();
    int getSlots();
    boolean isVisible();
    boolean hasCosmetic();
    String getIdentifier();
    Map<ResourceLocation, AttributeModifier> getModifiers();

**`ICurio` → `IDynamicStackHandler`**（`extends IItemHandlerModifiable`）：

    void setStackInSlot(int slot, @Nonnull ItemStack stack);
    @Nonnull ItemStack getStackInSlot(int slot);
    void setPreviousStackInSlot(int slot, @Nonnull ItemStack stack);
    ItemStack getPreviousStackInSlot(int slot);
    int getSlots();

**名称澄清**：`getCuriosHandler` **已不在 `CuriosApi` 上**，只存在于已整体弃用的
`top.theillusivec4.curios.api.type.util.ICuriosHelper`（`@Deprecated(since="1.20.1", forRemoval=true)`），
功能已被 `CuriosApi.getCuriosInventory(LivingEntity)` 取代。

**最小遍历示例**：

    public static void forEachCurio(LivingEntity entity, BiConsumer<SlotContext, ItemStack> action) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isEmpty()) return;
        ICuriosItemHandler handler = opt.get();

        for (Map.Entry<String, ICurioStacksHandler> e : handler.getCurios().entrySet()) {
            String slotId = e.getKey();                       // "ring" / "necklace" / ...
            ICurioStacksHandler stacksHandler = e.getValue();
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            var renders = stacksHandler.getRenders();
            var actives = stacksHandler.getActiveStates();

            for (int i = 0; i < stacks.getSlots(); i++) {
                if (i < actives.size() && !actives.get(i)) continue;   // 跳过被禁用槽
                ItemStack stack = stacks.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                SlotContext ctx = new SlotContext(slotId, entity, i, false,
                        i < renders.size() && renders.get(i));
                action.accept(ctx, stack);
            }
        }
    }

**Thaumaturge 自己的遍历写法**（`ThaumaturgeCuriosCompat.java:136-155`，更简洁）：

    public static List<ItemStack> equippedCurios(LivingEntity entity) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) return List.of();
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        List<ItemStack> stacks = new ArrayList<>(equipped.getSlots());
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        return stacks;
    }

    public static boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        return invOpt.isPresent() && invOpt.get().findFirstCurio(predicate).isPresent();
    }

### 10.6 事件与回调

#### 物品自身回调的触发时机（`CuriosEventHandler.java:610-745`，`EntityTickEvent.Post`）

- **`curioTick`**：每实体 tick，仅当该槽 active 且 stack 非空；之前会跑 `stack.inventoryTick(...)`。
- 变化检测：`ItemStack.matches(stack, getPreviousStackInSlot(i))` 不同才触发。
- **`onUnequip`**：`!prevStack.isEmpty()` 时对 **prevCurio** 调用。
- **`onEquip`**：`!stack.isEmpty()` 时对 **currentCurio** 调用。

#### NeoForge 事件（全部走 `NeoForge.EVENT_BUS`，包 `top.theillusivec4.curios.api.event`）

**(1) `CurioChangeEvent`（最常用，不可取消）**

    public class CurioChangeEvent extends LivingEvent {
        public CurioChangeEvent(LivingEntity living, String type, int index,
                                @Nonnull ItemStack from, @Nonnull ItemStack to)
        public String getIdentifier();
        public int getSlotIndex();
        @Nonnull public ItemStack getFrom();
        @Nonnull public ItemStack getTo();
        public LivingEntity getEntity();
    }

**(2) `CurioCanEquipEvent` / `CurioCanUnequipEvent`（可拦截，TriState）**

    public class CurioCanEquipEvent extends LivingEvent {
        public CurioCanEquipEvent(ItemStack stack, SlotContext slotContext, TriState result)
        public TriState getEquipResult();
        public void setEquipResult(TriState result);
        public SlotContext getSlotContext();
        public ItemStack getStack();
    }
    public class CurioCanUnequipEvent extends LivingEvent {
        public CurioCanUnequipEvent(ItemStack stack, SlotContext slotContext)
        public TriState getUnequipResult();
        public void setUnequipResult(TriState result);
        public SlotContext getSlotContext();
        public ItemStack getStack();
    }

**(3) `CurioAttributeModifierEvent`** —— Thaumaturge 用它注入 vis 折扣（见 §6.3）

    public class CurioAttributeModifierEvent extends Event {
        public CurioAttributeModifierEvent(ItemStack stack, SlotContext slotContext, ResourceLocation id,
                                           Multimap<Holder<Attribute>, AttributeModifier> modifiers)
        public Multimap<Holder<Attribute>, AttributeModifier> getModifiers();
        public Multimap<Holder<Attribute>, AttributeModifier> getOriginalModifiers();
        public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier);
        public boolean removeModifier(Holder<Attribute> attribute, AttributeModifier modifier);
        public Collection<AttributeModifier> removeAttribute(Holder<Attribute> attribute);
        public void clearModifiers();
        public SlotContext getSlotContext();
        public ItemStack getItemStack();
        public ResourceLocation getId();
    }

**(4) `CurioDropsEvent`（可取消）** / **(5) `DropRulesEvent`** / **(6) `SlotModifiersUpdatedEvent`**
**(7) Mod 总线 `RegisterCuriosExtensionsEvent` + `ICurioSlotExtension`**

#### 最小 tick + 装备事件示例

    public class RegenerationRingItem extends Item implements ICurioItem {
        public RegenerationRingItem() { super(new Item.Properties().stacksTo(1)); }

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            LivingEntity wearer = slotContext.entity();
            if (!wearer.level().isClientSide() && wearer.tickCount % 40 == 0) {
                wearer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true));
            }
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) { }

        @Override
        public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) { }
    }

    public final class CurioEvents {
        public static void init() { NeoForge.EVENT_BUS.addListener(CurioEvents::onCurioChange); }
        private static void onCurioChange(CurioChangeEvent evt) {
            String slotId = evt.getIdentifier();
            int index = evt.getSlotIndex();
            ItemStack from = evt.getFrom();
            ItemStack to = evt.getTo();
            var entity = evt.getEntity();
        }
    }

### 10.7 属性修饰符

- `ICurio#getAttributeModifiers(SlotContext, ResourceLocation id)` 返回 `Multimap<Holder<Attribute>, AttributeModifier>`。
  `id` 由 `CuriosApi.getSlotId(slotContext)` 生成：`curios:<slotId><index>`（例 `curios:ring0`）。
- `CuriosApi.addSlotModifier(atts, "ring", id, 1, AttributeModifier.Operation.ADD_VALUE)` 用于**增删槽位数量**
  （内部走 `SlotAttribute.getOrCreate(identifier)`）。
- **1.21 新增数据组件式修饰符**：`curios:attribute_modifiers`，
  类型 `top.theillusivec4.curios.api.CurioAttributeModifiers`。
  **优先级**：若该组件非空，则**完全忽略** `ICurio#getAttributeModifiers`。

### 10.8 附属（Forgotten Relics）需要写的三份 JSON

    /* 1) src/main/resources/data/curios/tags/item/charm.json —— 物品进 charm 槽 */
    { "replace": false, "values": ["forgotten_relics:mining_charm"] }

    /* 2) src/main/resources/data/forgotten_relics/curios/entities/player.json —— 确保玩家有槽 */
    {
      "entities": ["player"],
      "slots": ["head", "necklace", "ring", "belt", "charm"]
    }

    /* 3) （可选）自定义槽位 src/main/resources/data/forgotten_relics/curios/slots/sigil.json */
    { "order": 300, "size": 2, "icon": "forgotten_relics:slot/empty_sigil_slot", "validators": ["curios:tag"] }

> 现有工程已经有一份 `src/main/resources/data/curios/tags/item/charm.json`。

### 10.9 源码文件清单（Curios，本次实际读过）

`CuriosApi.java`(524)、`CuriosCapability.java`、`CuriosDataProvider.java`、`CurioAttributeModifiers.java`、
`SlotAttribute.java`、`ICuriosItemHandler.java`(448)、`ICurioStacksHandler.java`(255)、`IDynamicStackHandler.java`(97)、
`ISlotData.java`、`IEntitiesData.java`、`ICuriosHelper.java`(179)、`ISlotHelper.java`(165)、
`CurioChangeEvent.java`、`CurioAttributeModifierEvent.java`、`CurioCanEquipEvent.java`、`CurioCanUnequipEvent.java`、
`CurioDropsEvent.java`、`DropRulesEvent.java`、`SlotModifiersUpdatedEvent.java`、`ICurioSlotExtension.java`、
`RegisterCuriosExtensionsEvent.java`、`ICurio.java`(435)、`ICurioItem.java`(390)、`ISlotType.java`(108)、
`SlotContext.java`、`SlotResult.java`、`SlotPredicate.java`、`SlotTypePreset.java`、`SlotTypeMessage.java`、
`CuriosTags.java`(73)、`CuriosTriggers.java`、`ICurioRenderer.java`、`CuriosRendererRegistry.java`、
`SlotType.java`(360)、`CuriosConstants.java`、`Curios.java`(258)、`CuriosImplMixinHooks.java`(325)、
`MixinCuriosApi.java`(175)、`SlotData.java`(183)、`EntitiesData.java`(100)、`CuriosSlotManager.java`(381)、
`CuriosEntityManager.java`(244)、`CurioInventoryCapability.java`(709)、`CurioInventory.java`(245)、
`DynamicStackHandler.java`(118)、`CuriosEventHandler.java`(832，读 1-130/200-360/600-745)、
`LegacySlotManager.java`、`CuriosConfig.java`、`CuriosRegistry.java`、
`common/src/main/resources/data/curios/curios/slots/{10 个}.json`、
`neoforge/src/test/resources/data/curiostest/{curios/slots,curios/entities,tags/item}/*.json`、
`neoforge/src/test/java/.../CuriosTest.java`(308)、`CuriosTestProvider.java`、
`common/item/{RingItem,AmuletItem,CrownItem,KnucklesItem}.java`、`gradle.properties`、
`neoforge/src/main/resources/META-INF/neoforge.mods.toml`、
以及成品 jar `Forgotten-Relics-Unofficial/libs/curios-neoforge-9.5.1+1.21.1.jar`（270 个 zip 条目全量列举）。

---

## 11. Botania 依赖的替代

### 11.1 先明确：1.12.2 原版到底依赖 Botania 的什么

对 `Forgotten-Relics-RE-main`（1.12.2 反编译源码）做全量 import 扫描，得到：

**Botania import 清单（分文件计数）**

| import | 次数 |
|---|---|
| `vazkii.botania.common.Botania` | 20 |
| `vazkii.botania.common.core.helper.Vector3` | 23 |
| `vazkii.botania.common.core.helper.ItemNBTHelper` | 13 |
| `vazkii.botania.common.item.ModItems` | 2 |
| `vazkii.botania.common.entity.EntityDoppleganger` | 4 |
| `vazkii.botania.client.core.handler.MiscellaneousIcons` | 1 |
| `vazkii.botania.client.core.helper.IconHelper` / `RenderHelper` / `ShaderHelper` | 各 1 |
| `vazkii.botania.client.lib.LibResources` | 1 |
| `vazkii.botania.client.render.IModelRegister` | 1 |
| `vazkii.botania.api.item.ICosmeticAttachable` / `IPhantomInkable` | 各 1 |
| `vazkii.botania.api.mana.ManaItemHandler` | 1 |
| `vazkii.botania.common.block.subtile.functional.SubTileHeiseiDream` | 1 |
| `vazkii.botania.common.core.BotaniaCreativeTab` | 1 |
| `vazkii.botania.common.core.handler.ModSounds` | 2 |
| `vazkii.botania.common.core.helper.ExperienceHelper` | 1 |
| `vazkii.botania.common.core.helper.PlayerHelper` | 1 |

**实际用到的具体功能（grep 命中行）**：

| 用途 | 1.12.2 调用点 | 说明 |
|---|---|---|
| **Mana 消耗** | `ItemTerrorCrown.java:135`：`ManaItemHandler.requestManaExact(itemstack, player, terrorCrownManaCost, true)` | **唯一真正的 mana 逻辑** |
| **NBT 读写** | `ItemNBTHelper.setInt/getCompound/setCompound/getLong/setLong`（105 处） | 纯粹是 NBT 工具，不是 Botania 特性 |
| **向量数学** | `new Vector3(...)`、`Vector3.fromEntity`、`fromEntityCenter`、`.multiply`、`.add`（113 处） | 纯数学 |
| **特效** | `Botania.proxy.wispFX(...)`、`Botania.proxy.sparkleFX(...)`（44 处） | 客户端粒子 |
| **Gaia 判定** | `EntityDoppleganger.isTruePlayer(player)`（`ItemBaubleBase.java:110`）；`entity instanceof EntityDoppleganger`（`RelicsEventHandler.java:324`、`SuperpositionHandler.java:166`） | 「盖亚分身不算真玩家」+「Boss 免疫某些效果」 |
| **扫描注册** | `Main.java:164`：`new ScanItem("!EternalLifeEssence", new ItemStack(ModItems.manaResource, 1, 5))`；`:166` `manaResource:8`（PixieDust）；`:167` Dragonstone；`:203` `new ScanEntity("!GaiaGuardian", EntityDoppleganger.class, true)` | **把 Botania 物品/实体注册为可扫描** |
| **音效** | `ModSounds.starcaller` 等 | 音效引用 |
| **经验辅助** | `ExperienceHelper` | 经验操作工具 |
| **玩家辅助** | `PlayerHelper` | 玩家工具 |
| **酿造/花** | `SubTileHeiseiDream` | 功能花联动 |
| **创造标签** | `BotaniaCreativeTab` | 把物品放进 Botania 标签页 |
| **模型/图标** | `IModelRegister`、`IconHelper`、`MiscellaneousIcons`、`LibResources`、`RenderHelper`、`ShaderHelper` | 客户端渲染 |

### 11.2 Botania 1.21.1 现状（**已解包字节码核实**）

工作区**没有 Botania 源码**；解包的是
`E:/Modpacks/versions/Thaumcraft 1.21.1/mods/botania-neoforge-1.21.1-457-*.jar`。
`LibItemNames.class` / `LibBlockNames.class`（常量名与常量值成对）是注册名的权威来源。

**核心结论**：清单上点名要查的东西**没有一个被真正移除**，
但 **1.20.5/1.21 的注册名大清洗**让**超过一半的经典注册名改了名字**。

#### 11.2.1 Mana（魔力）

| 内容 | 1.21.1 是否还在 | 1.21.1 注册名 | 证据 |
|---|---|---|---|
| Mana Pool | ✅ **改名** | `botania:mana_pool` | `LibBlockNames`、`blockstates/mana_pool.json` |
| Mana Spreader | ✅ 同名 | `botania:mana_spreader` | `LibBlockNames` |
| Mana Tablet | ✅ 同名 | `botania:mana_tablet` | `LibItemNames` |
| Mana Ring | ✅ **改名** | `botania:band_of_mana` | `LibItemNames`（旧名 `mana_ring` 在 class 中 0 命中） |
| Mana Pearl | ✅ 同名 | `botania:mana_pearl` | `LibItemNames` |
| **Terrasteel Ingot** | ✅ **同名** | `botania:terrasteel_ingot` | `LibItemNames` |
| **Terrasteel Nugget** | ✅ **同名** | `botania:terrasteel_nugget` | `LibItemNames` |

其它确认存在的 Mana 系：`mana_diamond`、`manasteel_ingot`、`manasteel_nugget`、`mana_powder`、
`mana_string`、`mana_in_a_bottle`、`mana_quartz`、`mana_pylon`、`gaia_pylon`、`natura_pylon`、
`pulse_mana_spreader`(旧 redstone_spreader)、`elven_mana_spreader`(旧 elven_spreader)、
`gaia_mana_spreader`(旧 gaia_spreader)、`fabulous_mana_pool`/`diluted_mana_pool`/`creative_mana_pool`、
`mana_spark` / `corporea_spark` / `master_corporea_spark` / `spark_augment_*`。

**terrasteel 的制作方式（已确认到配方 JSON）**：
泰拉凝聚板 `botania:terrestrial_agglomeration_plate` **未改名**，
配方 `data/botania/recipe/terrestrial_agglomeration_plate/terrasteel_ingot.json`：
投入 `c:ingots/manasteel` + `c:gems/mana_pearl` + `c:gems/mana_diamond`，**mana: 500000**，产出 1 个 `terrasteel_ingot`。

#### 11.2.2 Gaia（盖亚）

| 内容 | 1.21.1 注册名 | 证据 |
|---|---|---|
| **Gaia Guardian（实体）** | `botania:gaia_guardian` | `loot_table/entities/gaia_guardian.json`、en_us lang |
| **Gaia Spirit** | ✅ 同名 `botania:gaia_spirit` | `LibItemNames`、`models/item/gaia_spirit.json` |
| Gaia Head | ✅ **改名** | 方块 `botania:gaia_head`（+ `gaia_wall_head`）；旧物品名 `gaiahead` 0 命中 |
| Gaia Pylon | ✅ 同名 | `botania:gaia_pylon` |
| Gaia Ingot | ✅ | `botania:gaia_ingot`（配方 = `c:ingots/terrasteel` ×1 + `gaia_spirit` ×4） |

**召唤方式（已确认）**：需要**激活的信标** + 若干 `botania:gaia_pylon`（比信标高 1 格），
**手持 `botania:terrasteel_ingot` shift 右击信标**启动仪式。
**Hardmode（Gaia II）**：制作 `botania:gaia_ingot` 后同样的信标仪式。
> ⚠️ Hardmode 的**触发动作**只从词典文本推断（"制作盖亚魂锭"），
> 具体是不是「手持 gaia_ingot 右击信标」= **待确认**。

**掉落物（逐条来自 loot table JSON）**：
- 普通（`gaia_guardian/reward.json`）：`gaia_spirit` ×6（带 `botania:true_guardian_killer` 时 ×8）；
  20% 掉 `scathed_music_disc_1`。
- Gaia II（`gaia_guardian/reward_hard.json`）：`gaia_spirit` ×10（最高 ×16）；50% 莲花表；
  100% `botania:ancient_wills` tag；100% 材料表；1–6 次符文表；10% `the_pinkinator`；
  44% 三选一唱片；`botania:enable_relics` 时 `dice_of_fate`。
- **Gaia Head**：只在用 **`botania:elementium_axe`** 击杀时判定，基础 **7.69%**，每级抢夺 +7.69%。

#### 11.2.3 Alfheim（亚尔夫海姆）

| 内容 | 1.21.1 注册名 | 证据 |
|---|---|---|
| **精灵门核心（旧 Alfheim Portal 的玩家放置方块）** | `botania:elven_gateway_core` | `LibBlockNames`、`recipe/elven_gateway_core.json` |
| 传送门面方块（内部） | `botania:alfheim_portal`（**语义已不同**，无 blockstates / loot table / lang key） | `BotaniaBlockEntities.class`、`AlfheimPortalBlock.class` 字符串常量 |
| **Natura Pylon** | ✅ 同名 `botania:natura_pylon` | `LibBlockNames` |
| Dreamwood | ✅ 同名 `botania:dreamwood`（+ `dreamwood_twig`） | `LibBlockNames`、`LibItemNames` |
| **Elementium Ingot / Nugget** | ✅ **同名** `botania:elementium_ingot` / `elementium_nugget` | `LibItemNames` |
| Elven Quartz | ✅ **改名** `botania:elven_quartz`（旧 `quartz_elven`） | `LibItemNames` |
| **Pixie Dust** | ✅ 同名 `botania:pixie_dust` | `LibItemNames` |
| Dragonstone | ✅ 同名 `botania:dragonstone` | `LibItemNames` |
| Alfglass / Pane | ✅ 同名 `botania:alfglass` / `alfglass_pane` | `LibBlockNames` |

**Alfheim 交易（`data/botania/recipe/elven_trade/*.json`，`type: "botania:elven_trade"`）**：

| 产物 | 投入 |
|---|---|
| `elementium_ingot` | `c:ingots/manasteel` ×2 |
| `pixie_dust` | `c:gems/mana_pearl` ×1 |
| `dragonstone` | `c:gems/mana_diamond` ×1 |
| `dreamwood` / `_log` | 活木 |
| `alfglass` | 见文件 |

**传送门搭建**：8 活木 + 3 微光活木 + 1 精灵门核心 + 2 魔力池 + 2 自然水晶，11×11 内，法杖右击核心开门。

#### 11.2.4 Elementium 全系（**注册名与 1.12.2 完全一致**）

`elementium_ingot` / `elementium_nugget` / `elementium_block` /
`elementium_helmet` / `_chestplate` / `_leggings` / `_boots` /
`elementium_pickaxe` / `_shovel` / `_axe` / `_hoe` / `_sword` / `_shears`

#### 11.2.5 其它常用物品

| 内容 | 1.21.1 注册名 | 备注 |
|---|---|---|
| Great Fairy Ring | `botania:great_fairy_ring` | **改名**（1.19.2 为 `pixie_ring`） |
| Mana Mirror | `botania:mana_mirror` | ✅ |
| Corporea 全系 | `corporea_index`/`_funnel`/`_interceptor`/`_crystal_cube`/`_retainer`/`_block`/`corporea_bricks`/`corporea_spark`/… | ✅ 全在 |
| Livingwood / Livingrock | `livingwood`/`livingwood_log`/`livingwood_planks`/`livingwood_twig`/`livingroot`/`livingrock`/… | ✅ 同名 |
| Runic Altar | `botania:runic_altar` | ✅ |
| Botanical Brewery | `botania:botanical_brewery` | **改名**（1.19.2 为 `brewery`） |
| Fel Pumpkin | `botania:fel_pumpkin` | ✅ |
| Spawner Claw | `botania:life_imbuer` | **改名**（旧名 `spawner_claw`） |
| Spawner Mover | `botania:life_aggregator` | **改名** |
| Red String | `botania:red_string` + 若干 `red_stringed_*` 方块 | ✅ |
| Lens 全系（25 个） | `mana_lens`(旧 lens_normal)、`velocity_lens`、`potency_lens`、`resistance_lens`、`efficiency_lens`、`bounce_lens`、`gravity_lens`、`bore_lens`、`damaging_lens`、`phantom_lens`、`magnetizing_lens`、`entropic_lens`、`influence_lens`、`weight_lens`、`paintslinger_lens`、`kindle_lens`、`force_lens`、`flash_lens`、`warp_lens`、`redirective_lens`、`celebratory_lens`、`flare_lens`、`messenger_lens`、`tripwire_lens`、`storm_lens` + `lens_clip`、`composite_lens` | **全部改名** |

#### 11.2.6 `manaResource`（1.12.2 多变体物品）

⚠️ **这是最容易被忽略的破坏性变化**（但见 §11.4 置信度说明）：
1.12.2 的 `botania:manaResource` + metadata 在 1.21.1 **拆成 20+ 个独立物品**，
`manaResource` 这个注册名在 1.21.1 字节码中 **0 命中**：

| 1.12.2 metadata | 1.21.1 独立注册名 |
|---|---|
| 0 Manasteel Ingot | `botania:manasteel_ingot` |
| 1 Mana Diamond | `botania:mana_diamond` |
| 2 Mana Powder | `botania:mana_powder` |
| 5 Gaia Spirit | `botania:gaia_spirit` |
| 6 Manasteel Nugget | `botania:manasteel_nugget` |
| 7 Terrasteel Nugget | `botania:terrasteel_nugget` |
| 8 Elementium Nugget | `botania:elementium_nugget` |
| 9 Mana String | `botania:mana_string` |
| 16 Livingwood Twig | `botania:livingwood_twig` |
| 17 Dreamwood Twig | `botania:dreamwood_twig` |
| 18 Living Root | `botania:living_root` |
| 19 Pebble | `botania:pebble` |
| 20 Redstone Root | `botania:redstone_root` |

> **1.12.2 的 metadata 编号本身 = 待确认**（无 1.12.2 jar，见 §11.4）。
> 但「1.21.1 侧它们是各自独立的注册名」**是 100% 确证的**。

### 11.3 **1.12.2 有、但 1.21.1 Botania 里找不到对应物** 清单

严格按「注册名 + 语义」在 1.21.1 的 class 字符串 / lang / 模型 / blockstates / recipe 搜过：

1. **`botania:fabulousManaRing` / Fabulous Mana Ring（神话魔力之戒）** —— 1.21.1 **无任何 fabulous*ring**。
   "Fabulous" 只出现在 **魔力池**上（`botania:fabulous_mana_pool` + 16 染色版）。
   最接近的替代：**`botania:greater_band_of_mana`（高级魔力之戒）**，但是升级版而非 Fabulous 版，
   容量是否等价 **待确认**。
2. **`botania:manaResource`（1.12.2 多 variant 物品）** —— 注册名本身消失，功能拆成独立物品（见 §11.2.6）。
3. **`botania:spawner_claw` 这个旧注册名** —— 物品仍在（`botania:life_imbuer`），旧名找不到。
4. **`botania:spawner_mover` 这个旧注册名** —— 物品仍在（`botania:life_aggregator`），旧名找不到。
5. **所有旧 `*_ring` 注册名**（`mana_ring`、`mana_ring_greater`、`aura_ring` 等）——
   功能全在，名字全换成描述性名称：
   `band_of_mana`、`greater_band_of_mana`、`band_of_aura`、`ring_of_magnetization`、
   `ring_of_chordata`、`ring_of_the_mantle`、`ring_of_far_reach`、`ring_of_correction`、
   `ring_of_thor`、`ring_of_odin`、`ring_of_loki`、`greater_ring_of_magnetization`、
   `ring_of_dexterous_motion`。
6. **所有旧 `lens_xxx` 注册名（25 个）** —— 功能全在，名字全换（见 §11.2.5）。
7. **旧 `botania:alfheim_portal` 的语义** —— 1.21.1 里 **核心方块已改成 `elven_gateway_core`**；
   `alfheim_portal` 这个注册名**仍被内部传送门面方块占用**，语义不同。
   **附属若直接给 `botania:alfheim_portal` 摆造型，会摆出一个传送门面而不是核心。**
8. **`botania:gaiahead`（1.12.2/1.19.2 的物品形态）** —— 已变成方块 `botania:gaia_head`；
   物品注册名 `gaiahead` 在 1.21.1 class 中 **0 命中**。
9. **其它旧拼写注册名全部作废**：`brewery`、`diluted_pool`、`fabulous_pool`、`creative_pool`、
   `redstone_spreader`、`elven_spreader`、`gaia_spreader`、`quartz_elven`、`mana_gun`、
   `mana_cookie`、`manasteel_pick`、`manasteel_shovel`。

### 11.4 ⚠️ 本节置信度说明（务必读）

Botania 调研**没有源码可用**，全部靠解包字节码的字符串常量 + lang + 模型 + 配方。
「1.21.1 注册名」这一列是 **100% 确证的**（`LibItemNames` / `LibBlockNames` 常量值 +
lang + `models/item/*.json` + `blockstates/*.json` + recipe JSON 多重交叉）。

但**「1.12.2 原来叫什么」这一列大部分没有直接证据**：
- 本机**没有 1.12.2 的 Botania jar**，工作区也没有 Botania 源码；
- **本机无外网**，拿不到 1.12.2 的 `LibItemNames.java`；
- 对照用的是 **1.19.2 jar**，它只能证明「改名的方向和时间点（1.19.2 → 1.21.1）」，
  **不能证明 1.12.2 就叫那个名字**。

**因此**：
- §11.3 里第 3、4、5、6、8、9 项（旧注册名找不到）**有 1.19.2 直接证据**，可信；
- §11.2.6 的「1.12.2 metadata 编号」与「`botania:manaResource`」这一拼写 **属推测，请标「待确认」**，
  不要拿它当迁移基准；
- §11.2.2 里 Gaia II 的**触发动作** = **待确认**；
- `botania:alfheim_portal`（传送门面方块）**只在字节码字符串常量里出现**，
  没有 blockstates / loot table / lang key，「它被注册了」是强证据，
  「它的确切注册名就是 alfheim_portal」属**推论**。

**给下游的实用建议**：

- **可以直接用（不改）**：`mana_tablet`、`mana_pearl`、`mana_diamond`、`manasteel_ingot/nugget`、
  `terrasteel_ingot`、`terrasteel_nugget`、`elementium_ingot/nugget` + 全套 elementium 工具盔甲、
  `gaia_spirit`、`gaia_ingot`、`gaia_guardian`（实体）、`mana_pylon`/`natura_pylon`/`gaia_pylon`、
  `dreamwood`、`pixie_dust`、`dragonstone`、`runic_altar`、`fel_pumpkin`、`red_string`、
  `mana_mirror`、整套 corporea 与 livingwood/livingrock。
- **必须改**：`mana_pool`、`mana_spreader`、`band_of_mana`、`greater_band_of_mana`、
  `great_fairy_ring`、`life_imbuer`、`life_aggregator`、`botanical_brewery`、
  `elven_gateway_core`、`gaia_head`、`elven_quartz`、`mana_lens` 等。
- **绝对不要用**：`botania:manaResource`、任何 `lens_xxx` 旧名、任何旧 `*_ring`、
  `spawner_claw`、`spawner_mover`、`gaiahead`、`brewery`、`diluted_pool`、`creative_pool`、
  `fabulous_pool`、`redstone_spreader`、`elven_spreader`、`gaia_spreader`、`quartz_elven`、
  `mana_gun`、`mana_cookie`。

### 11.5 1.12.2 用到的 Botania **API** 类在 1.21.1 的存在性（**已逐个实测**）

用文件存在性在解包目录 `build/botania-jar/` 逐个 `Test-Path`：

| 1.12.2 类 | 1.21.1 是否存在 |
|---|---|
| `vazkii.botania.api.mana.ManaItemHandler` | ✅ **存在** |
| `vazkii.botania.api.item.IPhantomInkable` | ❌ 不存在（方块侧改为 `vazkii.botania.api.block.PhantomInkableBlock`） |
| `vazkii.botania.api.item.ICosmeticAttachable` | ❌ 不存在 |
| `vazkii.botania.common.core.helper.ItemNBTHelper` | ❌ 不存在 |
| `vazkii.botania.common.core.helper.Vector3` | ❌ 不存在 |
| `vazkii.botania.common.entity.EntityDoppleganger` | ❌ 不存在 |
| `vazkii.botania.common.item.ModItems` | ❌ 不存在（改为 `vazkii.botania.common.item.BotaniaItems`） |
| `vazkii.botania.common.core.handler.ModSounds` | ❌ 不存在 |
| `vazkii.botania.common.core.helper.ExperienceHelper` | ❌ 不存在 |
| `vazkii.botania.common.core.helper.PlayerHelper` | ❌ 不存在 |
| `vazkii.botania.common.block.subtile.functional.SubTileHeiseiDream` | ❌ 不存在 |
| `vazkii.botania.common.item.BotaniaItems` | ✅ 存在（替代 `ModItems`） |
| `vazkii.botania.common.lib.LibItemNames` / `LibBlockNames` | ✅ 存在 |

**1.21.1 Botania 的 API 包已完全重构**（`vazkii.botania.api.*`）：
新增/现行子包 `api.block`、`api.block_entity`、`api.capability`、`api.crafting`、`api.recipe`、
`api.brew`、`api.attachment`、`api.configdata`、`api.state`、
`api.neoforge.recipe.ElvenPortalUpdateEvent` 等；
ManaItemHandler` 仍在 `vazkii.botania.api.mana`。

### 11.6 对 Forgotten Relics 迁移的直接结论

1.12.2 用 Botania 的 **15 个类**里，**只有 `ManaItemHandler` 一个 API 类在 1.21.1 还存在**，
其余全部消失。但**消失的那些绝大多数是工具类**（NBT / 向量 / 音效 / 经验 / 粒子代理），
1.21.1 有原版或 NeoForge 的等价物：

| 1.12.2 用途 | 1.21.1 替代 |
|---|---|
| `ItemNBTHelper.setInt/getCompound/...` | **数据组件 (DataComponent)**，例如 `stack.set(TCDataComponents.CHARGE.get(), n)`；或 `CustomData`（原版） |
| `Vector3.fromEntity/.multiply/.add` | 原版 `net.minecraft.world.phys.Vec3`（`Vec3.ZERO`、`position()`、`scale`、`add`） |
| `Botania.proxy.wispFX / sparkleFX` | Thaumaturge 的 `network/effect/ClientboundSpawnParticlePayload` + `content.effect.Effects`（见 §9.6） |
| `EntityDoppleganger.isTruePlayer(player)` | **1.21.1 Botania 里找不到同名方法** → **待确认**；需读 Gaia 相关 class 确认 |
| `new ScanItem("!X", stack)` / `new ScanEntity("!Y", clazz, true)` | Thaumaturge **数据包 JSON** `data/<ns>/thaumaturge/scan/*.json`（见 §4.8.1），**不再有 `"!"` 前缀** |
| `ManaItemHandler.requestManaExact(stack, player, cost, true)` | ✅ API 仍在；但 Forgotten Relics 在 1.21.1 更应改用 Thaumaturge 的 `RechargeAccess` / `AuraHelper` |
| `ModItems.manaResource` | 拆成独立注册名（见 §11.2.6） |
| `ModSounds.starcaller` | 原版 `SoundEvent`（`BuiltInRegistries.SOUND_EVENT`） |
| `BotaniaCreativeTab` | 自己的 `CreativeModeTab`（见 §1.5） |

---

## 12. 1.12.2 原版依赖、但 1.21.1 **没有对应物** 的清单（迁移必读）

> 判定标准：1.12.2 原版（`Forgotten-Relics-RE-main`）**实际 import 或调用过**，
> 且在 Thaumaturge 0.4.x / Curios 9.5.1 / Botania 1.21.1-457 的**源码或字节码里找不到同名或等价成员**。
> 每一条都注明「替代方案」。

### 12.1 Thaumcraft 侧（1.12.2 → Thaumaturge 1.21.1）

| # | 1.12.2 依赖 | 状态 | 1.21.1 替代 |
|---|---|---|---|
| 1 | `RechargeHelper.getVisFromInventory(EntityPlayer)` | **无对应物** | 概念消失。改为三条独立通路：`AuraHelper`（区块 aura，§3.2）+ `WandAccess`/`WandVisHelper`（法杖 centivis，§3.4）+ `RechargeAccess`（物品 charge，§3.5）。**没有任何 API 能从玩家背包抽 vis。** |
| 2 | `ThaumcraftApi` / `ThaumcraftApiHelper` | **无对应物** | 被拆成多个静态门面：`AuraHelper`、`AspectIndexAccess`、`KnowledgeAccess`、`AspectPoolAccess`、`ScanningManager`、`WandAccess`、`GogglesAccess`、`RechargeAccess`（**全部只能读，`bind` 禁止 addon 调**）。 |
| 3 | `ThaumcraftApi.registerResearchLocation(...)` 之类的研究**代码注册** | **无对应物** | 1.21.1 研究**只能写数据包 JSON**（`data/<ns>/thaumaturge/research_entry/*.json`），没有 Java 注册 API（§4.1）。 |
| 4 | `ScanningManager.addScannableThing` + `ScanItem`/`ScanEntity` 的 `"!Name"` 前缀语法 | **语义变了** | `ScanningManager.addScannableThing(IScanThing)` **仍在**（§4.9.2），`ScanItem`/`ScanEntity` **仍在**（§4.9.3），但 **`"!"` 前缀机制没了**，改为数据包 `data/<ns>/thaumaturge/scan/*.json` + `ScanKeys` 规范 key。 |
| 5 | `ResearchCategories`（1.12.2 的代码注册分类表） | **无对应物** | 分类改为数据包 `data/<ns>/thaumaturge/research_category/*.json`（§4.8）。`TCResearchCategories` 只提供 7 个内置 `ResourceKey` 常量，**没有注册方法**。 |
| 6 | `FXDispatcher`（客户端粒子派发） | **无对应物** | 用 `content.effect.Effects` + `network/effect/ClientboundSpawnParticlePayload`（§9.6）走网络同步粒子。 |
| 7 | `SoundsTC`（1.12.2 音效常量类） | **无对应物** | 用原版 `SoundEvent` + `BuiltInRegistries.SOUND_EVENT`；Thaumaturge 未提供集中常量类。 |
| 8 | `EntityUtils`（1.12.2 实体工具） | **无对应物** | 改用原版/NeoForge：`level.getEntities(...)`、`entity.getBoundingBox().inflate(r)`、`DamageSource` 构造。 |
| 9 | `ThaumcraftApi.*` 的 `IRechargable` 旧签名 `getMaxCharge(ItemStack, EntityLivingBase)` | **签名变了（类名保留）** | 仍是 `IRechargable`，但改为 `getMaxCharge(ItemStack, LivingEntity)` + 新增 `showInHud(ItemStack, LivingEntity)`（§3.5）。 |
| 10 | `IWarpingGear` / `IGoggles` / `IRevealer` / `IVisDiscountGear` | **类名保留，签名略变** | 都在 `api.items`（§6.2）。`IVisDiscountGear.getVisDiscount(ItemStack)` 不再接收 wearer 参数。 |
| 11 | 1.12.2 具名实体类 `EntityFluxRift` / `EntityThaumcraftBoss` / `EntityCultist` / `EntityGolemOrb` | **改名/换包** | `EntityFluxRift` → `content.entity.EntityFluxRift`（注册名 `thaumaturge:flux_rift`）；其余按 `TCEntities` 常量取（§8.6），**旧的 1.12.2 全限定名一律作废**。 |
| 12 | 1.12.2 的 `InfusionRecipe` 运行时类（`ThaumcraftApi.addInfusionCraftingRecipe`） | **无对应物** | 改为数据包 JSON `type: thaumaturge:infusion`（§5.5）；运行时给机器用的是 `InfusionCraftingTransaction`（§5.6.2）。 |

### 12.2 Botania 侧（1.12.2 → Botania 1.21.1-457）

| # | 1.12.2 依赖 | 状态 | 1.21.1 替代 |
|---|---|---|---|
| 1 | `vazkii.botania.common.item.ModItems` | **类不存在** | 改为 `vazkii.botania.common.item.BotaniaItems`。 |
| 2 | `ModItems.manaResource`（+ metadata 多变体物品） | **注册名与类都消失** | 拆成 20+ 个独立物品：`botania:manasteel_ingot` / `mana_diamond` / `mana_powder` / `gaia_spirit` / `manasteel_nugget` / `terrasteel_nugget` / `elementium_nugget` / `mana_string` / `livingwood_twig` / `dreamwood_twig` / `living_root` / `pebble` / `redstone_root`（§11.2.6）。**metadata 序号本身属推测，标「待确认」。** |
| 3 | `ItemNBTHelper`（105 处调用） | **类不存在** | 用**数据组件**（`stack.set/get(TCDataComponents.CHARGE.get(), n)`，§7.6）或原版 `CustomData`。 |
| 4 | `Vector3`（113 处调用） | **类不存在** | 用原版 `net.minecraft.world.phys.Vec3`（`Vec3.ZERO`、`position()`、`scale()`、`add()`）。 |
| 5 | `Botania.proxy.wispFX` / `sparkleFX`（44 处调用） | **无对应物** | 用 `network/effect/ClientboundSpawnParticlePayload` + `content.effect.Effects`（§9.6）。 |
| 6 | `EntityDoppleganger`（Gaia 实体类） | **类不存在** | 实体改注册为 `botania:gaia_guardian`（§11.2.2）。`isTruePlayer(player)` **找不到同名方法** —— **待确认**，需读 Gaia 相关 class 确认。 |
| 7 | `ModSounds`（常量类） | **类不存在** | 用原版 `SoundEvent` / `BuiltInRegistries.SOUND_EVENT`。 |
| 8 | `ExperienceHelper` / `PlayerHelper` | **类不存在** | 用原版 `player.giveExperiencePoints(...)` / `Player` 自带 API。 |
| 9 | `IPhantomInkable` | **接口不存在** | 方块侧改为 `vazkii.botania.api.block.PhantomInkableBlock`；物品侧无对应物。 |
| 10 | `ICosmeticAttachable` | **接口不存在** | 1.21.1 Botania 无对应物；外观系统已重构。 |
| 11 | `SubTileHeiseiDream`（功能花） | **类不存在** | 功能花体系已整体重写，无同名替代。 |
| 12 | `BotaniaCreativeTab` | **类不存在** | 用自己/原版的 `CreativeModeTab`（§1.5）。 |
| 13 | 客户端渲染工具 `IconHelper` / `MiscellaneousIcons` / `LibResources` / `RenderHelper` / `ShaderHelper` / `IModelRegister` | **全部不存在** | 用 NeoForge 的 `RegisterClientExtensionsEvent` / `ModelLayerLocation` / 数据驱动模型 JSON。 |
| 14 | 旧注册名 `botania:fabulousManaRing`（神话魔力之戒） | **注册名消失** | 1.21.1 **没有任何 fabulous*ring**。"Fabulous" 只出现在魔力池上。最接近的是 `botania:greater_band_of_mana`，**容量是否等价待确认**。 |
| 15 | 旧注册名 `spawner_claw` / `spawner_mover` | **注册名消失** | 物品仍在，改名 `botania:life_imbuer` / `botania:life_aggregator`。 |
| 16 | 旧注册名 `mana_ring` / `pixie_ring` / 所有旧 `*_ring` | **注册名消失** | `mana_ring` → `band_of_mana`；`pixie_ring` → `great_fairy_ring`；其余见 §11.2.5。 |
| 17 | 旧注册名 `brewery` / `diluted_pool` / `fabulous_pool` / `creative_pool` / `redstone_spreader` / `elven_spreader` / `gaia_spreader` / `quartz_elven` | **注册名全部消失** | `botanical_brewery` / `diluted_mana_pool` / `fabulous_mana_pool` / `creative_mana_pool` / `pulse_mana_spreader` / `elven_mana_spreader` / `gaia_mana_spreader` / `elven_quartz`。 |
| 18 | 25 个旧 `lens_xxx` 注册名 | **注册名全部消失** | 全部改为描述性名称（`mana_lens`、`velocity_lens`、`bore_lens`、…，§11.2.5）。 |
| 19 | `botania:gaiahead`（物品形态） | **注册名消失** | 变成方块 `botania:gaia_head`（+ `gaia_wall_head`）。 |
| 20 | `botania:alfheim_portal` **作为玩家放置的核心方块** | **语义变了** | 核心方块改为 `botania:elven_gateway_core`；`alfheim_portal` 这个注册名**仍被内部传送门面方块占用**。**直接给附属代码里用旧名摆造型会摆出传送门面而不是核心。** |

### 12.3 **仍然存在、可以放心用** 的 1.12.2 Botania 依赖（对照用）

- `vazkii.botania.api.mana.ManaItemHandler` —— **仍在**（1.12.2 里唯一还活着的 API 类）。
  不过 Forgotten Relics 在 1.21.1 更应改用 Thaumaturge 的 `RechargeAccess` / `AuraHelper`。
- `vazkii.botania.common.item.BotaniaItems`、`vazkii.botania.common.lib.LibItemNames` / `LibBlockNames` —— **在**。
- 注册名不变的 Botania 内容：`mana_tablet`、`mana_pearl`、`mana_diamond`、`manasteel_ingot/nugget`、
  `terrasteel_ingot/nugget`、`elementium_ingot/nugget` + 全套 elementium 工具盔甲、`gaia_spirit`、
  `gaia_ingot`、`gaia_guardian`（实体）、`mana_pylon`/`natura_pylon`/`gaia_pylon`、`dreamwood`、
  `pixie_dust`、`dragonstone`、`runic_altar`、`fel_pumpkin`、`red_string`、`mana_mirror`、
  整套 corporea 与 livingwood/livingrock（§11.3 / §11.4）。

### 12.4 1.12.2 依赖的 Curios 侧

1.12.2 的 Forgotten Relics **不使用 Curios**（它用 Thaumcraft 自己的 `ItemBaubleBase` / `IBauble`）。
因此**没有「Curios 旧 API 无对应物」的问题**；1.21.1 侧是从零接入 Curios 9.5.1，
接口见 §10（`ICurioItem`、`CuriosApi`、`SlotContext`）。
**注意 Curios 1.21.1 没有 `ICuriosApi` 接口，也没有 `CuriosApi.getInstance()`。**

---

## 13. 给 Forgotten Relics 附属的落地清单

1. **入口/注册**：照 §1.7 的最小示例，用 `DeferredRegister.Items` + 自己的 `CreativeModeTab`。
2. **物品**：没有通用基类（§7.1），直接 `extends Item` 并 **implements 能力接口**（§7.2）。
3. **饰品**：让 `Item` implements `ICurioItem`（§10.2.1），并写两份 JSON：
   `data/curios/tags/item/<slot>.json`（**namespace 必须是 `curios`**）+ `data/<你的ns>/curios/entities/player.json`（§10.8）。
4. **要素**：物品要素靠数据包 `data/<ns>/data_maps/item/base_aspects.json`（§2.9），不要写 Java。
5. **充能**：implements `IRechargable`，用 `RechargeAccess`（§3.5）；tooltip 自动出现。
6. **研究**：手写 `data/<ns>/thaumaturge/research_entry/*.json` + `research_category/*.json`（§4.11）。
7. **配方**：手写 `data/<ns>/recipe/**`，`type` 用 serializer id（§5.1）。
8. **伤害**：`ResourceKey<DamageType>` + 自己的 `data/<ns>/damage_type/*.json`（§8.5）。
9. **实体**：`DeferredRegister<EntityType<?>>` + `EntityAttributeCreationEvent` + 客户端渲染器（§8.8）。
10. **网络**：`CustomPacketPayload` record + 自己的 `@EventBusSubscriber` 注册类 + `PacketDistributor`（§9）。
11. **绝对不要调 `bind(...)`**：`KnowledgeAccess` / `AspectPoolAccess` / `ScanningManager` /
    `AspectKnowledgeAccess` / `ArcaneCraftCost` / `AuraHelper` / `WandAccess` / `GogglesAccess` /
    `RechargeAccess` —— 重复调用抛 `IllegalStateException`。
12. **datagen 产物只在 jar 里**（§0.2）：要抄格式去
    `E:/Modpacks/versions/Thaumcraft 1.21.1/mods/thaumaturge-1.21.1-NeoForge-BETA-0.4.4.jar` 解包，
    或读 `com/leclowndu93150/thaumaturge/data/` 下的 45 个 datagen 类。

---

> **文档结束。**
> 所有签名来源：Thaumaturge 1.21.1 源码（1483 `.java`）+ 0.4.4 成品 jar（6480 条目）、
> Curios 9.5.1+1.21.1 源码 + 成品 jar、Botania 1.21.1-457 解包字节码、
> NeoForge 21.1.235 sources、Thaumic Energistics CE 真实调用代码。
> 凡标注「**待确认**」处，均未找到直接证据，请勿据此迁移。
