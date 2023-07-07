package com.mystdev.recicropal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import com.mystdev.recicropal.ModItems;
import com.mystdev.recicropal.Recicropal;
import com.mystdev.recicropal.common.fluid.ModFluidUtils;
import com.mystdev.recicropal.content.crop.bottle_gourd.BottleGourdBlockEntity;
import com.mystdev.recicropal.content.mixing.Mixture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Recicropal.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void renderTooltip(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().is(ModItems.BOTTLE_GOURD.get())) {
            var cap = event.getItemStack().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

            cap.ifPresent(tank -> {
                var fluid = tank.getFluidInTank(0);
                var component = Component
                        .translatable(fluid.getTranslationKey())
                        .append(" " + fluid.getAmount() + " mB")
                        .withStyle(ChatFormatting.AQUA);
                if (fluid.isEmpty()) component = Component.empty().append("Empty").withStyle(ChatFormatting.GRAY);
                event.getTooltipElements().add(1, Either.left(component));
                if (fluid.getFluid().is(ModFluidUtils.tag("forge:potion"))) {
                    var potionTag = fluid.getOrCreateTag().getString(PotionUtils.TAG_POTION);
                    if (potionTag.equals("")) return;
                    var potionComponent = Component
                            // Wouldn't this create name collisions?
                            .translatable(Potion
                                                  .byName(potionTag)
                                                  .getName("item.minecraft.potion.effect."))
                            .withStyle(ChatFormatting.GRAY);
                    event.getTooltipElements().add(2, Either.left(potionComponent));
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


//        mc.getItemRenderer().renderStatic(ModItems.BOTTLE_GOURD.asStack(), ItemTransforms.TransformType.GROUND,
//                                          LightTexture.FULL_BLOCK,
//                                          OverlayTexture.NO_OVERLAY, poseStack, event.getMultiBufferSource(), 123123);


        poseStack.pushPose();
        poseStack.scale(0.01F, 0.01F, 0.01F);
        poseStack.mulPose(Vector3f.ZN.rotationDegrees(180));
        var title = Component.translatable(state.getBlock().getDescriptionId());
        var fluid = bottle.tank.getFluid();
        var content = Component.translatable(fluid.getTranslationKey()).append(" " + bottle.tank
                .getFluid()
                .getAmount() + " mB");
        if (fluid.isEmpty()) content = Component.empty().append("Empty").withStyle(ChatFormatting.GRAY);
        var fluidColor = 0x00FFFF;
        if (Mixture.isMixture(fluid)) {
            var mixture = Mixture.fromFluid(bottle.tank.getFluid());
            fluidColor = mixture.getColor();
            Recicropal.LOGGER.debug("Color " + fluidColor);
            content = Component
                    .translatable(fluid.getTranslationKey() + "." + mixture.getCategory().getSerializedName())
                    .append(" " + bottle.tank.getFluid().getAmount() + " mB");
        }
        var width = Math.max(mc.font.width(title), mc.font.width(content));
        var height = mc.font.lineHeight;
        var offset = 4;
        var gap = 2;
        var wtot = 2 * offset + width;
        var htot = 2 * offset + 2 * height + gap;
        RenderSystem.enableDepthTest();
        Gui.fill(poseStack, -(wtot / 2), -(htot / 2), wtot / 2, htot / 2, mc.options.getBackgroundColor(0.8F));
        RenderSystem.disableDepthTest();
        poseStack.pushPose();
        poseStack.translate(0, 0, -.005);
        mc.font.draw(poseStack, title, (float) -(wtot / 2) + offset, -((float) htot / 2) + offset, 0xFFFFFF);
        mc.font.draw(poseStack,
                     content,
                     (float) -(wtot / 2) + offset,
                     -((float) htot / 2) + height + offset + gap,
                     fluidColor);
        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

    }
}
