package com.beiwu.forgottenrelicsplus.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public final class FRToolTiers {

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
