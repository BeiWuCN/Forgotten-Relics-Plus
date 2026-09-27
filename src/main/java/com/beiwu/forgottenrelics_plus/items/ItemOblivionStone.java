package com.beiwu.forgottenrelics_plus.items;

import com.beiwu.forgottenrelics_plus.config.FRConfig;
import com.beiwu.forgottenrelics_plus.registry.FRDataComponents;
import com.beiwu.forgottenrelics_plus.registry.FRItems;
import com.beiwu.forgottenrelics_plus.utils.SoundHelper;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 湮灭之钥（Keystone of The Oblivion），注册名 {@code oblivion_stone}，
 * 1.7.10 原版 {@code ItemOblivionStone}（研究键 {@code OblivionStone}）。
 *
 * <p><b>1.7.10 原版行为（唯一行为参照）</b>：
 * <ol>
 *   <li>它的全部状态都压在物品 metadata（damage）上：{@code 0/1/2} 是启用中的三种模式
 *       （0 完全吸收 / 1 保留一组 / 2 超额吸收），{@code 100/101/102} 是同一模式的停用态；
 *       堆叠上限 1、稀有度 epic、附带 2 点扭曲；</li>
 *   <li>右键（不潜行）：{@code 0→1→2→0} 循环切换模式（停用态在 {@code 100/101/102} 之间同样循环），
 *       播放 {@code random.orb} 音效（音调 {@code 0.8 + random*0.2}）；</li>
 *   <li>潜行 + 右键：在「启用」与「停用」之间切换（{@code ±100}），播放
 *       {@code dftoolkit:sound.hhoff}（停用）/ {@code dftoolkit:sound.hhon}（启用）；</li>
 *   <li>两种右键最后都会 {@code player.swingItem()}；</li>
 *   <li>{@code onUpdate}（背包内每 tick，仅在 {@code ticksExisted % 10 == 0} 时）：
 *       若为启用态且物品带 NBT，就调用 {@code consumeStuff} 按模式吞噬已绑定的物品；</li>
 *   <li>{@code consumeStuff} 只处理主背包（{@code inventory.mainInventory}，1.7.10 为 36 格）：
 *       <ul>
 *         <li>把「除钥匙石外」的所有非空栈记进一张 slot→stack 表；</li>
 *         <li><b>模式 0（完全吸收）</b>：按绑定顺序，把背包里所有匹配的栈全部清空；</li>
 *         <li><b>模式 1（保留一组）</b>：按绑定顺序，每类只留「数量最大的一栈」，其余全部清空
 *             （数量相同则留下标号最小的那一栈）；</li>
 *         <li><b>模式 2（超额吸收）</b>：只有在主背包<b>没有任何空槽</b>时才生效；按绑定顺序找到第一个
 *             存在的类型，只清掉其中「数量最小的一栈」（数量相同则清标号最大的那一栈），
 *             然后本次结束（每次判定最多吞一栈）。</li>
 *       </ul>
 *   </li>
 *   <li>合成：把钥匙石与「任意一件物品」放进合成栏 → 那件物品被登记进可吞噬清单；
 *       只放钥匙石 → 清空整个清单（原版 {@code RecipeOblivionStone}，见
 *       {@link com.beiwu.forgottenrelics_plus.recipes.RecipeOblivionStone}）；</li>
 *   <li>tooltip：Shift 展开 15 行说明；Ctrl 展开已绑定物品清单（超过 softCap 时随机显示 softCap 条）；
 *       默认显示「Shift/Ctrl 提示 + 当前模式」。</li>
 * </ol>
 *
 * <p><b>1.21.1 对应关系与取舍</b>：
 * <ul>
 *   <li>物品 metadata → 数据组件 {@link FRDataComponents#OBLIVION_MODE}，
 *       <b>取值口径原样照抄</b>（{@code 0..2} 启用、{@code +100} 停用），方便与 1.7.10 逐行对照；</li>
 *   <li>原版两个平行 NBT 数组 {@code SupersolidID}（数值物品 id）/ {@code SupersolidMetaID}（metadata）
 *       → 数据组件 {@link FRDataComponents#OBLIVION_BOUND_ITEMS}。1.21.1 没有数值物品 id，
 *       也没有「metadata 表示变体」的概念，所以直接存整份样本 {@link ItemStack}（自带组件）。
 *       匹配语义按原版对照映射：<b>可损毁物品按物品类型通配</b>（原版 meta = -1），
 *       <b>其余按「物品 + 组件」精确匹配</b>（原版按「物品 + metadata」精确匹配）；</li>
 *   <li>{@code onUpdate} → {@code Item#inventoryTick}；原版在客户端也会跑（改的是客户端本地背包），
 *       这里只在服务端执行，避免客户端自行改背包导致不同步；</li>
 *   <li>{@code random.orb} → {@link SoundEvents#EXPERIENCE_ORB_PICKUP}；
 *       {@code dftoolkit:sound.hhoff} / {@code hhon}（来自 DFToolkit，1.21.1 没有对应物）
 *       → {@link SoundEvents#STONE_BUTTON_CLICK_OFF} / {@link SoundEvents#STONE_BUTTON_CLICK_ON}，
 *       用一对原版开/关音效保留「启用 / 停用」的听感区分，并经 {@link SoundHelper} 统一压低音量；</li>
 *   <li>合成栏登记 / 清空保留为真正的<b>自定义合成配方</b>（不是右键 GUI），见配方类注释；</li>
 *   <li>这件物品<b>不消耗也不储存 Vis</b>，原版从头到尾没有 Vis 逻辑，所以不实现 {@code FRRechargable}。</li>
 * </ul>
 *
 * <p><b>与 1.12.2 移植版（RE）的关系</b>：RE 的行为与 1.7.10 一致（只是把 metadata 读写换成了 1.12.2 的
 * {@code setItemDamage}），本件的 lang 文案（{@code item.OblivionStone*.lore} 等）与 1.7.10 逐条一致，
 * 无需改写。
 */
public class ItemOblivionStone extends FRItem implements IWarpingGear {

    /** 停用态相对启用态的偏移：原版用 {@code damage += 100} 表示「停用」。 */
    private static final int INACTIVE_OFFSET = 100;

    /** 原版 {@code onUpdate} 的判定间隔：{@code ticksExisted % 10 == 0}。 */
    private static final int CHECK_INTERVAL = 10;

    public ItemOblivionStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    // ---- 模式读写（对应原版直接读写的 item metadata） ----

    /** 读取模式编码（{@code 0/1/2} 启用，{@code 100/101/102} 停用）。没有组件时按原版初始 damage 0 处理。 */
    public static int getMode(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.OBLIVION_MODE.get(), 0);
    }

    /** 写回模式编码。 */
    public static void setMode(ItemStack stack, int mode) {
        stack.set(FRDataComponents.OBLIVION_MODE.get(), mode);
    }

    /** 是否处于启用态（原版 {@code damage < 100}）。 */
    public static boolean isActive(ItemStack stack) {
        return getMode(stack) < INACTIVE_OFFSET;
    }

    // ---- 绑定清单读写 ----

    /** 读取已绑定的样本清单；没有组件时返回空表。 */
    public static List<ItemStack> getBoundItems(ItemStack stack) {
        return stack.getOrDefault(FRDataComponents.OBLIVION_BOUND_ITEMS.get(), List.of());
    }

    /**
     * 把一件样本规范化成清单里的一条记录。
     *
     * <p>数量压到 1；可损毁物品把耐久损伤归零（反正匹配时按物品类型通配，不记具体损伤）。
     */
    public static ItemStack normalizeSample(ItemStack sample) {
        ItemStack entry = sample.copy();
        entry.setCount(1);
        if (entry.isDamageableItem()) {
            entry.setDamageValue(0);
        }
        return entry;
    }

    /**
     * 候选栈是否命中某条绑定记录。
     *
     * <p>对照原版 {@code (item == item) & (meta == -1 | meta == candidateMeta)}：
     * 可损毁物品按物品类型通配；其余按「物品 + 组件」精确匹配。
     */
    public static boolean matchesBound(ItemStack candidate, ItemStack bound) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (bound.isDamageableItem()) {
            return candidate.is(bound.getItem());
        }
        return ItemStack.isSameItemSameComponents(candidate, bound);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return FRConfig.OBLIVION_STONE_WARP.get();
    }

    // ---- 右键：切换模式 / 启停 ----

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 原版在客户端与服务端都会改 metadata 并播音；这里只在服务端改状态，
        // 音效也一并在服务端播放（原版 = 双端各播一次，1.21.1 服务端播放即广播给附近玩家，避免双响）。
        if (!level.isClientSide()) {
            int mode = getMode(stack);
            if (player.isShiftKeyDown()) {
                if (mode < INACTIVE_OFFSET) {
                    // 原版 damage += 100 并播 sound.hhoff（停用）
                    setMode(stack, mode + INACTIVE_OFFSET);
                    SoundHelper.play(level, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_OFF,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    // 原版 damage -= 100 并播 sound.hhon（启用）
                    setMode(stack, mode - INACTIVE_OFFSET);
                    SoundHelper.play(level, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_ON,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            } else {
                int next;
                if (mode == 0 || mode == 1 || mode == INACTIVE_OFFSET || mode == INACTIVE_OFFSET + 1) {
                    next = mode + 1;
                } else if (mode == 2 || mode == INACTIVE_OFFSET + 2) {
                    next = mode - 2;
                } else {
                    next = mode;
                }
                setMode(stack, next);
                // 原版 random.orb：音量 1.0、音调 0.8 + random*0.2
                SoundHelper.play(level, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 1.0F, (float) (0.8D + level.random.nextDouble() * 0.2D));
            }
        }
        // 原版显式调用 player.swingItem()；1.21.1 返回 SUCCESS 即会挥臂。
        return InteractionResultHolder.success(stack);
    }

    // ---- 背包内按模式吞噬 ----

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        // 原版：damage >= 100（停用）或没有 NBT（还没绑定任何东西）时什么都不做。
        if (!isActive(stack)) {
            return;
        }
        List<ItemStack> bound = getBoundItems(stack);
        if (bound.isEmpty()) {
            return;
        }
        consumeStuff(player, bound, getMode(stack));
    }

    /**
     * 按模式吞掉主背包里已绑定的物品。逐条对照原版 {@code ItemOblivionStone#consumeStuff}。
     *
     * @param player 执行吞噬的玩家
     * @param bound  已绑定的样本清单
     * @param mode   模式编码（{@code 0/1/2}；调用前已排除停用态）
     */
    public static void consumeStuff(Player player, List<ItemStack> bound, int mode) {
        Inventory inventory = player.getInventory();
        // 主背包：1.21.1 的 Inventory.items 是 36 格（armor / offhand 不在原版「mainInventory」范围内）。
        int size = inventory.items.size();

        // 原版的 filledStacks：主背包里非空槽位数（包含钥匙石自身）。
        int filledStacks = 0;
        for (int slot = 0; slot < size; slot++) {
            if (!inventory.items.get(slot).isEmpty()) {
                filledStacks++;
            }
        }

        switch (mode) {
            case 0 -> consumeTotal(inventory, bound, size);
            case 1 -> consumeLastStack(inventory, bound, size);
            case 2 -> consumeExcess(inventory, bound, size, filledStacks);
            default -> {
                // 原版只认 0/1/2；其余编码不做任何事。
            }
        }
    }

    /** 模式 0「完全吸收」：把背包里所有命中绑定清单的栈全部清空。 */
    private static void consumeTotal(Inventory inventory, List<ItemStack> bound, int size) {
        // 原版先建 slot→stack 表、再按绑定顺序遍历并清空；这里等价地对每个绑定条目整表扫一遍。
        boolean any = false;
        for (int slot = 0; slot < size; slot++) {
            if (!inventory.items.get(slot).isEmpty() && !isOblivionStone(inventory.items.get(slot))) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (ItemStack entry : bound) {
            for (int slot = 0; slot < size; slot++) {
                ItemStack candidate = inventory.items.get(slot);
                if (candidate.isEmpty() || isOblivionStone(candidate)) {
                    continue;
                }
                if (matchesBound(candidate, entry)) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    /** 模式 1「保留一组」：每个绑定条目只留下数量最大的一栈（数量相同留下标号最小的一栈）。 */
    private static void consumeLastStack(Inventory inventory, List<ItemStack> bound, int size) {
        for (ItemStack entry : bound) {
            List<Integer> slots = matchingSlots(inventory, entry, size);
            if (slots.size() <= 1) {
                continue;
            }
            int keep = slots.get(0);
            for (int slot : slots) {
                if (inventory.items.get(slot).getCount() > inventory.items.get(keep).getCount()) {
                    keep = slot;
                }
            }
            for (int slot : slots) {
                if (slot != keep) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * 模式 2「超额吸收」：只有在主背包一个空槽都没有时才生效；
     * 找到第一个存在的绑定类型，只清掉其中数量最小的一栈（数量相同清标号最大的一栈），随后结束。
     */
    private static void consumeExcess(Inventory inventory, List<ItemStack> bound, int size, int filledStacks) {
        if (filledStacks < size) {
            return;
        }
        for (ItemStack entry : bound) {
            List<Integer> slots = matchingSlots(inventory, entry, size);
            if (slots.isEmpty()) {
                continue;
            }
            int target = slots.get(0);
            for (int slot : slots) {
                int current = inventory.items.get(slot).getCount();
                int best = inventory.items.get(target).getCount();
                if (current < best || (current == best && slot > target)) {
                    target = slot;
                }
            }
            inventory.setItem(target, ItemStack.EMPTY);
            return;
        }
    }

    /** 主背包里命中该绑定条目的槽位列表（不含钥匙石自身）。 */
    private static List<Integer> matchingSlots(Inventory inventory, ItemStack entry, int size) {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) {
            ItemStack candidate = inventory.items.get(slot);
            if (candidate.isEmpty() || isOblivionStone(candidate)) {
                continue;
            }
            if (matchesBound(candidate, entry)) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static boolean isOblivionStone(ItemStack stack) {
        return stack.is(FRItems.OBLIVION_STONE.get());
    }

    // ---- tooltip：Shift / Ctrl / 默认三分支 ----

    /**
     * 覆写整个 tooltip，<b>不调用 {@code super}</b>。
     *
     * <p>原因：{@link FRItem} 的基类逻辑只处理「Shift 展开」两态，而本件是三态
     * （Shift 说明 / Ctrl 清单 / 默认提示），直接按 1.7.10 的分支结构重写更清楚。
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.OblivionStone1.lore"));
            tooltip.add(Component.translatable("item.OblivionStone2.lore"));
            tooltip.add(Component.translatable("item.OblivionStone2_more.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone3.lore"));
            tooltip.add(Component.translatable("item.OblivionStone4.lore"));
            tooltip.add(Component.translatable("item.OblivionStone5.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone6.lore"));
            tooltip.add(Component.translatable("item.OblivionStone7.lore"));
            tooltip.add(Component.translatable("item.OblivionStone8.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone9.lore"));
            tooltip.add(Component.translatable("item.OblivionStone10.lore"));
            tooltip.add(Component.translatable("item.OblivionStone11.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            tooltip.add(Component.translatable("item.OblivionStone12.lore"));
            tooltip.add(Component.translatable("item.OblivionStone13.lore"));
            tooltip.add(Component.translatable("item.OblivionStone14.lore"));
            tooltip.add(Component.translatable("item.OblivionStone15.lore"));
        } else if (Screen.hasControlDown()) {
            tooltip.add(Component.translatable("item.OblivionStoneCtrlList.lore"));
            addBoundListTooltip(stack, tooltip);
        } else {
            tooltip.add(Component.translatable("item.FRShiftTooltip.lore"));
            tooltip.add(Component.translatable("item.OblivionStoneCtrlTooltip.lore"));
            tooltip.add(Component.translatable("item.FREmpty.lore"));
            int mode = getMode(stack);
            Component modeName = mode < INACTIVE_OFFSET
                    ? Component.translatable("item.OblivionMode" + mode + ".lore")
                    : Component.translatable("item.OblivionStoneDeactivated.lore");
            tooltip.add(Component.translatable("item.OblivionStoneMode.lore").append(" ").append(modeName));
        }
        // 原版三种分支最后都会补一行空行。
        tooltip.add(Component.translatable("item.FREmpty.lore"));
    }

    /**
     * Ctrl 展开的绑定清单。
     *
     * <p>原版：条目数不超过 softCap 就全列；否则随机列 softCap 条。
     * 原版那段随机取下标写的是 {@code (int)(Math.random() * 30.0)}，当清单只有 29 或 30 条时会数组越界 ——
     * 这里按「在原清单范围内随机」实现（修正该越界，语义不变）。
     */
    private static void addBoundListTooltip(ItemStack stack, List<Component> tooltip) {
        List<ItemStack> bound = getBoundItems(stack);
        if (bound.isEmpty()) {
            return;
        }
        int softCap = FRConfig.OBLIVION_STONE_SOFT_CAP.get();
        if (bound.size() <= softCap) {
            for (ItemStack entry : bound) {
                tooltip.add(Component.literal(" - " + entry.getHoverName().getString()).withStyle(ChatFormatting.GOLD));
            }
        } else {
            for (int i = 0; i < softCap; i++) {
                ItemStack entry = bound.get(ThreadLocalRandom.current().nextInt(bound.size()));
                tooltip.add(Component.literal(" - " + entry.getHoverName().getString()).withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
