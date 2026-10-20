
package dev.voidstone.dmzvoidstone.items;

import java.util.function.Consumer;

import dev.blue_gamerwolf.voidstone.common.item.ModToolMaterials;
import dev.voidstone.dmzvoidstone.items.renderer.TwinStarKatanaRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class Twin_Star_Katana extends SwordItem {

    public Twin_Star_Katana() {
        super(
                ModToolMaterials.VOIDSTONE,
                6,
                -2.4F,
                new Item.Properties()
        );
    }

    @Override
    public void initializeClient(
            Consumer<IClientItemExtensions> consumer
    ) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TwinStarKatanaRenderer();
                }

                return renderer;
            }
        });
    }
}
