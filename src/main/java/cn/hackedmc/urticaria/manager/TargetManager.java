package cn.hackedmc.urticaria.manager;

import cn.hackedmc.urticaria.Client;
import cn.hackedmc.urticaria.module.impl.combat.KillAura;
import cn.hackedmc.urticaria.module.impl.combat.Teams;
import cn.hackedmc.urticaria.newevent.Listener;
import cn.hackedmc.urticaria.newevent.annotations.EventLink;
import cn.hackedmc.urticaria.newevent.impl.motion.PreUpdateEvent;
import cn.hackedmc.urticaria.newevent.impl.other.AttackEvent;
import cn.hackedmc.urticaria.newevent.impl.other.KillEvent;
import cn.hackedmc.urticaria.newevent.impl.other.TickEvent;
import cn.hackedmc.urticaria.newevent.impl.packet.PacketReceiveEvent;
import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author Alan
 * @since 3/03/2022
 */
public class TargetManager extends ConcurrentLinkedQueue<Entity> implements InstanceAccess {
    public static int kills = 0;
    public static int wins = 0;
    boolean players = true;
    boolean invisibles = false;
    boolean animals = false;
    boolean mobs = false;
    boolean healthCheck = false;
    boolean teams = false;

    private int loadedEntitySize;

    public void init() {
        Client.INSTANCE.getEventBus().register(this);
    }

    @EventLink
    public final Listener<KillEvent> onKill = event -> {
        kills++;
    };

    @EventLink()
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            IChatComponent chatComponent = ((S02PacketChat) packet).chatComponent;
            if (chatComponent != null) {
                final String text = chatComponent.getUnformattedText();

                if (text.contains("获得胜利!")) {
                    wins++;
                    kills = 0;
                } else if (text.contains("游戏开始 ...")) {
                    kills = 0;
                }
            }
        }

        if (packet instanceof S45PacketTitle) {
            final IChatComponent chatComponent = ((S45PacketTitle) packet).getMessage();

            if (chatComponent != null) {
                if(chatComponent.getUnformattedText().contains("胜利")){
                    wins++;
                }
            }
        }
    };

    @EventLink()
    public final Listener<TickEvent> onTick = event -> {
        if (mc.thePlayer.ticksExisted % 150 == 0 || loadedEntitySize != mc.theWorld.loadedEntityList.size()) {
            this.updateTargets();
            loadedEntitySize = mc.theWorld.loadedEntityList.size();
        }
    };

    private boolean checker(Entity entity) {
        return (players && entity instanceof EntityPlayer) || (animals && (entity instanceof EntityAnimal || entity instanceof EntitySquid || entity instanceof EntityGolem ||
                entity instanceof EntityBat)) || (mobs && (entity instanceof EntityMob || entity instanceof EntityVillager || entity instanceof EntitySlime ||
                entity instanceof EntityGhast || entity instanceof EntityDragon));
    }

    public void updateTargets() {
        try {
            KillAura killAura = getModule(KillAura.class);
            players = killAura.player.getValue();
            invisibles = killAura.invisibles.getValue();
            animals = killAura.animals.getValue();
            mobs = killAura.mobs.getValue();
            healthCheck = killAura.healthCheck.getValue();
            teams = Teams.INSTANCE.isEnabled();
            this.clear();
            mc.theWorld.loadedEntityList.stream()
                    .filter(entity -> entity != mc.thePlayer && checker(entity) && (invisibles || !entity.isInvisible()) && (!(entity instanceof EntityLivingBase) || ((EntityLivingBase) entity).getHealth() > 0 || !healthCheck) && (!teams || !Teams.isSameTeam(entity)))
                    .forEach(this::add);
        } catch (Exception e) {
            // Don't give crackers clues...
            if (Client.DEVELOPMENT_SWITCH) e.printStackTrace();
        }
    }

    public List<Entity> getTargets(final double range) {
        if (this.isEmpty()) {
            return new ArrayList<>();
        }
        return this.stream()
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) < range && mc.theWorld.loadedEntityList.contains(entity) && !Client.INSTANCE.getBotManager().contains(entity) && (!(entity instanceof EntityLivingBase) || ((EntityLivingBase) entity).getHealth() > 0 || !healthCheck))
                .sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity)))
                .collect(Collectors.toList());
    }
}