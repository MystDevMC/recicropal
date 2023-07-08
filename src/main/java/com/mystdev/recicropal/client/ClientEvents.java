package com.mystdev.recicropal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import com.mystdev.recicropal.ModItems;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Recicropal.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void renderTooltip(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().is(ModItems.BOTTLE_GOURD.get())) {
            var cap = event.getItemStack().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            cap.ifPresent(tank -> {
                var fluid = tank.getFluidInTank(0);
                var component = fluid.getFluid().getFluidType().getDescription(fluid);
                if (component.getStyle().isEmpty()) {
                    component = component.copy().withStyle(ChatFormatting.AQUA);
                }
                var volumeComponent = Component.empty()
                                               .append(fluid.getAmount() + " mB")
                                               .withStyle(ChatFormatting.GRAY);
                if (fluid.isEmpty()) {
                    component = Component.empty().append("Empty").withStyle(ChatFormatting.GRAY);
                }

                event.getTooltipElements().add(1, Either.left(component));
                if (!fluid.isEmpty()) {
                    event.getTooltipElements().add(2, Either.left(volumeComponent));
                }
            });
        }
    }

    @SubscribeEvent
    public static void renderHover(RenderHighlightEvent.Block event) {
        var mc = Minecraft.getInstance();
        var ray = event.getTarget();
        var blockpos = ray.getBlockPos();
        assert mc.level != null;
        var state = mc.level.getBlockState(blockpos);
        var be = mc.level.getBlockEntity(blockpos);
        if (!(be instanceof BottleGourdBlockEntity bottle)) return;
        if (mc.player == null) return;
        if (!mc.player.isCrouching()) return;

        var poseStack = event.getPoseStack();

        poseStack.pushPose();

        // Limit the vector to just outside the block pos box

        var playerPos = event.getCamera().getPosition();

        var closestPlane = ray.getDirection().getNormal();

        var x = (blockpos.getX() + 0.5) + (closestPlane.getX() * 0.75);
        var y = (blockpos.getY() + 0.5) + (closestPlane.getY() * 0.75);
        var z = (blockpos.getZ() + 0.5) + (closestPlane.getZ() * 0.75);

        var resVec = new Vec3(x, y, z).subtract(playerPos);

        poseStack.translate(resVec.x, resVec.y, resVec.z);

        var vec = mc.player.getLookAngle();
        vec = new Vec3(vec.x, 0, vec.z);
        var axis = new Vec3(0, 0, 1);
        var dp = vec.dot(axis);
        var vecMagnitude = Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.z, 2));
        var toArcCos = vecMagnitude == 0 ? 0 : dp / vecMagnitude;
        var angle = Math.acos(toArcCos);

        if (vec.x < 0) angle = (2 * Math.PI) - angle;

        poseStack.mulPose(Vector3f.YP.rotation((float) angle));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(10));

        poseStack.pushPose();
        poseStack.scale(0.01F, 0.01F, 0.01F);
        poseStack.mulPose(Vector3f.ZN.rotationDegrees(180));
        var title = Component.translatable(state.getBlock().getDescriptionId());
        var fluid = bottle.tank.getFluid();
        var content = fluid.getFluid().getFluidType().getDescription(fluid);
        if (fluid.isEmpty()) {
            content = Component.empty().append("Empty").withStyle(ChatFormatting.GRAY);
        }

        var width = Math.max(mc.font.width(title), mc.font.width(content));
        var height = mc.font.lineHeight;
        var offset = 4;
        var gap = 2;
        var wtot = 2 * offset + width;
        var htot = 2 * offset + 2 * height + gap;
        RenderSystem.enableDepthTest();
        Gui.fill(poseStack, -(wtot / 2), -(htot / 2), wtot / 2, htot / 2, mc.options.getBackgroundColor(0.8F));
        if (!fluid.isEmpty()) {
            // FluidStack BG
            var startH = -((wtot / 2) + gap + (2 * offset) + 16);
            var startV = -(htot / 2);
            Gui.fill(poseStack,
                     startH,
                     startV,
                     startH + 16 + 2 * offset,
                     startV + 16 + 2 * offset,
                     mc.options.getBackgroundColor(0.8F));
        }

        RenderSystem.disableDepthTest();
        poseStack.pushPose();
        poseStack.translate(0, 0, -.005);
        mc.font.draw(poseStack, title, (float) -(wtot / 2) + offset, -((float) htot / 2) + offset, 0xFFFFFF);
        var fontColor = Optional
                .ofNullable(content.getStyle().getColor())
                .orElse(TextColor.fromRgb(0x00FFFF))
                .getValue();
        mc.font.draw(poseStack,
                     content,
                     (float) -(wtot / 2) + offset,
                     -((float) htot / 2) + height + offset + gap, fontColor);

        if (!fluid.isEmpty()) {
            var clientFluid = IClientFluidTypeExtensions.of(fluid.getFluid());
            var texture = clientFluid.getStillTexture();
            var color = clientFluid.getTintColor(fluid);
            var fluidSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f,
                                        ((color >> 8) & 0xFF) / 255f,
                                        (color & 0xFF) / 255f,
                                        ((color >> 24) & 0xFF) / 255f);
            var startH = -((wtot / 2) + gap + 16 + offset);
            var startV = -(htot / 2) + offset;
            var ratio = (float) fluid.getAmount() / bottle.tank.getCapacity();
            var fluidHeight = Math.round((ratio) * 16);

            blit(poseStack,
                 startH,
                 startV + (16 - fluidHeight),
                 fluidSprite.getWidth(),
                 fluidHeight,
                 fluidSprite,
                 ratio);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

    }

    private static void blit(PoseStack poseStack,
                             int x0,
                             int y0,
                             int w,
                             int h,
                             TextureAtlasSprite sprite,
                             float cropY) {
        var dv = sprite.getV0() - sprite.getV1();
        var crop = dv - (cropY * dv);
        Gui.innerBlit(poseStack.last().pose(),
                      x0,
                      x0 + w,
                      y0,
                      y0 + h,
                      0,
                      sprite.getU0(),
                      sprite.getU1(),
                      sprite.getV0(),
                      sprite.getV1() + crop
        );
    }
}
