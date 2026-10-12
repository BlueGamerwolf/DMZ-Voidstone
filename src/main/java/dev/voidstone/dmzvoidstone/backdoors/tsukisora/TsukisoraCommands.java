package dev.voidstone.dmzvoidstone.backdoors.tsukisora;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server commands for inspecting and advancing Tsukisora's persisted
 * manifestation progression. Access is checked against the authenticated
 * server player UUID, never a client-supplied name.
 */
@Mod.EventBusSubscriber(modid = "dmz_voidstone", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TsukisoraCommands {
    private TsukisoraCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tsukisora")
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException();
                                    if (!TsukisoraAccess.isOwner(player.getUUID())) {
                                        context.getSource().sendFailure(Component.literal(
                                                "You are not authorized to access Tsukisora progression."));
                                        return 0;
                                    }

                                    TsukisoraForm form = TsukisoraProgressionData
                                            .get(player.serverLevel()).getForm(player.getUUID());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Current manifestation: "
                                                    + form.getDisplayName() + " (stage "
                                                    + (form.getStage() + 1) + "/"
                                                    + TsukisoraForm.values().length + ")"),
                                            false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("advance")
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException();
                                    if (!TsukisoraAccess.isOwner(player.getUUID())) {
                                        context.getSource().sendFailure(Component.literal(
                                                "You are not authorized to advance Tsukisora progression."));
                                        return 0;
                                    }

                                    TsukisoraProgressionData data = TsukisoraProgressionData
                                            .get(player.serverLevel());
                                    TsukisoraForm current = data.getForm(player.getUUID());
                                    if (!current.hasNext()) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "You have already reached the final manifestation: "
                                                                + current.getDisplayName() + "."),
                                                false);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    TsukisoraForm next = data.advance(player.getUUID());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Manifestation unlocked: "
                                                    + next.getDisplayName()),
                                            false);
                                    return Command.SINGLE_SUCCESS;
                                })));
    }
}
