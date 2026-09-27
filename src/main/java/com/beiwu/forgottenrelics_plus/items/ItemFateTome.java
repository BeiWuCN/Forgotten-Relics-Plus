package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.DeathPreventionBehaviour;
import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 破碎的命运巨著（Tome of Broken Fates），注册名 {@code tome_of_broken_fates}，
 * 1.7.10 原版 {@code ItemFateTome}。整件物品都是「随身携带生效」，效果分三处：
 *
 * <ol>
 *   <li>致死免死（原版 {@code RelicsEventHandler#onPlayerDeath}）：主背包里第一本冷却为 0 的巨著，
 *       先抽六大原初要素各 100 点（合计 600）的 Vis，成功就取消死亡、回满血，再按
 *       {@code fateTomeBuffChance}（0.75）二选一施加效果——抗性提升 II / 生命恢复 II / 抗火 I，
 *       否则虚弱 III / 失明 I / 凋零 II；最后把冷却设成 {@code [MIN, MAX] * 20} tick 内的随机值
 *       （默认 30~90 秒），并在玩家处播爆裂特效与充能音效；</li>
 *   <li>冷却递减与通知（同一个 {@code onUpdate}）：每服务端 tick 把 {@code IFateCooldown} 减 1，
 *       减到 0 时发一条 HUD 通知；</li>
 *   <li>携带惩罚：每 tick 有 {@code fateTomeMultiHeldChance}（默认 1.6E-5，约六万分之一）的概率，
 *       在主背包里不止一本时引爆自己——清空所有巨著，对以玩家为中心 ±64 格内的所有活体（含玩家本人）
 *       各造成 {@code fateTomeDamage}（40000）点命运伤害并各爆一次 16 半径的爆炸，
 *       最后在玩家处再爆一发 100 半径的。</li>
 * </ol>
 *
 * <p>1.21.1 对应：致死拦截 → {@link DeathPreventionBehaviour}（原版是欧米伽之核 if、命运巨著 else if，
 * 所以 {@link #priority()} 必须排在欧米伽之核之后）；「从背包法杖抽 Vis」没有对应 API，
 * 按本移植的统一约定改成 {@link FRRechargable} 物品自身充能；原版 {@code IFateCooldown} → 数据组件
 * {@link FRDataComponents#FATE_COOLDOWN}；爆裂特效改服务端 {@code sendParticles}，
 * 音效换成 {@link SoundEvents#RESPAWN_ANCHOR_CHARGE}。
 *
 * <p><b>刻意偏离原版</b>：{@code func_72885_a(..., true, true)} 的两处爆炸改成 {@code fire = false}、
 * {@code ExplosionInteraction.NONE}——保留威力、对实体的伤害与击退，但不破坏方块、不引燃；
 * 破坏地形属于 bug，不要打开。
 *
 * <p>冷却不用共用 {@code CooldownHelper}：1.7.10 与 RE 的巨著冷却
 * 都是物品自己的 {@code IFateCooldown}，每本各自记账，tooltip 要显示剩余秒数、冷却结束还要发通知；
 * 改用共用冷却会让一次免死锁住所有施法遗物，是明显的行为回归。因此与神圣护符的 {@code ICooldown}
 *（{@link FRDataComponents#INVINCIBILITY_COOLDOWN}）同一处理。
 *
 * <p>附带 7 点扭曲（全移植第二高，仅次于悖论之刃的 8）。
 */
public class ItemFateTome extends FRItem implements FRRechargable, IWarpingGear, DeathPreventionBehaviour {

    /**
     * 排在欧米伽之核（100）之后：原版 {@code onPlayerDeath} 里欧米伽之核是 if、命运巨著是 else if，
     * 带着欧米伽之核时绝不会触发命运巨著（不会扣 Vis、也不会进冷却）。
     */
    private static final int PRIORITY = 200;

    /** 免死时那圈爆裂粒子的颜色（Thaumcraft 爆裂特效的近似，偏冷的淡青色）。 */
    private static final int BURST_COLOR = 0x9FE8FF;

    public ItemFateTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** 单次免死的 Vis 消耗：原版六大要素各 100 点之和 600 点，再乘 {@code fateTomeVisMult}。 */
    public int getVisCost() {
        return (int) (FRConfig.TOME_OF_BROKEN_FATES_VIS_COST.get() * FRConfig.TOME_OF_BROKEN_FATES_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.TOME_OF_BROKEN_FATES_MAX_CHARGE.get();
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.TOME_OF_BROKEN_FATES_WARP.get();
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    /**
     * 对应原版 {@code ItemFateTome#func_77663_a}：冷却递减 + 冷却结束通知 + 携带多本的自毁判定。
     *
     * <p>原版还顺手初始化一个 {@code IFateID}（随机 int），但它从头到尾没有被任何地方读过，
     * 这里不移植（详见 {@link FRDataComponents#FATE_COOLDOWN}）。
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        // ① 每 tick 把冷却减 1；减到 0 时发 HUD 通知（原版减到 0 时 SuperpositionHandler.sendNotification(player, 1)）。
        int cooldown = stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0);
        if (cooldown > 0) {
            int remaining = cooldown - 1;
            stack.set(FRDataComponents.FATE_COOLDOWN.get(), remaining);
            if (remaining == 0) {
                player.displayClientMessage(Component.translatable("notification.fate_cooldown_over"), true);
            }
        }
        // ② 持有惩罚：原版 Math.random() <= fateTomeMultiHeldChance 且主背包里不止一本时自毁。
        //    原版每本巨著各自跑一次这个方法，所以带着两本时每 tick 会掷两次骰子，这里同样。
        if (player.getRandom().nextDouble() <= FRConfig.TOME_OF_BROKEN_FATES_MULTI_HELD_CHANCE.get()
                && countInMainInventory(player) > 1) {
            insanelyDisastrousConsequences(player);
        }
    }

    /**
     * 携带者自己将要死亡（原版 {@code RelicsEventHandler#onPlayerDeath} 里 {@code Main.itemFateTome} 那一段）。
     */
    @Override
    public void onLethalDamage(LivingDeathEvent event, Player player, ItemStack stack) {
        // 原版要求冷却恰好为 0；崭新巨著没有冷却组件，同样按 0 处理。
        if (stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0) > 0) {
            return;
        }
        // 原版先从背包法杖抽 600 点 Vis，抽不出来就什么都不发生（不取消死亡、不进冷却）。
        if (!RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        // 扣费成功后才设随机冷却；原版 fateTomeCooldownMAX == 0 时完全不设冷却。
        int maxSeconds = FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get();
        if (maxSeconds != 0) {
            int minTicks = FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MIN.get() * 20;
            int bonusTicks = maxSeconds * 20 - minTicks;
            // 原版 (int)(minCooldown + Math.random() * bonusCooldown)，这里逐字对应。
            int ticks = bonusTicks > 0
                    ? (int) (minTicks + player.getRandom().nextDouble() * bonusTicks)
                    : minTicks;
            stack.set(FRDataComponents.FATE_COOLDOWN.get(), ticks);
        }
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        applyFateEffects(player);
        playFateFeedback(player);
    }

    /** 原版 {@code triggerFateProtection} 里的那两段药水效果，概率与数值都是硬编码。 */
    private static void applyFateEffects(Player player) {
        if (player.getRandom().nextDouble() <= FRConfig.TOME_OF_BROKEN_FATES_BUFF_CHANCE.get()) {
            // 原版 PotionEffect(11, 200, 2) / (10, 500, 1) / (12, 1000, 0)。
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 500, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1000, 0));
        } else {
            // 原版 PotionEffect(18, 600, 2) / (15, 200, 0) / (20, 300, 1)。
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1));
        }
    }

    /**
     * 原版在玩家处调的 {@code imposeBurst(..., 1.5f)}（Thaumcraft 爆裂特效）与
     * {@code thaumcraft:runicShieldCharge} 音效。
     *
     * <p>这一段原版用的是 Thaumcraft 的 {@code proxy.burst}（<b>不是 Botania</b>），
     * 1.21.1 没有等价物，按「原版本来就不是 Botania 就保持原样」的口径继续用
     * 「一颗闪光 + 一簇淡青色 effect 粒子」近似；音效按本移植约定换成原版等价物，
     * 并过 {@link SoundHelper#play} 统一压音量。
     */
    private static void playFateFeedback(Player player) {
        if (player.level() instanceof ServerLevel server) {
            double y = player.getY() + 1.0D;
            // 原版同样是 imposeBurst（SuperpositionHandler:335/344，size 1.25）→ 本移植自带的 FXBurst。
            // 不要用 ParticleTypes.FLASH（巨大白色方片）：这里用同一族的青绿柔光精灵。
            FRParticles.serverWispBurst(server, player.getX(), y, player.getZ(),
                    0.0F,
                    (float) (0.8D + server.random.nextDouble() * 0.2D),
                    (float) (0.4D + server.random.nextDouble() * 0.6D),
                    1.25F, 1.0F, 1, 0.0D, 0.0D);
            server.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, BURST_COLOR),
                    player.getX(), y, player.getZ(), 32, 0.4D, 0.4D, 0.4D, 0.1D);
        }
        // 原版坐标是玩家 x/z + 0.5、y + 1.5。
        SoundHelper.play(player.level(), player.getX() + 0.5D, player.getY() + 1.5D, player.getZ() + 0.5D,
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * 原版 {@code SuperpositionHandler.insanelyDisastrousConsequences}：
     * 清空主背包里所有命运巨著，然后对 ±64 格内所有活体来一发 40000 的命运伤害并各自引爆，
     * 最后在玩家处爆一发 100 半径的。
     *
     * <p>伤害类型用 {@link FRDamageTypes#FATE}（原版 {@code DamageSourceFate}）；它属于「绝对伤害」，
     * 不会被七阳之戒、虚伪审判等拦截或转嫁——这是原版 {@code isDamageTypeAbsolute} 的行为。
     *
     * <p>注意原版的两处爆炸是 {@code newExplosion(..., true, true)}：带火焰、破坏方块。
     */
    private static void insanelyDisastrousConsequences(Player player) {
        Level level = player.level();
        Inventory inventory = player.getInventory();

        // ① 原版 while (inventory.hasItem(itemFateTome)) inventory.clearInventory(itemFateTome)：
        //    只清主背包（36 格），不含护甲与副手——原版的 hasItem/clearInventory 就是主背包。
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() == FRItems.TOME_OF_BROKEN_FATES.get()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }

        // ② 以玩家为中心取 ±range 的立方体（原版 AxisAlignedBB ±64），里面每个活体都吃伤害 + 爆炸。
        double range = FRConfig.TOME_OF_BROKEN_FATES_MULTI_HELD_RANGE.get();
        AABB area = new AABB(
                player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range);
        float damage = FRConfig.TOME_OF_BROKEN_FATES_DAMAGE.get().floatValue();
        float radius = FRConfig.TOME_OF_BROKEN_FATES_EXPLOSION_RADIUS.get().floatValue();
        // 原版 newExplosion(..., isFlaming=true, isSmoking=true) 会炸方块并引燃。这里刻意偏离原版：
        // 只保留爆炸对实体的伤害与击退，方块破坏与引燃都关掉——破坏地形属于 bug。
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            target.hurt(FRDamageTypes.source(level, FRDamageTypes.FATE), damage);
            level.explode(player, target.getX(), target.getY(), target.getZ(), radius, false, Level.ExplosionInteraction.NONE);
        }

        // ③ 最后在玩家自己的位置来一发大的。
        level.explode(player, player.getX(), player.getY(), player.getZ(),
                FRConfig.TOME_OF_BROKEN_FATES_BIG_EXPLOSION_RADIUS.get().floatValue(), false, Level.ExplosionInteraction.NONE);
    }

    /** 原版 {@code SuperpositionHandler.itemSearch} 只数主背包（36 格），这里照做。 */
    private static int countInMainInventory(Player player) {
        Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).getItem() == FRItems.TOME_OF_BROKEN_FATES.get()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 原版 Shift 展开的那几行。
     *
     * <p>原版的冷却范围那行拆成了 {@code ItemFateTome5_1} / {@code ItemFateTome5_2} 两个键，
     * RE（即现成 lang 的来源）把它们并成了带 {@code %1$s-%2$s} 占位的
     * {@code item.FateTome5.lore}；这里沿用合并后的键，数值实时取自配置。
     *
     * <p>原版还有一个 Ctrl 分支（{@code FRVisPerCast} + 逐要素成本），本移植共享基类 {@code FRItem}
     * 只实现 Shift 展开，且「按要素抽 Vis」已改成物品自身充能，该分支没有意义，故不实现
     * （与霹雳咒书、核子之怒、月耀咒书一致）；现成的 {@code item.FateTomeVisCost.lore} 因此不被引用。
     */
    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.FateTome1.lore"));
        tooltip.add(Component.translatable("item.FateTome2.lore"));
        tooltip.add(Component.translatable("item.FateTome3.lore"));
        tooltip.add(Component.translatable("item.FateTome4.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        if (FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get() != 0) {
            tooltip.add(Component.translatable("item.FateTome5.lore",
                    FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MIN.get(),
                    FRConfig.TOME_OF_BROKEN_FATES_COOLDOWN_MAX.get()));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
        }
        tooltip.add(Component.translatable("item.FateTome6.lore"));
        tooltip.add(Component.translatable("item.FateTome7.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FateTome8.lore"));
        tooltip.add(Component.translatable("item.FateTome9.lore"));
    }

    /**
     * 原版的剩余冷却行写在 Shift / Ctrl 两个分支<b>之外</b>，所以不带修饰键也一直显示；
     * 颜色码是每次渲染从 {@code FRCode1..15} 里随机抽一个（这是原版刻意为之的「命运不定」效果）。
     * 数字部分与原版一样用 {@code BigDecimal} 保留 1 位小数（{@code cooldown / 20.0} 秒）。
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int cooldown = stack.getOrDefault(FRDataComponents.FATE_COOLDOWN.get(), 0);
        if (cooldown <= 0) {
            return;
        }
        double seconds = BigDecimal.valueOf(cooldown / 20.0D).setScale(1, RoundingMode.HALF_UP).doubleValue();
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRCode" + ThreadLocalRandom.current().nextInt(1, 16) + ".lore")
                .append(Component.translatable("item.FateTomeCooldown.lore"))
                .append(" " + seconds + " ")
                .append(Component.translatable("item.FRSeconds.lore")));
    }
}
