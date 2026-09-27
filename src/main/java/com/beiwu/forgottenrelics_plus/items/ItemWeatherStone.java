package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.api.FRRechargable;
import com.beiwu.forgottenrelics_plus.client.FRParticles;
import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.utils.CooldownHelper;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import java.util.List;
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
 * 符文天象石（RE 里叫 Runic Stone，注册名 weather_stone）。
 *
 * <p>RE 的 {@code ItemWeatherStone} 逻辑：
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
 *       正好对应 RE「停雨 + 重设降雨时间」两句话。</li>
 * </ul>
 */
public class ItemWeatherStone extends FRItem implements FRRechargable {

    public ItemWeatherStone(Properties properties) {
        super(properties);
    }

    /** 单次施法的 Vis 消耗，对应 RE 的 {@code getVisCost()}。 */
    public int getVisCost() {
        return (int) (FRConfig.WEATHER_STONE_VIS_COST.get() * FRConfig.WEATHER_STONE_VIS_MULT.get());
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return FRConfig.WEATHER_STONE_MAX_CHARGE.get();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // RE 的 EnumAction.BOW
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FRConfig.WEATHER_STONE_CHANNEL_DURATION.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // RE 只有在下雨且不在冷却中时才开始蓄力；否则右键没有任何效果。
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
        // 蓄力过程中冒蓝色光点。原版这段在 onUsingTick 的 count == 1 分支里，
        // 也就是「蓄力完成的那一 tick 一次性撒 25 颗」；本移植刻意改放在每 tick：
        // 这里的结构不变，只把粒子换成下面的对应物。
        // 原版：for (i = 0; i <= 24; i++) wispFX(vec, r=0, g=0.3+rand*0.5, b=0.8+rand*0.2,
        //        size=0.2+rand*0.2, xm/ym/zm=(rand-0.5)*0.15, maxAgeMul=1.0)。
        if (level instanceof ServerLevel serverLevel) {
            FRParticles.serverWispBurst(serverLevel, center.x, center.y, center.z,
                    0.0F,
                    0.3F + level.random.nextFloat() * 0.5F,
                    0.8F + level.random.nextFloat() * 0.2F,
                    0.2F + level.random.nextFloat() * 0.2F, 1.0F,
                    25, 0.0D, 0.043D);
        }
        // RE 的触发条件是 count == 1，即蓄力的最后一 tick。
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
            // 停雨，并把下一次降雨推迟 24000 ~ 1000000 tick（RE 是 24000 + 随机 0~976000）。
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
