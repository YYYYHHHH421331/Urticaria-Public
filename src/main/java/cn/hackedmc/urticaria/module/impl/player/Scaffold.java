package cn.hackedmc.urticaria.module.impl.player;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.*;
import cn.hackedmc.urticaria.component.impl.render.SmoothCameraComponent;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.movement.Speed;
import cn.hackedmc.urticaria.module.impl.player.scaffold.sprint.*;
import cn.hackedmc.urticaria.module.impl.player.scaffold.tower.*;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.MoveInputEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.StrafeEvent;
import cn.hackedmc.urticaria.newevent.impl.other.PossibleClickEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render3DEvent;
import cn.hackedmc.urticaria.util.RandomUtil;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.player.EnumFacingOffset;
import cn.hackedmc.urticaria.util.player.MoveUtil;
import cn.hackedmc.urticaria.util.player.PlayerUtil;
import cn.hackedmc.urticaria.util.player.SlotUtil;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.render.RenderUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.util.vector.Vector3d;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.value.impl.*;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.Objects;
import java.util.Random;

/**
 * @author Alan
 * @since ??/??/21
 */

@Rise
@ModuleInfo(name = "module.player.scaffold.name", description = "module.player.scaffold.description", category = Category.PLAYER)
public class Scaffold extends Module {
    public static Scaffold INSTANCE;
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Snap"))
            .add(new SubMode("Telly"))
            .add(new SubMode("UPDATED-NCP"))
            .setDefault("Normal");
    private final ModeValue placeTime = new ModeValue("Place Time", this)
            .add(new SubMode("Pre"))
            .add(new SubMode("Post"))
            .add(new SubMode("Legit"))
            .add(new SubMode("Tick"))
            .setDefault("Pre");
    private final BooleanValue placeWhenFallDown = new BooleanValue("Place when fall", this, false, () -> !mode.getValue().getName().equalsIgnoreCase("Telly"));
    private final BooleanValue fastRotation = new BooleanValue("Telly Fast Rotate", this, false, () -> !mode.getValue().getName().equalsIgnoreCase("Telly"));
    private final NumberValue tellyTick = new NumberValue("Telly Ticks", this, 3, 1, 6, 1, () -> !mode.getValue().getName().equalsIgnoreCase("Telly"));
    private final NumberValue upTellyTick = new NumberValue("Up Telly Ticks", this, 1, 1, 6, 1, () -> !mode.getValue().getName().equalsIgnoreCase("Telly"));

    private final ModeValue rayCast = new ModeValue("Ray Cast", this)
            .add(new SubMode("Off"))
            .add(new SubMode("Normal"))
            .add(new SubMode("Strict"))
            .setDefault("Off");

    private final ModeValue sprint = new ModeValue("Sprint", this)
            .add(new SubMode("Normal"))
            .add(new DisabledSprint("Disabled", this))
            .add(new LegitSprint("Legit", this))
            .add(new BypassSprint("Bypass", this))
            .add(new VulcanSprint("Vulcan", this))
            .add(new OnGroundSprint("On Ground", this))
            .add(new NCPSprint("No Cheat Plus", this))
            .add(new MatrixSprint("Matrix", this))
            .add(new HuaYuTingSprint("HuaYuTing", this))
            .add(new WatchdogSprint("Watchdog", this))
            .setDefault("Normal");

    private final ModeValue tower = new ModeValue("Tower", this)
            .add(new SubMode("Disabled"))
            .add(new VulcanTower("Vulcan", this))
            .add(new VanillaTower("Vanilla", this))
            .add(new NormalTower("Normal", this))
            .add(new AirJumpTower("Air Jump", this))
            .add(new WatchdogTower("Watchdog", this))
            .add(new MMCTower("MMC", this))
            .add(new NCPTower("NCP", this))
            .add(new MatrixTower("Matrix", this))
            .add(new LegitTower("Legit", this))
            .setDefault("Disabled");

    private final ModeValue sameY = new ModeValue("Same Y", this)
            .add(new SubMode("Off"))
            .add(new SubMode("On"))
            .add(new SubMode("Auto Jump"))
            .setDefault("Off");
    private final BooleanValue fastJump = new BooleanValue("Fast Jump", this, false, () -> !sameY.getValue().getName().equalsIgnoreCase("Auto Jump"));
    private final BooleanValue hideJump = new BooleanValue("Hide Jump", this, false, () -> !sameY.getValue().getName().equalsIgnoreCase("Auto Jump"));
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation Speed", this, 5, 10, 0, 10, 1);
    private final BoundsNumberValue placeDelay = new BoundsNumberValue("Place Delay", this, 0, 0, 0, 5, 1);
    private final NumberValue timer = new NumberValue("Timer", this, 1, 0.1, 10, 0.1);
    public final BooleanValue movementCorrection = new BooleanValue("Movement Correction", this, false);
    private final BooleanValue clickSpoof = new BooleanValue("Click Spoof", this, false);
    private final BoundsNumberValue clickRate = new BoundsNumberValue("Click Rate", this, 0.5, 1.0, 0.1, 1.0, 0.1, () -> !clickSpoof.getValue());
    public final BooleanValue safeWalk = new BooleanValue("Safe Walk", this, true);
    private final BooleanValue sneak = new BooleanValue("Sneak", this, false);
    public final BoundsNumberValue startSneaking = new BoundsNumberValue("Start Sneaking", this, 0, 0, 0, 5, 1, () -> !sneak.getValue());
    public final BoundsNumberValue stopSneaking = new BoundsNumberValue("Stop Sneaking", this, 0, 0, 0, 5, 1, () -> !sneak.getValue());
    public final NumberValue sneakEvery = new NumberValue("Sneak every x blocks", this, 1, 1, 10, 1, () -> !sneak.getValue());

    public final NumberValue sneakingSpeed = new NumberValue("Sneaking Speed", this, 0.2, 0.2, 1, 0.05, () -> !sneak.getValue());

    private final BooleanValue render = new BooleanValue("Render", this, true);
    private final BooleanValue noSwing = new BooleanValue("No Swing", this, true);

    private final BooleanValue advanced = new BooleanValue("Advanced", this, false);

    private final ModeValue rotationMode = new ModeValue("Rotation Mode", this, () -> !advanced.getValue())
            .add(new SubMode("Off"))
            .add(new SubMode("Normal"))
            .add(new SubMode("Center"))
            .add(new SubMode("Static"))
            .add(new SubMode("Predict"))
            .setDefault("Normal");
    public final ModeValue yawOffset = new ModeValue("Yaw Offset", this, () -> !advanced.getValue())
            .add(new SubMode("0"))
            .add(new SubMode("45"))
            .add(new SubMode("-45"))
            .setDefault("0");

    public final BooleanValue ignoreSpeed = new BooleanValue("Ignore Speed Effect", this, false, () -> !advanced.getValue());
    public final BooleanValue upSideDown = new BooleanValue("Up Side Down", this, false, () -> !advanced.getValue());
    private final BooleanValue mark = new BooleanValue("Mark", this, false, () -> !advanced.getValue());
    private final ModeValue autoBlockMode = new ModeValue("Auto Block", this, () -> !advanced.getValue())
            .add(new SubMode("Off"))
            .add(new SubMode("Pick"))
            .add(new SubMode("Spoof"))
            .add(new SubMode("Silent"))
            .setDefault("Spoof");

    private boolean lastUpdateRotate = false;
    private Vec3 targetBlock;
    private EnumFacingOffset enumFacing;
    private BlockPos blockFace;
    private float targetYaw, targetPitch;
    private float oldTargetYaw, oldTargetPitch;
    private int ticksOnAir, sneakingTicks;
    private double startY;
    private float forward, strafe;
    private int placements;
    private boolean incrementedPlacements;
    public Scaffold () {
        this.setKeyCode(Keyboard.KEY_G);
        INSTANCE = this;
    }
    @Override
    protected void onEnable() {
        targetYaw = InstanceAccess.mc.thePlayer.rotationYaw - 180;
        targetPitch = 90;
        oldTargetYaw = mc.thePlayer.rotationYaw;
        oldTargetPitch = mc.thePlayer.rotationPitch;

        startY = Math.floor(InstanceAccess.mc.thePlayer.posY);
        targetBlock = null;

        this.sneakingTicks = -1;
        lastUpdateRotate = false;
    }

    public int getTellyTick() {
        return GameSettings.isKeyDown(mc.gameSettings.keyBindJump) ? upTellyTick.getValue().intValue() : tellyTick.getValue().intValue();
    }

    @Override
    protected void onDisable() {
        InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(InstanceAccess.mc.gameSettings.keyBindSneak.getKeyCode()));
        InstanceAccess.mc.gameSettings.keyBindJump.setPressed(Keyboard.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump.getKeyCode()));

        // This is a temporary patch
//        SlotComponent.setSlot(InstanceAccess.mc.thePlayer.inventory.currentItem);

        if (timer.getValue().floatValue() != 1.0F) mc.timer.timerSpeed = 1.0F;
    }

    @EventLink
    private final Listener<TickEvent> onTick = event -> {
        if (mc.thePlayer == null) return;

        if (placeTime.getValue().getName().equalsIgnoreCase("Tick"))
            work();

        final float timerValue = timer.getValue().floatValue();
        if (timerValue != 1.0F) mc.timer.timerSpeed = timerValue;
    };

//    @EventLink()
//    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
//        final Packet<?> packet = event.getPacket();
//
//        if (packet instanceof S2FPacketSetSlot) {
//            final S2FPacketSetSlot wrapper = ((S2FPacketSetSlot) packet);
//
//            if (wrapper.getStack() == null) {
//                event.setCancelled(true);
//            } else {
//                try {
//                    int slot = wrapper.getSlot() - 36;
//                    if (slot < 0) return;
//                    final ItemStack itemStack = InstanceAccess.mc.thePlayer.inventory.getStackInSlot(slot);
//                    final Item item = wrapper.getStack().getItem();
//
//                    if ((itemStack == null && wrapper.getStack().stackSize <= 6 && item instanceof ItemBlock && !SlotUtil.blacklist.contains(((ItemBlock) item).getBlock())) ||
//                            itemStack != null && Math.abs(Objects.requireNonNull(itemStack).stackSize - wrapper.getStack().stackSize) <= 6 ||
//                            wrapper.getStack() == null) {
//                        event.setCancelled(true);
//                    }
//                } catch (ArrayIndexOutOfBoundsException exception) {
//                    exception.printStackTrace();
//                }
//            }
//        }
//    };

    public void calculateSneaking(MoveInputEvent moveInputEvent) {
        forward = moveInputEvent.getForward();
        strafe = moveInputEvent.getStrafe();

        if (!this.sneak.getValue()) {
            return;
        }

        double speed = this.sneakingSpeed.getValue().doubleValue();

        if (speed <= 0.2) {
            return;
        }

        moveInputEvent.setSneakSlowDownMultiplier(speed);
    }

    public void calculateSneaking() {
        InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(false);

        if (!MoveUtil.isMoving()) {
            return;
        }

        this.sneakingTicks--;

        if (sneakingTicks < 0) incrementedPlacements = false;

        if (!this.sneak.getValue()) {
            return;
        }

        int ahead = (int) MathUtil.getRandom(startSneaking.getValue().intValue(), startSneaking.getSecondValue().intValue());
        int place = (int) MathUtil.getRandom(placeDelay.getValue().intValue(), placeDelay.getSecondValue().intValue());
        int after = (int) MathUtil.getRandom(stopSneaking.getValue().intValue(), stopSneaking.getSecondValue().intValue());

        if (this.ticksOnAir > 0) {
            this.sneakingTicks = (int) (Math.ceil((after + (place - this.ticksOnAir)) / this.sneakingSpeed.getValue().doubleValue()));
        }

        if (this.sneakingTicks >= 0 || (ahead == 5 && after == 5)) {
            if (placements % sneakEvery.getValue().intValue() == 0) {
                InstanceAccess.mc.gameSettings.keyBindSneak.setPressed(true);
            }

            if (!incrementedPlacements) placements++;
            incrementedPlacements = true;
            return;
        }

        if (ahead == 0 && place == 0 && this.ticksOnAir > 0) {
            this.sneakingTicks = 1;
            return;
        }

        if (PlayerUtil.blockRelativeToPlayer(InstanceAccess.mc.thePlayer.motionX * ahead * sneakingSpeed.getValue().doubleValue(), MoveUtil.HEAD_HITTER_MOTION, InstanceAccess.mc.thePlayer.motionZ * ahead * sneakingSpeed.getValue().doubleValue()) instanceof BlockAir) {
            this.sneakingTicks = (int) Math.floor((5 + place + after) / this.sneakingSpeed.getValue().doubleValue());
            placements++;
        }
    }

    public void calculateRotations() {
        float yawOffset = Float.parseFloat(String.valueOf(this.yawOffset.getValue().getName()));

        /* Calculating target rotations */
        switch (mode.getValue().getName()) {
            case "Normal":
                if (ticksOnAir > 0 && !RayCastUtil.overBlock(RotationComponent.rotations, enumFacing.getEnumFacing(), blockFace, rayCast.getValue().getName().equals("Strict"))) {
                    getRotations(Float.parseFloat(String.valueOf(this.yawOffset.getValue().getName())));
                }
                break;

            case "UPDATED-NCP":
                if (ticksOnAir > 0 && !RayCastUtil.overBlock(RotationComponent.rotations, enumFacing.getEnumFacing(), blockFace, rayCast.getValue().getName().equals("Strict"))) {
                    getRotations(Float.parseFloat(String.valueOf(this.yawOffset.getValue().getName())));
                }

                targetPitch = 69;
                break;

            case "Snap":
                getRotations(yawOffset);

                if (!(ticksOnAir > 0 && !RayCastUtil.overBlock(RotationComponent.rotations, enumFacing.getEnumFacing(), blockFace, true))) {
                    targetYaw = (float) (Math.toDegrees(MoveUtil.direction(InstanceAccess.mc.thePlayer.rotationYaw, forward, strafe))) + yawOffset;
                }
                break;

            case "Telly":
                if (InstanceAccess.mc.thePlayer.offGroundTicks >= getTellyTick() || (fastRotation.getValue() && !mc.thePlayer.onGround)) {
                    getRotations(yawOffset);
                    if (!RayCastUtil.overBlock(new Vector2f(targetYaw, targetPitch), enumFacing.getEnumFacing(), blockFace, rayCast.getValue().getName().equals("Strict"))) {
                        targetYaw = RotationComponent.rotations.x += (float) MathUtil.getRandom(-2, 2);
                        targetPitch = RotationComponent.rotations.y += (float) MathUtil.getRandom(-2, 2);
                        targetPitch = Math.max(-90, Math.min(targetPitch, 90));
                    }
                } else {
                    targetYaw = mc.thePlayer.rotationYaw;
                    targetPitch = (float) MathUtil.getRandom(85, 88);
                }
                break;
        }

        /* Randomising slightly */
        if (targetPitch > 50 && !rotationMode.getValue().getName().equalsIgnoreCase("Predict")) {
            final Vector2f random = new Vector2f((float) (Math.random() - 0.5), (float) (Math.random() / 2));

            if (ticksOnAir <= 0 || RayCastUtil.overBlock(new Vector2f(targetYaw + random.x, targetPitch + random.y), enumFacing.getEnumFacing(),
                    blockFace, rayCast.getValue().getName().equals("Strict"))) {

                targetYaw += random.x;
                targetPitch += random.y;
            }
        }

        /* Smoothing rotations */
        final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
        float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        if(rotationMode.getValue().getName().equalsIgnoreCase("test")){
            targetYaw += (int)(new Random().nextDouble() * 1000 ) * 360;
        }

        if (rotationSpeed != 0) {
            RotationComponent.setRotations(new Vector2f(targetYaw, targetPitch), rotationSpeed, movementCorrection.getValue() ? MovementFix.NORMAL : MovementFix.OFF);
            this.lastUpdateRotate = true;
        }
    }

    private void work() {
        // Same Y
        final boolean sameY = ((!this.sameY.getValue().getName().equals("Off") || this.getModule(Speed.class).isEnabled()) && !(GameSettings.isKeyDown(InstanceAccess.mc.gameSettings.keyBindJump) && (!placeWhenFallDown.getValue() || FallDistanceComponent.distance > 0))) && MoveUtil.isMoving();
        final boolean watchdogCheck = !sprint.getValue().getName().equalsIgnoreCase("Watchdog") || mc.thePlayer.ticksExisted % 3 != 0;

        //For Same Y
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && (!placeWhenFallDown.getValue() || FallDistanceComponent.distance > 0)) {
            startY = Math.floor(InstanceAccess.mc.thePlayer.posY);
        }

        if (InstanceAccess.mc.thePlayer.posY < startY || mc.thePlayer.onGround) {
            startY = Math.floor(InstanceAccess.mc.thePlayer.posY);
        }

        // Gets block to place
        targetBlock = PlayerUtil.getPlacePossibility(0, upSideDown.getValue() ? 3 : 0, 0, mc.playerController.getBlockReachDistance(), sameY && watchdogCheck ? startY : 256, rayCast.getValue().getName().equals("Strict"));

        if (targetBlock == null || (mode.getValue().getName().equalsIgnoreCase("Telly") && targetBlock.yCoord > startY)) {
            return;
        }

        //Gets EnumFacing
        enumFacing = PlayerUtil.getEnumFacing(targetBlock);

        if (enumFacing == null) {
            this.lastUpdateRotate = false;
            checkClick();

            return;
        }

        final BlockPos position = new BlockPos(targetBlock.xCoord, targetBlock.yCoord, targetBlock.zCoord);

        blockFace = position.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);

        if (blockFace == null || enumFacing == null) {
            this.lastUpdateRotate = false;
            checkClick();

            return;
        }

        this.calculateRotations();

        if (targetBlock == null || enumFacing == null || blockFace == null) {
            checkClick();

            return;
        }

        if (mode.getValue().getName().equalsIgnoreCase("Telly") && InstanceAccess.mc.thePlayer.offGroundTicks < (sprint.getValue().getName().equalsIgnoreCase("HuaYuTing") ? getTellyTick() + 1 : getTellyTick())) return;

//        if (startY - 1 != Math.floor(targetBlock.yCoord) && sameY && watchdogCheck) {
//            return;
//        }

        if(!rotationMode.getValue().getName().equalsIgnoreCase("test")) {
            if (InstanceAccess.mc.thePlayer.inventory.alternativeCurrentItem == (autoBlockMode.getValue().getName().equalsIgnoreCase("Silent") ? SlotComponent.getItemIndexNative() : SlotComponent.getItemIndex())) {
                if (!BadPacketsComponent.bad(false, true, false, false, true) &&
                        ticksOnAir > MathUtil.getRandom(placeDelay.getValue().intValue(), placeDelay.getSecondValue().intValue()) &&
                        (RayCastUtil.overBlock(enumFacing.getEnumFacing(), blockFace, rayCast.getValue().getName().equals("Strict")) || rayCast.getValue().getName().equals("Off"))) {

                    Vec3 hitVec = this.getHitVec();

                    if (autoBlockMode.getValue().getName().equalsIgnoreCase("Silent") && SlotComponent.getItemIndexNative() != mc.thePlayer.inventory.currentItem)
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(SlotComponent.getItemIndexNative()));
                    if (InstanceAccess.mc.playerController.onPlayerRightClick(InstanceAccess.mc.thePlayer, InstanceAccess.mc.theWorld, autoBlockMode.getValue().getName().equalsIgnoreCase("Silent") ? SlotComponent.getItemStackNative() : SlotComponent.getItemStack(), blockFace, enumFacing.getEnumFacing(), hitVec)) {
                        if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                        else InstanceAccess.mc.thePlayer.swingItem();
                    }
                    if (autoBlockMode.getValue().getName().equalsIgnoreCase("Silent") && SlotComponent.getItemIndexNative() != mc.thePlayer.inventory.currentItem)
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                    InstanceAccess.mc.rightClickDelayTimer = 0;
                    ticksOnAir = 0;

                    assert SlotComponent.getItemStack() != null;
                    if (SlotComponent.getItemStack() != null && SlotComponent.getItemStack().stackSize == 0) {
                        InstanceAccess.mc.thePlayer.inventory.mainInventory[SlotComponent.getItemIndex()] = null;
                    }
                } else {
                    checkClick();
                }
            }
        }
    }

    @EventLink
    private final Listener<PossibleClickEvent> onPossibleClick = event -> {
        if (mc.thePlayer == null) return;

        if (placeTime.getValue().getName().equalsIgnoreCase("Legit"))
            work();
    };

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (sameY.getValue().getName().equalsIgnoreCase("Auto Jump") && hideJump.getValue() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && MoveUtil.isMoving()) {
            if (mc.thePlayer.onGround) SmoothCameraComponent.setY(startY, 0.1F);
            else SmoothCameraComponent.setY(0.1F);
        }

        // Getting ItemSlot
        switch (autoBlockMode.getValue().getName().toLowerCase()) {
            case "pick":
                mc.thePlayer.inventory.currentItem = SlotUtil.findBlock();
                break;
            case "spoof":
                SlotComponent.setSlot(SlotUtil.findBlock(), render.getValue()); // it must work in PreUpdate.
                break;
            case "silent":
                SlotComponent.setSlotNative(SlotUtil.findBlock(), render.getValue()); // it must work in PreUpdate.
                break;
        }

        InstanceAccess.mc.thePlayer.safeWalk = this.safeWalk.getValue() && InstanceAccess.mc.thePlayer.onGround;

        //Used to detect when to place a block, if over air, allow placement of blocks
        if (PlayerUtil.blockRelativeToPlayer(0, upSideDown.getValue() ? 2 : -1, 0) instanceof BlockAir) {
            ticksOnAir++;
        } else {
            ticksOnAir = 0;
        }

        this.calculateSneaking();

        if (placeTime.getValue().getName().equalsIgnoreCase("Pre"))
            work();

        RotationComponent.active = this.lastUpdateRotate;
    };

    @EventLink(value = Priorities.VERY_LOW)
    private final Listener<PostMotionEvent> onPostMotion = event -> {
        if (placeTime.getValue().getName().equalsIgnoreCase("Post"))
            work();
    };

    private void checkClick() {
        if (clickSpoof.getValue() && Math.random() <= MathUtil.getRandom(clickRate.getValue().doubleValue(), clickRate.getSecondValue().doubleValue()) && SlotComponent.getItemStack() != null && SlotComponent.getItemStack().getItem() instanceof ItemBlock) {
//                ChatUtil.display("Drag: " + Math.random());
            PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
        }
    }

    @EventLink()
    public final Listener<MoveInputEvent> onMove = this::calculateSneaking;

    public void getRotations(final float yawOffset) {
        switch (rotationMode.getValue().getName().toLowerCase()) {
            case "normal": {
                boolean found = false;
                for (float possibleYaw = InstanceAccess.mc.thePlayer.rotationYaw - 180 + yawOffset; possibleYaw <= InstanceAccess.mc.thePlayer.rotationYaw + 360 - 180 && !found; possibleYaw += 45) {
                    for (float possiblePitch = 90; possiblePitch > 30 && !found; possiblePitch -= possiblePitch > (InstanceAccess.mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 60 : 80) ? 1 : 10) {
                        if (RayCastUtil.overBlock(new Vector2f(possibleYaw, possiblePitch), enumFacing.getEnumFacing(), blockFace, true)) {
                            targetYaw = possibleYaw;
                            targetPitch = possiblePitch;
                            found = true;
                        }
                    }
                }

                if (!found) {
                    final Vector2f rotations = RotationUtil.calculate(
                            new Vector3d(blockFace.getX(), blockFace.getY(), blockFace.getZ()), enumFacing.getEnumFacing());

                    targetYaw = rotations.x;
                    targetPitch = rotations.y;
                }

                targetYaw += sprint.getValue().getName().equalsIgnoreCase("Watchdog") ? RandomUtil.nextInt(10, 20) : 0;

                break;
            }

            case "center": {
                final Vector2f rotations = RotationUtil.calculate(
                        new Vector3d(blockFace.getX(), blockFace.getY(), blockFace.getZ()), enumFacing.getEnumFacing());

                targetYaw = rotations.x;
                targetPitch = rotations.y;

                break;
            }

            case "static": {
                targetYaw = mc.thePlayer.rotationYaw + 180 + yawOffset;
                targetPitch = (float) (78 + Math.random());

                break;
            }

            case "predict": {
                final Vector2f rotations = RotationUtil.calculatePredicate(new Vector3d(blockFace.getX(), blockFace.getY(), blockFace.getZ()), enumFacing.getEnumFacing());

                targetYaw = rotations.x;
                targetPitch = rotations.y;

                break;
            }
        }
    }

    @EventLink()
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!Objects.equals(yawOffset.getValue().getName(), "0") && !movementCorrection.getValue()) {
            MoveUtil.useDiagonalSpeed();
        }

        if (this.sameY.getValue().getName().equals("Auto Jump")) {
            if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                if (this.fastJump.getValue())
                    mc.thePlayer.jump();
            }
        }
    };

    @EventLink
    private final Listener<MoveInputEvent> onMoveInput = event -> {
        if (mc.thePlayer == null) return;

        if (this.sameY.getValue().getName().equals("Auto Jump")) {
            if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                if (!this.fastJump.getValue())
                    event.setJump(true);
            }
        }
    };

    @EventLink
    private final Listener<Render3DEvent> onRender3D = event -> {
        if (mark.getValue() && targetBlock != null)
            RenderUtil.drawBlockBox(new BlockPos(targetBlock), ColorUtil.withAlpha(getTheme().getAccentColor(new Vector2d(0, 100)), 150), false);
    };

    public Vec3 getHitVec() {
        /* Correct HitVec */
        Vec3 hitVec = new Vec3(blockFace.getX() + Math.random(), blockFace.getY() + Math.random(), blockFace.getZ() + Math.random());

        final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationComponent.rotations, InstanceAccess.mc.playerController.getBlockReachDistance());

        switch (enumFacing.getEnumFacing()) {
            case DOWN:
                hitVec.yCoord = blockFace.getY();
                break;

            case UP:
                hitVec.yCoord = blockFace.getY() + 1;
                break;

            case NORTH:
                hitVec.zCoord = blockFace.getZ();
                break;

            case EAST:
                hitVec.xCoord = blockFace.getX() + 1;
                break;

            case SOUTH:
                hitVec.zCoord = blockFace.getZ() + 1;
                break;

            case WEST:
                hitVec.xCoord = blockFace.getX();
                break;
        }

        if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && movingObjectPosition.getBlockPos().equals(blockFace) &&
                movingObjectPosition.sideHit == enumFacing.getEnumFacing()) {
            hitVec = movingObjectPosition.hitVec;
        }

        if(rotationMode.getValue().getName().equalsIgnoreCase("test")){
            BlockPos position = blockFace;
            double x = position.getX() + 0.5D;
            double y = position.getY() + 0.5D;
            double z = position.getZ() + 0.5D;

/*            x += (double) enumFacing.enumFacing.getDirectionVec().getX() * 0.5D;
            y += (double) enumFacing.enumFacing.getDirectionVec().getY() * 0.5D;
            z += (double) enumFacing.enumFacing.getDirectionVec().getZ() * 0.5D;*/

            double fixY = 0;
            double fixX = 0;
            double fixZ = 0;

            fixX = MathUtil.getRandom(-0.3,0.3);
            fixY = MathUtil.getRandom(-0.3,0.3);
            fixZ = MathUtil.getRandom(-0.3,0.3);
/*            if (enumFacing.enumFacing.getDirectionVec().getX() == 0) {
                fixX = MathUtil.getRandom(-0.3,0.3);
            }
            if (enumFacing.enumFacing.getDirectionVec().getY() == 0) {
                fixY = MathUtil.getRandom(-0.3,0.3);
            }
            if (enumFacing.enumFacing.getDirectionVec().getZ() == 0) {
                fixZ = MathUtil.getRandom(-0.3,0.3);
            }*/

            hitVec = new Vec3(x + fixX, y + fixY, z + fixZ);
        }

        return hitVec;
    }
}