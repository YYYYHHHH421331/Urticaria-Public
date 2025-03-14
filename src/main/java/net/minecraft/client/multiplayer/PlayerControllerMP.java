package net.minecraft.client.multiplayer;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.component.impl.player.SlotComponent;
import cn.hackedmc.urticaria.newevent.impl.input.WindowClickEvent;
import cn.hackedmc.urticaria.newevent.impl.inventory.SyncCurrentItemEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockBreakEvent;
import cn.hackedmc.urticaria.newevent.impl.other.BlockDamageEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerControllerMP {
    /**
     * The Minecraft instance.
     */
    private final Minecraft mc;
    private final NetHandlerPlayClient netClientHandler;
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);

    /**
     * The Item currently being used to destroy a block
     */
    private ItemStack currentItemHittingBlock;

    /**
     * Current block damage (MP)
     */
    public float curBlockDamageMP;

    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    private float stepSoundTickCounter;

    /**
     * Delays the first damage on the block after the first click on the block
     */
    public int blockHitDelay;

    /**
     * Tells if the player is hitting a block
     */
    private boolean isHittingBlock;

    /**
     * Current game type for the player
     */
    private WorldSettings.GameType currentGameType = WorldSettings.GameType.SURVIVAL;

    /**
     * Index of the current item held by the player in the inventory hotbar
     */
    private int currentPlayerItem;

    public PlayerControllerMP(final Minecraft mcIn, final NetHandlerPlayClient p_i45062_2_) {
        this.mc = mcIn;
        this.netClientHandler = p_i45062_2_;
    }

    public static void clickBlockCreative(final Minecraft mcIn, final PlayerControllerMP p_178891_1_, final BlockPos p_178891_2_, final EnumFacing p_178891_3_) {
        if (!mcIn.theWorld.extinguishFire(mcIn.thePlayer, p_178891_2_, p_178891_3_)) {
            p_178891_1_.onPlayerDestroyBlock(p_178891_2_, p_178891_3_);
        }
    }

    /**
     * Sets player capabilities depending on current gametype. params: player
     */
    public void setPlayerCapabilities(final EntityPlayer p_78748_1_) {
        this.currentGameType.configurePlayerCapabilities(p_78748_1_.capabilities);
    }

    /**
     * None
     */
    public boolean isSpectator() {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    /**
     * Sets the game type for the player.
     */
    public void setGameType(final WorldSettings.GameType p_78746_1_) {
        this.currentGameType = p_78746_1_;
        this.currentGameType.configurePlayerCapabilities(this.mc.thePlayer.capabilities);
    }

    /**
     * Flips the player around.
     */
    public void flipPlayer(final EntityPlayer playerIn) {
        playerIn.rotationYaw = -180.0F;
    }

    public boolean shouldDrawHUD() {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Called when a player completes the destruction of a block
     *
     * @param pos  The block's coordinates
     * @param side The side it was destroyed from
     */
    public boolean onPlayerDestroyBlock(final BlockPos pos, final EnumFacing side) {
        final BlockDamageEvent bdEvent = new BlockDamageEvent(this.mc.thePlayer, this.mc.thePlayer.worldObj, pos);
        Client.INSTANCE.getEventBus().handle(bdEvent);

        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.thePlayer.isAllowEdit()) {
                final Block block = this.mc.theWorld.getBlockState(pos).getBlock();
                final ItemStack itemstack = mc.thePlayer.inventory.alternativeSlot ? SlotComponent.getItemStackNative() : mc.thePlayer.getHeldItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (this.currentGameType.isCreative() && this.mc.thePlayer.getHeldItem() != null && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            return false;
        } else {
            final World world = this.mc.theWorld;
            final IBlockState iblockstate = world.getBlockState(pos);
            final Block block1 = iblockstate.getBlock();

            if (block1.getMaterial() == Material.air) {
                return false;
            } else {
                world.playAuxSFX(2001, pos, Block.getStateId(iblockstate));
                final boolean flag = world.setBlockToAir(pos);

                if (flag) {
                    block1.onBlockDestroyedByPlayer(world, pos, iblockstate);
                }

                this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!this.currentGameType.isCreative()) {
                    final ItemStack itemstack1 = mc.thePlayer.inventory.alternativeSlot ? SlotComponent.getItemStackNative() : mc.thePlayer.getHeldItem();

                    if (itemstack1 != null) {
                        itemstack1.onBlockDestroyed(world, block1, pos, this.mc.thePlayer);

                        if (itemstack1.stackSize == 0) {
                            this.mc.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                }

                return flag;
            }
        }
    }

    /**
     * Called when the player is hitting a block with an item.
     *
     * @param loc  location of the block being clicked
     * @param face Blockface being clicked
     */
    public boolean clickBlock(final BlockPos loc, final EnumFacing face) {
        if (this.currentGameType.isAdventure()) {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.thePlayer.isAllowEdit()) {
                final Block block = this.mc.theWorld.getBlockState(loc).getBlock();
                final ItemStack itemstack = mc.thePlayer.inventory.alternativeSlot ? SlotComponent.getItemStack() : mc.thePlayer.getHeldItem();

                if (itemstack == null) {
                    return false;
                }

                if (!itemstack.canDestroy(block)) {
                    return false;
                }
            }
        }

        if (!this.mc.theWorld.getWorldBorder().contains(loc)) {
            return false;
        } else {
            final BlockBreakEvent event = new BlockBreakEvent(loc);
            Client.INSTANCE.getEventBus().handle(event);

            if (event.isCancelled()) {
                return false;
            }

            if (this.currentGameType.isCreative()) {
                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                clickBlockCreative(this.mc, this, loc, face);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
                if (this.isHittingBlock) {
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }

                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                final Block block1 = this.mc.theWorld.getBlockState(loc).getBlock();
                final boolean flag = block1.getMaterial() != Material.air;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    block1.onBlockClicked(this.mc.theWorld, loc, this.mc.thePlayer);
                }

                if (flag && block1.getPlayerRelativeBlockHardness(mc.thePlayer, this.mc.thePlayer.worldObj, loc) >= 1.0F) {
                    this.onPlayerDestroyBlock(loc, face);
                } else {
                    this.isHittingBlock = true;
                    this.currentBlock = loc;
                    this.currentItemHittingBlock = mc.thePlayer.inventory.alternativeSlot ? SlotComponent.getItemStack() : mc.thePlayer.getHeldItem();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, -1);
        }
    }

    public boolean onPlayerDamageBlock(final BlockPos posBlock, final EnumFacing directionFacing) {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
            return true;
        } else if (this.currentGameType.isCreative() && this.mc.theWorld.getWorldBorder().contains(posBlock)) {
            this.blockHitDelay = 5;
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(this.mc, this, posBlock, directionFacing);
            return true;
        } else if (this.isHittingPosition(posBlock)) {
            final Block block = this.mc.theWorld.getBlockState(posBlock).getBlock();

            if (block.getMaterial() == Material.air) {
                this.isHittingBlock = false;
                return false;
            } else {

                this.curBlockDamageMP += block.getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, posBlock);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepSound()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getFrequency() * 0.5F, (float) posBlock.getX() + 0.5F, (float) posBlock.getY() + 0.5F, (float) posBlock.getZ() + 0.5F));
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock, directionFacing);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        } else {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * player reach distance = 4F
     */
    public float getBlockReachDistance() {
        return this.currentGameType.isCreative() ? 5.0F : 4.5F;
    }

    public void updateController() {
        this.syncCurrentPlayItem();

        if (this.netClientHandler.getNetworkManager().isChannelOpen()) {
            this.netClientHandler.getNetworkManager().processReceivedPackets();
        } else {
            this.netClientHandler.getNetworkManager().checkDisconnected();
        }
    }

    private boolean isHittingPosition(final BlockPos pos) {
        final ItemStack itemstack = mc.thePlayer.inventory.alternativeSlot ? SlotComponent.getItemStack() : mc.thePlayer.getHeldItem();
        boolean flag = this.currentItemHittingBlock == null && itemstack == null;

        if (this.currentItemHittingBlock != null && itemstack != null) {
            flag = itemstack.getItem() == this.currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.currentItemHittingBlock.getMetadata());
        }

        return pos.equals(this.currentBlock) && flag;
    }

    /**
     * Syncs the current player item with the server
     */
    public void syncCurrentPlayItem() {
        InventoryPlayer inventoryPlayer = this.mc.thePlayer.inventory;
        int i = inventoryPlayer.currentItem;

        final SyncCurrentItemEvent event = new SyncCurrentItemEvent(i);
        Client.INSTANCE.getEventBus().handle(event);

        i = event.getSlot();

        if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            this.netClientHandler.addToSendQueue(new C09PacketHeldItemChange(this.currentPlayerItem));
        }
    }

    public boolean onPlayerRightClick(final EntityPlayerSP player, final WorldClient worldIn, final ItemStack heldStack, final BlockPos hitPos, final EnumFacing side, final Vec3 hitVec) {
        this.syncCurrentPlayItem();
        final float f = (float) (hitVec.xCoord - (double) hitPos.getX());
        final float f1 = (float) (hitVec.yCoord - (double) hitPos.getY());
        final float f2 = (float) (hitVec.zCoord - (double) hitPos.getZ());
        boolean flag = false;

        if (!this.mc.theWorld.getWorldBorder().contains(hitPos)) {
            return false;
        } else {
            if (this.currentGameType != WorldSettings.GameType.SPECTATOR) {
                final IBlockState iblockstate = worldIn.getBlockState(hitPos);

                if ((!player.isSneaking() || player.getHeldItem() == null) && iblockstate.getBlock().onBlockActivated(worldIn, hitPos, iblockstate, player, side, f, f1, f2)) {
                    flag = true;
                }

                if (!flag && heldStack != null && heldStack.getItem() instanceof ItemBlock) {
                    final ItemBlock itemblock = (ItemBlock) heldStack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, hitPos, side, player, heldStack)) {
                        return false;
                    }
                }
            }

            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(hitPos, side.getIndex(), heldStack, f, f1, f2));

            if (!flag && this.currentGameType != WorldSettings.GameType.SPECTATOR) {
                if (heldStack == null) {
                    return false;
                } else if (this.currentGameType.isCreative()) {
                    final int i = heldStack.getMetadata();
                    final int j = heldStack.stackSize;
                    final boolean flag1 = heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                    heldStack.setItemDamage(i);
                    heldStack.stackSize = j;
                    return flag1;
                } else {
                    return heldStack.onItemUse(player, worldIn, hitPos, side, f, f1, f2);
                }
            } else {
                return true;
            }
        }
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */

    public float getCurBlockDamageMP() {
        return this.curBlockDamageMP;
    }

    public void setCurBlockDamageMP(float dmg) {
        this.curBlockDamageMP = dmg;
    }

    public boolean sendUseItem(final EntityPlayer playerIn, final World worldIn, final ItemStack itemStackIn) {
        if (this.currentGameType == WorldSettings.GameType.SPECTATOR) {
            return false;
        } else {
            this.syncCurrentPlayItem();
            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(playerIn.inventory.getCurrentItemReal()));
            final int i = itemStackIn.stackSize;
            final ItemStack itemstack = itemStackIn.useItemRightClick(worldIn, playerIn);

            if (itemstack != itemStackIn || itemstack != null && itemstack.stackSize != i) {
                playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0) {
                    playerIn.inventory.mainInventory[playerIn.inventory.currentItem] = null;
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public EntityPlayerSP func_178892_a(final World worldIn, final StatFileWriter p_178892_2_) {
        return new EntityPlayerSP(this.mc, worldIn, this.netClientHandler, p_178892_2_);
    }

    /**
     * Attacks an entity
     */
    public void attackEntity(final EntityPlayer playerIn, final Entity targetEntity) {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.ATTACK));

        if (this.currentGameType != WorldSettings.GameType.SPECTATOR) {
            playerIn.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    public boolean interactWithEntitySendPacket(final EntityPlayer playerIn, final Entity targetEntity) {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && playerIn.interactWith(targetEntity);
    }

    public boolean func_178894_a(final EntityPlayer p_178894_1_, final Entity p_178894_2_, final MovingObjectPosition p_178894_3_) {
        this.syncCurrentPlayItem();
        final Vec3 vec3 = new Vec3(p_178894_3_.hitVec.xCoord - p_178894_2_.posX, p_178894_3_.hitVec.yCoord - p_178894_2_.posY, p_178894_3_.hitVec.zCoord - p_178894_2_.posZ);
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(p_178894_2_, vec3));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && p_178894_2_.interactAt(p_178894_1_, vec3);
    }

    /**
     * Handles slot clicks sends a packet to the server.
     */
    public ItemStack windowClick(final int windowId, final int slotId, final int mouseButtonClicked, final int mode, final EntityPlayer playerIn) {
        final WindowClickEvent windowClickEvent = new WindowClickEvent(windowId, slotId, mouseButtonClicked, mode);
        Client.INSTANCE.getEventBus().handle(windowClickEvent);
        if (windowClickEvent.isCancelled()) {
            return null;
        } else {
            final short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
            final ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, mode, playerIn);
            this.netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, mode, itemstack, short1));
            return itemstack;
        }
    }

    public ItemStack windowClickOnlyPacket(final int windowId, final int slotId, final int mouseButtonClicked, final int mode, final EntityPlayer playerIn) {
        final ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, mode, playerIn);
        this.netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, mode, itemstack, (short) 0));
        return itemstack;
    }

    /**
     * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
     * enchantment action the player has taken.
     */
    public void sendEnchantPacket(final int p_78756_1_, final int p_78756_2_) {
        this.netClientHandler.addToSendQueue(new C11PacketEnchantItem(p_78756_1_, p_78756_2_));
    }

    /**
     * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
     */
    public void sendSlotPacket(final ItemStack itemStackIn, final int slotId) {
        if (this.currentGameType.isCreative()) {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(slotId, itemStackIn));
        }
    }

    /**
     * Sends a Packet107 to the server to drop the item on the ground
     */
    public void sendPacketDropItem(final ItemStack itemStackIn) {
        if (this.currentGameType.isCreative() && itemStackIn != null) {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(-1, itemStackIn));
        }
    }

    public void onStoppedUsingItem(final EntityPlayer playerIn) {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        playerIn.stopUsingItem();
    }

    public boolean gameIsSurvivalOrAdventure() {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative() {
        return !this.currentGameType.isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode() {
        return this.currentGameType.isCreative();
    }

    /**
     * true for hitting entities far away.
     */
    public boolean extendedReach() {
        return this.currentGameType.isCreative();
    }

    /**
     * Checks if the player is riding a horse, used to chose the GUI to open
     */
    public boolean isRidingHorse() {
        return this.mc.thePlayer.isRiding() && this.mc.thePlayer.ridingEntity instanceof EntityHorse;
    }

    public boolean isSpectatorMode() {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    public WorldSettings.GameType getCurrentGameType() {
        return this.currentGameType;
    }

    public boolean func_181040_m() {
        return this.isHittingBlock;
    }
}
