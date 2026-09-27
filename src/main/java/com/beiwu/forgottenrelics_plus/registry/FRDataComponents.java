package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品数据组件。
 *
 * <p>RE 里往物品上存临时状态靠的是 {@code ItemNBTHelper.setFloat(stack, "IDamageStored", ...)}
 * 这样直接写 NBT；1.20.5 起原版（Minecraft）把 NBT 换成了「数据组件」（{@code DataComponentType}），
 * 每种用途都要先注册一个带编解码器的类型。这里登记本移植用到的几个。
 *
 * <p>对比 RE 的字段：
 * <ul>
 *   <li>{@code IDamageStored}（float）→ {@link #STORED_DAMAGE}；</li>
 *   <li>{@code ICooldown}（int）→ {@link #INVINCIBILITY_COOLDOWN}。</li>
 * </ul>
 *
 * <p>{@code persistent} 决定存档里怎么写，{@code networkSynchronized} 决定怎么同步给客户端，
 * 两者都给了才能在客户端 tooltip 里正确读到数值。
 */
public final class FRDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(ForgottenRelics.MOD_ID);

    /** 湮灭护符累计储存的伤害值（对应 RE 的 NBT 字段 {@code IDamageStored}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> STORED_DAMAGE =
            DATA_COMPONENTS.registerComponentType("stored_damage",
                    builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

    /** 神圣护符无敌帧延长效果的剩余冷却（对应 RE 的 NBT 字段 {@code ICooldown}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INVINCIBILITY_COOLDOWN =
            DATA_COMPONENTS.registerComponentType("invincibility_cooldown",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** 日耀石：连续静止的累计量（对应 RE 的 NBT 字段 {@code Static}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHINY_STILL_TICKS =
            DATA_COMPONENTS.registerComponentType("shiny_still_ticks",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** 休眠浑浊之核：剩余寿命（对应 RE 的 NBT 字段 {@code ILifetime}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DORMANT_LIFETIME =
            DATA_COMPONENTS.registerComponentType("dormant_lifetime",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * 虚空吞噬者：右键时锁定的神秘方尖碑坐标。
     *
     * <p>对应原版 {@code ItemObeliskDrainer} 往物品上写的三个 NBT 字段
     * {@code IDetectedX / IDetectedY / IDetectedZ}（都是 double），这里合并成一个 {@code BlockPos}。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> DEVOURER_TARGET =
            DATA_COMPONENTS.registerComponentType("devourer_target",
                    builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    /**
     * 永恒放逐之诫：右键时锁定的目标实体 id。
     *
     * <p>对应原版 {@code ItemOverthrower} 里那张以玩家为键的静态 map {@code targetList}
     *（放逐的引导期间要一直盯着同一个目标）。实体 id 只在同一个服务端会话内有效，正好符合
     *「引导结束即失效」的用途；不用实体对象做键是为了避免长期持有已移除实体的引用。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EDICT_TARGET =
            DATA_COMPONENTS.registerComponentType("edict_target",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * 深渊魔典：右键时锁定的目标实体 id。
     *
     * <p>对应原版 {@code ItemVoidGrimoire} 里那张以玩家为键的静态 map {@code targetList}
     *（引导期间要一直盯着同一个目标）。与 {@link #EDICT_TARGET} 同源，只是各物品一份、语义清晰。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VOID_GRIMOIRE_TARGET =
            DATA_COMPONENTS.registerComponentType("void_grimoire_target",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * 破碎的命运巨著：免死效果的剩余冷却（对应原版 NBT 字段 {@code IFateCooldown}，单位 tick）。
     *
     * <p>原版把这个冷却存在物品自己的 NBT 上，而<b>不是</b>走 {@code SuperpositionHandler}
     * 的共用施法冷却——两者是不同的东西：共用冷却是「所有施法类遗物共享一个计时」，
     * 命运巨著则是每一本各自记账（带着两本时，只有第一本的冷却会被读取与写入）。
     * 本移植沿用同一口径，与神圣护符的 {@link #INVINCIBILITY_COOLDOWN}（对应 RE 的 {@code ICooldown}）
     * 一致：物品自己的冷却存物品自己身上。详见 {@code ItemFateTome} 的类注释。
     *
     * <p>原版另有一个 {@code IFateID}（首次使用写一个随机 int）从头到尾没有被读过，
     * 没有任何行为依赖它，这里不移植。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FATE_COOLDOWN =
            DATA_COMPONENTS.registerComponentType("fate_cooldown",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * 湮灭之钥：当前模式。
     *
     * <p>对应原版压在物品 metadata 上的「模式 + 启用位」：原版 {@code 0/1/2} 是启用中的三种模式，
     * {@code 100/101/102} 是同一模式的停用态。1.21.1 的物品没有 metadata，取值口径原样搬进组件，
     * 便于与原版逐行对照（见 {@code ItemOblivionStone}）。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OBLIVION_MODE =
            DATA_COMPONENTS.registerComponentType("oblivion_mode",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * 湮灭之钥：已绑定的「可消耗物品」样本清单。
     *
     * <p>对应原版两个平行 NBT 数组 {@code SupersolidID}（数值物品 id）与 {@code SupersolidMetaID}
     * （metadata；{@code -1} 表示可损毁物品，按物品类型通配）。1.21.1 既没有数值物品 id 也没有 metadata：
     * 直接存整份 {@link ItemStack} 样本，天然带上组件（染色、药水内容等变体信息），
     * 不必自己维护两份平行数组，匹配规则见 {@code ItemOblivionStone#matchesBound}。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> OBLIVION_BOUND_ITEMS =
            DATA_COMPONENTS.registerComponentType("oblivion_bound_items",
                    builder -> builder.persistent(ItemStack.CODEC.listOf())
                            .networkSynchronized(ItemStack.LIST_STREAM_CODEC));

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }

    private FRDataComponents() {
    }
}
