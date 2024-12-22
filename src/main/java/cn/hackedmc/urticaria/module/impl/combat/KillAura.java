package cn.hackedmc.urticaria.module.impl.combat;

import cn.hackedmc.urticaria.api.Rise;
import cn.hackedmc.urticaria.component.impl.player.*;
import cn.hackedmc.urticaria.module.impl.movement.TargetStrafe;
import cn.hackedmc.urticaria.module.impl.other.Nuker;
import cn.hackedmc.urticaria.module.impl.player.AutoGApple;
import cn.hackedmc.urticaria.module.impl.player.Manager;
import cn.hackedmc.urticaria.util.RandomUtil;
import cn.hackedmc.urticaria.util.RayCastUtil;
import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.component.impl.hypixel.InventoryDeSyncComponent;
import cn.hackedmc.urticaria.component.impl.player.rotationcomponent.MovementFix;
import cn.hackedmc.urticaria.component.impl.render.ESPComponent;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.api.ESPColor;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.impl.AboveBox;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.impl.FullBox;
import cn.hackedmc.urticaria.component.impl.render.espcomponent.impl.SigmaRing;
import cn.hackedmc.urticaria.module.Module;
import cn.hackedmc.urticaria.module.api.Category;
import cn.hackedmc.urticaria.module.api.ModuleInfo;
import cn.hackedmc.urticaria.module.impl.player.Scaffold;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.Priorities;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.input.ClickEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PostMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreMotionEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.motion.SlowDownEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.other.WorldChangeEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketSendEvent;
import cn.hackedmc.urticaria.newevent.impl.render.MouseOverEvent;
import cn.hackedmc.urticaria.newevent.impl.render.Render2DEvent;
import cn.hackedmc.urticaria.newevent.impl.render.RenderItemEvent;
import cn.hackedmc.urticaria.util.chat.ChatUtil;
import cn.hackedmc.urticaria.util.math.MathUtil;
import cn.hackedmc.urticaria.util.packet.PacketUtil;
import cn.hackedmc.urticaria.util.render.ColorUtil;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import cn.hackedmc.urticaria.util.vector.Vector2d;
import cn.hackedmc.urticaria.util.vector.Vector2f;
import cn.hackedmc.urticaria.value.impl.*;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.viamcp.ViaMCP;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import util.time.StopWatch;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Alan
 * @since 11/17/2021
 */
@Rise
@ModuleInfo(name = "module.combat.killaura.name", description = "module.combat.killaura.description", category = Category.COMBAT)
public final class KillAura extends Module {
    public static KillAura INSTANCE;
    private final ModeValue mode = new ModeValue("Attack Mode", this)
            .add(new SubMode("Single"))
            .add(new SubMode("Switch"))
            .add(new SubMode("Multiple"))
            .setDefault("Single");

    private final BoundsNumberValue switchTicks = new BoundsNumberValue("Switch Ticks", this, 100, 1000, 0, 2000, 1);

    private final ModeValue autoBlock = new ModeValue("Auto Block", this)
            .add(new SubMode("None"))
            .add(new SubMode("Fake"))
            .add(new SubMode("Vanilla"))
            .add(new SubMode("NCP"))
            .add(new SubMode("Watchdog Blink"))
            .add(new SubMode("Watchdog 1.9+"))
            .add(new SubMode("Watchdog A"))
            .add(new SubMode("HuaYuTing"))
            .add(new SubMode("GrimAC"))
            .add(new SubMode("Legit"))
            .add(new SubMode("Intave"))
            .add(new SubMode("Old Intave"))
            .add(new SubMode("Imperfect Vanilla"))
            .add(new SubMode("Vanilla ReBlock"))
            .add(new SubMode("New NCP"))
            .setDefault("None");

    private final ModeValue clickMode = new ModeValue("Click Delay Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Hit Select"))
            .add(new SubMode("1.9+"))
            .add(new SubMode("1.9+ (1.8 Visuals)"))
            .setDefault("Normal");

    public final NumberValue range = new NumberValue("Range", this, 3, 3, 6, 0.1);
    private final BoundsNumberValue cps = new BoundsNumberValue("CPS", this, 10, 15, 1, 20, 1);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation speed", this, 5, 10, 0, 10, 1);
    private final ListValue<MovementFix> movementCorrection = new ListValue<>("Movement correction", this);
    private final BooleanValue keepSprint = new BooleanValue("Keep sprint", this, false);

    private final ModeValue espMode = new ModeValue("Target ESP Mode", this)
            .add(new SubMode("Ring"))
            .add(new SubMode("Box"))
            .add(new SubMode("None"))
            .setDefault("Ring");

    public final ModeValue boxMode = new ModeValue("Box Mode", this, () -> !(espMode.getValue()).getName().equals("Box"))
            .add(new SubMode("Above"))
            .add(new SubMode("Full"))
            .setDefault("Ring");

    private final ModeValue colorMode = new ModeValue("Color Mode", this, () -> espMode.getValue().getName().equalsIgnoreCase("None"))
            .add(new SubMode("Basic"))
            .add(new SubMode("White"))
            .add(new SubMode("Mixed"))
            .setDefault("Basic");

    private final BooleanValue rayCast = new BooleanValue("Ray cast", this, false);

    private final BooleanValue advanced = new BooleanValue("Advanced", this, false);
    private final BooleanValue lookAtTheClosestPoint = new BooleanValue("Look at the closest point on the player", this, true, () -> !advanced.getValue());
    private final BooleanValue subTicks = new BooleanValue("Attack outside ticks", this, false, () -> !advanced.getValue());
    private final StringValue runMovementFixIfNot = new StringValue("Exclude MovementCorrection if", this, "", () -> !advanced.getValue());
    private final ModeValue rotationMode = new ModeValue("Rotation Mode", this, () -> !advanced.getValue())
            .add(new SubMode("Off"))
            .add(new SubMode("Legit/Normal"))
            .add(new SubMode("Autistic AntiCheat"))
            .setDefault("Legit/Normal");
    private final BooleanValue attackWhilstScaffolding = new BooleanValue("Attack whilst Scaffolding", this, false, () -> !advanced.getValue());
    private final BooleanValue noChest = new BooleanValue("No Inventory",this, false, () -> !advanced.getValue());
    private final BooleanValue noBlink = new BooleanValue("No Blink", this, false, () -> !advanced.getValue());
    private final BooleanValue noOneTickCheck = new BooleanValue("Attack with action", this, false, () -> !advanced.getValue());

    private final BooleanValue noSwing = new BooleanValue("No swing", this, false, () -> !advanced.getValue());
    private final BooleanValue autoDisable = new BooleanValue("Auto disable", this, false, () -> !advanced.getValue());
    private final BooleanValue grimFalse = new BooleanValue("Prevent Grim false positives", this, false, () -> !advanced.getValue());

    private final BooleanValue showTargets = new BooleanValue("Targets", this, false);
    public final BooleanValue player = new BooleanValue("Player", this, false, () -> !showTargets.getValue());
    public final BooleanValue invisibles = new BooleanValue("Invisibles", this, false, () -> !showTargets.getValue());
    public final BooleanValue animals = new BooleanValue("Animals", this, false, () -> !showTargets.getValue());
    public final BooleanValue mobs = new BooleanValue("Mobs", this, false, () -> !showTargets.getValue());
    public final BooleanValue healthCheck = new BooleanValue("HealthCheck", this, true, () -> !showTargets.getValue());
    private final StopWatch attackStopWatch = new StopWatch();
    private final StopWatch clickStopWatch = new StopWatch();

    private float randomYaw, randomPitch;
    public boolean blocking, swing, allowAttack;
    private long nextSwing;

    private int blinkTick = 0;
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    public static List<Entity> targets;
    public static List<Entity> pastTargets = new ArrayList<>();
    public Entity target;
    public static Vector2f aurarotation;
    public StopWatch subTicksStopWatch = new StopWatch();
    public StopWatch switchChangeTicks = new StopWatch();


    private int attack, hitTicks;


    public KillAura() {
        for (MovementFix movementFix : MovementFix.values()) {
            movementCorrection.add(movementFix);
        }

        movementCorrection.setDefault(MovementFix.OFF);

        INSTANCE = this;
    }

    @EventLink()
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        this.hitTicks++;

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        if (target == null || mc.thePlayer.isDead || (!attackWhilstScaffolding.getValue() && Scaffold.INSTANCE.isEnabled())) {
            if (!autoBlock.getValue().getName().equalsIgnoreCase("Watchdog Blink")) this.unblock(true);
            target = null;
        }

        if (target != null && !this.canBlock()) {
            if (!autoBlock.getValue().getName().equalsIgnoreCase("Watchdog Blink"))this.unblock(true);
        }
    };

    @Override
    protected void onEnable() {
        this.attack = 0;
        this.switchChangeTicks.reset();
        this.blinkTick = -1;
        // this.blockingTick = 0;
    }

    @Override
    public void onDisable() {
        target = null;

        this.unblock(true);
        while (!packets.isEmpty()) {
            final Packet<?> packet1 = packets.poll();

            if (!(packet1 instanceof C09PacketHeldItemChange)) mc.getNetHandler().addToSendQueueUnregistered(packet1);
        }
    }

    @EventLink()
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (this.autoDisable.getValue()) {
            this.toggle();
        }
    };

    public void getTargets(double range) {
        targets = Client.INSTANCE.getTargetManager().getTargets(range);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.thePlayer.getHealth() <= 0.0 && this.autoDisable.getValue()) {
            this.toggle();
        }

        if (Scaffold.INSTANCE.isEnabled() && !attackWhilstScaffolding.getValue()) {
            target = null;
            return;
        }

        if ((noChest.getValue() && (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiInventory || getModule(Manager.class).open)) || (noBlink.getValue() && BlinkComponent.blinking)) {
            target = null;
            return;
        }

        this.attack = Math.max(Math.min(this.attack, this.attack - 2), 0);

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        if (Nuker.INSTANCE.isEnabled() && Nuker.INSTANCE.needUpdate) {
            return;
        }

        /*
         * Getting targets and selecting the nearest one
         */
        this.getTargets(range.getValue().doubleValue());

        if (targets.isEmpty()) {
            this.randomiseTargetRotations();
            target = null;
            return;
        }

        if (mode.getValue().getName().equalsIgnoreCase("Single"))
            target = targets.get(0);
        else if (this.switchChangeTicks.finished(RandomUtil.nextInt(switchTicks.getMin().intValue(), switchTicks.getMax().intValue())) && targets.size() > 1) {
            Client.INSTANCE.getTargetManager().updateTargets();
            if (targets.contains(target)) {
                targets.remove(target);
                Entity oldTarget = target;
                target = targets.get(0);
                targets.add(oldTarget);
            } else {
                target = targets.get(0);
            }
            this.switchChangeTicks.reset();
        } else if (targets.size() == 1) {
            target = targets.get(0);
        }

//        while (target == null || pastTargets.contains(target) || mc.thePlayer.getDistance(target.posX, target.posY, target.posZ) > range.getValue().doubleValue()) {
//            target = targets.get(0);
//            targets.remove(0);
//
//            if (targets.isEmpty()) {
//                pastTargets.clear();
//                this.getTargets(range.getValue().doubleValue());
//            }
//        }

        if (target == null || mc.thePlayer.isDead) {
            this.randomiseTargetRotations();
            return;
        }

        Color color = new Color(19, 89, 6, 4);
        switch (colorMode.getValue().getName().toLowerCase()) {
            case "basic": {
                color = this.getTheme().getFirstColor();

                break;
            }

            case "white": {
                color = Color.WHITE;

                break;
            }

            case "mixed": {
                double factor = this.getTheme().getBlendFactor(new Vector2d(0, 0));
                color = ColorUtil.mixColors(this.getTheme().getFirstColor(), this.getTheme().getSecondColor(), factor);

                break;
            }
        }

        switch (espMode.getValue().getName()) {
            case "Ring":
                ESPComponent.add(new SigmaRing(new ESPColor(color, color, color)));
                break;
            case "Box":
                switch (boxMode.getValue().getName()) {
                    case "Full":
                        ESPComponent.add(new FullBox(new ESPColor(color, color, color)));
                        break;
                    case "Above":
                        ESPComponent.add(new AboveBox(new ESPColor(color, color, color)));
                        break;
                }
                break;

        }

        if (this.canBlock()) {
            this.preBlock();
        }

        /*
         * Calculating rotations to target
         */
        this.rotations();

        /*
         * Doing the attack
         */
        this.doAttack(targets);

        /*
         * Blocking
         */
        if (this.canBlock()) {
            this.postAttackBlock();
        }
    };

    public void rotations() {
        if (mc.thePlayer.getDistanceToEntity(target) > range.getValue().doubleValue()) return;
        final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
        final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
        final float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);

        switch (rotationMode.getValue().getName()) {
            case "Legit/Normal":
                final Vector2f targetRotations = RotationUtil.calculate(target, lookAtTheClosestPoint.getValue(), range.getValue().doubleValue());

                this.randomiseTargetRotations();

                targetRotations.x += randomYaw;
                targetRotations.y += randomPitch;

                if (RayCastUtil.rayCast(targetRotations, range.getValue().doubleValue()).typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    randomYaw = randomPitch = 0;
                }
                aurarotation = targetRotations;

                if (rotationSpeed != 0) {
                    RotationComponent.setRotations(targetRotations, rotationSpeed,
                            movementCorrection.getValue() == MovementFix.OFF || shouldRun() ? MovementFix.OFF : movementCorrection.getValue());
                }
                break;

            case "Autistic AntiCheat":
                double speed = rotationSpeed * 10;
                RotationComponent.setRotations(new Vector2f((float) (RotationComponent.rotations.x + speed), 0), speed / 18,
                        movementCorrection.getValue() == MovementFix.OFF || shouldRun() ? MovementFix.OFF : movementCorrection.getValue());
                break;
        }

    }

    public boolean shouldRun() {
        // If you're Tecnio don't scroll down
        String userEnteredCode = runMovementFixIfNot.getValue();

        // Legit no one can bypass this to make a rce
        if (userEnteredCode.length() > 60 || userEnteredCode.length() <= 1 || userEnteredCode.contains(";") || userEnteredCode.contains(".")) {
            return false;
        }

        // I don't think you could write something more scuffed if you tried
        String script =
                // Don't kill me please
                "" +
                        "boolean onGround = " + mc.thePlayer.onGround + ";" +
                        "boolean ground = onGround;" +

                        "int ticksOnGround = " + mc.thePlayer.onGroundTicks + ";" +
                        "int onGroundTicks = ticksOnGround;" +
                        "int groundTicks = ticksOnGround;" +

                        "int ticksInAir = " + mc.thePlayer.offGroundTicks + ";" +
                        "int airTicks = ticksInAir;" +
                        "int ticksOffGround = ticksInAir;" +

                        "int ticksSinceVelocity = " + mc.thePlayer.ticksSinceVelocity + ";" +
                        "int velocityTicks = ticksSinceVelocity;" +

                        "boolean runIf = " + userEnteredCode + ";" +

                        "System.out.println(runIf);";

        ScriptEvaluator scriptEvaluator = new ScriptEvaluator();

        // Preserve current console which contains.
        PrintStream previousConsole = System.out;

        // Set the standard output to use newConsole.
        ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newConsole));

        try {
            scriptEvaluator.cook(script);
            scriptEvaluator.evaluate(new Object[0]);
        } catch (CompileException | InvocationTargetException e) {
            return false;
        }

        boolean result = newConsole.toString().contains("true");

        System.setOut(previousConsole);

        return result;
    }

    /*
     * Randomising rotation target to simulate legit players
     */
    private void randomiseTargetRotations() {
        randomYaw += (float) (Math.random() - 0.5f);
        randomPitch += (float) (Math.random() - 0.5f) * 2;
    }

    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> {
        event.setRange(event.getRange() + range.getValue().doubleValue() - 3);
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if ((noChest.getValue() && (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiInventory || getModule(Manager.class).open)) || (noBlink.getValue() && BlinkComponent.blinking))
            return;

        if (target != null && this.canBlock()) {
            this.postBlock();
        }
    };

    private void doAttack(final List<Entity> targets) {
        String autoBlock = this.autoBlock.getValue().getName();
        if (BadPacketsComponent.bad(false, true, true, true, true) &&
                (autoBlock.equals("Fake") || autoBlock.equals("None") ||
                        autoBlock.equals("Imperfect Vanilla") || autoBlock.equals("Vanilla ReBlock"))) {
            return;
        }

        double delay = -1;
        boolean flag = false;

        switch (clickMode.getValue().getName()) {
            case "Hit Select": {
                delay = 9;
                flag = target.hurtResistantTime <= 10;
                break;
            }

            case "1.9+": {
                double speed = 4;

                if (mc.thePlayer.getHeldItem() != null) {
                    final Item item = mc.thePlayer.getHeldItem().getItem();

                    if (item instanceof ItemSword) {
                        speed = 1.6;
                    } else if (item instanceof ItemSpade) {
                        speed = 1;
                    } else if (item instanceof ItemPickaxe) {
                        speed = 1.2;
                    } else if (item instanceof ItemAxe) {
                        switch (((ItemAxe) item).getToolMaterial()) {
                            case WOOD:
                            case STONE:
                                speed = 0.8;
                                break;

                            case IRON:
                                speed = 0.9;
                                break;

                            default:
                                speed = 1;
                                break;
                        }
                    } else if (item instanceof ItemHoe) {
                        switch (((ItemHoe) item).getToolMaterial()) {
                            case WOOD:
                            case GOLD:
                                speed = 1;
                                break;

                            case STONE:
                                speed = 2;
                                break;

                            case IRON:
                                speed = 3;
                                break;
                        }
                    }
                }

                delay = 1 / speed * 20 - 1;
                break;
            }
        }

        if (attackStopWatch.finished(this.nextSwing) && (!grimFalse.getValue() || !(mc.thePlayer.ticksSprint <= 1 && mc.thePlayer.isSprinting())) && (!BadPacketsComponent.bad(false, true, true, false, true) || noOneTickCheck.getValue()) && target != null && (clickStopWatch.finished((long) (delay * 50)) || flag)) {
            final long clicks = (long) (Math.round(MathUtil.getRandom(this.cps.getValue().intValue(), this.cps.getSecondValue().intValue())) * 1.5);
            this.nextSwing = 1000 / clicks;

            if (Math.sin(nextSwing) + 1 > Math.random() || attackStopWatch.finished(this.nextSwing + 500) || Math.random() > 0.5) {
                this.allowAttack = true;

                if (this.canBlock()) {
                    this.attackBlock();
                }

                if (this.allowAttack) {
                    /*
                     * Attacking target
                     */
                    final double range = this.range.getValue().doubleValue();
                    final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationComponent.rotations, range);

                    switch (this.mode.getValue().getName()) {
                        case "Single":
                        case "Switch": {
                            if ((mc.thePlayer.getDistanceToEntity(target) <= range && !rayCast.getValue()) || RayCastUtil.isEntity(target, movingObjectPosition)) {
                                this.attack(target);
                            } else if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && targets.contains(movingObjectPosition.entityHit)) {
                                this.attack(movingObjectPosition.entityHit);
                            } else {
                                switch (clickMode.getValue().getName()) {
                                    case "Normal":
                                    case "Hit Select":
                                        PacketUtil.send(new C0APacketAnimation());
                                        this.clickStopWatch.reset();
                                        this.hitTicks = 0;
                                        break;
                                }
                            }
                            break;
                        }

                        case "Multiple": {
                            targets.removeIf(target -> mc.thePlayer.getDistanceToEntity(target) > range);

                            if (!targets.isEmpty()) {
                                targets.forEach(this::attack);
                            }
                            break;
                        }
                    }
                }

                this.attackStopWatch.reset();
            }
        }
    }

    private void attack(final Entity target) {
        this.attack = Math.min(Math.max(this.attack, this.attack + 2), 5);

        Client.INSTANCE.getEventBus().handle(new ClickEvent());

        final AttackEvent event = new AttackEvent(target);
        Client.INSTANCE.getEventBus().handle(event);

        if (!event.isCancelled()) {
            if (ViaMCP.getInstance().getVersion() <= 47) {
                if (AutoGApple.eating && AutoGApple.INSTANCE.alwaysAttack.getValue()) {
                    if (noSwing.getValue()) PacketUtil.sendNoEvent(new C0APacketAnimation());
                    else mc.thePlayer.swingItemNoPacket();
                } else {
                    if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                    else mc.thePlayer.swingItem();
                }
            }

            if (this.keepSprint.getValue()) {
                mc.playerController.syncCurrentPlayItem();

                if (AutoGApple.eating && AutoGApple.INSTANCE.alwaysAttack.getValue())
                    PacketUtil.sendNoEvent(new C02PacketUseEntity(event.getTarget(), C02PacketUseEntity.Action.ATTACK));
                else
                    PacketUtil.send(new C02PacketUseEntity(event.getTarget(), C02PacketUseEntity.Action.ATTACK));

                if (mc.thePlayer.fallDistance > 0 && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null) {
                    mc.thePlayer.onCriticalHit(target);
                }
            } else {
                mc.playerController.attackEntity(mc.thePlayer, target);
            }

            if (ViaMCP.getInstance().getVersion() > 47) {
                if (AutoGApple.eating && AutoGApple.INSTANCE.alwaysAttack.getValue()) {
                    if (noSwing.getValue()) PacketUtil.sendNoEvent(new C0APacketAnimation());
                    else mc.thePlayer.swingItemNoPacket();
                } else {
                    if (noSwing.getValue()) PacketUtil.send(new C0APacketAnimation());
                    else mc.thePlayer.swingItem();
                }
            }
        }

        this.clickStopWatch.reset();
        this.hitTicks = 0;

//        if (!pastTargets.contains(target)) pastTargets.add(target);
    }

    private void block(final boolean check, final boolean interact, final boolean raycast) {
        if (!blocking || !check) {
            if (interact && target != null && (mc.objectMouseOver.entityHit == target || !raycast)) {
                mc.playerController.interactWithEntitySendPacket(mc.thePlayer, target);
            }

            PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
            blocking = true;
        }
    }

    private void unblock(final boolean swingCheck) {
        if (blocking && (!swingCheck || !swing)) {
            if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
                PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            } else {
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }
            blocking = false;
        }
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<RenderItemEvent> onRenderItem = event -> {
        if (this.canRenderBlock()) {
            event.setEnumAction(EnumAction.BLOCK);
            event.setUseItem(true);
        }
    };

    @EventLink()
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C0APacketAnimation) {
            swing = true;
        } else if (packet instanceof C03PacketPlayer) {
            swing = false;
        }

        this.packetBlock(event);
    };

    public void packetBlock(final PacketSendEvent event) {
        final Packet<?> packet = event.getPacket();

        switch (autoBlock.getValue().getName()) {
            case "Intave":
                if (packet instanceof C03PacketPlayer) {
                    event.setCancelled(true);
                    this.unblock(false);
                    PacketUtil.sendNoEvent(packet);
                    this.block(false, true, true);
                }
                break;

            case "Watchdog Blink": {
                if (blinkTick >= 0 && !BlinkComponent.blinking) {
                    if (packet instanceof C0APacketAnimation || packet instanceof C02PacketUseEntity || packet instanceof C0FPacketConfirmTransaction || packet instanceof C00PacketKeepAlive || packet instanceof C0BPacketEntityAction) {
                        event.setCancelled();
                        packets.add(packet);
                    }
                }

                if (packet instanceof C03PacketPlayer) {
                    if (target != null && canBlock() && !BlinkComponent.blinking) {
                        if (blinkTick >= 0) {
                            event.setCancelled();
                            if (blinkTick == 0) {
                                packets.add(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            } else if (blinkTick == 1) {
                                packets.add(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) packets.add(new C02PacketUseEntity(mc.objectMouseOver.entityHit, C02PacketUseEntity.Action.INTERACT));
                                packets.add(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStackNative()));
                            }

                            packets.add(packet);
                            if (blinkTick >= 1) {
                                while (!packets.isEmpty())
                                    mc.getNetHandler().addToSendQueueUnregistered(packets.poll());
                                this.blocking = true;
                                blinkTick = -1;
                            }
                        }
                        blinkTick++;
                    } else if (this.blinkTick >= 0) {
                        this.unblock(true);
                        while (!packets.isEmpty()) {
                            final Packet<?> packet1 = packets.poll();

                            if (packet1 instanceof C08PacketPlayerBlockPlacement || packet1 instanceof C09PacketHeldItemChange) continue;

                            mc.getNetHandler().addToSendQueueUnregistered(packet1);
                        }
                        blinkTick = -1;
                    }
                }

                break;
            }

            case "Fake":
            case "None":
                if (SlotComponent.getItemStack() == null || !(SlotComponent.getItemStack().getItem() instanceof ItemSword)) {
                    return;
                }

                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    final C08PacketPlayerBlockPlacement wrapper = (C08PacketPlayerBlockPlacement) packet;

                    if (wrapper.getPlacedBlockDirection() == 255) {
                        event.setCancelled(true);
                    }
                } else if (packet instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging wrapper = ((C07PacketPlayerDigging) packet);

                    if (wrapper.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                        event.setCancelled(true);
                    }
                }
                break;
        }
    }

    private void attackBlock() {
        if ("Legit".equals(autoBlock.getValue().getName())) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }


            this.allowAttack = !BadPacketsComponent.bad(false, false, false, true, false);
        }
    }

    private void postAttackBlock() {
        switch (autoBlock.getValue().getName()) {
            case "Legit":
                if (this.hitTicks == 1) {
                    mc.gameSettings.keyBindUseItem.setPressed(true);
                    this.blocking = true;
                }
                break;

            case "Intave":
            case "Watchdog 1.9+":
                this.block(true, true, true);
                break;

            case "Vanilla":
                if (this.hitTicks != 0) {
                    this.block(false, true, true);
                }
                break;

            case "Imperfect Vanilla":
                if (this.hitTicks == 1 && mc.thePlayer.isSwingInProgress && Math.random() > 0.1) {
                    this.block(false, true, true);
                }
                break;

            case "Vanilla ReBlock":
                if (this.hitTicks == 1 || !this.blocking) {
                    this.block(false, true, true);
                }
                break;

        }
    }

    private void preBlock() {
        switch (autoBlock.getValue().getName()) {
            case "NCP":
            case "Intave":
                this.unblock(false);
                break;

            case "New NCP":
            case "Watchdog 1.9+":
                if (this.blocking) {
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                    this.blocking = false;
                }
                break;

            case "Watchdog A":
                PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                mc.playerController.interactWithEntitySendPacket(mc.thePlayer, target);
                PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                this.blocking = true;

                break;

            case "HuaYuTing":
                if (this.blocking && !AutoGApple.eating) {
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                    this.blocking = false;
                }
                break;

            case "Old Intave":
                if (mc.thePlayer.isUsingItem()) {
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex() % 8 + 1));
                    PacketUtil.send(new C09PacketHeldItemChange(SlotComponent.getItemIndex()));
                }

                break;

            case "GrimAC":
                this.unblock(true);
                break;
        }
    }

    private void postBlock() {
        switch (autoBlock.getValue().getName()) {
            case "NCP":
            case "New NCP":
            case "GrimAC":
                this.block(true, false, true);
                break;

            case "HuaYuTing":
                if (!blocking) {
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                    if (!AutoGApple.eating && ViaMCP.getInstance().getVersion() > 47) {
                        PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                        useItem.write(Type.VAR_INT, 1);
                        PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                    }
                    blocking = true;
                }

                break;

            case "Old Intave":
                if (mc.thePlayer.isUsingItem() && InventoryDeSyncComponent.isDeSynced()) {
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(SlotComponent.getItemStack()));
                }

                break;
        }
    }

    public boolean canRenderBlock() {
        final String modeValue = autoBlock.getValue().getName();

        return mc.thePlayer != null && target != null && canBlock() && !(modeValue.equalsIgnoreCase("Old Intave") || modeValue.equalsIgnoreCase("None")|| modeValue.equalsIgnoreCase("Legit"));
    }

    private boolean canBlock() {
        return SlotComponent.getItemStackNative() != null && SlotComponent.getItemStackNative().getItem() instanceof ItemSword && mc.thePlayer.getDistanceToEntity(target) <= range.getValue().floatValue();
    }

    @EventLink()
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (this.subTicks.getValue() && this.attack <= 5 && target != null && this.subTicksStopWatch.finished(10)) {
            this.subTicksStopWatch.reset();

            /*
             * Getting targets and selecting the nearest one
             */
            targets = Client.INSTANCE.getTargetManager().getTargets(range.getValue().doubleValue());

            if (targets.isEmpty()) {
                this.randomiseTargetRotations();
                target = null;
                return;
            }

            this.doAttack(targets);
        }
    };
}