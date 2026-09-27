package com.beiwu.forgottenrelics_plus.recipes;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.items.ItemOblivionStone;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.registry.FRRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 湮灭之钥的「绑定 / 清空」合成配方，1.7.10 原版 {@code RecipeOblivionStone}。
 *
 * <p><b>原版怎么做</b>：这是一个实现 {@code IRecipe} 的<b>无序动态配方</b>，挂在全局合成表里：
 * <ul>
 *   <li>{@code matches}：整个合成格里必须恰好有一把钥匙石，外加 0 或 1 件其它物品；
 *       且当有 1 件其它物品时，要求清单未达上限、该物品尚未被绑定（重复判定见下）；</li>
 *   <li>{@code getCraftingResult}：
 *       <ul>
 *         <li>钥匙石 + 1 件物品 → 复制钥匙石，把该物品的数值 id 追加到 {@code SupersolidID}、
 *             把 metadata 追加到 {@code SupersolidMetaID}（可损毁物品写 {@code -1}，表示按类型通配）；</li>
 *         <li>只有钥匙石 → 返回一把<b>没有 NBT</b> 的新钥匙石（metadata 即模式照旧），等于清空清单；</li>
 *         <li>其余情况（没有钥匙石 / 不止一件样品 / 两把钥匙石）→ 空结果，即不匹配。</li>
 *       </ul>
 *   </li>
 *   <li>重复判定：若清单里已存在同一物品且（该条目的 metadata 为 {@code -1}，或与新样本的
 *       metadata 相同）则拒绝（返回空），也就是「同一物品的同一种变体只能绑定一次」。</li>
 * </ul>
 *
 * <p><b>1.21.1 怎么对应</b>：1.7.10 的 {@code IRecipe} + 全局合成表在 1.21.1 对应
 * 「实现 {@link net.minecraft.world.item.crafting.CraftingRecipe} 的自定义配方 + 注册
 * {@link RecipeSerializer}」。这里直接继承原版的 {@code CustomRecipe}
 * （{@code crafting_special_*} 那一套的抽象基类）：
 * <ul>
 *   <li>{@code CraftingRecipe#getType()} 默认返回 {@code RecipeType.CRAFTING}，
 *       所以这张配方会被 {@code RecipeManager} 归进合成类型，工作台/背包 2×2 的
 *       {@code getRecipeFor(RecipeType.CRAFTING, ...)} 都能查到它，且 {@code isSpecial()} 为 true，
 *       与「动态产物」的语义一致；</li>
 *   <li>序列化器用原版通用的 {@link net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer}，
 *       配方 JSON 只需要写 {@code type} 与 {@code category}（见
 *       {@code data/forgotten_relics_plus/recipe/oblivion_stone_binding.json}）；</li>
 *   <li>{@code getRecipeSize() == 10}（原版）只影响配方排序，不影响可用性，
 *       所以 {@code canCraftInDimensions} 一律返回 true，2×2 也能用，与原版一致；</li>
 *   <li>metadata / 数值 id → 数据组件，见 {@link ItemOblivionStone}；
 *       重复判定的语义映射为：可损毁物品按物品类型判定（对应 {@code -1}），
 *       其余按「物品 + 组件」判定（对应「物品 + metadata」）。</li>
 * </ul>
 *
 * <p><b>与 1.7.10 的偏差</b>：1.7.10 里「钥匙石 + 样品」合成时，格子里的样品只被扣掉 1 个
 * （原版 {@code SlotCrafting} 每格扣 1），1.21.1 的 {@code ResultSlot} 同样是每格扣 1，
 * 因此「一整叠加的样品只消耗 1 个、其余留在格子里」的行为一致。
 */
public class RecipeOblivionStone extends CustomRecipe {

    public RecipeOblivionStone(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // matches 不允许产生副作用：bindingResult 全程只读输入、产物是副本，安全。
        return !bindingResult(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return bindingResult(input);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // 原版 getRecipeSize() 返回 10 只用于排序；实际在任何尺寸的合成格里都能匹配。
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FRRecipeSerializers.OBLIVION_STONE_BINDING.get();
    }

    /**
     * 统一计算产物：返回 {@link ItemStack#EMPTY} 表示这张配方不匹配。
     *
     * <p>{@code matches} 与 {@code assemble} 共用同一段判定，避免两处逻辑漂移。
     */
    private static ItemStack bindingResult(CraftingInput input) {
        ItemStack stone = ItemStack.EMPTY;
        List<ItemStack> samples = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(FRItems.OBLIVION_STONE.get())) {
                if (!stone.isEmpty()) {
                    // 合成格里有第二把钥匙石：不匹配（原版同样直接返回空）。
                    return ItemStack.EMPTY;
                }
                stone = stack;
            } else {
                samples.add(stack);
            }
        }
        if (stone.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (samples.isEmpty()) {
            // 只放钥匙石：清空清单，模式（damage）照旧。等价于原版 new ItemStack(stone, 1, damage)。
            ItemStack cleared = new ItemStack(FRItems.OBLIVION_STONE.get());
            cleared.set(FRDataComponents.OBLIVION_MODE.get(), ItemOblivionStone.getMode(stone));
            return cleared;
        }

        if (samples.size() != 1) {
            // 一把钥匙石 + 多件样品：不匹配。
            return ItemStack.EMPTY;
        }

        ItemStack sample = samples.get(0);
        List<ItemStack> bound = ItemOblivionStone.getBoundItems(stone);
        if (bound.size() >= FRConfig.OBLIVION_STONE_HARD_CAP.get()) {
            // 原版：arr.length >= oblivionStoneHardCap 时不再接受新条目。
            return ItemStack.EMPTY;
        }
        ItemStack entry = ItemOblivionStone.normalizeSample(sample);
        for (ItemStack existing : bound) {
            // 原版重复判定：同物品，且（已存条目按类型通配，或变体相同）→ 拒绝。
            if (existing.is(entry.getItem())
                    && (existing.isDamageableItem() || ItemStack.isSameItemSameComponents(existing, entry))) {
                return ItemStack.EMPTY;
            }
        }

        // 复制钥匙石（连模式与既有清单一起带走），追加新条目，数量固定 1。
        ItemStack result = stone.copy();
        result.setCount(1);
        List<ItemStack> next = new ArrayList<>(bound);
        next.add(entry);
        result.set(FRDataComponents.OBLIVION_BOUND_ITEMS.get(), List.copyOf(next));
        return result;
    }
}
