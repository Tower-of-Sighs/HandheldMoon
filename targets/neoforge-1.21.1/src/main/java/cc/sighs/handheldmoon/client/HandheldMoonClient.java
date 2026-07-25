package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.LampConeTransformHooks;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.client.renderer.LegacyRayConeRenderer;
import cc.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.compat.curios.CuriosCompat;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.registry.ModKeyBindings;
import cc.sighs.handheldmoon.util.AeronauticsUtils;
import cc.sighs.handheldmoon.network.ClientNetworkHooks;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;


@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class HandheldMoonClient {
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RayConeRenderer.installBackend(LegacyRayConeRenderer::render);
            LampConeTransformHooks.install((lamp, apex, direction) -> {
                if (!AeronauticsUtils.isPhysicalized(lamp)) {
                    return new LampConeTransformHooks.LampCone(apex, direction);
                }
                Vec3 transformedApex = AeronauticsUtils.getPhysicalizedRenderPosition(lamp);
                Quaterniond orientation = AeronauticsUtils.getPhysicalizedRenderOrientation(lamp);
                if (orientation == null) {
                    return new LampConeTransformHooks.LampCone(transformedApex != null ? transformedApex : apex, direction);
                }
                Vector3d transformedDirection = new Vector3d(direction.x, direction.y, direction.z);
                orientation.transform(transformedDirection);
                return new LampConeTransformHooks.LampCone(
                        transformedApex != null ? transformedApex : apex,
                        new Vec3(transformedDirection.x, transformedDirection.y, transformedDirection.z).normalize()
                );
            });
            ClientNetworkHooks.installLampState(lamp -> PacketDistributor.sendToServer(
                    new ServerMoonLightLampSyncPacket(lamp.getBlockPos(), lamp.getXRot(), lamp.getYRot(), lamp.getPowered())
            ));
            ClientNetworkHooks.installDeviceConfigSenders(
                    (pos, config) -> PacketDistributor.sendToServer(new ServerMoonlightLampConfigSyncPacket(pos, config)),
                    (pos, config) -> PacketDistributor.sendToServer(new ServerFullMoonConfigSyncPacket(pos, config)),
                    (hand, config) -> PacketDistributor.sendToServer(new ServerHeldMoonlightLampConfigSyncPacket(hand == net.minecraft.world.InteractionHand.OFF_HAND ? 1 : 0, config)),
                    (hand, config) -> PacketDistributor.sendToServer(new ServerHeldFullMoonConfigSyncPacket(hand == net.minecraft.world.InteractionHand.OFF_HAND ? 1 : 0, config))
            );
            ItemProperties.register(
                    ModItems.MOONLIGHT_LAMP.get(),
                    HandheldMoon.id("powered"),
                    (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            CuriosCompat.init();
        });
    }

    @SubscribeEvent
    public static void startWordTick(ClientTickEvent.Pre event) {
        HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors();
        HandheldMoonDynamicLightsInitializer.updateFullMoonEntityBehaviors();
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.FLASHLIGHT_SWITCH);
        event.register(ModKeyBindings.OPEN_DEVICE_CONFIG);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        event.registerEntityRenderer(ModEntities.MOONLIGHT.get(), FullMoonRenderer::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on")));
    }
}
