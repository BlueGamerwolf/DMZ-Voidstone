package dev.voidstone.dmzvoidstone.mixin;

import com.dragonminez.common.network.C2S.UpdateCharacterC2S;
import dev.voidstone.dmzvoidstone.backdoors.tsukisora.TsukisoraAccess;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Enforces the owner-only Void class when a character's class is changed.
 * Character creation has a separate server-side guard.
 */
@Mixin(value = UpdateCharacterC2S.class, remap = false)
public abstract class TsukisoraClassSelectionMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dmz$restrictVoidClass(
            UpdateCharacterC2S message,
            Supplier<NetworkEvent.Context> contextSupplier,
            CallbackInfo callback) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context == null) {
            return;
        }

        var player = context.getSender();
        if (player == null) {
            return;
        }

        String requestedClass = ((UpdateCharacterC2SAccessor) message).dmz$getClassName();
        if (requestedClass == null
                || !"void".equalsIgnoreCase(requestedClass.trim())
                || TsukisoraAccess.isOwner(player.getUUID())) {
            return;
        }

        player.sendSystemMessage(Component.literal(
                "The Void class is restricted to its authorized owner."));
        context.setPacketHandled(true);
        callback.cancel();
    }
}
