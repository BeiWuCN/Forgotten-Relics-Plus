package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
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
 * 1.7.10 原版 {@code ItemObeliskDrainer}（原版注册名与研究键都是 ObeliskDrainer，显示名才是
 * Devourer of The Void）。
 *
 * <p>原版行为：
 * <ol>
 *   <li>{@code onItemRightClick}：遍历世界已加载的全部 {@code TileEntity}，找<b>同维度、距玩家
 *       （{@code getDistance(x, y + 2.5, z)}）不超过 16 格</b>的 {@code TileEldritchObelisk}
 *       （神秘方尖碑）；找到就把坐标写进物品 NBT（{@code IDetectedX / IDetectedY / IDetectedZ}）
 *       并 {@code setItemInUse(stack, 72000)}（{@code EnumAction.bow}）；找不到则右键无反应；</li>
 *   <li>{@code onUsingTick}：每 tick 先复核「NBT 坐标处仍是神秘方尖碑」且距离仍不超过 16 格，
 *       不满足立刻停止使用；</li>
 *   <li>每当剩余使用时间满足 {@code count % 30 == 0}（且不是第一个 tick）抽一次：
 *       <ul>
 *         <li>从方尖碑往玩家方向画一道闪电，并播放 {@code thaumcraft:zap}（音量 1.0、音调 0.8）；</li>
 *         <li>对玩家造成 0.01 的 {@code DamageSource.generic} 伤害，随后回复 4 点生命、补 2 点饥饿；</li>
 *         <li>随机挑一个原初要素，再从背包里随机挑一根该要素还有余量的法杖，给它补
 *             {@code (int)((5.0 + random * 15.0) * 1.5 * obeliskDrainerVisMult)} 点 Vis
 *             （原版走 {@code ItemWandCasting#addVis}）。</li>
 *       </ul>
 *   </li>
 *   <li>此外每 tick 还有 17.5% 概率在方尖碑周围补一道随机弧光；客户端另撒 wisp 与 portal 粒子；</li>
 *   <li>堆叠上限 1、稀有度 EPIC；{@code getWarp} 返回 4。</li>
 * </ol>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code setItemInUse} → {@code Player#startUsingItem}；{@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「遍历已加载 TileEntity」在 1.21.1 没有公开 API</b>：{@code Level} 不再暴露 tile entity 总表，
 *       这里改成以玩家为中心的方块立方体扫描，只认 {@code TCBlocks.ELDRITCH_OBELISK}，
 *       再用 {@code level.isLoaded} 保证只看已加载区块，最后按原版口径复核距离，语义一致；</li>
 *   <li>NBT 的三个 double 坐标 → 数据组件 {@link FRDataComponents#DEVOURER_TARGET}（{@code BlockPos}）；</li>
 *   <li>自定义网络包画的闪电 → 原版 {@link ParticleTypes#ELECTRIC_SPARK}（与霹雳咒书同一种画法），
 *       不引入自定义网络；客户端 wisp / portal 粒子 → 服务端 {@code ServerLevel#sendParticles}；</li>
 *   <li>{@code thaumcraft:zap} → {@link SoundEvents#FIREWORK_ROCKET_BLAST}，沿用霹雳咒书的替代方案；</li>
 *   <li><b>「给背包法杖补 Vis」这一支完整保留</b>：Thaumaturge 的 {@link WandVisHelper#addVis} 就是
 *       {@code ItemWandCasting#addVis} 的直接对应物，欧米伽之核也是这么给法杖补 Vis 的
 *       （两者的寻杖逻辑完全相同，见 {@link #findWandWithRoom}）。</li>
 * </ul>
 *
 * <p><b>与 1.12.2 移植版（RE）的关系</b>：RE 把这件物品改成了「吸收咒波裂隙（Flux Rift）、右键开关」，
 * 本仓库现成的 {@code item.ItemDevourerOfTheVoid*.lore} 与 {@code fr.text.ObeliskDrainer.*} 文案
 * 也是照 RE 写的。本项目以 1.7.10 为唯一行为参照，所以<b>行为按 1.7.10 复刻，并已把这些文案
 * 改回 1.7.10 的「神秘方尖碑 + 引导充能」版本</b>（键名不变，只改文本）。
 *
 * <p>这件物品<b>不消耗也不储存 Vis</b>：原版从头到尾只用「附近有没有方尖碑」做门槛，
 * 所以它没有实现 {@code FRRechargable}，也没有冷却。
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
        // 抽取、治疗、音效与粒子都只在服务端做；客户端交给服务端同步
        //（原版客户端那几颗 wisp / portal 粒子同样改成服务端 sendParticles）。
        if (level.isClientSide()) {
            return;
        }

        BlockPos target = stack.get(FRDataComponents.DEVOURER_TARGET.get());
        if (target == null || !isValidTarget(level, player, target)) {
            // 原版：目标方尖碑消失、换掉，或玩家走远到 16 格之外时立即停止使用。
            player.stopUsingItem();
            return;
        }

        int interval = FRConfig.DEVOURER_OF_THE_VOID_PULSE_INTERVAL.get();
        // 原版条件 count % 30 == 0 且 count != getMaxItemUseDuration（第一个 tick 不触发）。
        if (remainingUseDuration <= 0
                || remainingUseDuration % interval != 0
                || remainingUseDuration == USE_DURATION) {
            return;
        }

        drain(level, player, target);

        // 原版每 tick 17.5% 概率在方尖碑周围补一道随机弧光（imposeArcLightning）。
        if (level.random.nextFloat() <= ARC_LIGHTNING_CHANCE && level instanceof ServerLevel server) {
            Vec3 to = obeliskTop(target);
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    to.x + (level.random.nextDouble() - 0.5D) * 4.0D,
                    to.y + (level.random.nextDouble() - 0.5D) * 4.0D,
                    to.z + (level.random.nextDouble() - 0.5D) * 4.0D,
                    12, 0.3D, 0.3D, 0.3D, 0.05D);
        }
    }

    /** 抽一次：闪电 + 音效 + 自伤 0.01 + 回血 4 + 补饥饿 2 + 给背包里的法杖补 Vis。 */
    private static void drain(Level level, Player player, BlockPos target) {
        drawLightning(level, player, obeliskTop(target));

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

    /**
     * 从玩家中心到方尖碑顶端画一道带抖动的电弧。
     *
     * <p>对应原版 {@code SuperpositionHandler.imposeLightning}。这里用
     * {@link ParticleTypes#ELECTRIC_SPARK} 沿连线撒点近似，不引入自定义网络包；
     * 末尾那几颗 portal 粒子对应原版客户端在方尖碑上方撒的 portal。
     */
    private static void drawLightning(Level level, Player player, Vec3 to) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 from = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) * 3.0D));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / (double) steps;
            double x = from.x + (to.x - from.x) * t + (level.random.nextDouble() - 0.5D) * 0.3D;
            double y = from.y + (to.y - from.y) * t + (level.random.nextDouble() - 0.5D) * 0.3D;
            double z = from.z + (to.z - from.z) * t + (level.random.nextDouble() - 0.5D) * 0.3D;
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.sendParticles(ParticleTypes.PORTAL, to.x, to.y - 0.25D, to.z, 5, 0.2D, 1.0D, 0.2D, 0.05D);
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
        // 1.7.10 原版的五行结构（第 2 行接感知距离）。文案已按 1.7.10 原版改写，见类注释最后一段。
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid1.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid2.lore",
                (int) FRConfig.DEVOURER_OF_THE_VOID_RANGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid3.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid4.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid5.lore"));
    }
}
