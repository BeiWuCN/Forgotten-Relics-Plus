package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.particle.FRBoltParticleData;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 虚空吞噬者（Devourer of The Void），注册名 {@code devourer_of_the_void}，
 * 原版 {@code ItemObeliskDrainer}（原版注册名与研究键都是 ObeliskDrainer，显示名才是
 * Devourer of The Void）。堆叠上限 1、Warp 4、{@code EnumAction.bow}，不消耗也不储存 Vis、
 * 没有冷却——原版从头到尾只用「附近有没有方尖碑」做门槛，所以它没有实现 {@code FRRechargable}。
 *
 * <p>行为：右键遍历世界找同维度、距玩家（{@code getDistance(x, y + 2.5, z)}）不超过 16 格的神秘
 * 方尖碑，找到就把坐标写进物品并进入 72000 tick 的拉弓姿态，找不到则右键无反应。引导期间每 tick
 * 复核「坐标处仍是方尖碑且距离未超限」，不满足立刻停止；另外每 tick 有 17.5% 概率在方尖碑上方补
 * 一道随机弧光（与 {@code count % 30} 无关），并撒 5 颗传送门粒子。每当剩余时长满足
 * {@code count % 30 == 0} 且不是第一 tick 时抽一次：从方尖碑向玩家画一道闪电、播 {@code thaumcraft:zap}
 *（音量 1.0、音调 0.8）、对玩家造成 0.01 的 {@code DamageSource.generic} 伤害后回 4 点血并补 2 点饥饿，
 * 再随机挑一个原初要素，给背包里该要素还有余量的随机法杖补
 * {@code (int)((5.0 + rand * 15.0) * 1.5 * obeliskDrainerVisMult)} 点 Vis。
 *
 * <p>1.21.1 对应：{@code onItemRightClick / onUsingTick / setItemInUse / EnumAction.bow} →
 * {@code use / onUseTick / startUsingItem / UseAnim.BOW}；<b>「遍历已加载 TileEntity」没有公开 API</b>，
 * 改为以玩家为中心、边长 2×range 的立方体扫描，只认 {@code TCBlocks.ELDRITCH_OBELISK}，用
 * {@code level.isLoaded} 保证只看已加载区块，最后按原版口径复核距离；NBT 的三个 double 坐标 →
 * {@link FRDataComponents#DEVOURER_TARGET}（{@code BlockPos}）；闪电（1.6.2 重做）改用
 * {@link FRBoltParticleData#broadcast} 把两端经原版粒子包送到客户端，再由 {@code client/FRBolts} 画成
 * 折线闪电，端点 / 宽度 / 颜色逐条对齐原版；{@code thaumcraft:zap} →
 * {@link SoundEvents#FIREWORK_ROCKET_BLAST}（沿用霹雳咒书的替代方案）。「给背包法杖补 Vis」这一支
 * 完整保留：Thaumaturge 的 {@link WandVisHelper#addVis} 就是 {@code ItemWandCasting#addVis} 的直接
 * 对应物（寻杖逻辑与欧米伽之核完全相同）。
 *
 * <p>与 RE 的关系：RE 把这件物品改成了「吸收咒波裂隙、右键开关」，本仓库现成的
 * {@code item.ItemDevourerOfTheVoid*.lore} 与 {@code fr.text.ObeliskDrainer.*} 文案也是照 RE 写的。
 * 本移植以原版为唯一行为参照，所以行为按原版复刻，并已把这些文案改回「神秘方尖碑 + 引导充能」版本
 *（键名不变，只改文本）。
 */
public class ItemDevourerOfTheVoid extends FRItem implements IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；实际节奏靠 {@code onUseTick} 每 30 tick 触发。 */
    private static final int USE_DURATION = 72000;

    /** 原版那段「随机弧光」的触发概率：{@code Math.random() <= 0.175}。 */
    private static final float ARC_LIGHTNING_CHANCE = 0.175F;

    public ItemDevourerOfTheVoid(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.DEVOURER_OF_THE_VOID_WARP.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.bow
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // 原版 getMaxItemUseDuration(stack) 返回 72000。
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 原版会遍历已加载的 TileEntity 找神秘方尖碑；找不到时右键完全无反应。
        BlockPos target = findObelisk(level, player);
        if (target == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            // 原版把检测到的坐标写进 NBT（IDetectedX/Y/Z）；1.21.1 改用数据组件。
            stack.set(FRDataComponents.DEVOURER_TARGET.get(), target);
        }
        // 原版 setItemInUse(stack, 72000)：进入拉弓姿态，之后交给 onUsingTick 周期触发。
        // 客户端也一起进入姿态，这样弓的拉扯动画才会出现（与符文天象石同一写法）。
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        BlockPos target = stack.get(FRDataComponents.DEVOURER_TARGET.get());

        // 方尖碑上方每 tick 的 5 颗传送门粒子走客户端本地生成：原版就是
        // onUsingTick + world.spawnParticle（服务端那半是空操作），这里同样用客户端
        // addParticle——逐颗的位置 / 初速随机与原版逐字一致。
        // 不要改成服务端每 tick 发包：本物品引导时长上限 72000 tick，每 tick 发 5 个
        // count == 0 的包会变成本移植最重的一处每 tick 广播；目标是<b>一个包都不发</b>。
        // 旁观者客户端也会跑 onUseTick——LivingEntity#onSyncedDataUpdated 收到
        // DATA_LIVING_ENTITY_FLAGS 变化后会为观察者设好 useItem / useItemRemaining，
        // 目标坐标又随物品数据组件同步——所以其他玩家同样看得到。
        if (level.isClientSide()) {
            if (target != null) {
                obeliskPortals(level, target);
            }
            return;
        }

        // 抽取、治疗、音效与雷弧都只在服务端做；客户端交给服务端同步。
        ServerLevel server = (ServerLevel) level;
        if (target == null || !isValidTarget(level, player, target)) {
            // 原版：目标方尖碑消失、换掉，或玩家走远到 16 格之外时立即停止使用。
            player.stopUsingItem();
            return;
        }

        // <b>雷弧不受 count % 30 约束</b>：每 tick 掷一次 17.5%，不要放进下面「每 30 tick 一次」的分支。
        if (level.random.nextFloat() <= ARC_LIGHTNING_CHANCE) {
            randomArc(server, target);
        }

        int interval = FRConfig.DEVOURER_OF_THE_VOID_PULSE_INTERVAL.get();
        // 原版条件 count % 30 == 0 且 count != getMaxItemUseDuration（第一个 tick 不触发）。
        if (remainingUseDuration <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }

        drain(server, player, target);
    }

    /** 抽一次：闪电 + 音效 + 自伤 0.01 + 回血 4 + 补饥饿 2 + 给背包里的法杖补 Vis。 */
    private static void drain(ServerLevel level, Player player, BlockPos target) {
        // 原版 for (counter = 0; counter <= 3; ++counter)：同一处连画 4 道 imposeLightning。
        // 这里换算成一次广播里的 count = 4（Botania 的 BoltParticleOptions#count）。
        FRBoltParticleData.broadcast(level, obeliskTop(target), playerCenter(player),
                FRBoltParticleData.WIDTH_MAIN, 4,
                FRBoltParticleData.ARC_RED, FRBoltParticleData.ARC_GREEN, FRBoltParticleData.ARC_BLUE);

        // 原版 thaumcraft:zap（音量 1.0、音调 0.8）；沿用霹雳咒书对 Thaumcraft 音效的替代方案。
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);

        // 原版顺序：先 attackEntityFrom(DamageSource.generic, 0.01F)，再 heal(4.0F) 与
        // FoodStats.addStats(2, 1.0F)。1.21.1 的 FoodData#eat 语义相同。
        player.hurt(level.damageSources().generic(), 0.01F);
        player.heal(FRConfig.DEVOURER_OF_THE_VOID_HEAL.get().floatValue());
        player.getFoodData().eat(FRConfig.DEVOURER_OF_THE_VOID_HUNGER.get(), 1.0F);

        chargeRandomWand(player);
    }

    /**
     * 对应原版那三行：
     * {@code Aspect randomAspect = primals.get(rand)} → {@code getRandomValidWand(player, aspect)}
     * → {@code ((ItemWandCasting) wand.getItem()).addVis(wand, aspect, (5 + rand * 15) * 1.5 * mult, true)}。
     */
    private static void chargeRandomWand(Player player) {
        List<ResourceKey<IAspect>> primals = TCAspects.PRIMALS;
        ResourceKey<IAspect> aspect = primals.get(player.getRandom().nextInt(primals.size()));
        ItemStack wand = findWandWithRoom(player, aspect);
        if (wand == null) {
            return;
        }
        double mult = FRConfig.DEVOURER_OF_THE_VOID_VIS_MULT.get();
        int amount = (int) ((5.0D + player.getRandom().nextDouble() * 15.0D) * 1.5D * mult);
        WandVisHelper.addVis(wand, aspect, amount, true);
    }

    /**
     * 在玩家背包里随机挑一根「该要素还没满」的法杖。
     *
     * <p>对应原版 {@code SuperpositionHandler.getRandomValidWand}；与
     * {@code ItemOmegaCore#findWandWithRoom} 是同一套逻辑（欧米伽之核也是给法杖补 Vis）。
     */
    private static ItemStack findWandWithRoom(Player player, ResourceKey<IAspect> aspect) {
        Inventory inventory = player.getInventory();
        List<ItemStack> candidates = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !(candidate.getItem() instanceof ItemWand)) {
                continue;
            }
            if (WandVisHelper.getVis(candidate, aspect) < WandVisHelper.getMaxVis(candidate)) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    /** 原版闪电的起点：{@code (x + 0.5, y + 2.75, z + 0.5)}。 */
    private static Vec3 obeliskTop(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 2.75D, pos.getZ() + 0.5D);
    }

    /** 玩家身体中心，对应原版 {@code Vector3.fromEntityCenter(player)} 与 {@code motionVec} 的基准点。 */
    private static Vec3 playerCenter(Player player) {
        return player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
    }

    /**
     * 原版每 tick 17.5% 概率的「方尖碑周围随机弧光」，对应
     * {@code SuperpositionHandler.imposeArcLightning(..., x + 0.5, y + 2.5 ± 1, z + 0.5,
     * x + 0.5 ± 2, y + 2.5 ± 2, z + 0.5 ± 2, 1.0F, 0.6F, 1.0F, h)}。
     *
     * <p>端点全部按原版那样在方尖碑上方抖动：起点竖直 ±1 格，终点三轴 ±2 格；
     * 颜色是原版写死的 {@code (1.0, 0.6, 1.0)}；宽度取原版那个随机变量 {@code h} 的绝对值 0.4
     * （{@code h} 的符号只决定四边带的缠绕方向，对这条对称的光带没有可见影响，
     * 而 Botania 的 {@code size} 必须是正数）。
     */
    private static void randomArc(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        Vec3 from = new Vec3(x, pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 2.0D, z);
        Vec3 to = new Vec3(
                x + (level.random.nextDouble() - 0.5D) * 4.0D,
                pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 4.0D,
                z + (level.random.nextDouble() - 0.5D) * 4.0D);
        FRBoltParticleData.broadcast(level, from, to, 0.4F, 1,
                FRBoltParticleData.OBELISK_ARC_RED,
                FRBoltParticleData.OBELISK_ARC_GREEN,
                FRBoltParticleData.OBELISK_ARC_BLUE);
    }

    /**
     * 原版客户端每 tick 在方尖碑上方撒的 5 颗 {@code EntityPortalFX}
     * （{@code func_72869_a("portal", x + 0.5, y + 2.5 ± 1, z + 0.5, ±1.5, ±0.15, ±1.5)}）。
     *
     * <p>走客户端本地 {@code addParticle}，与原版完全一致：位置与初速逐颗独立随机，不发任何网络包。
     *
     * <p><b>不能改用服务端的 {@code count > 0} 整簇发包</b>：整簇三个轴只能共用一个高斯初速
     * 标量（见 {@code ClientPacketListener#handleParticleEvent}），而这里水平 ±1.5、竖直 ±0.15
     * 的各向异性正是观感的一部分；又因为 {@code PortalParticle} 的位移就是「初速 × f2(age)」，
     * 初速被拉大到水平量级后竖直散布会从约 0.17 格涨到约 1 格。
     */
    private static void obeliskPortals(Level level, BlockPos pos) {
        for (int i = 0; i <= 4; i++) {
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5D,
                    pos.getY() + 2.5D + (level.random.nextDouble() - 0.5D) * 2.0D,
                    pos.getZ() + 0.5D,
                    (level.random.nextDouble() - 0.5D) * 3.0D,
                    (level.random.nextDouble() - 0.5D) * 0.3D,
                    (level.random.nextDouble() - 0.5D) * 3.0D);
        }
    }

    /**
     * 对应原版 {@code onItemRightClick} 里那段「遍历已加载 TileEntity 找神秘方尖碑」。
     *
     * <p>1.21.1 没有公开的 tile entity 总表，所以改为以玩家为中心、边长 2×range 的立方体扫描：
     * 只看已加载方块、只认方尖碑方块，并按原版口径 {@code getDistance(x, y + 2.5, z)} 复核距离。
     *
     * @return 找到的方尖碑坐标，找不到返回 {@code null}
     */
    private static BlockPos findObelisk(Level level, Player player) {
        double range = FRConfig.DEVOURER_OF_THE_VOID_RANGE.get();
        int radius = (int) Math.ceil(range);
        double maxSqr = range * range;
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).is(com.leclowndu93150.thaumaturge.registry.TCBlocks.ELDRITCH_OBELISK.get())) {
                continue;
            }
            if (player.position().distanceToSqr(pos.getX(), pos.getY() + 2.5D, pos.getZ()) > maxSqr) {
                continue;
            }
            return pos.immutable();
        }
        return null;
    }

    /**
     * 对应原版 {@code onUsingTick} 开头那段复核：
     * {@code world.getTileEntity(x, y, z) instanceof TileEldritchObelisk}
     * 且 {@code player.getDistance(x, y + 2.5, z) <= 16}。
     */
    private static boolean isValidTarget(Level level, Player player, BlockPos pos) {
        if (!level.isLoaded(pos)
                || !(level.getBlockEntity(pos)
                        instanceof com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk)) {
            return false;
        }
        double range = FRConfig.DEVOURER_OF_THE_VOID_RANGE.get();
        return player.position().distanceToSqr(pos.getX(), pos.getY() + 2.5D, pos.getZ()) <= range * range;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 原版的五行结构（第 2 行接感知距离）。文案已按原版改写，见类注释最后一段。
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid1.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid2.lore",
                (int) FRConfig.DEVOURER_OF_THE_VOID_RANGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid3.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid4.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid5.lore"));
    }
}
