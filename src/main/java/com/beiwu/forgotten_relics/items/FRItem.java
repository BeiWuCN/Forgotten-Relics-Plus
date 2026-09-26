package com.beiwu.forgottenrelics_re.items;

import net.minecraft.world.item.Item;

/**
 * 普通物品基类（不可装备）。
 *
 * <p>对应 1.12.2 原版的 {@code ItemModBase}。原版在构造器里做三件事：设置创造模式标签页、
 * 设置注册名、设置未本地化名。1.21.1 里这些全部交给 {@code DeferredRegister} 与创造模式标签页
 * 统一处理（注册名来自注册表键，翻译键由物品自身的描述 ID 推导），所以这里只剩一个透传
 * {@code Properties} 的构造器。
 */
public class FRItem extends Item {

    public FRItem(Properties properties) {
        super(properties);
    }
}
