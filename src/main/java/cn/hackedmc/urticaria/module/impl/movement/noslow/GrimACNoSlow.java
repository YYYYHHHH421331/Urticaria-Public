package cn.hackedmc.urticaria.module.impl.movement.noslow;

import cn.hackedmc.urticaria.component.impl.render.notificationcomponent.NotificationComponent;
import cn.hackedmc.urticaria.module.impl.movement.NoSlow;
import cn.hackedmc.urticaria.module.impl.player.AutoGApple;
import cn.hackedmc.urticaria.module.impl.player.AutoThrow;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.WindowClickEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.ChestRenderEvent;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.ItemUtil;
import cn.hackedmc.urticaria.value.Mode;
import cn.hackedmc.urticaria.value.impl.BooleanValue;
import cn.hackedmc.urticaria.value.impl.ModeValue;
import cn.hackedmc.urticaria.value.impl.SubMode;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ByteType;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.viamcp.ViaMCP;

import java.util.concurrent.LinkedBlockingQueue;

public class GrimACNoSlow extends Mode<NoSlow> {
    private final BooleanValue bowPacket = new BooleanValue("Bow",this,true);
    private final ModeValue bow = new ModeValue("Bow Mode", this)
            .add(new SubMode("Basic"))
            .add(new SubMode("With AAC"))
            .add(new SubMode("Use Item Wrap"))
            .setDefault("Basic");
    private final ModeValue mode = new ModeValue("Food Mode", this)
            .add(new SubMode("1.9+ Blink"))
            .add(new SubMode("Switch Item"))
            .add(new SubMode("Swap Hand"))
            .add(new SubMode("HYT Pit"))
            .add(new SubMode("Store"))
            .add(new SubMode("HYT BedWars"))
            .add(new SubMode("HYT BedWars With AAC"))
            .add(new SubMode("Drop"))
            .add(new SubMode("Off"))
            .setDefault("1.9+ Blink");

    public GrimACNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    private boolean ignoreRelease = false;
    private boolean sent = false;
    private boolean blinking = false;
    private GuiContainer lastOpenContainer;
    private boolean needCancelNextSound;
    private int windowId;
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();

    @Override
    public void onEnable() {
        blinking = false;
        ignoreRelease = false;
        sent = false;
        lastOpenContainer = null;
        needCancelNextSound = false;
        windowId = 0;
    }

    @Override
    public void onDisable() {
        packets.forEach(packet -> mc.getNetHandler().addToSendQueueUnregistered(packet));
        packets.clear();
        if (windowId != 0)
            mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow(windowId));
        windowId = 0;
        lastOpenContainer = null;
    }

    @EventLink
    private final Listener<ChestRenderEvent> onChestRender = event -> {
        final GuiContainer guiContainer = event.getScreen();

        if (!(guiContainer instanceof GuiInventory)) {
            lastOpenContainer = guiContainer;
        }
    };

    @EventLink()
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.getHeldItem() != null) {
            if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata()))))
                sent = false;

            if (!AutoGApple.eating && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }

            if (mode.getValue().getName().equalsIgnoreCase("switch item")) {
                if (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && mc.thePlayer.isUsingItem()) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            if (mode.getValue().getName().equalsIgnoreCase("swap hand")) {
                PacketWrapper digging = PacketWrapper.create(19, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                digging.write(Type.VAR_INT, 6);
                digging.write(Type.LONG, BlockPos.ORIGIN.toLong());
                digging.write(Type.BYTE, (byte) 0);
                PacketUtil.sendToServer(digging, Protocol1_8To1_9.class, true, true);
            }

            if (mode.getValue().getName().equalsIgnoreCase("hyt bedwars with aac") && mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata()))) && mc.thePlayer.isUsingItem()) {
                mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).up(), EnumFacing.UP));
            }

            if (bowPacket.getValue()) {
                switch (bow.getValue().getName().toLowerCase()) {
                    case "use item wrap":
                    case "basic":
                        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && !getModule(AutoThrow.class).isEnabled()) {
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        }

                        break;

                    case "with aac":
                        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem()) {
                            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        }

                        break;
                }
            }
        }
    };

    @EventLink()
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) {
            mc.getNetHandler().addToSendQueueUnregistered(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            if (ViaMCP.getInstance().getVersion() > 47) {
                PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                useItem.write(Type.VAR_INT, 1);
                PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
            }
        }

        if (bow.getValue().getName().equalsIgnoreCase("use item wrap") && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && !getModule(AutoThrow.class).isEnabled()) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            if (ViaMCP.getInstance().getVersion() > 47) {
                PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                useItem.write(Type.VAR_INT, 1);
                PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
            }
        }
    };

    @EventLink()
    private final Listener<SlowDownEvent> onSlowDown = event -> {
        if (mc.thePlayer.getHeldItem() == null) return;

        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            if (bowPacket.getValue()) event.setCancelled();
        } else {
            switch (mode.getValue().getName().toLowerCase()) {
                case "off": {
                    if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                        event.setCancelled();
                    }

                    break;
                }
                case "hyt pit": {
                    if (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                        if (!ItemUtil.isHoldingEnchantedGoldenApple() && !sent) {
                            event.setCancelled();
                        }
                    } else if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion)) {
                        event.setCancelled();
                    }

                    break;
                }

                case "swap hand":
                case "1.9+ blink":
                case "switch item": {
                    event.setCancelled();

                    break;
                }

                case "hyt bedwars with aac":
                case "hyt bedwars": {
                    if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                        if (!sent) {
                            event.setCancelled();
                        }
                    } else {
                        event.setCancelled();
                    }

                    break;
                }

                case "store": {
                    if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                        if (windowId != 0 && !sent)
                            event.setCancelled();
                    } else {
                        event.setCancelled();
                    }

                    break;
                }

                case "drop": {
                    if (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                        if (!ItemUtil.isHoldingEnchantedGoldenApple() && !sent && mc.thePlayer.getHeldItem().stackSize >= 2) {
                            event.setCancelled();
                        }
                    } else if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion)) {
                        event.setCancelled();
                    }

                    break;
                }
            }
        }
    };

    @EventLink()
    private final Listener<WorldChangeEvent> onWorld = event -> {
        blinking = false;
        packets.clear();
        lastOpenContainer = null;
        windowId = 0;
        needCancelNextSound = false;
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (mc.thePlayer == null) return;

        final Packet<?> packet = event.getPacket();

        if (mode.getValue().getName().equalsIgnoreCase("drop") || mode.getValue().getName().equalsIgnoreCase("hyt pit")) {
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !ItemUtil.isHoldingEnchantedGoldenApple()) {
                if (packet instanceof S2FPacketSetSlot) {
                    S2FPacketSetSlot s2f = (S2FPacketSetSlot) packet;
                    if (s2f.getWindowId() == 0 && s2f.getStack() != null && mc.thePlayer.getHeldItem() != null && s2f.getStack().getItem() == mc.thePlayer.getHeldItem().getItem()) {
                        mc.thePlayer.getHeldItem().stackSize = s2f.getStack().stackSize;
                        event.setCancelled();
                        sent = false;
                        ignoreRelease = true;
                    }
                }
            }
        }

//        if (mode.getValue().getName().equalsIgnoreCase("swap hand")) {
//            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
//                if (packet instanceof S2FPacketSetSlot) {
//                    sent = false;
//                }
//            }
//        }

        if (mode.getValue().getName().equalsIgnoreCase("hyt bedwars") || mode.getValue().getName().equalsIgnoreCase("hyt bedwars with aac")) {
            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                if (packet instanceof S2DPacketOpenWindow) {
                    event.setCancelled();
                    sent = false;
                    PacketUtil.sendNoEvent(new C0DPacketCloseWindow(((S2DPacketOpenWindow) packet).getWindowId()));
                }
            }
        }

        if (mode.getValue().getName().equalsIgnoreCase("store")) {
            if (packet instanceof S2DPacketOpenWindow) {
                final S2DPacketOpenWindow wrapped = (S2DPacketOpenWindow) packet;
                windowId = wrapped.getWindowId();
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata()))) && windowId != 0) {
                    sent = false;
                    ignoreRelease = true;
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                    event.setCancelled();
                    if (lastOpenContainer != null) lastOpenContainer.inventorySlots.windowId = windowId;
                }
            }

            if (mc.currentScreen == null && windowId != 0 && lastOpenContainer != null) {
                if ((packet instanceof S2FPacketSetSlot && ((S2FPacketSetSlot) packet).getWindowId() == windowId) || (packet instanceof S30PacketWindowItems && ((S30PacketWindowItems) packet).func_148911_c() == windowId) || (packet instanceof S31PacketWindowProperty && ((S31PacketWindowProperty) packet).getWindowId() == windowId)) {
                    mc.thePlayer.openContainer = lastOpenContainer.inventorySlots;
                }
            }

            if (packet instanceof S2EPacketCloseWindow) {
                final S2EPacketCloseWindow wrapped = (S2EPacketCloseWindow) packet;
                if (lastOpenContainer != null && wrapped.windowId == lastOpenContainer.inventorySlots.windowId) {
                    lastOpenContainer = null;
                    ignoreRelease = false;
                }
            }

            if (packet instanceof S29PacketSoundEffect) {
                if (needCancelNextSound)
                    event.setCancelled();
                needCancelNextSound = false;
            }
        }
    };

    @EventLink
    private final Listener<WindowClickEvent> onWindowClick = event -> {
        if (mode.getValue().getName().equalsIgnoreCase("store") && mc.currentScreen == null && windowId != 0 && lastOpenContainer != null) {
            mc.thePlayer.openContainer = lastOpenContainer.inventorySlots;
        }
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null || mc.isSingleplayer() || event.isCancelled() || AutoGApple.eating) return;

        switch (mode.getValue().getName().toLowerCase()) {
            case "1.9+ blink": {
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                    if (packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement) packet).getPosition().equals(new BlockPos(-1, -1, -1))) {
                        blinking = true;

                        if (ViaMCP.getInstance().getVersion() <= 47)
                            NotificationComponent.post("No Slow", "This NoSlow only supports higher versions!", 10000);
                    } else if (!(packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                            packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                            packet instanceof C01PacketEncryptionResponse) && blinking) {
                        event.setCancelled();

                        if (packet instanceof C07PacketPlayerDigging) {
                            final C07PacketPlayerDigging wrapped = (C07PacketPlayerDigging) packet;

                            if (wrapped.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                                blinking = false;
                                mc.getNetHandler().addToSendQueueUnregistered(wrapped);
                                packets.forEach(p -> mc.getNetHandler().addToSendQueueUnregistered(p));
                                packets.clear();
                                return;
                            }
                        }

                        packets.add(packet);
                    }
                } else if (blinking) {
                    blinking = false;
                    mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    packets.forEach(p -> mc.getNetHandler().addToSendQueueUnregistered(p));
                    packets.clear();
                }

                break;
            }

            case "hyt bedwars with aac":
            case "hyt bedwars": {
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                    if(packet instanceof C08PacketPlayerBlockPlacement) {
                        final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                        if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                            if (!sent) {
                                mc.thePlayer.sendChatMessageNoEvent("/lizi open");
                                sent = true;
                            }
                        }
                    }
                } else {
                    sent = false;
                }

                break;
            }

            case "store": {
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                    if (windowId != 0) {
                        if(packet instanceof C08PacketPlayerBlockPlacement) {
                            final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                            if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                                if (!sent) {
                                    mc.getNetHandler().addToSendQueueUnregistered(new C0EPacketClickWindow(windowId, 0, 0, 4, mc.thePlayer.getHeldItem(), (short) 0));
                                    sent = true;
                                    needCancelNextSound = true;
                                }
                            }
                        }

                        if (packet instanceof C07PacketPlayerDigging) {
                            final C07PacketPlayerDigging digging = (C07PacketPlayerDigging) packet;
                            if (ignoreRelease && digging.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                                event.setCancelled();
                            }
                        }

                        if (packet instanceof C0EPacketClickWindow) {
                            final C0EPacketClickWindow clickWindow = (C0EPacketClickWindow) packet;

                            if (mc.currentScreen instanceof GuiInventory && lastOpenContainer != null) {
                                clickWindow.windowId = windowId;
                                if (clickWindow.getActionNumber() == 2)
                                    clickWindow.setUsedButton(clickWindow.getUsedButton() + lastOpenContainer.inventorySlots.getInventory().size() - 36);
                                clickWindow.slotId += lastOpenContainer.inventorySlots.getInventory().size() - 36;
                            }
                        }
                    }
                } else {
                    ignoreRelease = false;
                }

                if (packet instanceof C0DPacketCloseWindow) {
                    event.setCancelled();
                }

                break;
            }

            case "drop": {
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !ItemUtil.isHoldingEnchantedGoldenApple()) {
                    if(packet instanceof C08PacketPlayerBlockPlacement) {
                        final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                        if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                            if (mc.thePlayer.getHeldItem().stackSize >= 2) {
                                if (!sent) {
                                    mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                    sent = true;
                                }
                            }
                        }
                    }
                    if (packet instanceof C07PacketPlayerDigging) {
                        C07PacketPlayerDigging digging = (C07PacketPlayerDigging) packet;
                        if (ignoreRelease && digging.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM && mc.thePlayer.getHeldItem().stackSize >= 2) {
                            event.setCancelled();
                            ignoreRelease = false;
                        }
                    }
                }

                break;
            }

            case "hyt pit": {
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !ItemUtil.isHoldingEnchantedGoldenApple()) {
                    if(packet instanceof C08PacketPlayerBlockPlacement) {
                        final C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                        if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                            if (!sent) {
                                mc.getNetHandler().addToSendQueueUnregistered(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                sent = true;
                            }
                        }
                    }
                    if (packet instanceof C07PacketPlayerDigging) {
                        C07PacketPlayerDigging digging = (C07PacketPlayerDigging) packet;
                        if (ignoreRelease && digging.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                            event.setCancelled();
                            ignoreRelease = true;
                        }
                    }
                }

                break;
            }
        }
    };
}
