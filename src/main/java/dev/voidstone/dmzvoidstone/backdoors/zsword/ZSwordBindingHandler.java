package dev.voidstone.dmzvoidstone.backdoors.zsword;

import dev.voidstone.dmzvoidstone.DMZVoidstoneMOD;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DMZVoidstoneMOD.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ZSwordBindingHandler {

    private static final String OWNER_TAG = "ZSwordOwner";
    private static final String OWNER_GLOW_TAG = "ZSwordOwnerGlow";
    private static final String Z_SWORD_ID = "dragonminez:z_sword";

    private ZSwordBindingHandler() {
    }

    private static boolean isZSword(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && Z_SWORD_ID.equals(id.toString());
    }

    private static UUID getOwner(ItemStack stack) {
        var tag = stack.getTag();

        if (tag == null || !tag.hasUUID(OWNER_TAG)) {
            return null;
        }

        return tag.getUUID(OWNER_TAG);
    }

    private static void bind(ItemStack stack, ServerPlayer player) {
        stack.getOrCreateTag().putUUID(OWNER_TAG, player.getUUID());

        player.sendSystemMessage(
                Component.literal("The Z Sword is now bound to you!")
                        .withStyle(ChatFormatting.GOLD));
    }

    /**
     * Updates the synchronized ownership marker.
     *
     * Note: this marker alone does not render an enchanted glint.
     * A rendering change is required for the visual effect.
     */
    private static void updateOwnerGlow(
            ItemStack stack,
            ServerPlayer holder) {
        UUID ownerUUID = getOwner(stack);

        boolean isOwner = ownerUUID != null
                && ownerUUID.equals(holder.getUUID());

        stack.getOrCreateTag().putBoolean(OWNER_GLOW_TAG, isOwner);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer holder)) {
            return;
        }

        MinecraftServer server = holder.getServer();

        if (server == null) {
            return;
        }

        for (int slot = 0; slot < holder.getInventory().getContainerSize(); slot++) {

            ItemStack stack = holder.getInventory().getItem(slot);

            if (!isZSword(stack)) {
                continue;
            }

            UUID ownerUUID = getOwner(stack);

            // The first holder claims an unbound Z Sword.
            if (ownerUUID == null) {
                bind(stack, holder);
                updateOwnerGlow(stack, holder);
                continue;
            }

            // The rightful owner may keep the sword.
            if (ownerUUID.equals(holder.getUUID())) {
                updateOwnerGlow(stack, holder);
                continue;
            }

            // Unauthorized holder: disable the ownership marker.
            stack.getOrCreateTag().putBoolean(OWNER_GLOW_TAG, false);

            ItemStack returningSword = stack.copy();
            holder.getInventory().setItem(slot, ItemStack.EMPTY);

            ServerPlayer owner = server.getPlayerList().getPlayer(ownerUUID);

            if (owner == null) {
                ZSwordReturnData.get(server)
                        .queueReturn(ownerUUID, returningSword);

                holder.sendSystemMessage(
                        Component.literal(
                                "This Z Sword belongs to another player!").withStyle(ChatFormatting.RED));

                continue;
            }

            if (!deliverSword(owner, returningSword)) {
                ZSwordReturnData.get(server)
                        .queueReturn(ownerUUID, returningSword);
            }

            holder.sendSystemMessage(
                    Component.literal(
                            "This Z Sword belongs to another player!").withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Delivers a sword to an online owner.
     */
    private static boolean deliverSword(
            ServerPlayer player,
            ItemStack stack) {
        ItemStack delivery = stack.copy();

        // Mark it for the rightful owner's visual effect.
        delivery.getOrCreateTag().putBoolean(OWNER_GLOW_TAG, true);

        player.getInventory().add(delivery);

        if (!delivery.isEmpty()) {
            ItemEntity dropped = player.drop(delivery, false);

            if (dropped == null) {
                return false;
            }

            dropped.setPickUpDelay(0);
        }

        player.sendSystemMessage(
                Component.literal("Your Z Sword has returned to you!")
                        .withStyle(ChatFormatting.GREEN));

        return true;
    }

    /**
     * Delivers pending returns when the owner logs in.
     */
    @SubscribeEvent
    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        ZSwordReturnData data = ZSwordReturnData.get(server);
        UUID ownerUUID = player.getUUID();

        List<ItemStack> pending = data.getPendingReturns(ownerUUID);

        for (ItemStack stack : pending) {
            if (!deliverSword(player, stack)) {
                break;
            }

            data.removeFirstPendingReturn(ownerUUID);
        }
    }
}