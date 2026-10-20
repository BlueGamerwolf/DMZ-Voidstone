package dev.voidstone.dmzvoidstone.items.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.voidstone.dmzvoidstone.items.models.TwinStarKatanaModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TwinStarKatanaRenderer extends BlockEntityWithoutLevelRenderer {

    // Must match the folder under assets/ that holds the PNG (usually your @Mod id)
    private static final String NAMESPACE = "voidstone";

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NAMESPACE, "textures/item/twin_star_katanas.png");

    public TwinStarKatanaRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        // Model is centered on x/z = 0, but item space starts at the block corner
        poseStack.translate(0.5D, 0.0D, 0.5D);

        VertexConsumer vertices =
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        TwinStarKatanaModel.render(
                poseStack,
                vertices,
                packedLight,
                packedOverlay,
                0xFFFFFFFF
        );

        poseStack.popPose();
    }
}