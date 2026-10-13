package dev.voidstone.dmzvoidstone.mixin;

import com.dragonminez.common.network.C2S.UpdateSkillC2S;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import dev.voidstone.dmzvoidstone.backdoors.tsukisora.TsukisoraAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Ki Control is granted to Tsukisora by the final Void Training quest.
 * Block the normal shop/skill-menu purchase routes on the server.
 */
@Mixin(value = UpdateSkillC2S.class, remap = false)
public abstract class TsukisoraKiControlLockMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private void dmz$requireVoidTraining(Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo callback) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context == null) return;
        ServerPlayer player = context.getSender();
        if (player == null) return;

        UpdateSkillC2SAccessor access = (UpdateSkillC2SAccessor) this;
        String skillName = access.dmz$getSkillName();
        UpdateSkillC2S.SkillAction action = access.dmz$getAction();
        if (skillName == null || !"kicontrol".equalsIgnoreCase(skillName.trim())) return;
        if (action != UpdateSkillC2S.SkillAction.PURCHASE
                && action != UpdateSkillC2S.SkillAction.UPGRADE) return;

        boolean isTsukisora = StatsProvider.get(StatsCapability.INSTANCE, player)
                .map(data -> data.getCharacter() != null
                        && TsukisoraAccess.isTsukisoraRace(data.getCharacter().getRaceName()))
                .orElse(false);
        if (!isTsukisora) return;

        player.sendSystemMessage(Component.literal(
                "Tsukisora must complete the Void Training saga before receiving Ki Control."));
        context.setPacketHandled(true);
        callback.cancel();
    }
}
