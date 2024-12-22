package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.GUIDetectionComponent;
import cn.hackedmc.urticaria.component.impl.player.SelectorDetectionComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.exploit.Disabler;
import cn.hackedmc.urticaria.module.impl.movement.InventoryMove;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.ItemUtil;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.value.impl.*;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;
import util.time.StopWatch;

@Rise
@ModuleInfo(name = "module.player.manager.name", description = "module.player.manager.description", category = Category.PLAYER)
public class Manager extends Module {
    private final ModeValue mode = new ModeValue("Click Time", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Fast"))
            .setDefault("Normal");
    private final BooleanValue sendPacket = new BooleanValue("Only Packet", this, false);
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 100, 150, 0, 500, 25);
    private final BooleanValue legit = new BooleanValue("Legit", this, false);

    private final BooleanValue swordValue = new BooleanValue("Sword", this, true);
    private final NumberValue swordSlot = new NumberValue("Sword Slot", this, 1, 1, 9, 1, () -> !swordValue.getValue());
    private final BooleanValue pickaxeValue = new BooleanValue("Pickaxe", this, true);
    private final NumberValue pickaxeSlot = new NumberValue("Pickaxe Slot", this, 2, 1, 9, 1, () -> !pickaxeValue.getValue());
    private final BooleanValue axeValue = new BooleanValue("Axe", this, true);
    private final NumberValue axeSlot = new NumberValue("Axe Slot", this, 3, 1, 9, 1, () -> !axeValue.getValue());
    private final BooleanValue shovelValue = new BooleanValue("Shovel", this, true);
    private final NumberValue shovelSlot = new NumberValue("Shovel Slot", this, 4, 1, 9, 1, () -> !shovelValue.getValue());
    private final BooleanValue blockValue = new BooleanValue("Block", this, true);
    private final NumberValue blockSlot = new NumberValue("Block Slot", this, 5, 1, 9, 1, () -> !blockValue.getValue());
    private final BooleanValue potionValue = new BooleanValue("Potion", this, true);
    private final NumberValue potionSlot = new NumberValue("Potion Slot", this, 6, 1, 9, 1, () -> !potionValue.getValue());
    private final BooleanValue bowValue = new BooleanValue("Bow", this, true);
    private final NumberValue bowSlot = new NumberValue("Bow Slot", this, 8, 1, 9, 1, () -> !bowValue.getValue());
    private final BooleanValue foodValue = new BooleanValue("Food", this, true);
    private final NumberValue foodSlot = new NumberValue("Food Slot", this, 9, 1, 9, 1, () -> !foodValue.getValue());

    private final int INVENTORY_ROWS = 4, INVENTORY_COLUMNS = 9, ARMOR_SLOTS = 4;
    private final int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;

    private final StopWatch stopwatch = new StopWatch();
    private int chestTicks, attackTicks, placeTicks;
    private boolean moved;
    public boolean open;
    private long nextClick;
    public Manager(){
        this.setKeyCode(Keyboard.KEY_X);
    }

    @EventLink
    private final Listener<Render2DEvent> onRender2D = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("fast"))
            work();
    };

    @EventLink()
    private final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        this.chestTicks++;
        this.attackTicks++;
        this.placeTicks++;

        if (mode.getValue().getName().equalsIgnoreCase("normal"))
            work();
    };

    private void work() {
        if (mc.thePlayer.ticksExisted <= 40 || PlayerUtil.isInLobby() || GUIDetectionComponent.inGUI()) {
            return;
        }

        if (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiFurnace || mc.currentScreen instanceof GuiBrewingStand) {
            this.chestTicks = 0;
        }

        // Calls stopwatch.reset() to simulate opening an inventory, checks for an open inventory to be legit.
        if (legit.getValue() && !(mc.currentScreen instanceof GuiInventory)) {
            this.stopwatch.reset();
            return;
        }

        if (!this.stopwatch.finished(this.nextClick) || this.chestTicks < 5 || this.attackTicks < 5 || this.placeTicks < 5 || mc.thePlayer.isUsingItem() || getModule(Scaffold.class).isEnabled()) {
            this.closeInventory();
            return;
        }

        if (!this.getModule(InventoryMove.class).isEnabled() && MoveUtil.isMoving()) {
            return;
        }

        this.moved = false;

        int helmet = -1;
        int chestplate = -1;
        int leggings = -1;
        int boots = -1;

        int sword = -1;
        int pickaxe = -1;
        int axe = -1;
        int shovel = -1;
        int bow = -1;
        int block = -1;
        int potion = -1;
        int food = -1;

        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (!ItemUtil.useful(stack)) {
                this.throwItem(i);
            }

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;
                final double reduction = ItemUtil.armorReduction(stack);

                switch (armor.armorType) {
                    case 0:
                        if (helmet == -1 || reduction > ItemUtil.armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) {
                            helmet = i;
                        }
                        break;

                    case 1:
                        if (chestplate == -1 || reduction > ItemUtil.armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) {
                            chestplate = i;
                        }
                        break;

                    case 2:
                        if (leggings == -1 || reduction > ItemUtil.armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) {
                            leggings = i;
                        }
                        break;

                    case 3:
                        if (boots == -1 || reduction > ItemUtil.armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) {
                            boots = i;
                        }
                        break;
                }
            }

            if (item instanceof ItemSword) {
                if (sword == -1 || ItemUtil.damage(stack) > ItemUtil.damage(mc.thePlayer.inventory.getStackInSlot(sword))) {
                    sword = i;
                }

                if (i != sword) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemPickaxe) {
                if (pickaxe == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                    pickaxe = i;
                }

                if (i != pickaxe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemAxe) {
                if (axe == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) {
                    axe = i;
                }

                if (i != axe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemSpade) {
                if (shovel == -1 || ItemUtil.mineSpeed(stack) > ItemUtil.mineSpeed(mc.thePlayer.inventory.getStackInSlot(shovel))) {
                    shovel = i;
                }

                if (i != shovel) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemBlock) {
                if (block == -1) {
                    block = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(block);

                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        block = i;
                    }
                }
            }

            if (item instanceof ItemBow) {
                if (bow == -1 || ItemUtil.bow(stack) > ItemUtil.bow(mc.thePlayer.inventory.getStackInSlot(bow))) {
                    bow = i;
                }

                if (i != bow) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemPotion) {
                if (potion == -1) {
                    potion = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(potion);

                    if (currentStack == null) {
                        continue;
                    }

                    final ItemPotion currentItemPotion = (ItemPotion) currentStack.getItem();
                    final ItemPotion itemPotion = (ItemPotion) item;

                    boolean foundCurrent = false;

                    for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                        if (e.getPotionID() == currentItemPotion.getEffects(currentStack).get(0).getPotionID() && e.getDuration() > 0) {
                            foundCurrent = true;
                            break;
                        }
                    }

                    boolean found = false;

                    for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                        if (e.getPotionID() == itemPotion.getEffects(stack).get(0).getPotionID() && e.getDuration() > 0) {
                            found = true;
                            break;
                        }
                    }

                    if (itemPotion.getEffects(stack) != null && currentItemPotion.getEffects(currentStack) != null) {
                        if ((PlayerUtil.potionRanking(itemPotion.getEffects(stack).get(0).getPotionID()) > PlayerUtil.potionRanking(currentItemPotion.getEffects(currentStack).get(0).getPotionID()) || foundCurrent) && !found) {
                            potion = i;
                        }
                    }
                }
            }

            if (item instanceof ItemFood) {
                if (food == -1) {
                    food = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(food);

                    if (currentStack == null) {
                        continue;
                    }

                    final ItemFood currentItemFood = (ItemFood) currentStack.getItem();
                    final ItemFood itemFood = (ItemFood) item;

                    if (itemFood.getSaturationModifier(stack) > currentItemFood.getSaturationModifier(currentStack) || (itemFood.hasEffect(stack) && !itemFood.hasEffect(currentStack))) {
                        food = i;
                    }
                }
            }
        }

        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;

                switch (armor.armorType) {
                    case 0:
                        if (i != helmet) {
                            this.throwItem(i);
                        }
                        break;

                    case 1:
                        if (i != chestplate) {
                            this.throwItem(i);
                        }
                        break;

                    case 2:
                        if (i != leggings) {
                            this.throwItem(i);
                        }
                        break;

                    case 3:
                        if (i != boots) {
                            this.throwItem(i);
                        }
                        break;
                }
            }
        }

        if (helmet != -1 && helmet != 39) {
            this.equipItem(helmet);
        }

        if (chestplate != -1 && chestplate != 38) {
            this.equipItem(chestplate);
        }

        if (leggings != -1 && leggings != 37) {
            this.equipItem(leggings);
        }

        if (boots != -1 && boots != 36) {
            this.equipItem(boots);
        }

        int slot;

        if (swordValue.getValue() && sword != -1 && sword != this.swordSlot.getValue().intValue() - 1) {
            slot = this.swordSlot.getValue().intValue() + 35;

            if (!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
                this.moveItem(sword, this.swordSlot.getValue().intValue() - 37);
            } else this.moveItem(slot);

        }

        if (pickaxeValue.getValue() && pickaxe != -1 && pickaxe != this.pickaxeSlot.getValue().intValue() - 1) {
            slot = this.pickaxeSlot.getValue().intValue() + 35;

            if (!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
                this.moveItem(pickaxe, this.pickaxeSlot.getValue().intValue() - 37);
            } else this.moveItem(slot);
        }

        if (axeValue.getValue() && axe != -1 && axe != this.axeSlot.getValue().intValue() - 1) {
            slot = this.axeSlot.getValue().intValue() + 35;

            if (!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
                this.moveItem(axe, this.axeSlot.getValue().intValue() - 37);
            } else this.moveItem(slot);
        }

        if (shovelValue.getValue() && shovel != -1 && shovel != this.shovelSlot.getValue().intValue() - 1) {
            this.moveItem(shovel, this.shovelSlot.getValue().intValue() - 37);
        }

        if (bowValue.getValue() && bow != -1 && bow != this.bowSlot.getValue().intValue() - 1) {
            slot = this.bowSlot.getValue().intValue() + 35;

            if (!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
                this.moveItem(bow, this.bowSlot.getValue().intValue() - 37);
            } else this.moveItem(slot);
        }

        if (blockValue.getValue() && block != -1 && block != this.blockSlot.getValue().intValue() - 1 && !this.getModule(Scaffold.class).isEnabled()) {
            this.moveItem(block, this.blockSlot.getValue().intValue() - 37);
        }

        if (potionValue.getValue() && potion != -1 && potion != this.potionSlot.getValue().intValue() - 1) {
            slot = this.potionSlot.getValue().intValue() + 35;

            if (!mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
                this.moveItem(potion, this.potionSlot.getValue().intValue() - 37);
            } else this.moveItem(slot);
        }

        if (foodValue.getValue() && food != -1 && food != this.foodSlot.getValue().intValue() - 1) {
            this.moveItem(food, this.foodSlot.getValue().intValue() - 37);
        }

        if (this.canOpenInventory() && !this.moved) {
            this.closeInventory();
        }
    }

    @EventLink()
    public final Listener<AttackEvent> onAttack = event -> {
        this.attackTicks = 0;
    };

    @Override
    protected void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
    }

    private void openInventory() {
        if (!this.open) {
            PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void closeInventory() {
        if (this.open) {
            PacketUtil.send(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private boolean canOpenInventory() {
        return this.getModule(InventoryMove.class).isEnabled() && !(mc.currentScreen instanceof GuiInventory);
    }

    private void throwItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            if (sendPacket.getValue())
                mc.playerController.windowClickOnlyPacket(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);
            else
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);

            this.nextClick = Math.round(MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getSecondValue().intValue()));
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void moveItem(final int slot, final int destination) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            if (sendPacket.getValue())
                mc.playerController.windowClickOnlyPacket(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);
            else
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);

            this.nextClick = Math.round(MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getSecondValue().intValue()));
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void equipItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            if (Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.watchdog.getValue() && Disabler.INSTANCE.watchdogDisabler.windowClick.getValue()) {
                mc.playerController.windowClick(0, this.slot(slot), 0, 0, mc.thePlayer);
                mc.playerController.windowClick(0, 5 + ((ItemArmor) mc.thePlayer.inventory.getItemStack().getItem()).armorType, 0, 0, mc.thePlayer);
            } else {
                if (sendPacket.getValue())
                    mc.playerController.windowClickOnlyPacket(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);
                else
                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);
            }

            this.nextClick = Math.round(MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getSecondValue().intValue()));
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void moveItem(int slot) {
        if (sendPacket.getValue())
            mc.playerController.windowClickOnlyPacket(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
        else
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            this.placeTicks = 0;
        }
    };
}
