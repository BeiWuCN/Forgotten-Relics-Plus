package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.api.IncomingDamageBehaviour;
import com.beiwu.forgottenrelics_plus.api.WearerTickBehaviour;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.FRDamageTypes;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 七阳之戒（Ring of The Seven Suns），1.12.2 原版 {@code ItemDarkSunRing}，戒指槽。
 *
 * <p>原版行为：
 * <ul>
 *   <li>佩戴者着火时立刻熄灭；</li>
 *   <li>完全抵消任何超过 {@code darkSunRingDamageCap}（默认 100）点的单次伤害，并弹一条提示；</li>
 *   <li>火焰与岩浆伤害不生效，反而按伤害量回血（可用 {@code darkSunRingHealLimit} 加冷却）；</li>
 *   <li>有 {@code darkSunRingDeflectChance}（默认 20%）的概率把攻击反弹给攻击者；</li>
 *   <li>另有 25% 概率让受到的普通攻击变得更强——这是原版刻意设计的诅咒面。</li>
 * </ul>
 *
 * <p>原版把「反弹」写在 {@code LivingAttackEvent}、「免除」写在 {@code LivingHurtEvent}，
 * 两段相关逻辑分散在两个事件里，先后顺序只能靠猜。这里全部收进一个
 * {@link #onIncomingDamage}，顺序照着原版的实际执行次序排：免疫 → 反弹 → 免除 → 诅咒。
 *
 * <p>它同时是 Thaumaturge 的「可充能」物品，储量上限来自 {@code darkSunRingMaxCharge}（默认 500）。
 */
public class ItemDarkSunRing extends FRCurioItem
        implements FRRechargable, WearerTickBehaviour, IncomingDamageBehaviour {

    /** 原版 {@code Main.darkRingDamageNegations}：七阳之戒视为免疫的三类伤害。 */
    private static final List<ResourceKey<DamageType>> NEGATIONS =
            List.of(DamageTypes.LAVA, DamageTypes.IN_FIRE, DamageTypes.ON_FIRE);

    /** 让攻击变强的概率。原版硬编码 0.25，未做配置。 */
    private static final double AMPLIFY_CHANCE = 0.25D;

    public ItemDarkSunRing(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.DARK_SUN_RING_MAX_CHARGE.get();
    }

    /** 紧跟在浑浊之核的闪避之后（原版 {@code :204} / {@code :249}）。 */
    @Override
    public int priority() {
        return 20;
    }

    @Override
    public void onWearerTick(LivingEntity wearer, ItemStack stack) {
        if (wearer.isOnFire()) {
            wearer.clearFire();
        }
    }

    @Override
    public void onIncomingDamage(LivingIncomingDamageEvent event, Player wearer, ItemStack stack) {
        DamageSource source = event.getSource();

        // 1. 火焰/岩浆伤害不生效，反而回血。配置开启时用 invulnerableTime 做冷却，避免站火里瞬间回满。
        if (NEGATIONS.stream().anyMatch(source::is)) {
            if (FRConfig.DARK_SUN_RING_HEAL_LIMIT.get()) {
                if (wearer.invulnerableTime == 0) {
                    wearer.heal(event.getAmount());
                    event.setInvulnerabilityTicks(20);
                }
            } else {
                wearer.heal(event.getAmount());
            }
            event.setCanceled(true);
            return;
        }

        // 2. 概率反弹：要求有无敌帧可用，弹回去后自己也进无敌，与原版一致。
        if (source.getEntity() != null
                && !FRDamageTypes.isAbsolute(source)
                && wearer.invulnerableTime == 0
                && wearer.getRandom().nextDouble() <= FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get()) {
            wearer.invulnerableTime = 20;
            source.getEntity().hurt(source, event.getAmount());
            event.setCanceled(true);
            return;
        }

        // 3. 单次伤害超过上限 → 完全免除，并给玩家一条提示（对应原版 NotificationMessage type 2）。
        if (event.getAmount() > FRConfig.DARK_SUN_RING_DAMAGE_CAP.get() && !FRDamageTypes.isAbsolute(source)) {
            wearer.displayClientMessage(Component.translatable("notification.overdamage_block"), true);
            event.setCanceled(true);
            return;
        }

        // 4. 代价面：25% 概率让这次攻击变得更疼。
        if (!FRDamageTypes.isAbsolute(source) && wearer.getRandom().nextDouble() <= AMPLIFY_CHANCE) {
            event.setAmount(event.getAmount() + event.getAmount() * (float) wearer.getRandom().nextDouble());
        }
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemDarkSunRing1.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing2_1.lore",
                Math.round(FRConfig.DARK_SUN_RING_DAMAGE_CAP.get())));
        tooltip.add(Component.translatable("item.ItemDarkSunRing3.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing4.lore",
                Math.round(FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get() * 100.0D)));
        tooltip.add(Component.translatable("item.ItemDarkSunRing5.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing6.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing7.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing8.lore"));
        tooltip.add(Component.translatable("item.ItemDarkSunRing9.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.FRRing.lore"));
    }
}
