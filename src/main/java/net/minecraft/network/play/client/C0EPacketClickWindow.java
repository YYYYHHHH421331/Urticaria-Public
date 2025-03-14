package net.minecraft.network.play.client;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C0EPacketClickWindow implements Packet<INetHandlerPlayServer> {
    /**
     * The id of the window which was clicked. 0 for player inventory.
     */
    public int windowId;

    /**
     * Id of the clicked slot
     */
    public int slotId;

    /**
     * Button used
     */
    private int usedButton;

    /**
     * A unique number for the action, used for transaction handling
     */
    private short actionNumber;

    /**
     * The item stack present in the slot
     */
    private ItemStack clickedItem;

    /**
     * Inventory operation mode
     */
    private int mode;

    public C0EPacketClickWindow() {
    }

    public C0EPacketClickWindow(final int windowId, final int slotId, final int usedButton, final int mode, final ItemStack clickedItem, final short actionNumber) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.usedButton = usedButton;
        this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
        this.actionNumber = actionNumber;
        this.mode = mode;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(final INetHandlerPlayServer handler) {
        handler.processClickWindow(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(final PacketBuffer buf) throws IOException {
        this.windowId = buf.readByte();
        this.slotId = buf.readShort();
        this.usedButton = buf.readByte();
        this.actionNumber = buf.readShort();
        this.mode = buf.readByte();
        this.clickedItem = buf.readItemStackFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(final PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeShort(this.slotId);
        buf.writeByte(this.usedButton);
        buf.writeShort(this.actionNumber);
        buf.writeByte(this.mode);
        buf.writeItemStackToBuffer(this.clickedItem);
    }

    public int getWindowId() {
        return this.windowId;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public int getUsedButton() {
        return this.usedButton;
    }

    public short getActionNumber() {
        return this.actionNumber;
    }

    public ItemStack getClickedItem() {
        return this.clickedItem;
    }

    public int getMode() {
        return this.mode;
    }

    public void setUsedButton(int usedButton) {
        this.usedButton = usedButton;
    }

    public void setActionNumber(short actionNumber) {
        this.actionNumber = actionNumber;
    }
}
