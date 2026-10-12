package dev.voidstone.dmzvoidstone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.dragonminez.common.network.C2S.CreateCharacterC2S;

@Mixin(value = CreateCharacterC2S.class, remap = false)
public interface CreateCharacterC2SAccessor {
    @Accessor(value = "raceName", remap = false)
    String dmz$getRaceName();

    @Accessor(value = "className", remap = false)
    String dmz$getClassName();
}
