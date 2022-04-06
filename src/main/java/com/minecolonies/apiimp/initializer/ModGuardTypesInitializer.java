package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.jobs.JobKnight;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModGuardTypesInitializer
{

    private ModGuardTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypesInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<GuardType> event)
    {
        final IForgeRegistry<GuardType> reg = event.getRegistry();

        ModGuardTypes.knight = new GuardType.Builder()
                                 .setJobTranslationKey("com.minecolonies.coremod.job.knight")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.knight")
                                 .setPrimarySkill(Skill.Adaptability)
                                 .setSecondarySkill(Skill.Stamina)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.knight)
                                 .setRegistryName(ModGuardTypes.KNIGHT_ID)
                                 .setClazz(JobKnight.class)
                                 .createGuardType();

        ModGuardTypes.ranger = new GuardType.Builder()
                                 .setJobTranslationKey("com.minecolonies.coremod.job.ranger")
                                 .setButtonTranslationKey("com.minecolonies.coremod.gui.workerhuts.ranger")
                                 .setPrimarySkill(Skill.Agility)
                                 .setSecondarySkill(Skill.Adaptability)
                                 .setWorkerSoundName("archer")
                                 .setJobEntry(() -> ModJobs.ranger)
                                 .setRegistryName(ModGuardTypes.RANGER_ID)
                                 .setClazz(JobRanger.class)
                                 .createGuardType();

        reg.register(ModGuardTypes.knight);
        reg.register(ModGuardTypes.ranger);
    }
}
