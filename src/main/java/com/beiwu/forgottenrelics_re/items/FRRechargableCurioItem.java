package com.beiwu.forgottenrelics_re.items;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 「既能在饰品栏佩戴、又能被 Vis 充能」的物品基类。
 *
 * <p>1.12.2 原版里，七阳之戒、神圣护身符、湮灭护符都是同时实现 {@code IBauble} 与
 * {@code IRechargable} 的；1.21.1 这边对应 {@code ICurioItem} 与 {@link IRechargable}。
 * 两个接口本可以分别实现，但它们的通用行为（右键装备、Shift 展开式 tooltip、HUD 充能显示）
 * 完全一致，所以合并成一个基类，省得三份重复代码。
 */
public abstract class FRRechargableCurioItem extends FRCurioItem implements IRechargable {

    protected FRRechargableCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        // 原版这三个饰品返回的都是 EnumChargeDisplay.NORMAL，即始终在 HUD 上显示储量。
        return ChargeDisplay.NORMAL;
    }
}
