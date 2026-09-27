package com.beiwu.forgottenrelics_plus.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * 模组自有的工具材质。
 *
 * <p>1.7.10 用 {@code EnumHelper.addToolMaterial("PARADOXICALSTUFF", 4, 3000, 16.0F, -4.0F, 100)}
 * 造了一个材质；1.21.1 换成了 {@link Tier} 接口，数值一一对应：
 *
 * <table>
 *   <tr><th>原版参数</th><th>1.21.1</th><th>值</th></tr>
 *   <tr><td>harvestLevel</td><td>{@link Tier#getIncorrectBlocksForDrops()}（按等级选标签）</td><td>4 → 下界合金级</td></tr>
 *   <tr><td>maxUses</td><td>{@link Tier#getUses()}</td><td>3000</td></tr>
 *   <tr><td>efficiency</td><td>{@link Tier#getSpeed()}</td><td>16.0</td></tr>
 *   <tr><td>damage</td><td>{@link Tier#getAttackDamageBonus()}</td><td>-4.0（负值：本体伤害很低，威力来自悖论效果）</td></tr>
 *   <tr><td>enchantability</td><td>{@link Tier#getEnchantmentValue()}</td><td>100</td></tr>
 * </table>
 */
public final class FRToolTiers {

    /**
     * 悖论材质。
     *
     * <p>修复材料取了下界合金锭：原版走的是 Thaumcraft 的 {@code IRepairable}（用 Vis 修），
     * 1.21.1 没有对应机制，而悖论之刃本身每 20 tick 自修 1 点耐久，所以这里只是补齐接口要求。
     */
    public static final Tier PARADOX = new Tier() {
        @Override
        public int getUses() {
            return 3000;
        }

        @Override
        public float getSpeed() {
            return 16.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return -4.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 100;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.NETHERITE_INGOT);
        }
    };

    private FRToolTiers() {
    }
}
