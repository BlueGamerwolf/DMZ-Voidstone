package dev.voidstone.dmzvoidstone.mixin;

import com.dragonminez.client.gui.character.QuestTreeScreen;
import com.dragonminez.common.quest.Saga;
import com.dragonminez.common.stats.StatsData;
import dev.voidstone.dmzvoidstone.backdoors.tsukisora.TsukisoraAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Keep the Voidstone story out of the quest tree for characters other than Tsukisora.
 * The server-side quest requirements remain the authoritative access control.
 */
@Mixin(value = QuestTreeScreen.class, remap = false)
public abstract class TsukisoraSagaVisibilityMixin {
    @Shadow
    private StatsData statsData;

    @Shadow
    @Final
    private List<Saga> availableSagas;

    @Inject(method = "loadAvailableSagas", at = @At("RETURN"), remap = false)
    private void dmz$hideVoidstoneSagasFromOtherRaces(CallbackInfo callback) {
        boolean isTsukisora = statsData != null
                && statsData.getCharacter() != null
                && TsukisoraAccess.isTsukisoraRace(statsData.getCharacter().getRaceName());

        if (!isTsukisora) {
            availableSagas.removeIf(saga ->
                    "void_training".equals(saga.getId())
                            || "voidstone_ascension".equals(saga.getId()));
        }
    }
}
