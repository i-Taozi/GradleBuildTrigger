package crazypants.enderio.base.block.painted;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Predicate;

import crazypants.enderio.util.CapturedMob;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum EnumPressurePlateType implements IStringSerializable {

  WOOD(true, Entity.class) {
    @Override
    public int getFlammability() {
      return 20;
    }

    @Override
    public int getFireSpreadSpeed() {
      return 5;
    }

    @Override
    public void playClickOnSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
    }

    @Override
    public void playClickOffSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_WOOD_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
    }
  },
  STONE(true, EntityLivingBase.class) {
    @Override
    public void playClickOnSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    @Override
    public void playClickOffSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
      worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
  },
  IRON(true, CountingMode.ENTITIES, Entity.class),
  GOLD(true, CountingMode.ITEMS, EntityItem.class),
  DARKSTEEL(false, EntityPlayer.class),
  @SuppressWarnings("unchecked")
  SOULARIUM(false, EntityLiving.class, EntitySlime.class, EntityGhast.class, EntityMob.class, EntitySkeletonHorse.class, EntityZombieHorse.class,
      EntityShulker.class),
  TUNED(false, EntityLivingBase.class) {
    @Override
    public @Nonnull Predicate<Entity> getPredicate(final @Nullable CapturedMob capturedMob) {
      return new Predicate<Entity>() {
        @Override
        public boolean apply(@Nullable Entity entity) {
          if (capturedMob == null || entity == null || !entity.isEntityAlive() || entity.doesEntityNotTriggerPressurePlate()
              || ((entity instanceof EntityPlayer) && ((EntityPlayer) entity).isSpectator())) {
            return false;
          }
          return capturedMob.isSameType(entity);
        }

        @Override
        public int hashCode() {
          return super.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
          return super.equals(obj);
        }
      };
    }

    @Override
    public boolean hasEffect() {
      return true;
    }
  };

  public enum CountingMode {
    BINARY {
      @Override
      public int count(List<Entity> list) {
        return list.isEmpty() ? 0 : 15;
      }
    },
    ENTITIES {
      @Override
      public int count(List<Entity> list) {
        return Math.min(15, list.size());
      }
    },
    ITEMS {
      @Override
      public int count(List<Entity> list) {
        int result = 0;
        for (Entity entity : list) {
          if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getItem();
            result += stack.getCount();
            if (result >= 15) {
              return 15;
            }
          }
        }
        return Math.min(15, result);
      }
    };

    public abstract int count(List<Entity> list);
  }

  private final boolean shadowsVanilla;
  private final @Nonnull CountingMode countingMode;
  private final @Nonnull Class<? extends Entity> searchClass;
  private final @Nonnull List<Class<? extends Entity>> whiteClasses;

  private EnumPressurePlateType(boolean shadowsVanilla, @Nonnull Class<? extends Entity> searchClass) {
    this.shadowsVanilla = shadowsVanilla;
    this.countingMode = CountingMode.BINARY;
    this.searchClass = searchClass;
    this.whiteClasses = Collections.emptyList();
  }

  private EnumPressurePlateType(boolean shadowsVanilla, @Nonnull Class<? extends Entity> searchClass,
      @SuppressWarnings("unchecked") Class<? extends Entity>... whiteClasses) {
    this.shadowsVanilla = shadowsVanilla;
    this.countingMode = CountingMode.BINARY;
    this.searchClass = searchClass;
    this.whiteClasses = Arrays.<Class<? extends Entity>> asList(whiteClasses);
  }

  private EnumPressurePlateType(boolean shadowsVanilla, @Nonnull CountingMode countingMode, @Nonnull Class<? extends Entity> searchClass) {
    this.shadowsVanilla = shadowsVanilla;
    this.countingMode = countingMode;
    this.searchClass = searchClass;
    this.whiteClasses = Collections.<Class<? extends Entity>> emptyList();
  }

  private EnumPressurePlateType(boolean shadowsVanilla, @Nonnull CountingMode countingMode, @Nonnull Class<? extends Entity> searchClass,
      @SuppressWarnings("unchecked") Class<? extends Entity>... whiteClasses) {
    this.shadowsVanilla = shadowsVanilla;
    this.countingMode = countingMode;
    this.searchClass = searchClass;
    this.whiteClasses = Arrays.<Class<? extends Entity>> asList(whiteClasses);
  }

  @Override
  public @Nonnull String getName() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  public @Nonnull String getUnlocName(@Nonnull Item me) {
    return me.getUnlocalizedName() + "." + getName();
  }

  public static @Nonnull EnumPressurePlateType getTypeFromMeta(int meta) {
    int meta1 = meta >> 1;
    return NullHelper.notnullJ(values()[meta1 >= 0 && meta1 < values().length ? meta1 : 0], "Enum.values()");
  }

  public static boolean getSilentFromMeta(int meta) {
    return (meta & 1) != 0;
  }

  public static int getMetaFromType(EnumPressurePlateType value, boolean isSilent) {
    return (value.ordinal() << 1) | (isSilent ? 1 : 0);
  }

  public int getMetaFromType(boolean isSilent) {
    return getMetaFromType(this, isSilent);
  }

  public int getMetaFromType() {
    return getMetaFromType(this, false);
  }

  protected @Nonnull CountingMode getCountingMode() {
    return countingMode;
  }

  public @Nonnull Class<? extends Entity> getSearchClass() {
    return searchClass;
  }

  public @Nonnull Predicate<Entity> getPredicate(@Nullable final CapturedMob capturedMob) {
    return new Predicate<Entity>() {
      @Override
      public boolean apply(@Nullable Entity entity) {
        if (entity == null) {
          return false;
        }
        if (!entity.isEntityAlive() || entity.doesEntityNotTriggerPressurePlate() || ((entity instanceof EntityPlayer) && ((EntityPlayer) entity).isSpectator())
            || (capturedMob != null && !capturedMob.isSameType(entity))) {
          return false;
        }
        if (searchClass.isInstance(entity) && whiteClasses.isEmpty()) {
          return true;
        }
        for (Class<? extends Entity> clazz : whiteClasses) {
          if (clazz.isInstance(entity)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
      }
    };
  }

  public boolean isShadowsVanilla() {
    return shadowsVanilla;
  }

  public int getFlammability() {
    return 0;
  }

  public int getFireSpreadSpeed() {
    return 0;
  }

  public boolean hasEffect() {
    return false;
  }

  public void playClickOnSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
    worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.9F);
  }

  public void playClickOffSound(@Nonnull World worldIn, @Nonnull BlockPos pos) {
    worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F);
  }

}
