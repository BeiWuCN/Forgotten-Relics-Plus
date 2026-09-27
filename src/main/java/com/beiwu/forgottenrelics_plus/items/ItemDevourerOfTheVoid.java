package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 虚空吞噬者（Devourer of The Void），注册名 {@code devourer_of_the_void}，1.7.10 原版
 * {@code ItemObeliskDrainer}（原版注册名 / 研究键都叫 ObeliskDrainer，显示名才是 Devourer of The Void）。
 *
 * <p>原版行为：
 * <ol>
 *   <li>{@code onItemRightClick}：遍历世界已加载的全部 {@code TileEntity}，找<b>同维度、距玩家
 *       （{@code getDistance(x, y+2.5, z)}）不超过 16 格</b>的 {@code TileEldritchObelisk}（神秘方尖碑）；
 *       找到就把它的坐标写进物品 NBT（{@code IDetectedX / IDetectedY / IDetectedZ}，三个 double）
 *       并 {@code setItemInUse(stack, 72000)}，动作是 {@code EnumAction.bow}；找不到就什么都不做；</li>
 *   <li>{@code onUsingTick}：每 tick 先复核「NBT 里的坐标仍是一个神秘方尖碑」且距离仍不超过 16 格，
 *       不满足就立刻停止使用；</li>
 *   <li>每当剩余使用时间满足 {@code count % 30 == 0}（且不是第一个 tick）时抽一次：
 *       <ul>
 *         <li>从方尖碑往玩家方向画一道闪电（{@code SuperpositionHandler.imposeLightning}）
 *             并播放 {@code thaumcraft:zap}（音量 1.0、音调 0.8）；</li>
 *         <li>对玩家造成 0.01 的 {@code DamageSource.generic} 伤害，随后回复 4 点生命、补 2 点饥饿；</li>
 *         <li>随机挑一个原初要素，再从背包里随机挑一根该要素还有余量的法杖，给它加
 *             {@code (int)((5.0 + Math.random() * 15.0) * 1.5 * obeliskDrainerVisMult)} 厘 Vis。</li>
 *       </ul>
 *   </li>
 *   <li>此外每 tick 有 17.5% 概率在方尖碑周围补一道随机弧光
 *       （{@code imposeArcLightning}），客户端再撒 wisp 与 portal 粒子；</li>
 *   <li>堆叠上限 1，稀有度 EPIC；{@code ThaumcraftApi.addWarpToItem(obeliskDrainer, 3)}，
 *       另外 {@code ItemObeliskDrainer#getWarp} 又返回 4（两套并存的数值）。</li>
 * </ol>
 *
 * <p>1.21.1 的对应关系：
 * <ul>
 *   <li>{@code onItemRightClick} → {@code Item#use}；{@code onUsingTick} → {@code Item#onUseTick}；
 *       {@code setItemInUse} → {@code Player#startUsingItem} / {@code Player#stopUsingItem}；
 *       {@code EnumAction.bow} → {@link UseAnim#BOW}；</li>
 *   <li><b>「遍历已加载 TileEntity」在 1.21.1 没有公开 API</b>（{@code Level} 不再暴露 tile entity 列表），
 *       这里改成以玩家为中心的方块立方体扫描：先看方块是不是
 *       {@code TCBlocks.ELDRITCH_OBELISK}，再复核距离，语义与原版一致（同样只看已加载区块，
 *       {@code level.isLoaded} 负责这一点）；</li>
 *   <li><b>NBT 坐标 → 数据组件</b>：{@code IDetectedX/Y/Z} 三个 double 合并成
 *       {@link FRDataComponents#DEVOURER_TARGET}（{@code BlockPos}）；</li>
 *   <li>自定义网络包画的 {@code imposeLightning} / {@code imposeArcLightning} → 原版
 *       {@link ParticleTypes#ELECTRIC_SPARK}（与霹雳咒书的雷电球同一种画法），<b>不引入自定义网络包</b>；</li>
 *   <li>{@code thaumcraft:zap} → {@link SoundEvents#FIREWORK_ROCKET_BLAST}，沿用霹雳咒书的替代方案
 *       （本模组对 Thaumcraft 音效一律换原版等价物）；</li>
 *   <li>原版的客户端 wisp / portal 粒子 → 服务端 {@code ServerLevel#sendParticles}，客户端不必自己算。</li>
 * </ul>
 *
 * <p><b>与原版的取舍（重要，逐条列明）</b>：
 * <ol>
 *   <li><b>Thaumaturge 的方尖碑根本没有「抽取」API</b>：{@code BlockEntityEldritchObelisk} 全文只有一个
 *       {@code serverTick}，作用是每 20 tick 给 6 格内的 {@code IEldritchMob} 上
 *       {@code DAMAGE_BOOST} / {@code REGENERATION}，既没有 Vis 池，也没有 get / drain 接口。
 *       原版「从方尖碑抽 Vis 再给法杖充能」因此无法逐字复刻。按项目约定，这里把「Vis」统一落到
 *       {@link FRRechargable} 的<b>物品自身充能</b>上，用 {@link RechargeAccess#consumeCharge}
 *       支付每次抽取（与霹雳咒书、错位之典同一条路）；方尖碑只作为「附近有没有可抽取的源」的开关；</li>
 *   <li><b>Vis 流向反了</b>：原版是净<b>产出</b>（给背包法杖补 Vis），本移植是净<b>消耗</b>
 *       （扣物品自身充能）。这是「Vis 一律用物品自身充能」约定带来的必然结果，也是本件与其它遗物
 *       最大的一处偏差。原版每脉冲产出 {@code (5 + rand*15) * 1.5} 厘 Vis（约 0.075~0.30 点），
 *       这里折成 {@code devourerOfTheVoidVisCost}（默认 1，即「一点 Vis 换 4 血 + 2 饥饿」，
 *       与原版文案「只需一点 Vis 就能恢复大量状态」相合），倍率沿用原版的
 *       {@code obeliskDrainerVisMult} → {@code devourerOfTheVoidVisMult}（默认 1.0）；</li>
 *   <li><b>原版「给背包法杖补 Vis」这一支没有保留</b>。技术上可以用
 *       {@code WandVisHelper} 照搬（欧米伽之核就是这么给法杖充能的），但那样本件会同时存在
 *       「产出」和「消耗」两条 Vis 通路，与约定冲突，故刻意不做，并在提交说明里写明；</li>
 *   <li><b>扭曲取 3 而不是 4</b>：1.7.10 里 {@code addWarpToItem(obeliskDrainer, 3)} 与
 *       {@code ItemObeliskDrainer#getWarp} 返回的 4 是两套并存的值，任务口径取 3，
 *       所以 {@link IWarpingGear#getWarp} 返回 {@code devourerOfTheVoidWarp}（默认 3）；</li>
 *   <li><b>文案与行为对不齐，是既有语言键造成的</b>：{@code item.ItemDevourerOfTheVoid*.lore}
 *       是 1.12.2 移植版按「咒波裂隙（Flux Rift）+ 右键开关」写的，而本次复刻的是 1.7.10 的
 *       「神秘方尖碑 + 拉弓引导」版本。按任务要求沿用既有键、不新造键，所以 tooltip 里的
 *       Flux Rifts / rift energy / toggle 字样保持原样，仅把可配置的数值（范围、单次消耗）
 *       接进对应占位符。这是已知的、无从在本次范围内消除的偏差；</li>
 *   <li>本件没有冷却：原版靠「必须站在方尖碑附近」和拉弓引导限速，未使用
 *       {@code SuperpositionHandler.setCasted}，所以这里也不引入 {@code CooldownHelper}。</li>
 * </ol>
 */
public class ItemDevourerOfTheVoid extends FRItem implements FRRechargable, IWarpingGear {

    /** 原版 {@code getMaxItemUseDuration} 返回 72000；实际节奏靠 {@code onUseTick} 每 30 tick 触发。 */
    private static final int USE_DURATION = 72000;

    /** 原版那段「随机弧光」的触发概率：{@code Math.random() <= 0.175}。 */
    private static final float ARC_LIGHTNING_CHANCE = 0.175F;

    public ItemDevourerOfTheVoid(Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * 单次抽取脉冲的 Vis 消耗：原版基础值乘 {@code obeliskDrainerVisMult}。
     *
     * <p>原版这一项是「产出」给法杖的 Vis；本移植按约定改成「消耗物品自身充能」，
     * 数值取整逻辑与霹雳咒书 / 错位之典一致。
     */
    public int getVisCost() {
        return (int) (FRConfig.DEVOURER_OF_THE_VOID_VIS_COST.get()
                * FRConfig.DEVOURER_OF_THE_VOID_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DEVOURER_OF_THE_VOID_MAX_CHARGE.get();
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

        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            // 原版靠方尖碑「产出」Vis，压根没有充能不足这一支；本移植改为消耗物品自身充能后，
            // 充能耗尽就停止引导（见类注释的取舍第 2 条）。
            player.stopUsingItem();
            return;
        }

        drain(level, player, target);
    }

    /** 抽一次：闪电 + 音效 + 自伤 0.01 + 回血 4 + 补饥饿 2 + 17.5% 的随机弧光。 */
    private static void drain(Level level, Player player, BlockPos target) {
        Vec3 to = new Vec3(target.getX() + 0.5D, target.getY() + 2.75D, target.getZ() + 0.5D);
        drawLightning(level, player, to);

        // 原版 thaumcraft:zap（音量 1.0、音调 0.8）；沿用霹雳咒书对 Thaumcraft 音效的替代方案。
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);

        // 原版：先 attackEntityFrom(DamageSource.generic, 0.01F)，再 heal(4.0F) 与
        // FoodStats.addStats(2, 1.0F)。1.21.1 的 FoodData#eat(food, saturationModifier) 语义相同。
        player.hurt(level.damageSources().generic(), 0.01F);
        player.heal(FRConfig.DEVOURER_OF_THE_VOID_HEAL.get().floatValue());
        player.getFoodData().eat(FRConfig.DEVOURER_OF_THE_VOID_HUNGER.get(), 1.0F);

        // 原版每 tick 17.5% 概率在方尖碑周围补一道随机弧光（imposeArcLightning）。
        if (level.random.nextFloat() <= ARC_LIGHTNING_CHANCE && level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    to.x + (level.random.nextDouble() - 0.5D) * 4.0D,
                    to.y + (level.random.nextDouble() - 0.5D) * 4.0D,
                    to.z + (level.random.nextDouble() - 0.5D) * 4.0D,
                    12, 0.3D, 0.3D, 0.3D, 0.05D);
        }
    }

    /**
     * 从玩家中心到方尖碑顶端画一道带抖动的电弧。
     *
     * <p>对应原版 {@code SuperpositionHandler.imposeLightning}：从方尖碑
     * {@code (x + 0.5, y + 2.75, z + 0.5)} 指向玩家、带随机 curve 的一束闪电。
     * 这里用 {@link ParticleTypes#ELECTRIC_SPARK} 沿连线撒点近似，不引入自定义网络包。
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
        // 原版客户端在 (x + 0.5, y + 2.5 ± 1, z + 0.5) 撒 5 颗 portal 粒子。
        server.sendParticles(ParticleTypes.PORTAL, to.x, to.y - 0.25D, to.z, 5, 0.2D, 1.0D, 0.2D, 0.05D);
    }

    /**
     * 对应原版 {@code onItemRightClick} 里那段「遍历已加载 TileEntity 找神秘方尖碑」。
     *
     * <p>1.21.1 没有公开的 tile entity 总表，所以改为以玩家为中心、边长 2×range 的立方体扫描：
     * 只看已加载方块（{@code level.isLoaded}）、只认方尖碑方块，并按原版口径
     * {@code getDistance(x, y + 2.5, z)} 复核距离。
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
            if (!level.getBlockState(pos).is(TCBlocks.ELDRITCH_OBELISK.get())) {
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
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof BlockEntityEldritchObelisk)) {
            return false;
        }
        double range = FRConfig.DEVOURER_OF_THE_VOID_RANGE.get();
        return player.position().distanceToSqr(pos.getX(), pos.getY() + 2.5D, pos.getZ()) <= range * range;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 沿用 1.7.10 原版的五行结构（第 2 行接范围、第 4 行接单次消耗）。
        // 文案本身取自项目既有的 item.ItemDevourerOfTheVoid*.lore（1.12.2 移植版按咒波裂隙写的），
        // 与本次复刻的神秘方尖碑行为并不完全对应，见类注释的取舍第 5 条。
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid1.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid2.lore",
                (int) FRConfig.DEVOURER_OF_THE_VOID_RANGE.get().doubleValue()));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid3.lore"));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid4.lore", getVisCost()));
        tooltip.add(Component.translatable("item.ItemDevourerOfTheVoid5.lore"));
    }
}
