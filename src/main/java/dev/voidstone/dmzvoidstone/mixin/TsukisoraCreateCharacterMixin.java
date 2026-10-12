package dev.voidstone.dmzvoidstone.mixin;

import com.dragonminez.common.network.C2S.CreateCharacterC2S;
import dev.voidstone.dmzvoidstone.backdoors.tsukisora.TsukisoraAccess;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = CreateCharacterC2S.class, remap = false)
public abstract class TsukisoraCreateCharacterMixin {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dmz$restrictTsukisora(
            CreateCharacterC2S message,
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

        CreateCharacterC2SAccessor access = (CreateCharacterC2SAccessor) message;
        String requestedRace = access.dmz$getRaceName();
        String requestedClass = access.dmz$getClassName();

        boolean requestsTsukisora = TsukisoraAccess.isTsukisoraRace(requestedRace);
        boolean requestsVoid = requestedClass != null
                && "void".equalsIgnoreCase(requestedClass.trim());

        // Tsukisora and the Void class are both owner-only.
        // Void may only be created as part of the Tsukisora race.
        boolean denied = (requestsTsukisora || requestsVoid)
                && !TsukisoraAccess.isOwner(player.getUUID());
        denied |= requestsVoid && !requestsTsukisora;

        if (!denied) {
            return;
        }

        player.sendSystemMessage(Component.literal(
                "Tsukisora and the Void class are restricted to their authorized owner."));
        context.setPacketHandled(true);
        callback.cancel();
    }
}
