package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.api.FRRechargable;
import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.utils.CooldownHelper;
import com.beiwu.forgottenrelics_re.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_re.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 食尸鬼之颅（Ghastly Skull），注册名 {@code ghastly_skull}。
 *
 * <p><b>这是一件原创补完物品，不是对照复刻。</b>1.7.10 原版里 {@code ItemGhastlySkull} 有类、
 * 有贴图、有注册（因此能在创造模式里看到），也分配了要素
 * （{@code DEATH 16 + SOUL 14 + DARKNESS 14 + ENTROPY 10 + VOID 8 + MAGIC 8}），
 * 但它的逻辑从未写完：
 *
 * <pre>
 * public ItemStack onItemRightClick(...) {
 *     player.setHealth(1.0f);              // 把自己血量设成 1
 *     Vector3 look = ...multiply(16.0);    // 算了半天的落点，后面没用上
 *     if (world.isRemote) {
 *         array.add(array);                // 把数组加进它自己
 *         Main.log.info("The array: " + array);   // 打印调试日志
 *     }
 * }
 * </pre>
 *
 * <p>而且 {@code en_US.lang} 与 {@code ru_RU.lang} 里都没有它的任何条目（连名字都没有），
 * 所以它在游戏里显示的是未翻译的键名。1.12.2 移植版（RE）也没有移植它。
 *
 * <p>本实现只做两件事：<b>沿用原作者已经定下的意图</b>——那段残代码里「把生命压到 1 点」＋
 * 「向视线前方 16 格取点」两个骨架，以及上面那组倾向死亡的要素——把它补成一个能用的遗物：
 *
 * <ul>
 *   <li>右键<b>献祭</b>：记录当前生命，然后把生命压到 1 点（血量不足
 *       {@code ghastlySkullMinHealth} 时不允许使用，免得变成自杀）；</li>
 *   <li>在视线方向最多 {@code ghastlySkullBurstRange} 格处（被方块挡住就在方块前）引爆一次
 *       <b>怨魂冲击</b>，半径 {@code ghastlySkullBurstRadius} 格内的活体生物受到
 *       {@code 献祭生命 × ghastlySkullDamageMult} 的伤害（上限 {@code ghastlySkullMaxDamage}），
 *       伤害类型是模组自有的「夺魂」（对应原版 {@code DamageSourceSoulDrain}）；</li>
 *   <li>被击中的目标附加凋零，时限与等级可配置；</li>
 *   <li><b>食尸</b>：每命中一个目标回复 {@code ghastlySkullHealPerTarget} 点生命，
 *       但总量不会超过献祭前的生命值，所以不会变成白赚的恢复手段；</li>
 *   <li>消耗 Vis，并有 {@code ghastlySkullCooldown} 的冷却；附带
 *       {@code ghastlySkullWarp} 点扭曲（原版就是 3）。</li>
 * </ul>
 *
 * <p>两处刻意的设计决定：
 * <ul>
 *   <li>献祭后给 20 tick 的无敌帧。原版那段代码把血量直接写成 1，紧接着还要面向近战距离的敌人，
 *       没有这一下保护就是纯粹的送死，与「以血换力」的设计意图不符；</li>
 *   <li>夺魂伤害属于「绝对伤害」（见 {@link FRDamageTypes#isAbsolute}），与原作者那份
 *       {@code isDamageTypeAbsolute} 的分类一致——它靠的是直接夺取灵魂，不该被护甲或本模组的
 *       减伤/吸收拦下。</li>
 * </ul>
 */
public class ItemGhastlySkull extends FRItem implements FRRechargable, IWarpingGear {

    public ItemGhastlySkull(Properties properties) {
        super(properties);
    }

    /** 单次献祭的 Vis 消耗。 */
    public int getVisCost() {
        return FRConfig.GHASTLY_SKULL_VIS_COST.get();
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.GHASTLY_SKULL_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.GHASTLY_SKULL_WARP.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 献祭、伤害结算、粒子都只在服务端做；客户端交给服务端同步。
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (CooldownHelper.isOnCooldown(player)) {
            return InteractionResultHolder.fail(stack);
        }

        float before = player.getHealth();
        if (before < FRConfig.GHASTLY_SKULL_MIN_HEALTH.get()) {
            player.displayClientMessage(Component.translatable("item.ItemGhastlySkullFail.lore"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            player.displayClientMessage(Component.translatable("item.ItemGhastlySkullNoVis.lore"), true);
            return InteractionResultHolder.fail(stack);
        }

        // 落点沿用原版那段没写完的代码：视线方向最多 16 格（现在可配置），
        // 区别是这里真的把它用上了，并且被方块挡住时会停在方块前。
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 center = findImpactPoint(level, player, eye, FRConfig.GHASTLY_SKULL_BURST_RANGE.get());

        // 献祭：把生命压到 1 点。血量的「亏空」就是这次冲击的威力来源。
        float sacrificed = Math.max(0.0F, before - 1.0F);
        player.setHealth(1.0F);
        player.invulnerableTime = 20;

        float damage = (float) Math.min(sacrificed * FRConfig.GHASTLY_SKULL_DAMAGE_MULT.get(),
                FRConfig.GHASTLY_SKULL_MAX_DAMAGE.get());
        int hits = detonate(level, player, center, damage);

        // 食尸：按命中数回复，但补不回献祭掉的那部分以上。
        float wanted = hits * FRConfig.GHASTLY_SKULL_HEAL_PER_TARGET.get().floatValue();
        if (wanted > 0.0F) {
            player.heal(Math.min(wanted, before - player.getHealth()));
        }

        // SOUL_ESCAPE 在 1.21.1 是注册表里的 Holder<SoundEvent>，取值要显式 .value()。
        SoundHelper.play(level, center.x, center.y, center.z, SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.PLAYERS, 1.0F, 0.7F);
        SoundHelper.play(level, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_HURT,
                SoundSource.PLAYERS, 0.6F, 0.6F);
        CooldownHelper.setCooldown(player, FRConfig.GHASTLY_SKULL_COOLDOWN.get());
        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    /** 沿视线做一次方块射线检测，命中就落在方块前，否则用最大距离处的点。 */
    private static Vec3 findImpactPoint(Level level, Player player, Vec3 eye, int range) {
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(range));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    /**
     * 引爆怨魂冲击，返回被命中的目标数（食尸的回复量按它计算）。
     *
     * <p>命中判定用「到落点的距离 <= 半径」，而不是直接吃 {@code AABB} 的结果——
     * {@code inflate} 出来的是立方体，角落上的目标实际已超出球形范围。
     */
    private static int detonate(Level level, Player caster, Vec3 center, float damage) {
        double radius = FRConfig.GHASTLY_SKULL_BURST_RADIUS.get();
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != caster && entity.isAlive());
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        int hits = 0;
        for (LivingEntity target : targets) {
            if (target.distanceToSqr(center) > radius * radius) {
                continue;
            }
            if (target.hurt(FRDamageTypes.source(level, FRDamageTypes.SOUL_DRAIN), damage)) {
                hits++;
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                    FRConfig.GHASTLY_SKULL_WITHER_DURATION.get(),
                    FRConfig.GHASTLY_SKULL_WITHER_LEVEL.get(), true, false));
        }

        // 鬼火由施术者脚下窜向落点，再在落点炸开一圈。
        for (double t = 0.0D; t <= 1.0D; t += 0.1D) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    caster.getX() + (center.x - caster.getX()) * t,
                    caster.getY() + 1.0D + (center.y - caster.getY()) * t,
                    caster.getZ() + (center.z - caster.getZ()) * t,
                    2, 0.05D, 0.05D, 0.05D, 0.01D);
        }
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                center.x, center.y, center.z, 60, radius * 0.4D, radius * 0.4D, radius * 0.4D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SOUL,
                center.x, center.y, center.z, 24, radius * 0.3D, radius * 0.3D, radius * 0.3D, 0.02D);
        return hits;
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemGhastlySkull1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemGhastlySkull2.lore", getVisCost()));
    }
}
