package crazypants.enderio.base.teleport;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.Util;
import com.enderio.core.common.vecmath.VecmathUtil;
import com.enderio.core.common.vecmath.Vector3d;

import crazypants.enderio.api.teleport.IItemOfTravel;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.api.teleport.TeleportEntityEvent;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.config.config.PersonalConfig;
import crazypants.enderio.base.config.config.TeleportConfig;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.teleport.packet.PacketOpenAuthGui;
import crazypants.enderio.base.teleport.packet.PacketTravelEvent;
import crazypants.enderio.util.Prep;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = EnderIO.MODID, value = Side.CLIENT)
public class TravelController {

  private static final @Nonnull Random rand = new Random();

  private static boolean wasJumping = false;

  private static boolean wasSneaking = false;

  private static boolean tempJump;

  private static boolean tempSneak;

  private static boolean showTargets = false;

  private static BlockPos onBlockCoord;

  private static BlockPos selectedCoord;

  private static final @Nonnull HashSet<BlockPos> candidates = new HashSet<>();

  private static boolean selectionEnabled = true;

  private static double fovRad;

  private static boolean doesHandAllowTravel(@Nonnull EnumHand hand) {
    return TeleportConfig.enableOffHandTravel.get() || hand == EnumHand.MAIN_HAND;
  }

  private static boolean doesHandAllowBlink(@Nonnull EnumHand hand) {
    return TeleportConfig.enableOffHandBlink.get() || hand == EnumHand.MAIN_HAND;
  }

  public static boolean activateTravelAccessable(@Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull World world, @Nonnull EntityPlayer player,
      @Nonnull TravelSource source) {
    BlockPos target = selectedCoord;
    if (target == null) {
      return false;
    }
    TileEntity te = world.getTileEntity(target);
    if (te instanceof ITravelAccessable) {
      ITravelAccessable ta = (ITravelAccessable) te;
      if (ta.getRequiresPassword(player)) {
        PacketOpenAuthGui p = new PacketOpenAuthGui(target);
        PacketHandler.INSTANCE.sendToServer(p);
        return true;
      }
    }
    if (doesHandAllowTravel(hand)) {
      travelToSelectedTarget(player, equipped, hand, source, false);
      return true;
    }
    return true;
  }

  public static boolean doBlink(@Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull EntityPlayer player) {
    if (!doesHandAllowBlink(hand)) {

      return false;
    }
    Vector3d eye = Util.getEyePositionEio(player);
    Vector3d look = Util.getLookVecEio(player);

    Vector3d sample = new Vector3d(look);
    sample.scale(TravelSource.STAFF_BLINK.getMaxDistanceTravelled());
    sample.add(eye);
    Vec3d eye3 = new Vec3d(eye.x, eye.y, eye.z);
    Vec3d end = new Vec3d(sample.x, sample.y, sample.z);

    double playerHeight = player.getYOffset();
    // if you looking at you feet, and your player height to the max distance, or part there of
    double lookComp = -look.y * playerHeight;
    double maxDistance = TravelSource.STAFF_BLINK.getMaxDistanceTravelled() + lookComp;

    RayTraceResult p = player.world.rayTraceBlocks(eye3, end, !TeleportConfig.enableBlinkNonSolidBlocks.get());
    if (p == null) {

      // go as far as possible
      for (double i = maxDistance; i > 1; i--) {

        sample.set(look);
        sample.scale(i);
        sample.add(eye);
        // we test against our feets location
        sample.y -= playerHeight;
        if (doBlinkAround(player, equipped, hand, sample, true)) {
          return true;
        }
      }
      return false;
    } else {

      List<RayTraceResult> res = Util.raytraceAll(player.world, eye3, end, !TeleportConfig.enableBlinkNonSolidBlocks.get());
      for (RayTraceResult pos : res) {
        if (pos != null) {
          IBlockState hitBlock = player.world.getBlockState(pos.getBlockPos());
          if (isBlackListedBlock(player, pos, hitBlock)) {
            BlockPos bp = pos.getBlockPos();
            maxDistance = Math.min(maxDistance, VecmathUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) - 1.5 - lookComp);
          }
        }
      }

      Vector3d targetBc = new Vector3d(p.getBlockPos());
      double sampleDistance = 1.5;
      BlockPos bp = p.getBlockPos();
      double teleDistance = VecmathUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) + sampleDistance;

      while (teleDistance < maxDistance) {
        sample.set(look);
        sample.scale(sampleDistance);
        sample.add(targetBc);
        // we test against our feets location
        sample.y -= playerHeight;

        if (doBlinkAround(player, equipped, hand, sample, false)) {
          return true;
        }
        teleDistance++;
        sampleDistance++;
      }
      sampleDistance = -0.5;
      teleDistance = VecmathUtil.distance(eye, new Vector3d(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5)) + sampleDistance;
      while (teleDistance > 1) {
        sample.set(look);
        sample.scale(sampleDistance);
        sample.add(targetBc);
        // we test against our feets location
        sample.y -= playerHeight;

        if (doBlinkAround(player, equipped, hand, sample, false)) {
          return true;
        }
        sampleDistance--;
        teleDistance--;
      }
    }
    return false;
  }

  private static boolean isBlackListedBlock(@Nonnull EntityPlayer player, @Nonnull RayTraceResult pos, @Nonnull IBlockState hitBlock) {
    return TeleportConfig.blockBlacklist.get().contains(hitBlock.getBlock())
        || (hitBlock.getBlockHardness(player.world, pos.getBlockPos()) < 0 && !TeleportConfig.enableBlinkUnbreakableBlocks.get());
  }

  private static boolean doBlinkAround(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull Vector3d sample,
      boolean conserveMomentum) {
    if (doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y) - 1, (int) Math.floor(sample.z)),
        conserveMomentum)) {
      return true;
    }
    if (doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y), (int) Math.floor(sample.z)), conserveMomentum)) {
      return true;
    }
    if (doBlink(player, equipped, hand, new BlockPos((int) Math.floor(sample.x), (int) Math.floor(sample.y) + 1, (int) Math.floor(sample.z)),
        conserveMomentum)) {
      return true;
    }
    return false;
  }

  private static boolean doBlink(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull BlockPos coord,
      boolean conserveMomentum) {
    return travelToLocation(player, equipped, hand, TravelSource.STAFF_BLINK, coord, conserveMomentum);
  }

  public static boolean showTargets() {
    return showTargets && selectionEnabled;
  }

  public static void setSelectionEnabled(boolean b) {
    selectionEnabled = b;
    if (!selectionEnabled) {
      candidates.clear();
    }
  }

  public static boolean isBlockSelected(@Nonnull BlockPos coord) {
    return coord.equals(selectedCoord);
  }

  public static void addCandidate(@Nonnull BlockPos coord) {
    candidates.add(coord);
  }

  @SubscribeEvent
  public static void onRender(@Nonnull RenderWorldLastEvent event) {
    float fov = Minecraft.getMinecraft().gameSettings.fovSetting;
    fovRad = Math.toRadians(fov);
  }

  @SuppressWarnings({ "unused", "null" })
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public static void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      if (NullHelper.untrust(player) == null) {
        return;
      }
      if (player.isSpectator()) {
        showTargets = false;
        candidates.clear();
        return;
      }
      Pair<BlockPos, ITravelAccessable> pair = getActiveTravelBlock(player);
      onBlockCoord = pair != null ? pair.getLeft() : null;
      boolean onBlock = onBlockCoord != null;
      showTargets = onBlock || isTravelItemActiveForSelecting(player);
      if (showTargets) {
        updateSelectedTarget(player);
      } else {
        selectedCoord = null;
      }
      MovementInput input = player.movementInput;
      if (input == null) {
        return;
      }
      tempJump = input.jump;
      tempSneak = input.sneak;

      // Handles teleportation if a target is selected
      if ((input.jump && !wasJumping && onBlock && selectedCoord != null && TeleportConfig.activateJump.get())
          || (input.sneak && !wasSneaking && onBlock && selectedCoord != null && TeleportConfig.activateSneak.get())) {

        onInput(player);
      }

      // Elevator: If there is no selected coordinate and the input is jump, go up
      if (input.jump && !wasJumping && onBlock && selectedCoord == null) {
        updateVerticalTarget(player, 1);
        onInput(player);
      }

      // Elevator: If there is no selected coordinate and the input is sneak, go down
      if (input.sneak && !wasSneaking && onBlock && selectedCoord == null) {
        updateVerticalTarget(player, -1);
        onInput(player);
      }

      wasJumping = tempJump;
      wasSneaking = tempSneak;
      candidates.clear();
    }
  }

  private static int getEnergyInTravelItem(@Nonnull ItemStack equipped) {
    if (!(equipped.getItem() instanceof IItemOfTravel)) {
      return 0;
    }
    return ((IItemOfTravel) equipped.getItem()).getEnergyStored(equipped);
  }

  public static boolean isTravelItemActiveForRendering(@Nonnull EntityPlayer ep) {
    return isTravelItemActive(ep, ep.getHeldItemMainhand()) || (TeleportConfig.enableOffHandTravel.get() && isTravelItemActive(ep, ep.getHeldItemOffhand()));
  }

  private static boolean isTravelItemActiveForSelecting(@Nonnull EntityPlayer ep) {
    return isTravelItemActive(ep, ep.getHeldItemMainhand()) || isTravelItemActive(ep, ep.getHeldItemOffhand());
  }

  private static boolean isTravelItemActive(@Nonnull EntityPlayer ep, @Nonnull ItemStack equipped) {
    return equipped.getItem() instanceof IItemOfTravel && ((IItemOfTravel) equipped.getItem()).isActive(ep, equipped);
  }

  private static boolean travelToSelectedTarget(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull TravelSource source,
      boolean conserveMomentum) {
    final BlockPos selectedCoord_nullchecked = selectedCoord;
    if (selectedCoord_nullchecked == null) {
      return false;
    }
    return travelToLocation(player, equipped, hand, source, selectedCoord_nullchecked, conserveMomentum);
  }

  private static boolean travelToLocation(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull EnumHand hand, @Nonnull TravelSource source,
      @Nonnull BlockPos coord, boolean conserveMomentum) {

    if (source != TravelSource.STAFF_BLINK) {
      TileEntity te = player.world.getTileEntity(coord);
      if (te instanceof ITravelAccessable) {
        ITravelAccessable ta = (ITravelAccessable) te;
        if (!ta.canBlockBeAccessed(player)) {
          player.sendMessage(Lang.GUI_TRAVEL_UNAUTHORIZED.toChatServer());
          return false;
        }
      }
    }

    int requiredPower = 0;
    requiredPower = getRequiredPower(player, equipped, source, coord);
    if (requiredPower < 0) {
      return false;
    }

    if (!isInRangeTarget(player, coord, source.getMaxDistanceTravelledSq())) {
      if (source != TravelSource.STAFF_BLINK) {
        player.sendStatusMessage(Lang.GUI_TRAVEL_OUT_OF_RANGE.toChatServer(), true);
      }
      return false;
    }
    if (!isValidTarget(player, coord, source)) {
      if (source != TravelSource.STAFF_BLINK) {
        player.sendStatusMessage(Lang.GUI_TRAVEL_INVALID_TARGET.toChatServer(), true);
      }
      return false;
    }
    if (doClientTeleport(player, hand, coord, source, requiredPower, conserveMomentum) && PersonalConfig.machineParticlesEnabled.get()) {
      for (int i = 0; i < 6; ++i) {
        player.world.spawnParticle(EnumParticleTypes.PORTAL, player.posX + (rand.nextDouble() - 0.5D), player.posY + rand.nextDouble() * player.height - 0.25D,
            player.posZ + (rand.nextDouble() - 0.5D), (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
      }
    }
    return true;
  }

  private static int getRequiredPower(@Nonnull EntityPlayer player, @Nonnull ItemStack equipped, @Nonnull TravelSource source, @Nonnull BlockPos coord) {
    if (!isTravelItemActive(player, equipped)) {
      return 0;
    }
    int requiredPower;
    requiredPower = (int) (getDistance(player, coord) * source.getPowerCostPerBlockTraveledRF());
    int canUsePower = getEnergyInTravelItem(equipped);
    if (requiredPower > canUsePower) {
      player.sendStatusMessage(Lang.STAFF_NO_POWER.toChat(), true);
      return -1;
    }
    return requiredPower;
  }

  private static boolean isInRangeTarget(@Nonnull EntityPlayer player, @Nonnull BlockPos bc, float maxSq) {
    return getDistanceSquared(player, bc) <= maxSq;
  }

  private static double getDistanceSquared(@Nonnull EntityPlayer player, @Nonnull BlockPos bc) {
    Vector3d eye = Util.getEyePositionEio(player);
    Vector3d target = new Vector3d(bc.getX() + 0.5, bc.getY() + 0.5, bc.getZ() + 0.5);
    return eye.distanceSquared(target);
  }

  private static double getDistance(@Nonnull EntityPlayer player, @Nonnull BlockPos coord) {
    return Math.sqrt(getDistanceSquared(player, coord));
  }

  private static boolean isValidTarget(@Nonnull EntityPlayer player, @Nonnull BlockPos bc, @Nonnull TravelSource source) {
    World w = player.world;
    BlockPos baseLoc = bc;
    if (source != TravelSource.STAFF_BLINK) {
      // targeting a block so go one up
      baseLoc = bc.offset(EnumFacing.UP);
    }

    return canTeleportTo(player, source, baseLoc, w) && canTeleportTo(player, source, baseLoc.offset(EnumFacing.UP), w);
  }

  private static boolean canTeleportTo(@Nonnull EntityPlayer player, @Nonnull TravelSource source, @Nonnull BlockPos bc, @Nonnull World w) {
    if (bc.getY() < 1) {
      return false;
    }
    if (source == TravelSource.STAFF_BLINK && !TeleportConfig.enableBlinkSolidBlocks.get()) {
      Vec3d start = Util.getEyePosition(player);
      Vec3d target = new Vec3d(bc.getX() + 0.5f, bc.getY() + 0.5f, bc.getZ() + 0.5f);
      if (!canBlinkTo(bc, w, start, target)) {
        return false;
      }
    }

    IBlockState bs = w.getBlockState(bc);
    Block block = bs.getBlock();
    if (block.isAir(bs, w, bc)) {
      return true;
    }

    final AxisAlignedBB aabb = bs.getBoundingBox(w, bc);
    return aabb.getAverageEdgeLength() < 0.7;
  }

  private static boolean canBlinkTo(@Nonnull BlockPos bc, @Nonnull World w, @Nonnull Vec3d start, @Nonnull Vec3d target) {
    RayTraceResult p = w.rayTraceBlocks(start, target, !TeleportConfig.enableBlinkNonSolidBlocks.get());
    if (p != null) {
      if (!TeleportConfig.enableBlinkNonSolidBlocks.get()) {
        return false;
      }
      IBlockState bs = w.getBlockState(p.getBlockPos());
      Block block = bs.getBlock();
      if (isClear(w, bs, block, p.getBlockPos())) {
        if (BlockCoord.get(p).equals(bc)) {
          return true;
        }
        // need to step
        Vector3d sv = new Vector3d(start.x, start.y, start.z);
        Vector3d rayDir = new Vector3d(target.x, target.y, target.z);
        rayDir.sub(sv);
        rayDir.normalize();
        rayDir.add(sv);
        return canBlinkTo(bc, w, new Vec3d(rayDir.x, rayDir.y, rayDir.z), target);

      } else {
        return false;
      }
    }
    return true;
  }

  private static boolean isClear(@Nonnull World w, @Nonnull IBlockState bs, @Nonnull Block block, @Nonnull BlockPos bp) {
    if (block.isAir(bs, w, bp)) {
      return true;
    }
    final AxisAlignedBB aabb = bs.getBoundingBox(w, bp);
    if (aabb.getAverageEdgeLength() < 0.7) {
      return true;
    }

    return block.getLightOpacity(bs, w, bp) < 2;
  }

  @SideOnly(Side.CLIENT)
  private static void updateVerticalTarget(@Nonnull EntityPlayerSP player, int direction) {

    Pair<BlockPos, ITravelAccessable> pair = getActiveTravelBlock(player);
    BlockPos currentBlock = pair.getKey();
    World world = Minecraft.getMinecraft().world;
    for (int i = 0, y = currentBlock.getY() + direction; i < pair.getValue().getTravelRangeDeparting() && y >= 0 && y <= 255; i++, y += direction) {

      // Circumvents the raytracing used to find candidates on the y axis
      TileEntity selectedBlock = world.getTileEntity(new BlockPos(currentBlock.getX(), y, currentBlock.getZ()));

      if (selectedBlock instanceof ITravelAccessable) {
        ITravelAccessable travelBlock = (ITravelAccessable) selectedBlock;
        BlockPos targetBlock = new BlockPos(currentBlock.getX(), y, currentBlock.getZ());

        if (travelBlock.canBlockBeAccessed(player) && isValidTarget(player, targetBlock, TravelSource.BLOCK)) {
          selectedCoord = targetBlock;
          return;
        } else if (travelBlock.getRequiresPassword(player)) {
          player.sendStatusMessage(Lang.GUI_TRAVEL_SKIP_LOCKED.toChatServer(), true);
        } else if (travelBlock.getAccessMode() == ITravelAccessable.AccessMode.PRIVATE && !travelBlock.canUiBeAccessed(player)) {
          player.sendStatusMessage(Lang.GUI_TRAVEL_SKIP_PRIVATE.toChatServer(), true);
        } else if (!isValidTarget(player, targetBlock, TravelSource.BLOCK)) {
          player.sendStatusMessage(Lang.GUI_TRAVEL_SKIP_OBSTRUCTED.toChatServer(), true);
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  private static void updateSelectedTarget(@Nonnull EntityPlayerSP player) {
    selectedCoord = null;
    if (candidates.isEmpty()) {
      return;
    }

    double closestDistance = Double.MAX_VALUE;
    for (BlockPos bc : candidates) {
      // Exclude the block the player is standing on
      if (!bc.equals(onBlockCoord)) {
        Vector3d loc = new Vector3d(bc.getX() + 0.5, bc.getY() + 0.5, bc.getZ() + 0.5);
        double[] distanceAndAngle = getCandidateDistanceAndAngle(loc);
        double distance = distanceAndAngle[0];
        double angle = distanceAndAngle[1];

        if (distance < closestDistance && angle < getCandidateHitAngle()) {
          // Valid hit, sorted by distance.
          selectedCoord = bc;
          closestDistance = distance;
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  private static void onInput(@Nonnull EntityPlayerSP player) {

    MovementInput input = player.movementInput;
    BlockPos target = selectedCoord;
    if (target == null) {
      return;
    }

    TileEntity te = player.world.getTileEntity(target);
    if (te instanceof ITravelAccessable) {
      ITravelAccessable ta = (ITravelAccessable) te;
      if (ta.getRequiresPassword(player)) {
        PacketOpenAuthGui p = new PacketOpenAuthGui(target);
        PacketHandler.INSTANCE.sendToServer(p);
        return;
      }
    }

    if (travelToSelectedTarget(player, Prep.getEmpty(), EnumHand.MAIN_HAND, TravelSource.BLOCK, false)) {
      input.jump = false;
      try {
        ObfuscationReflectionHelper.setPrivateValue(EntityPlayer.class, (EntityPlayer) player, 0, "flyToggleTimer", "field_71101_bC");
      } catch (Exception e) {
        // ignore
      }
    }

  }

  public static double getCandidateHitScale(double fullScreenScaling, double distance) {
    // Take 10% of the screen width per default as the maximum scale for hits (perfectly looking at block)
    return 0.10 * fullScreenScaling;
  }

  public static double getCandidateMissScale(double fullScreenScaling, double distance) {
    // At least 1.5 times the normal block size if the angle is not close to the block
    return 1.5;
  }

  public static double getCandidateHitAngle() {
    // CAREFUL: missAngle MUST BE >= hitAngle
    // Hit scale for blocks with an angle below this threshold
    return 0.087; // == ~5 degree
  }

  public static double getCandidateMissAngle() {
    // CAREFUL: missAngle MUST BE >= hitAngle
    // Miss scale for blocks with an angle above this threshold
    return Math.PI / 5; // == 36 degrees
  }

  public static double[] getCandidateDistanceAndAngle(@Nonnull Vector3d loc) {
    Vector3d eye = Util.getEyePositionEio(Minecraft.getMinecraft().player);
    Vector3d look = Util.getLookVecEio(Minecraft.getMinecraft().player);
    Vector3d relativeBlock = new Vector3d(loc);

    relativeBlock.sub(eye);
    double distance = relativeBlock.length();
    relativeBlock.normalize();

    // Angle in [0,pi]
    double angle = Math.acos(look.dot(relativeBlock));
    return new double[] { distance, angle };
  }

  public static double getScaleForCandidate(@Nonnull Vector3d loc, int maxDistanceSq) {
    // Retrieve the candidate distance and angle
    double[] distanceAndAngle = getCandidateDistanceAndAngle(loc);
    double distance = distanceAndAngle[0];
    double angle = distanceAndAngle[1];

    // To get screen relative scaling, normalize based on fov and
    // distance (this scaling factor would cause the block to be displayed
    // horizontally fitted to the screen)
    double fullScreenScaling = Math.tan(fovRad / 2) * 2 * distance;

    double scaleHit = getCandidateHitScale(fullScreenScaling, distance);
    double scaleMiss = getCandidateMissScale(fullScreenScaling, distance);

    double hitAngle = getCandidateHitAngle();
    double missAngle = getCandidateMissAngle();

    // Always apply configuration scaling factor
    // FIXME: The (1/.2) is there because currently .2 is the default value for this config
    // and this new algorithm needs 1 to be the base value. Maybe it is best to change
    // the default config value and remove this factor. The then the config represents
    // intuitive scaling (1.0 = 100%)
    double scale = (1 / .2) * TeleportConfig.visualScale.get();

    // Now we will scale according to the angle:
    // scaleHit for [0,hitAngle)
    // interpolate(scaleHit, scaleMiss) for [hitAngle,missAngle)
    // scaleMiss for [missAngle,pi]
    if (angle < hitAngle) {
      scale *= scaleHit;
    } else if (angle >= hitAngle && angle < missAngle) {
      double lerp = (angle - hitAngle) / (missAngle - hitAngle);
      scale *= scaleHit + lerp * (scaleMiss - scaleHit);
    } else {
      scale *= scaleMiss;
    }

    return scale;
  }

  // Note: This is restricted to the current player
  public static boolean doClientTeleport(@Nonnull Entity entity, @Nonnull EnumHand hand, @Nonnull BlockPos bc, @Nonnull TravelSource source, int powerUse,
      boolean conserveMomentum) {

    TeleportEntityEvent evt = new TeleportEntityEvent(entity, source, bc, entity.dimension);
    if (MinecraftForge.EVENT_BUS.post(evt)) {
      return false;
    }

    PacketTravelEvent p = new PacketTravelEvent(evt.getTarget(), powerUse, conserveMomentum, source, hand);
    PacketHandler.INSTANCE.sendToServer(p);
    return true;
  }

  @SideOnly(Side.CLIENT)
  private static Pair<BlockPos, ITravelAccessable> getActiveTravelBlock(@Nonnull EntityPlayerSP player) {
    World world = Minecraft.getMinecraft().world;
    if (NullHelper.untrust(world) == null) {
      // Log.warn("(in TickEvent.ClientTickEvent) net.minecraft.client.Minecraft.world is marked @Nonnull but it is null.");
      return null;
    }
    int x = MathHelper.floor(player.posX);
    int y = MathHelper.floor(player.getEntityBoundingBox().minY) - 1;
    int z = MathHelper.floor(player.posZ);
    final BlockPos pos = new BlockPos(x, y, z);
    TileEntity tileEntity = world.getTileEntity(pos);
    if (tileEntity instanceof ITravelAccessable) {
      if (((ITravelAccessable) tileEntity).isTravelSource()) {
        return Pair.of(new BlockPos(x, y, z), ((ITravelAccessable) tileEntity));
      }
    }
    return null;
  }

  public static BlockPos getPosPlayerOn() {
    return onBlockCoord;
  }

}
