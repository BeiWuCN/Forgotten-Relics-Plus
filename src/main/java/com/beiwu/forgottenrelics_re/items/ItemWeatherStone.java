package com.beiwu.forgottenrelics_re.items;

import com.beiwu.forgottenrelics_re.config.FRConfig;
import com.beiwu.forgottenrelics_re.utils.CooldownHelper;
import com.beiwu.forgottenrelics_re.utils.SoundHelper;
import java.util.List;
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
 * 符文天象石（原版叫 Runic Stone，注册名 weather_stone）。
 *
 * <p>1.12.2 原版（{@code ItemWeatherStone}）逻辑：
 * <ul>
 *   <li>手持右键且<b>正在下雨</b>时开始蓄力，蓄力时长 {@code weatherStoneChannelDuration}（默认 60 tick），
 *       动作是 {@code EnumAction.BOW}；</li>
 *   <li>蓄力结束的那一 tick（{@code count == 1}）：扣 Vis，播放音效，停止降雨，
 *       并把下次降雨时间设为 24000 + 随机 0~976000 tick；</li>
 *   <li>使用后进入 {@code weatherStoneCooldown}（默认 100 tick）的冷却，冷却期间不能再蓄力；</li>
 *   <li>Vis 消耗 = {@code weatherStoneVisCost}（默认 25）乘 {@code weatherStoneVisMult}（默认 1.0），
 *       储量上限 {@code weatherStoneMaxCharge}（默认 100）。</li>
 * </ul>
 *
 * <p>1.21.1 对应关系：
 * <ul>
 *   <li>{@code onUsingTick} → {@code Item#onUseTick}；{@code getMaxItemUseDuration} → {@code getUseDuration}；
 *       {@code EnumAction.BOW} → {@code UseAnim.BOW}；</li>
 *   <li>「玩家背包里的 Vis」在 1.21.1 已不存在，改为 Thaumaturge 的
 *       {@code RechargeAccess.consumeCharge}，扣的是物品自身存储的 Vis（由周围灵气补充）；</li>
 *   <li>{@code WorldInfo.setRaining(false)} 在 1.21.1 由
 *       {@code ServerLevel#setWeatherParameters} 一并设置晴/雨/雷三个计时器，
 *       正好对应原版「停雨 + 重设降雨时间」两句话。</li>
 * </ul>
 */
public class ItemWeatherStone extends FRRechargableItem {

    public ItemWeatherStone(Properties properties) {
        super(properties);
    }

    /** 单次施法的 Vis 消耗，对应原版 {@code getVisCost()}。 */
    public int getVisCost() {
        return (int) (FRConfig.WEATHER_STONE_VIS_COST.get() * FRConfig.WEATHER_STONE_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.WEATHER_STONE_MAX_CHARGE.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // 原版 EnumAction.BOW
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FRConfig.WEATHER_STONE_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版：只有在下雨且不在冷却中时才开始蓄力；否则右键没有任何效果。
        if (level.isRaining() && !CooldownHelper.isOnCooldown(player)) {
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        Vec3 center = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
        // 蓄力过程中持续冒蓝色光点（原版用 Botania 的 wispFX，这里换成原版的末影/灵魂粒子）。
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i <= 24; i++) {
                double xm = (Math.random() - 0.5D) * 0.15D;
                double ym = (Math.random() - 0.5D) * 0.15D;
                double zm = (Math.random() - 0.5D) * 0.15D;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        center.x, center.y, center.z, 1, xm, ym, zm, 0.05D);
            }
        }
        // 原版的触发条件是 count == 1，即蓄力的最后一 tick。
        if (remainingUseDuration != 1) {
            return;
        }
        if (!level.isRaining()) {
            return;
        }
        if (!com.leclowndu93150.thaumaturge.api.items.RechargeAccess.consumeCharge(stack, player, getVisCost())) {
            return;
        }
        SoundHelper.play(level, center.x, center.y, center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS,
                1.0F, (float) (0.8D + Math.random() * 0.2D));
        if (level instanceof ServerLevel serverLevel) {
            // 停雨，并把下一次降雨推迟 24000 ~ 1000000 tick（原版是 24000 + 随机 0~976000）。
            serverLevel.setWeatherParameters(24000 + (int) (Math.random() * 976000.0D), 0, false, false);
        }
        CooldownHelper.setCooldown(player, FRConfig.WEATHER_STONE_COOLDOWN.get());
    }

    @Override
    protected void appendShiftTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ItemWeatherStone1.lore"));
        tooltip.add(Component.translatable("item.FREmpty.lore"));
        tooltip.add(Component.translatable("item.ItemWeatherStone2.lore", getVisCost()));
    }
}
