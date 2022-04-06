package crazypants.enderio.machines.machine.obelisk.base;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import crazypants.enderio.machines.config.config.InhibitorConfig;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpawningObeliskController {

  public static SpawningObeliskController instance = new SpawningObeliskController();

  static {
    MinecraftForge.EVENT_BUS.register(SpawningObeliskController.instance);
  }

  private final @Nonnull Map<Integer, Map<BlockPos, ISpawnCallback>> perWorldGuards = new HashMap<Integer, Map<BlockPos, ISpawnCallback>>();

  private SpawningObeliskController() {
  }

  public <T extends TileEntity & ISpawnCallback> void registerGuard(T guard) {
    if (guard == null) {
      return;
    }
    Map<BlockPos, ISpawnCallback> chargers = getGuardsForWorld(guard.getWorld());
    chargers.put(guard.getPos(), guard);
  }

  public <T extends TileEntity & ISpawnCallback> void deregisterGuard(T guard) {
    if (guard == null) {
      return;
    }
    Map<BlockPos, ISpawnCallback> chargers = getGuardsForWorld(guard.getWorld());
    chargers.remove(guard.getPos());
  }

  // Note: This is called even if spawning mechanics (e.g. light levels) will prevent the entity from being spawned later
  @SubscribeEvent
  public void onEntitySpawn(LivingSpawnEvent.CheckSpawn evt) {
    if (evt.getResult() == Result.DENY || evt.isSpawner()) {
      return;
    }

    if (InhibitorConfig.stopAllSlimes.get() && evt.getEntity() instanceof EntitySlime) {
      evt.setResult(Result.DENY);
      return;
    }
    if (InhibitorConfig.stopAllSquid.get() && evt.getEntity().getClass() == EntitySquid.class) {
      evt.setResult(Result.DENY);
      return;
    }

    Map<BlockPos, ISpawnCallback> guards = getGuardsForWorld(evt.getWorld());
    for (ISpawnCallback guard : guards.values()) {
      ISpawnCallback.Result result = guard.isSpawnPrevented(evt.getEntityLiving());
      if (result == ISpawnCallback.Result.DENY) {
        evt.setResult(Result.DENY);
        return;
      } else if (result == ISpawnCallback.Result.DONE) {
        return;
      }
    }
  }

  private @Nonnull Map<BlockPos, ISpawnCallback> getGuardsForWorld(World world) {
    Map<BlockPos, ISpawnCallback> res = perWorldGuards.get(world.provider.getDimension());
    if (res == null) {
      res = new HashMap<BlockPos, ISpawnCallback>();
      perWorldGuards.put(world.provider.getDimension(), res);
    }
    return res;
  }

}
