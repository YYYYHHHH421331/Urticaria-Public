package cn.hackedmc.urticaria.util.player;

import cn.hackedmc.urticaria.util.interfaces.InstanceAccess;
import cn.hackedmc.urticaria.util.rotation.RotationUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This is a player util which can be used to do various things related to the player
 *
 * @author Auth
 * @since 21/10/2021
 */
@UtilityClass
public class PlayerUtil implements InstanceAccess {

    private final HashMap<Integer, Integer> GOOD_POTIONS = new HashMap<Integer, Integer>() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public static boolean colorTeam(EntityPlayer sb) {
        String s = sb.getDisplayName().getFormattedText().replace("\u00a7r", "");
        String s1 = mc.thePlayer.getDisplayName().getFormattedText().replace("\u00a7r", "");
        return s.startsWith("\u00a7" + s1.charAt(1));
    }

    public static boolean armorTeam(EntityPlayer entityPlayer) {
        if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
            ItemStack itemstack = mc.thePlayer.inventory.armorInventory[3];
            ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
            ItemStack itemstack1 = entityPlayer.inventory.armorInventory[3];
            ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();
            if (String.valueOf(itemarmor1.getColor(itemstack1)).equals("10511680")) {
                return true;
            } else {
                return itemarmor.getColor(itemstack) == itemarmor1.getColor(itemstack1);
            }
        } else {
            return false;
        }
    }

    public static boolean scoreTeam(EntityPlayer entityPlayer) {
        return mc.thePlayer.isOnSameTeam(entityPlayer);
    }

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    /**
     * Gets the distance between 2 positions
     *
     * @return distance
     */
    public double distance(final BlockPos pos1, final BlockPos pos2) {
        final double x = pos1.getX() - pos2.getX();
        final double y = pos1.getY() - pos2.getY();
        final double z = pos1.getZ() - pos2.getZ();
        return x * x + y * y + z * z;
    }

    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    public Block blockAheadOfPlayer(final double offsetXZ, final double offsetY) {
        return blockRelativeToPlayer(-Math.sin(MoveUtil.direction()) * offsetXZ, offsetY, Math.cos(MoveUtil.direction()) * offsetXZ);
    }

    /**
     * Gets another players' username without any formatting
     *
     * @return players username
     */
    public String name(final EntityPlayer player) {
        return player.getCommandSenderName();
    }

    /**
     * Gets the players' username without any formatting
     *
     * @return players username
     */
    public String name() {
        return mc.thePlayer.getCommandSenderName();
    }

    /**
     * Checks if another players' team is the same as the players' team
     *
     * @return same team
     */
    public boolean sameTeam(final EntityLivingBase player) {
        if (player.getTeam() != null && mc.thePlayer.getTeam() != null) {
            final char c1 = player.getDisplayName().getFormattedText().charAt(1);
            final char c2 = mc.thePlayer.getDisplayName().getFormattedText().charAt(1);
            return c1 == c2;
        }
        return false;
    }

    /**
     * Checks if there is a block under the player
     *
     * @return block under
     */
    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (PlayerUtil.blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isBlockUnder() {
        return isBlockUnder(mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
    }

    /**
     * Checks if a potion is good
     *
     * @return good potion
     */
    public boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

    /**
     * Gets a potions ranking
     *
     * @return potion ranking
     */
    public int potionRanking(final int id) {
        return GOOD_POTIONS.getOrDefault(id, -1);
    }

    /**
     * Checks if the player is in a liquid
     *
     * @return in liquid
     */
    public boolean inLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava();
    }

    /**
     * Fake damages the player
     */
    public void fakeDamage() {
        mc.thePlayer.handleHealthUpdate((byte) 2);
        mc.ingameGUI.healthUpdateCounter = mc.ingameGUI.updateCounter + 20;
    }

    /**
     * Checks if the player is near a block
     *
     * @return block near
     */
    public boolean blockNear(final int range) {
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = blockRelativeToPlayer(x, y, z);

                    if (!(block instanceof BlockAir)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the player is inside a block
     *
     * @return inside block
     */
    public boolean insideBlock() {
        if (mc.thePlayer.ticksExisted < 5) {
            return false;
        }

        final EntityPlayerSP player = PlayerUtil.mc.thePlayer;
        final WorldClient world = PlayerUtil.mc.theWorld;
        final AxisAlignedBB bb = player.getEntityBoundingBox();
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; ++z) {
                    final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    final AxisAlignedBB boundingBox;
                    if (block != null && !(block instanceof BlockAir) && (boundingBox = block.getCollisionBoundingBox(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)))) != null && player.getEntityBoundingBox().intersectsWith(boundingBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sends a click to Minecraft legitimately
     */
    public void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }

    public static boolean onLiquid() {
        boolean onLiquid = false;
        final AxisAlignedBB playerBB = PlayerUtil.mc.thePlayer.getEntityBoundingBox();
        final WorldClient world = PlayerUtil.mc.theWorld;
        final int y = (int) playerBB.offset(0.0, -0.01, 0.0).minY;
        for (int x = MathHelper.floor_double(playerBB.minX); x < MathHelper.floor_double(playerBB.maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(playerBB.minZ); z < MathHelper.floor_double(playerBB.maxZ) + 1; ++z) {
                final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public EnumFacingOffset getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!(PlayerUtil.block(position.xCoord + x2, position.yCoord, position.zCoord) instanceof BlockAir)) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord + y2, position.zCoord) instanceof BlockAir)) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord, position.zCoord + z2) instanceof BlockAir)) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public boolean placable(Block block) {
        if (block == null) return false;
        return block instanceof BlockAir || block instanceof BlockSnow || block instanceof BlockLiquid || block instanceof BlockBush || block instanceof BlockButton || block instanceof BlockContainer || block instanceof BlockFire;
    }

    public static boolean isInLobby() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.isSingleplayer()) return false;

        final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(0);

        if (itemStack == null) return false;

        if (itemStack.getItem() == Items.compass || itemStack.getItem() == Items.nether_star) {
            final String name = itemStack.getDisplayName();
            final String expectedName = PlayerUtil.expectedName(itemStack);
            final String strippedName = name.toLowerCase().replace(" ", "");
            final String strippedExpectedName = expectedName.toLowerCase().replace(" ", "");

            return !strippedName.contains(strippedExpectedName);
        }

        return false;
    }

    public static String expectedName(final ItemStack stack) {
        String s = ("" + StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name")).trim();
        final String s1 = EntityList.getStringFromID(stack.getMetadata());

        if (s1 != null) {
            s = s + " " + StatCollector.translateToLocal("entity." + s1 + ".name");
        }

        return s;
    }

    public Vec3 getNearlyBlock(float blockRange, Block type) {
        final List<Vec3> possibleBlock = new ArrayList<>();
        final int range = (int) blockRange;

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);

                    if (block == type) possibleBlock.add(new Vec3(x, y, z).addVector(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                }
            }
        }

        if (possibleBlock.isEmpty()) {
            return null;
        } else if (possibleBlock.size() == 1) {
            return possibleBlock.get(0);
        } else {
            possibleBlock.sort(Comparator.comparingDouble(vec3 -> {

                final double d0 = (mc.thePlayer.posX) - vec3.xCoord;
                final double d1 = (mc.thePlayer.posY - 1) - vec3.yCoord;
                final double d2 = (mc.thePlayer.posZ) - vec3.zCoord;
                return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

            }));

            return possibleBlock.get(0);
        }
    }

    // This methods purpose is to get block placement possibilities, blocks are 1 unit thick so please don't change it to 0.5 it causes bugs.
    public Vec3 getPlacePossibility(double offsetX, double offsetY, double offsetZ, float blockRange, double startY, boolean rayCast) {
        final List<Vec3> possibilities = new ArrayList<>();
        final int range = (int) (blockRange + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= -1; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);

                    if (!placable(block)) {
                        for (int x2 = -1; x2 <= 1; x2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x + x2, mc.thePlayer.posY + y, mc.thePlayer.posZ + z));

                        for (int y2 = -1; y2 <= 1; y2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y + y2, mc.thePlayer.posZ + z));

                        for (int z2 = -1; z2 <= 1; z2 += 1)
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > blockRange || !(PlayerUtil.block(vec3.xCoord, vec3.yCoord, vec3.zCoord) instanceof BlockAir) || vec3.yCoord >= startY || (rayCast && !RotationUtil.placeable(blockRange, RotationUtil.calculate(vec3), true, vec3)));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = (mc.thePlayer.posX + offsetX) - vec3.xCoord;
            final double d1 = (mc.thePlayer.posY - 1 + offsetY) - vec3.yCoord;
            final double d2 = (mc.thePlayer.posZ + offsetZ) - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.get(0);
    }

    public Vec3 getPlacePossibility() {
        return getPlacePossibility(0, 0, 0, 5, 255, false);
    }

}