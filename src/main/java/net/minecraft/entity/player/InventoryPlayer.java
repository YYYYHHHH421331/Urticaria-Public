package net.minecraft.entity.player;

import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;

import java.util.concurrent.Callable;

public class InventoryPlayer implements IInventory {
    /**
     * An array of 36 item stacks indicating the main player inventory (including the visible bar).
     */
    public ItemStack[] mainInventory = new ItemStack[36];

    /**
     * An array of 4 item stacks containing the currently worn armor pieces.
     */
    public ItemStack[] armorInventory = new ItemStack[4];

    /**
     * The index of the currently held item (0-8).
     */
    public int currentItem, alternativeCurrentItem;
    public boolean alternativeSlot, breakNotNative;

    /**
     * The player whose inventory this is.
     */
    public EntityPlayer player;
    private ItemStack itemStack;

    /**
     * Set true whenever the inventory changes. Nothing sets it false so you will have to write your own code to check
     * it and reset the value.
     */
    public boolean inventoryChanged;

    public InventoryPlayer(final EntityPlayer playerIn) {
        this.player = playerIn;
    }

    public int getCurrentItemIndexNative() {
        return this.player == InstanceAccess.mc.thePlayer ? SlotComponent.getItemIndexNative() : currentItem;
    }

    public int getCurrentItemIndex() {
        return this.player == InstanceAccess.mc.thePlayer ? SlotComponent.getItemIndex() : currentItem;
    }

    public ItemStack getCurrentItemReal() {
        return this.getCurrentItemIndex() < 9 && this.getCurrentItemIndex() >= 0 ? this.mainInventory[this.getCurrentItemIndex()] : null;
    }

    /**
     * Returns the item stack currently held by the player.
     */
    public ItemStack getCurrentItem() {
        return this.currentItem < 9 && this.currentItem >= 0 ? this.mainInventory[this.currentItem] : null;
    }

    /**
     * Get the size of the player hotbar inventory
     */
    public static int getHotbarSize() {
        return 9;
    }

    private int getInventorySlotContainItem(final Item itemIn) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemIn) {
                return i;
            }
        }

        return -1;
    }

    private int getInventorySlotContainItemAndDamage(final Item itemIn, final int p_146024_2_) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemIn && this.mainInventory[i].getMetadata() == p_146024_2_) {
                return i;
            }
        }

        return -1;
    }

    /**
     * stores an itemstack in the users inventory
     */
    private int storeItemStack(final ItemStack itemStackIn) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemStackIn.getItem() && this.mainInventory[i].isStackable() && this.mainInventory[i].stackSize < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getMetadata() == itemStackIn.getMetadata()) && ItemStack.areItemStackTagsEqual(this.mainInventory[i], itemStackIn)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the first item stack that is empty.
     */
    public int getFirstEmptyStack() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public void setCurrentItem(final Item itemIn, final int p_146030_2_, final boolean p_146030_3_, final boolean p_146030_4_) {
        final ItemStack itemstack = this.getCurrentItemReal();
        final int i = p_146030_3_ ? this.getInventorySlotContainItemAndDamage(itemIn, p_146030_2_) : this.getInventorySlotContainItem(itemIn);

        if (i >= 0 && i < 9) {
            this.currentItem = i;
        } else if (p_146030_4_ && itemIn != null) {
            final int j = this.getFirstEmptyStack();

            if (j >= 0 && j < 9) {
                this.currentItem = j;
            }

            if (itemstack == null || !itemstack.isItemEnchantable() || this.getInventorySlotContainItemAndDamage(itemstack.getItem(), itemstack.getItemDamage()) != this.currentItem) {
                final int k = this.getInventorySlotContainItemAndDamage(itemIn, p_146030_2_);
                final int l;

                if (k >= 0) {
                    l = this.mainInventory[k].stackSize;
                    this.mainInventory[k] = this.mainInventory[this.currentItem];
                } else {
                    l = 1;
                }

                this.mainInventory[this.currentItem] = new ItemStack(itemIn, l, p_146030_2_);
            }
        }
    }

    /**
     * Switch the current item to the next one or the previous one
     */
    public void changeCurrentItem(int p_70453_1_) {
        if (p_70453_1_ > 0) {
            p_70453_1_ = 1;
        }

        if (p_70453_1_ < 0) {
            p_70453_1_ = -1;
        }

        for (this.currentItem -= p_70453_1_; this.currentItem < 0; this.currentItem += 9) {
        }

        while (this.currentItem >= 9) {
            this.currentItem -= 9;
        }
    }

    /**
     * Removes matching items from the inventory.
     *
     * @param itemIn      The item to match, null ignores.
     * @param metadataIn  The metadata to match, -1 ignores.
     * @param removeCount The number of items to remove. If less than 1, removes all matching items.
     * @param itemNBT     The NBT data to match, null ignores.
     * @return The number of items removed from the inventory.
     */
    public int clearMatchingItems(final Item itemIn, final int metadataIn, final int removeCount, final NBTTagCompound itemNBT) {
        int i = 0;

        for (int j = 0; j < this.mainInventory.length; ++j) {
            final ItemStack itemstack = this.mainInventory[j];

            if (itemstack != null && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack.getTagCompound(), true))) {
                final int k = removeCount <= 0 ? itemstack.stackSize : Math.min(removeCount - i, itemstack.stackSize);
                i += k;

                if (removeCount != 0) {
                    this.mainInventory[j].stackSize -= k;

                    if (this.mainInventory[j].stackSize == 0) {
                        this.mainInventory[j] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        for (int l = 0; l < this.armorInventory.length; ++l) {
            final ItemStack itemstack1 = this.armorInventory[l];

            if (itemstack1 != null && (itemIn == null || itemstack1.getItem() == itemIn) && (metadataIn <= -1 || itemstack1.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.func_181123_a(itemNBT, itemstack1.getTagCompound(), false))) {
                final int j1 = removeCount <= 0 ? itemstack1.stackSize : Math.min(removeCount - i, itemstack1.stackSize);
                i += j1;

                if (removeCount != 0) {
                    this.armorInventory[l].stackSize -= j1;

                    if (this.armorInventory[l].stackSize == 0) {
                        this.armorInventory[l] = null;
                    }

                    if (removeCount > 0 && i >= removeCount) {
                        return i;
                    }
                }
            }
        }

        if (this.itemStack != null) {
            if (itemIn != null && this.itemStack.getItem() != itemIn) {
                return i;
            }

            if (metadataIn > -1 && this.itemStack.getMetadata() != metadataIn) {
                return i;
            }

            if (itemNBT != null && !NBTUtil.func_181123_a(itemNBT, this.itemStack.getTagCompound(), false)) {
                return i;
            }

            final int i1 = removeCount <= 0 ? this.itemStack.stackSize : Math.min(removeCount - i, this.itemStack.stackSize);
            i += i1;

            if (removeCount != 0) {
                this.itemStack.stackSize -= i1;

                if (this.itemStack.stackSize == 0) {
                    this.itemStack = null;
                }

                if (removeCount > 0 && i >= removeCount) {
                    return i;
                }
            }
        }

        return i;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of
     * left over items.
     */
    private int storePartialItemStack(final ItemStack itemStackIn) {
        final Item item = itemStackIn.getItem();
        int i = itemStackIn.stackSize;
        int j = this.storeItemStack(itemStackIn);

        if (j < 0) {
            j = this.getFirstEmptyStack();
        }

        if (j < 0) {
            return i;
        } else {
            if (this.mainInventory[j] == null) {
                this.mainInventory[j] = new ItemStack(item, 0, itemStackIn.getMetadata());

                if (itemStackIn.hasTagCompound()) {
                    this.mainInventory[j].setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
                }
            }

            int k = i;

            if (i > this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize) {
                k = this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize;
            }

            if (k > this.getInventoryStackLimit() - this.mainInventory[j].stackSize) {
                k = this.getInventoryStackLimit() - this.mainInventory[j].stackSize;
            }

            if (k == 0) {
                return i;
            } else {
                i = i - k;
                this.mainInventory[j].stackSize += k;
                this.mainInventory[j].animationsToGo = 5;
                return i;
            }
        }
    }

    /**
     * Decrement the number of animations remaining. Only called on client side. This is used to handle the animation of
     * receiving a block.
     */
    public void decrementAnimations() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null) {
                this.mainInventory[i].updateAnimation(this.player.worldObj, this.player, i, this.currentItem == i);
            }
        }
    }

    /**
     * removed one item of specified Item from inventory (if it is in a stack, the stack size will reduce with 1)
     */
    public boolean consumeInventoryItem(final Item itemIn) {
        final int i = this.getInventorySlotContainItem(itemIn);

        if (i < 0) {
            return false;
        } else {
            if (--this.mainInventory[i].stackSize <= 0) {
                this.mainInventory[i] = null;
            }

            return true;
        }
    }

    /**
     * Checks if a specified Item is inside the inventory
     */
    public boolean hasItem(final Item itemIn) {
        final int i = this.getInventorySlotContainItem(itemIn);
        return i >= 0;
    }

    /**
     * Adds the item stack to the inventory, returns false if it is impossible.
     */
    public boolean addItemStackToInventory(final ItemStack itemStackIn) {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
            try {
                if (itemStackIn.isItemDamaged()) {
                    final int j = this.getFirstEmptyStack();

                    if (j >= 0) {
                        this.mainInventory[j] = ItemStack.copyItemStack(itemStackIn);
                        this.mainInventory[j].animationsToGo = 5;
                        itemStackIn.stackSize = 0;
                        return true;
                    } else if (this.player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;

                    while (true) {
                        i = itemStackIn.stackSize;
                        itemStackIn.stackSize = this.storePartialItemStack(itemStackIn);

                        if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i) {
                            break;
                        }
                    }

                    if (itemStackIn.stackSize == i && this.player.capabilities.isCreativeMode) {
                        itemStackIn.stackSize = 0;
                        return true;
                    } else {
                        return itemStackIn.stackSize < i;
                    }
                }
            } catch (final Throwable throwable) {
                final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                final CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
                crashreportcategory.addCrashSectionCallable("Item name", new Callable<String>() {
                    public String call() throws Exception {
                        return itemStackIn.getDisplayName();
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            return false;
        }
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     *
     * @param index The slot to remove from.
     * @param count The maximum amount of items to remove.
     */
    public ItemStack decrStackSize(int index, final int count) {
        ItemStack[] aitemstack = this.mainInventory;

        if (index >= this.mainInventory.length) {
            aitemstack = this.armorInventory;
            index -= this.mainInventory.length;
        }

        if (aitemstack[index] != null) {
            if (aitemstack[index].stackSize <= count) {
                final ItemStack itemstack1 = aitemstack[index];
                aitemstack[index] = null;
                return itemstack1;
            } else {
                final ItemStack itemstack = aitemstack[index].splitStack(count);

                if (aitemstack[index].stackSize == 0) {
                    aitemstack[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    /**
     * Removes a stack from the given slot and returns it.
     *
     * @param index The slot to remove a stack from.
     */
    public ItemStack getStackInSlotOnClosing(int index) {
        ItemStack[] aitemstack = this.mainInventory;

        if (index >= this.mainInventory.length) {
            aitemstack = this.armorInventory;
            index -= this.mainInventory.length;
        }

        if (aitemstack[index] != null) {
            final ItemStack itemstack = aitemstack[index];
            aitemstack[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setInventorySlotContents(int index, final ItemStack stack) {
        ItemStack[] aitemstack = this.mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = this.armorInventory;
        }

        aitemstack[index] = stack;
    }

    public float getStrVsBlock(final Block blockIn) {
        float f = 1.0F;

        if (this.mainInventory[alternativeSlot ? alternativeCurrentItem : currentItem] != null) {
            f *= this.mainInventory[alternativeSlot ? alternativeCurrentItem : currentItem].getStrVsBlock(blockIn);
        }

        return f;
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +80
     * for crafting).
     */
    public NBTTagList writeToNBT(final NBTTagList p_70442_1_) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null) {
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.mainInventory[i].writeToNBT(nbttagcompound);
                p_70442_1_.appendTag(nbttagcompound);
            }
        }

        for (int j = 0; j < this.armorInventory.length; ++j) {
            if (this.armorInventory[j] != null) {
                final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) (j + 100));
                this.armorInventory[j].writeToNBT(nbttagcompound1);
                p_70442_1_.appendTag(nbttagcompound1);
            }
        }

        return p_70442_1_;
    }

    /**
     * Reads from the given tag list and fills the slots in the inventory with the correct items.
     */
    public void readFromNBT(final NBTTagList p_70443_1_) {
        this.mainInventory = new ItemStack[36];
        this.armorInventory = new ItemStack[4];

        for (int i = 0; i < p_70443_1_.tagCount(); ++i) {
            final NBTTagCompound nbttagcompound = p_70443_1_.getCompoundTagAt(i);
            final int j = nbttagcompound.getByte("Slot") & 255;
            final ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null) {
                if (j >= 0 && j < this.mainInventory.length) {
                    this.mainInventory[j] = itemstack;
                }

                if (j >= 100 && j < this.armorInventory.length + 100) {
                    this.armorInventory[j - 100] = itemstack;
                }
            }
        }
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSizeInventory() {
        return this.mainInventory.length + 4;
    }

    /**
     * Returns the stack in the given slot.
     *
     * @param index The slot to retrieve from.
     */
    public ItemStack getStackInSlot(int index) {
        ItemStack[] aitemstack = this.mainInventory;

        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = this.armorInventory;
        }

        return aitemstack[index];
    }

    /**
     * Gets the name of this command sender (usually username, but possibly "Rcon")
     */
    public String getCommandSenderName() {
        return "container.inventory";
    }

    /**
     * Returns true if this thing is named
     */
    public boolean hasCustomName() {
        return false;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public IChatComponent getDisplayName() {
        return this.hasCustomName() ? new ChatComponentText(this.getCommandSenderName()) : new ChatComponentTranslation(this.getCommandSenderName(), new Object[0]);
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean canHeldItemHarvest(final Block blockIn) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            final ItemStack itemstack = this.getStackInSlot(this.alternativeCurrentItem);
            return itemstack != null && itemstack.canHarvestBlock(blockIn);
        }
    }

    /**
     * returns a player armor item (as itemstack) contained in specified armor slot.
     */
    public ItemStack armorItemInSlot(final int p_70440_1_) {
        return this.armorInventory[p_70440_1_];
    }

    /**
     * Based on the damage values and maximum damage values of each armor item, returns the current armor value.
     */
    public int getTotalArmorValue() {
        int i = 0;

        for (int j = 0; j < this.armorInventory.length; ++j) {
            if (this.armorInventory[j] != null && this.armorInventory[j].getItem() instanceof ItemArmor) {
                final int k = ((ItemArmor) this.armorInventory[j].getItem()).damageReduceAmount;
                i += k;
            }
        }

        return i;
    }

    /**
     * Damages armor in each slot by the specified amount.
     */
    public void damageArmor(float damage) {
        damage = damage / 4.0F;

        if (damage < 1.0F) {
            damage = 1.0F;
        }

        for (int i = 0; i < this.armorInventory.length; ++i) {
            if (this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
                this.armorInventory[i].damageItem((int) damage, this.player);

                if (this.armorInventory[i].stackSize == 0) {
                    this.armorInventory[i] = null;
                }
            }
        }
    }

    /**
     * Drop all armor and main inventory items.
     */
    public void dropAllItems() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] != null) {
                this.player.dropItem(this.mainInventory[i], true, false);
                this.mainInventory[i] = null;
            }
        }

        for (int j = 0; j < this.armorInventory.length; ++j) {
            if (this.armorInventory[j] != null) {
                this.player.dropItem(this.armorInventory[j], true, false);
                this.armorInventory[j] = null;
            }
        }
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty() {
        this.inventoryChanged = true;
    }

    /**
     * Set the stack helds by mouse, used in GUI/Container
     */
    public void setItemStack(final ItemStack itemStackIn) {
        this.itemStack = itemStackIn;
    }

    /**
     * Stack helds by mouse, used in GUI and Containers
     */
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean isUseableByPlayer(final EntityPlayer player) {
        return !this.player.isDead && player.getDistanceSqToEntity(this.player) <= 64.0D;
    }

    /**
     * Returns true if the specified ItemStack exists in the inventory.
     */
    public boolean hasItemStack(final ItemStack itemStackIn) {
        for (int i = 0; i < this.armorInventory.length; ++i) {
            if (this.armorInventory[i] != null && this.armorInventory[i].isItemEqual(itemStackIn)) {
                return true;
            }
        }

        for (int j = 0; j < this.mainInventory.length; ++j) {
            if (this.mainInventory[j] != null && this.mainInventory[j].isItemEqual(itemStackIn)) {
                return true;
            }
        }

        return false;
    }

    public void openInventory(final EntityPlayer player) {
    }

    public void closeInventory(final EntityPlayer player) {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return true;
    }

    /**
     * Copy the ItemStack contents from another InventoryPlayer instance
     */
    public void copyInventory(final InventoryPlayer playerInventory) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            this.mainInventory[i] = ItemStack.copyItemStack(playerInventory.mainInventory[i]);
        }

        for (int j = 0; j < this.armorInventory.length; ++j) {
            this.armorInventory[j] = ItemStack.copyItemStack(playerInventory.armorInventory[j]);
        }

        this.currentItem = playerInventory.currentItem;
    }

    public int getField(final int id) {
        return 0;
    }

    public void setField(final int id, final int value) {
    }

    public int getFieldCount() {
        return 0;
    }

    public void clear() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            this.mainInventory[i] = null;
        }

        for (int j = 0; j < this.armorInventory.length; ++j) {
            this.armorInventory[j] = null;
        }
    }
}
