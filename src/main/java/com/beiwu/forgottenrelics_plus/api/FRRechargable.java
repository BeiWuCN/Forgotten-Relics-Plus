package com.beiwu.forgottenrelics_plus.api;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 模组的可充能物品标记。
 *
 * <p>1.12.2 原版是让物品继承 {@code ItemBaubleBase} 再实现 {@code IRechargable}，于是「饰品」
 * 「可充能」这些正交的特性被迫挤进一条继承链；这里改成接口 + 默认方法，想要哪个特性就实现哪个
 * 接口，继承链不再膨胀。
 *
 * <p>模组所有可充能物品的 HUD 显示方式都是 {@code NORMAL}（与原版
 * {@code EnumChargeDisplay.NORMAL} 一致），所以只需要覆写储量上限。
 */
public interface FRRechargable extends IRechargable {

    @Override
    default ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        return ChargeDisplay.NORMAL;
    }
}
