package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;

public class ContainerBrewingStand extends Container {
    public final IInventory tileBrewingStand;

    /**
     * Instance of Slot.
     */
    private final Slot theSlot;
    private int brewTime;

    public ContainerBrewingStand(final InventoryPlayer playerInventory, final IInventory tileBrewingStandIn) {
        this.tileBrewingStand = tileBrewingStandIn;
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 0, 56, 46));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 1, 79, 53));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 2, 102, 46));
        this.theSlot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(tileBrewingStandIn, 3, 79, 17));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public void onCraftGuiOpened(final ICrafting listener) {
        super.onCraftGuiOpened(listener);
        listener.func_175173_a(this, this.tileBrewingStand);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (int i = 0; i < this.crafters.size(); ++i) {
            final ICrafting icrafting = this.crafters.get(i);

            if (this.brewTime != this.tileBrewingStand.getField(0)) {
                icrafting.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
            }
        }

        this.brewTime = this.tileBrewingStand.getField(0);
    }

    public void updateProgressBar(final int id, final int data) {
        this.tileBrewingStand.setField(id, data);
    }

    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.tileBrewingStand.isUseableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack itemstack = null;
        final Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if ((index < 0 || index > 2) && index != 3) {
                if (!this.theSlot.getHasStack() && this.theSlot.isItemValid(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 3, 4, false)) {
                        return null;
                    }
                } else if (ContainerBrewingStand.Potion.canHoldPotion(itemstack)) {
                    if (!this.mergeItemStack(itemstack1, 0, 3, false)) {
                        return null;
                    }
                } else if (index >= 4 && index < 31) {
                    if (!this.mergeItemStack(itemstack1, 31, 40, false)) {
                        return null;
                    }
                } else if (index >= 31 && index < 40) {
                    if (!this.mergeItemStack(itemstack1, 4, 31, false)) {
                        return null;
                    }
                } else if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
                    return null;
                }
            } else {
                if (!this.mergeItemStack(itemstack1, 4, 40, true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    class Ingredient extends Slot {
        public Ingredient(final IInventory inventoryIn, final int index, final int xPosition, final int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        public boolean isItemValid(final ItemStack stack) {
            return stack != null && stack.getItem().isPotionIngredient(stack);
        }

        public int getSlotStackLimit() {
            return 64;
        }
    }

    static class Potion extends Slot {
        private final EntityPlayer player;

        public Potion(final EntityPlayer playerIn, final IInventory inventoryIn, final int index, final int xPosition, final int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
            this.player = playerIn;
        }

        public boolean isItemValid(final ItemStack stack) {
            return canHoldPotion(stack);
        }

        public int getSlotStackLimit() {
            return 1;
        }

        public void onPickupFromSlot(final EntityPlayer playerIn, final ItemStack stack) {
            if (stack.getItem() == Items.potionitem && stack.getMetadata() > 0) {
                this.player.triggerAchievement(AchievementList.potion);
            }

            super.onPickupFromSlot(playerIn, stack);
        }

        public static boolean canHoldPotion(final ItemStack stack) {
            return stack != null && (stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle);
        }
    }
}
