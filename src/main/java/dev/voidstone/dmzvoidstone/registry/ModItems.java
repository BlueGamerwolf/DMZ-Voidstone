package dev.voidstone.dmzvoidstone.registry;

import dev.voidstone.dmzvoidstone.items.Twin_Star_Katana;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "dmz_voidstone");

    public static final RegistryObject<Item> TWIN_STAR_KATANA =
            ITEMS.register("twin_star_katana", Twin_Star_Katana::new);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}