package com.beiwu.forgottenrelics_re;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.items.ItemOblivionAmulet;
import com.beiwu.forgottenrelics_re.registry.FRDataComponents;
import com.beiwu.forgottenrelics_re.registry.FRItems;
import com.beiwu.forgottenrelics_re.utils.CurioHelper;
import com.beiwu.forgottenrelics_re.utils.FRDamageTypes;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/**
 * 伤害相关事件（双端）。
 *
 * <p>对应 1.12.2 原版 {@code RelicsEventHandler} 里 {@code onEntityAttacked}（LivingAttackEvent）
 * 与 {@code onEntityHurt}（LivingHurtEvent）这两个方法。1.21.1 把「能不能打中」与「打中多少」
 * 合并成了 {@link LivingIncomingDamageEvent}：它可取消、可改数值，还带无敌帧设置接口，
 * 一个事件就够用了。
 *
 * <p>各物品的规则都照抄原版：
 * <ul>
 *   <li><b>远古之庇护</b>：佩戴者减伤；没佩戴的人被打时，把 40% 伤害转给附近的佩戴者；</li>
 *   <li><b>七阳之戒</b>：超上限伤害直接免除并提示；火焰/岩浆伤害转为治疗；按概率反弹攻击；
 *       另有 25% 概率让攻击变强；</li>
 *   <li><b>湮灭护符</b>：吸收全部伤害并累加储存，代价是扣 Vis；</li>
 *   <li><b>神圣护符</b>：延长无敌帧。</li>
 * </ul>
 */
@EventBusSubscriber(modid = ForgottenRelics.MOD_ID)
public final class FRDamageEvents {

    /** 原版 {@code Main.darkRingDamageNegations}：七阳之戒视为免疫的三类伤害。 */
    private static final List<net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType>>
            DARK_RING_NEGATIONS = List.of(DamageTypes.LAVA, DamageTypes.IN_FIRE, DamageTypes.ON_FIRE);

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();

        if (handleDarkSunRing(event, player, source)) {
            return;
        }
        handleOblivionAmulet(event, player, source);
        handleDeificAmulet(event, player);
        handleAncientAegis(event, player, source);
    }

    /**
     * 七阳之戒。返回 {@code true} 表示已经取消掉这次伤害，后续规则不必再看。
     *
     * <p>原版顺序：先判火焰/岩浆免疫，再判超上限免除，再判反弹，最后才是「让攻击变强」。
     */
    private static boolean handleDarkSunRing(LivingIncomingDamageEvent event, Player player, DamageSource source) {
        if (!CurioHelper.isEquipped(player, FRItems.DARK_SUN_RING.get())) {
            return false;
        }

        // 火焰/岩浆伤害不生效，反而回血。原版可选用 hurtResistantTime 做冷却，避免站火里瞬间回满。
        if (DARK_RING_NEGATIONS.stream().anyMatch(source::is)) {
            if (FRConfig.DARK_SUN_RING_HEAL_LIMIT.get()) {
                if (player.invulnerableTime == 0) {
                    player.heal(event.getAmount());
                    event.setInvulnerabilityTicks(20);
                }
            } else {
                player.heal(event.getAmount());
            }
            event.setCanceled(true);
            return true;
        }

        // 概率反弹：要求有无敌帧可用，弹回去后自己也进无敌，与原版一致。
        // 原版把反弹写在 LivingAttackEvent、把下面的免除写在 LivingHurtEvent，
        // 前者先触发，所以这里保持「先反弹、再判上限」的顺序。
        if (source.getEntity() != null
                && !FRDamageTypes.isAbsolute(source)
                && player.invulnerableTime == 0
                && player.getRandom().nextDouble() <= FRConfig.DARK_SUN_RING_DEFLECT_CHANCE.get()) {
            player.invulnerableTime = 20;
            source.getEntity().hurt(source, event.getAmount());
            event.setCanceled(true);
            return true;
        }

        // 单次伤害超过上限 → 完全免除，并给玩家一条提示（对应原版 NotificationMessage type 2）。
        if (event.getAmount() > FRConfig.DARK_SUN_RING_DAMAGE_CAP.get() && !FRDamageTypes.isAbsolute(source)) {
            player.displayClientMessage(Component.translatable("notification.overdamage_block"), true);
            event.setCanceled(true);
            return true;
        }

        // 代价面：25% 概率让这次攻击变得更疼（原版也是硬编码 0.25，未做配置）。
        if (!FRDamageTypes.isAbsolute(source) && player.getRandom().nextDouble() <= 0.25D) {
            event.setAmount(event.getAmount() + event.getAmount() * (float) player.getRandom().nextDouble());
        }
        return false;
    }

    /**
     * 湮灭护符：吸收伤害并累加储存。
     *
     * <p>代价是按「伤害 × 8 × Vis 倍率」扣 Vis；Vis 不够就照常挨打。
     */
    private static void handleOblivionAmulet(LivingIncomingDamageEvent event, Player player, DamageSource source) {
        if (FRDamageTypes.isAbsolute(source)) {
            return;
        }
        Optional<SlotResult> found = findCurio(player, FRItems.OBLIVION_AMULET.get());
        if (found.isEmpty()) {
            return;
        }
        ItemStack amulet = found.get().stack();
        int cost = (int) (event.getAmount() * 8.0F * FRConfig.OBLIVION_AMULET_VIS_MULT.get());
        if (!RechargeAccess.consumeCharge(amulet, player, cost)) {
            return;
        }
        ItemOblivionAmulet.setStoredDamage(amulet, ItemOblivionAmulet.getStoredDamage(amulet) + event.getAmount());
        event.setCanceled(true);
    }

    /**
     * 神圣护符：把无敌帧拉长。
     *
     * <p>1.12.2 是在 {@code onWornTick} 里直接写 {@code hurtResistantTime}；1.21.1 那个字段只读，
     * 官方给的口子是 {@code setInvulnerabilityTicks}，所以就挪到这个事件里做。
     * 冷却记在物品数据组件上，冷却没到就不延长。
     */
    private static void handleDeificAmulet(LivingIncomingDamageEvent event, Player player) {
        if (!FRConfig.DEIFIC_AMULET_INVINCIBILITY.get()) {
            return;
        }
        Optional<SlotResult> found = findCurio(player, FRItems.DEIFIC_AMULET.get());
        if (found.isEmpty()) {
            return;
        }
        ItemStack amulet = found.get().stack();
        int cooldown = amulet.getOrDefault(FRDataComponents.INVINCIBILITY_COOLDOWN.get(), 0);
        if (cooldown > 0) {
            return;
        }
        event.setInvulnerabilityTicks(FRConfig.DEIFIC_AMULET_INVINCIBILITY_EXTENSION.get());
        amulet.set(FRDataComponents.INVINCIBILITY_COOLDOWN.get(),
                FRConfig.DEIFIC_AMULET_INVINCIBILITY_COOLDOWN.get());
    }

    /**
     * 远古之庇护：减伤与伤害转嫁。
     *
     * <p>转嫁只对「没戴庇护的人」生效——把 40% 伤害丢给 32 格内一位佩戴者，自己少挨 40%。
     * 佩戴者本人则按配置比例直接减伤。
     */
    private static void handleAncientAegis(LivingIncomingDamageEvent event, Player player, DamageSource source) {
        if (FRDamageTypes.isAbsolute(source)) {
            return;
        }
        if (CurioHelper.isEquipped(player, FRItems.ANCIENT_AEGIS.get())) {
            event.setAmount(event.getAmount()
                    * (1.0F - FRConfig.ANCIENT_AEGIS_DAMAGE_REDUCTION.get().floatValue()));
            return;
        }

        Player bearer = findNearbyBearer(player, 32.0D);
        if (bearer == null) {
            return;
        }
        float transferred = event.getAmount() * 0.4F;
        bearer.hurt(source, transferred);
        event.setAmount(event.getAmount() * 0.6F);
    }

    /** 在 32 格内随机找一位佩戴远古之庇护的玩家，对应原版 {@code findPlayerWithBauble}。 */
    private static Player findNearbyBearer(Player player, double radius) {
        List<Player> candidates = player.level().getEntitiesOfClass(Player.class,
                player.getBoundingBox().inflate(radius),
                other -> other != player && CurioHelper.isEquipped(other, FRItems.ANCIENT_AEGIS.get()));
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    /** 在饰品栏里找指定物品，拿到那一个具体的 ItemStack（数据组件存在它身上）。 */
    private static Optional<SlotResult> findCurio(LivingEntity entity, net.minecraft.world.item.Item item) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(item));
    }

    private FRDamageEvents() {
    }
}
