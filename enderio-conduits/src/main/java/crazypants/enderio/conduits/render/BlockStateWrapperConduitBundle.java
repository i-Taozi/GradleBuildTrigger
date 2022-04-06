package crazypants.enderio.conduits.render;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.render.IRenderMapper.IBlockRenderMapper;
import crazypants.enderio.base.render.pipeline.BlockStateWrapperBase;
import crazypants.enderio.base.render.util.QuadCollector;
import crazypants.enderio.conduits.EnderIOConduits;
import crazypants.enderio.conduits.conduit.IConduitComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = EnderIOConduits.MODID, value = Side.CLIENT)
public class BlockStateWrapperConduitBundle extends BlockStateWrapperBase {

  private final static Cache<ConduitCacheKey, QuadCollector> cache = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(10, TimeUnit.MINUTES)
      .<ConduitCacheKey, QuadCollector> build();

  @Override
  public String toString() {
    String r = "BlockStateWrapperConduitBundle@" + Integer.toHexString(hashCode()) + " [cache=";

    for (Entry<ConduitCacheKey, QuadCollector> e : cache.asMap().entrySet()) {
      r += "(" + e.getKey() + "=" + e.getValue().toString() + ") ";
    }

    return r + "]";
  }

  public BlockStateWrapperConduitBundle(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, IBlockRenderMapper renderMapper) {
    super(state, world, pos, renderMapper);
  }

  public BlockStateWrapperConduitBundle(@Nonnull BlockStateWrapperBase parent, @Nonnull IBlockState state) {
    super(parent, state);
  }

  private final @Nonnull ConduitCacheKey cachekey = new ConduitCacheKey();

  public ConduitCacheKey getCachekey() {
    return cachekey;
  }

  @Override
  protected void putIntoCache(@Nonnull QuadCollector quads) {
    cache.put(cachekey, quads);
  }

  @Override
  protected QuadCollector getFromCache() {
    return cache.getIfPresent(cachekey);
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public static void invalidate(@Nonnull ModelBakeEvent event) {
    cache.invalidateAll();
  }

  @Override
  protected void addCacheKeyInternal(@Nonnull Object addlCacheKey) {
    super.addCacheKeyInternal(addlCacheKey);
    if (addlCacheKey instanceof IConduitComponent.IConduitComponentProvider) {
      ((IConduitComponent.IConduitComponentProvider) addlCacheKey).hashCodeForModelCaching(this, cachekey);
    } else if (addlCacheKey instanceof IConduitComponent) {
      ((IConduitComponent) addlCacheKey).hashCodeForModelCaching(cachekey);
    } else if (addlCacheKey instanceof IBlockState) {
      cachekey.add(Block.BLOCK_STATE_IDS.get((IBlockState) addlCacheKey));
    } else {
      cachekey.add(addlCacheKey);
    }
  }

  @Override
  protected void resetCacheKeyInternal() {
    super.resetCacheKeyInternal();
    cachekey.reset();
  }

  public static class ConduitCacheKey {
    private int idx = 0, hashCode = 1;
    private int[] hashCodes = new int[16];

    public void reset() {
      idx = 0;
      hashCode = 1;
    }

    public void add(Object o) {
      add(o.hashCode());
    }

    public void add(int i) {
      assert hashCodes != null;
      if (idx == hashCodes.length) {
        hashCodes = Arrays.copyOf(hashCodes, hashCodes.length * 2);
      }
      hashCodes[idx++] = i;
      hashCode = 31 * hashCode + i;
    }

    public void addBoolean(Map<EnumFacing, Boolean> o) {
      assert EnumFacing.values().length <= 1 + 2 + 4;
      int i = 0;
      for (EnumFacing face : EnumFacing.values()) {
        Boolean b = o.get(face);
        i = (i << 1) | (b != null && b.booleanValue() ? 1 : 0);
      }
      add(i);
    }

    public <T extends Enum<?>> void addEnum(Map<EnumFacing, T> o) {
      int i = 0;
      for (EnumFacing face : EnumFacing.values()) {
        final T value = o.get(face);
        assert value == null || value.ordinal() < 1 + 2 + 4 + 8 + 16 : value.getClass();
        i = (i << 5) | (value == null ? 1 + 2 + 4 + 8 + 16 : value.ordinal());
      }
      add(i);
    }

    public void add(Set<EnumFacing> o1, Set<EnumFacing> o2, Map<EnumFacing, ConnectionMode> o3) {
      assert EnumFacing.values().length <= 1 + 2 + 4;
      int i = 0;
      for (EnumFacing face : EnumFacing.values()) {
        i = (i << 1) | (o1.contains(face) ? 1 : 0);
        i = (i << 1) | (o2.contains(face) ? 1 : 0);
        i = (i << 3) | (o3.containsKey(face) ? o3.get(face).ordinal() : 1 + 2 + 4);
      }
      add(i);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ConduitCacheKey && ((ConduitCacheKey) obj).idx == idx) {
        assert hashCodes != null;
        assert ((ConduitCacheKey) obj).hashCodes != null;
        for (int i = 0; i < idx; i++) {
          if (hashCodes[i] != ((ConduitCacheKey) obj).hashCodes[i]) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    public String toString() {
      return "ConduitCacheKey [idx=" + idx + ", hashCode=" + hashCode + ", hashCodes=" + Arrays.toString(hashCodes) + "]";
    }

  }
}
