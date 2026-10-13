package dev.voidstone.dmzvoidstone.mixin;

import com.dragonminez.common.network.C2S.UpdateSkillC2S;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = UpdateSkillC2S.class, remap = false)
public interface UpdateSkillC2SAccessor {
    @Accessor(value = "skillName", remap = false)
    String dmz$getSkillName();

    @Accessor(value = "action", remap = false)
    UpdateSkillC2S.SkillAction dmz$getAction();
}
