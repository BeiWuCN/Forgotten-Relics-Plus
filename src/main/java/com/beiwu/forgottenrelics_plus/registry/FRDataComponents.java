package com.beiwu.forgottenrelics_plus.registry;

import com.beiwu.forgottenrelics_plus.ForgottenRelics;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品数据组件。
 *
 * <p>1.12.2 里往物品上存临时状态靠的是 {@code ItemNBTHelper.setFloat(stack, "IDamageStored", ...)}
 * 这样直接写 NBT；1.20.5 起原版把 NBT 换成了「数据组件」（{@code DataComponentType}），
 * 每种用途都要先注册一个带编解码器的类型。这里登记本模组用到的几个。
 *
 * <p>对比原版字段：
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

    /** 湮灭护符累计储存的伤害值（对应原版 NBT 字段 {@code IDamageStored}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> STORED_DAMAGE =
            DATA_COMPONENTS.registerComponentType("stored_damage",
                    builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

    /** 神圣护符无敌帧延长效果的剩余冷却（对应原版 NBT 字段 {@code ICooldown}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INVINCIBILITY_COOLDOWN =
            DATA_COMPONENTS.registerComponentType("invincibility_cooldown",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** 日耀石：连续静止的累计量（对应原版 NBT 字段 {@code Static}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SHINY_STILL_TICKS =
            DATA_COMPONENTS.registerComponentType("shiny_still_ticks",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** 不和谐之戒：是否开启「不和谐」模式（对应原版 NBT 字段 {@code discordEnabled}）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DISCORD_ENABLED =
            DATA_COMPONENTS.registerComponentType("discord_enabled",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    /** 休眠浑浊之核：剩余寿命（对应原版 NBT 字段 {@code ILifetime}）。 */
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

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }

    private FRDataComponents() {
    }
}
