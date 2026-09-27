package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.entity.EntityBabylonWeapon;
import net.minecraft.util.Mth;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.common.handler.BotaniaSounds;

/**
 * 神化（Apotheosis），注册名 {@code apotheosis}，1.7.10 原版 {@code ItemApotheosis}。
 *
 * <p>原版逻辑：
 * <ul>
 *   <li>右键 {@code setItemInUse(stack, 72000)} 进入 {@code EnumAction.bow} 的拉弓姿态，
 *       <b>没有施法后冷却</b>（从不调 {@code SuperpositionHandler.setCasted}）；</li>
 *   <li>{@code onUsingTick}：只要 {@code count != getMaxItemUseDuration() && count % 2 == 0}
 *       （即除第一个 tick 外每 2 tick 一次），就尝试从背包法杖抽一次 Vis：
 *       地（Terra）{@code 30} + 火（Ignis）{@code 60} + 秩序（Ordo）{@code 50} +
 *       混沌（Perditio）{@code 75} 厘 Vis，合计 {@code 215} 厘 = {@code 2.15} 点，
 *       四项都乘 {@code apotheosisVisMult}（默认 1.0）；抽得出来才召唤一把巴比伦武器
 *       {@code EntityBabylonWeaponSS}。于是引导期间<b>每 2 tick 铺一把武器</b>（10 把/秒）；</li>
 *   <li>{@code spawnBabylonWeapon}：以玩家头部朝向为基准，把武器撒在玩家周围——
 *       先把头朝向换算成单位方向向量并做一次「对角收缩」（{@code yaw % 90} 关于 45° 对折），
 *       再在 ±80° 内随机旋转、乘 {@code 2~12} 格距离、叠加 {@code 0~1} 倍原方向、
 *       竖直漂移 {@code -0.5~7.5} 格；最多重掷 101 次，<b>避开任何 2 格内已有武器</b>；
 *       落点定下后设 variety（0~11）、delay 0，并在落点播 {@code botania:babylonSpawn}
 *       （音量 1.0、音调 1.0 + 随机 × 3.0）；</li>
 *   <li>物品堆叠上限 1，稀有度 EPIC，{@code getWarp} 返回 <b>5</b>。</li>
 * </ul>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code getMaxItemUseDuration} → {@code Item#getUseDuration}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「从背包法杖抽 Vis」在 1.21.1 没有对应 API</b>（见 {@code docs/reference/thaumaturge-1.21.1-api.md} §12.1）。
 *       按本模组统一约定改成 {@link FRRechargable} 的<b>物品自身充能</b>，用
 *       {@link RechargeAccess#consumeCharge} 扣除；</li>
 *   <li>音效 {@code botania:babylonSpawn} 在 Botania 1.21.1 里改名为
 *       {@link BotaniaSounds#TREASURE_WEAPON_SPAWN}（音效文件就是 {@code treasureweaponspawn.ogg}），
 *       直接引用原音效并按项目约定过 {@link SoundHelper#play} 压音量；</li>
 *   <li>原版的 {@code Vector3.rotate/fromEntityCenter} 等向量工具在 1.21.1 的 Botania 里已不存在，
 *       落点散布改用原版 {@link Vec3}/ {@link AABB} 逐字重写（见 {@link #spawnBabylonWeapon}）。</li>
 * </ul>
 *
 * <p><b>Vis 折算</b>：原版每次召唤 2.15 点，而物品充能是整数，按本项目既有做法
 * （霹雳咒书 2.2 → 2、月耀咒书 1.5 → 2）就近取 {@code apotheosisVisCost = 2}。
 * 最大储量取 300：原版四项里混沌（Perditio）是瓶颈，一把每要素满 100 的标准法杖
 * 约支撑 {@code 10000 / 75 ≈ 133} 次召唤，乘 2 得 266，向上取整到 300（≈150 次），
 * 与月耀咒书「按满法杖瓶颈量换算、取整」的口径一致。
 *
 * <p><b>两处与 RE 的差异</b>：
 * <ol>
 *   <li>原版 tooltip 的 Ctrl 分支（{@code FRVisPerSecond.lore} + 四个要素成本）依赖
 *       {@code GuiScreen.isCtrlKeyDown}，而本项目的共享基类 {@code FRItem} 只实现了 Shift 展开，
 *       近几件施法物品也都没有该行；此外本模组已把「按要素抽 Vis」改成物品自身充能，
 *       那个分支已无意义，所以同样不实现；</li>
 *   <li>原版 {@code isFull3D()} 返回 false、{@code EnumRarity.epic} 等 1.21.1 已无对应概念，
 *       分别略过与改用 {@code Item.Properties.rarity} 声明。</li>
 * </ol>
 */
public class ItemApotheosis extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；实际节奏靠 {@code onUseTick} 每 2 tick 触发。 */
    private static final int USE_DURATION = 72000;

    /** 原版重掷落点时使用的「已有武器」排斥半径（{@code range = 2.0}）。 */
    private static final double AVOID_RADIUS = 2.0D;

    public ItemApotheosis(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 每次召唤的 Vis 消耗：原版合计 215 厘（2.15 点），取整为 2，再乘 {@code apotheosisVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.APOTHEOSIS_VIS_COST.get() * FRConfig.APOTHEOSIS_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.APOTHEOSIS_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.APOTHEOSIS_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版 onItemRightClick 只是 setItemInUse(stack, 72000)，真正的节奏在 onUsingTick。
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与符文天象石、月耀咒书同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        // 召唤、扣费、音效都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return;
        }
        int interval = FRConfig.APOTHEOSIS_COOLDOWN.get();
        // 原版条件 count != getMaxItemUseDuration() && count % 2 == 0（第一个 tick 不召唤）。
        if (remainingUseDuration <= 0
                || interval <= 0
                || remainingUseDuration == USE_DURATION
                || remainingUseDuration % interval != 0) {
            return;
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        spawnBabylonWeapon(level, player);
    }

    /**
     * 对应原版 {@code spawnBabylonWeapon}：把一把巴比伦武器撒到玩家周围的一个空位上。
     *
     * <p>落点算法逐字保留原版：
     * <ol>
     *   <li>把头朝向 {@code rotationYawHead} 归一化到 {@code [0,360)}，按四个象限换算成
     *       水平单位方向 {@code (x,z)}，再按 {@code yaw % 90} 关于 45° 对折得到 {@code multV3}，
     *       乘出基准方向 {@code lookV}（这就是「对角收缩」，让正东/正南等对角线方向不会偏长）；</li>
     *   <li>循环最多 101 次：把基准方向绕 Y 轴随机转 ±80°、乘 {@code 2~12} 格、叠加上
     *       {@code 0~1} 倍的基准方向、竖直漂移 {@code -0.5~7.5} 格，再加上玩家身体中心；
     *       若该点 2 格内已经有别的巴比伦武器就重掷，否则采用；</li>
     *   <li>把武器放到落点、朝向设成玩家头朝向、variety 取 {@code 0~11}、delay 设 0，
     *       并在落点播 {@code botania:babylonSpawn}（1.0 / 1.0 + 随机 × 3.0）。</li>
     * </ol>
     *
     * <p>原版这段散布用的是全局 {@code Math.random()}、{@code Item.itemRand} 与
     * {@code world.rand} 三个随机源；这里统一用 {@code player.getRandom()}，分布等价。
     */
    private static void spawnBabylonWeapon(Level level, Player player) {
        Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);

        double rawYaw = player.getYHeadRot();
        double yaw = rawYaw < 0.0D ? Math.abs(rawYaw) : 360.0D - rawYaw;
        double x = 0.0D;
        double z = 0.0D;
        if (yaw >= 0.0D && yaw <= 90.0D) {
            double m = yaw / 90.0D;
            z = 1.0D - m;
            x = m;
        } else if (yaw <= 180.0D) {
            double m = (yaw - 90.0D) / 90.0D;
            x = 1.0D - m;
            z = -m;
        } else if (yaw <= 270.0D) {
            double m = (yaw - 180.0D) / 90.0D;
            z = -(1.0D - m);
            x = -m;
        } else {
            double m = (yaw - 270.0D) / 90.0D;
            x = -(1.0D - m);
            z = m;
        }

        double multV2 = yaw % 90.0D;
        // 原版这一句只在 multV2 > 45 时把角度对折回 [0,45]（== 45 时保持 45）。
        if (!(multV2 < 45.0D) && multV2 > 45.0D) {
            multV2 = 45.0D - (multV2 - 45.0D);
        }
        double multV3 = 1.0D + multV2 / 90.0D;
        Vec3 lookV = new Vec3(x * multV3, 0.0D, z * multV3);
        // additive 是循环开始前的基准方向，循环里只改 lookV，不改它。
        Vec3 additive = lookV;

        for (int counter = 0; counter <= 100; counter++) {
            double negative = player.getRandom().nextDouble() >= 0.5D ? -1.0D : 1.0D;
            Vec3 finalVec = EntityBabylonWeapon.rotateAroundY(lookV, 80.0D * negative);
            finalVec = finalVec.scale(2.0D + player.getRandom().nextDouble() * 10.0D);
            finalVec = finalVec.add(additive.scale(player.getRandom().nextDouble()));
            finalVec = finalVec.add(0.0D, -0.5D + player.getRandom().nextDouble() * 8.0D, 0.0D);
            finalVec = finalVec.add(playerCenter);

            AABB occupied = new AABB(finalVec, finalVec).inflate(AVOID_RADIUS);
            if (!level.getEntitiesOfClass(EntityBabylonWeapon.class, occupied).isEmpty()) {
                continue;
            }
            lookV = finalVec;
            break;
        }

        EntityBabylonWeapon weapon = new EntityBabylonWeapon(level, player);
        weapon.setPos(lookV.x, lookV.y, lookV.z);
        // 原版 weapon.rotationYaw = player.rotationYawHead。
        weapon.setYRot(player.getYHeadRot());
        // 原版 ItemApotheosis.java:150：weapon.setRotation(wrapAngleTo180_float(-player.rotationYawHead + 180))。
        // 渲染器 FRBabylonWeaponRenderer 读的就是这个 rotation；此前召唤路径从来没写过它，
        // 导致所有巴比伦武器朝向恒为 0（只有读档才会恢复该字段）。
        weapon.setRotation(Mth.wrapDegrees(-player.getYHeadRot() + 180.0F));
        weapon.setVariety(player.getRandom().nextInt(12));
        weapon.setDelay(0);
        level.addFreshEntity(weapon);

        // 原版 world.playSoundAtEntity(weapon, "botania:babylonSpawn", 1.0F, 1.0F + random * 3.0F)。
        SoundHelper.play(level, weapon.getX(), weapon.getY(), weapon.getZ(),
                BotaniaSounds.TREASURE_WEAPON_SPAWN, SoundSource.PLAYERS, 1.0F,
                1.0F + player.getRandom().nextFloat() * 3.0F);
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 行序与 1.7.10 原版逐条对齐：1 / 空 / 2 / 3 / 空 / 4（含直击伤害） / 5（含范围伤害） / 6。
        tooltip.add(Component.translatable("item.ItemApotheosis1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemApotheosis2.lore"));
        tooltip.add(Component.translatable("item.ItemApotheosis3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        // 原版把 100 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.ItemApotheosis4.lore",
                (int) FRConfig.APOTHEOSIS_DIRECT_DAMAGE.get().doubleValue()));
        // 原版把 75 硬编码进文案；这里显示真实配置值，且同样按整数显示。
        tooltip.add(Component.translatable("item.ItemApotheosis5.lore",
                (int) FRConfig.APOTHEOSIS_IMPACT_DAMAGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemApotheosis6.lore"));
    }
}
