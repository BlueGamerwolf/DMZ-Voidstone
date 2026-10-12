
package dev.voidstone.dmzvoidstone.backdoors.zsword;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ZSwordReturnData extends SavedData {

    private static final String DATA_NAME = "dmzvoidstone_zsword_returns";

    private static final String OWNERS_TAG = "Owners";
    private static final String OWNER_TAG = "Owner";
    private static final String ITEMS_TAG = "Items";

    private final Map<UUID, List<ItemStack>> pendingReturns = new HashMap<>();

    public ZSwordReturnData() {
    }

    public static ZSwordReturnData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        ZSwordReturnData::load,
                        ZSwordReturnData::new,
                        DATA_NAME);
    }

    public void queueReturn(UUID ownerUUID, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        pendingReturns
                .computeIfAbsent(
                        ownerUUID,
                        ignored -> new ArrayList<>())
                .add(stack.copy());

        setDirty();
    }

    public List<ItemStack> getPendingReturns(UUID ownerUUID) {
        List<ItemStack> items = pendingReturns.get(ownerUUID);

        if (items == null) {
            return new ArrayList<>();
        }

        List<ItemStack> copies = new ArrayList<>(items.size());

        for (ItemStack stack : items) {
            copies.add(stack.copy());
        }

        return copies;
    }

    /**
     * Removes one queued sword after successful delivery.
     */
    public void removeFirstPendingReturn(UUID ownerUUID) {
        List<ItemStack> items = pendingReturns.get(ownerUUID);

        if (items == null || items.isEmpty()) {
            return;
        }

        items.remove(0);

        if (items.isEmpty()) {
            pendingReturns.remove(ownerUUID);
        }

        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag owners = new ListTag();

        for (Map.Entry<UUID, List<ItemStack>> entry : pendingReturns.entrySet()) {

            CompoundTag ownerData = new CompoundTag();
            ownerData.putUUID(OWNER_TAG, entry.getKey());

            ListTag items = new ListTag();

            for (ItemStack stack : entry.getValue()) {
                if (!stack.isEmpty()) {
                    items.add(stack.save(new CompoundTag()));
                }
            }

            ownerData.put(ITEMS_TAG, items);
            owners.add(ownerData);
        }

        tag.put(OWNERS_TAG, owners);

        return tag;
    }

    public static ZSwordReturnData load(CompoundTag tag) {
        ZSwordReturnData data = new ZSwordReturnData();

        ListTag owners = tag.getList(OWNERS_TAG, 10);

        for (int i = 0; i < owners.size(); i++) {
            CompoundTag ownerData = owners.getCompound(i);

            if (!ownerData.hasUUID(OWNER_TAG)) {
                continue;
            }

            UUID ownerUUID = ownerData.getUUID(OWNER_TAG);
            ListTag items = ownerData.getList(ITEMS_TAG, 10);

            List<ItemStack> stacks = new ArrayList<>();

            for (int j = 0; j < items.size(); j++) {
                ItemStack stack = ItemStack.of(items.getCompound(j));

                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }

            if (!stacks.isEmpty()) {
                data.pendingReturns.put(ownerUUID, stacks);
            }
        }

        return data;
    }
}