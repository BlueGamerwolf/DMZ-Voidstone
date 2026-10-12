package dev.voidstone.dmzvoidstone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.dragonminez.common.network.C2S.UpdateCharacterC2S;

@Mixin(value = UpdateCharacterC2S.class, remap = false)
public interface UpdateCharacterC2SAccessor {
    @Accessor(value = "className", remap = false)
    String dmz$getClassName();
}
