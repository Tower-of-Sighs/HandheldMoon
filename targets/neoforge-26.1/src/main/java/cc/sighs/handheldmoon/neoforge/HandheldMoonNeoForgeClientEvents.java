package cc.sighs.handheldmoon.neoforge;
import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.HandheldMoonClient;
import cc.sighs.handheldmoon.neoforge.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.neoforge.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.neoforge.client.renderer.item.MoonlightLampPoweredProperty;
import cc.sighs.handheldmoon.neoforge.event.handler.EffectManager;
import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
@EventBusSubscriber(modid=HandheldMoon.MOD_ID,value=Dist.CLIENT)
public final class HandheldMoonNeoForgeClientEvents {
 private HandheldMoonNeoForgeClientEvents(){}
 @SubscribeEvent public static void onClientTick(ClientTickEvent.Pre e){HandheldMoonClient.onClientTick();}
 @SubscribeEvent public static void onKey(InputEvent.Key e){OperationEventHandler.onKey(e.getKey(),e.getAction());}
 @SubscribeEvent public static void onMouseButton(InputEvent.MouseButton.Pre e){if(OperationEventHandler.onMouseButton(e.getButton(),e.getAction()))e.setCanceled(true);}
 @SubscribeEvent public static void onMouseScroll(InputEvent.MouseScrollingEvent e){boolean c=InteractEventHandler.onMouseScroll(e.getScrollDeltaY());if(!c)c=OperationEventHandler.onMouseScroll(e.getScrollDeltaY());if(c)e.setCanceled(true);}
 @SubscribeEvent public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e){if(e.getSide().isClient()&&e.getHand()==InteractionHand.MAIN_HAND)InteractEventHandler.onClientMainHandRightClickBlock();}
 @SubscribeEvent public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterWeather e){Minecraft mc=Minecraft.getInstance();RayEvent.renderPlayerViewConesWithRadialGradient(e.getPoseStack(),e.getLevelRenderState().cameraRenderState.pos,e.getModelViewMatrix(),mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));}
 @SubscribeEvent public static void onAfterLevel(RenderLevelStageEvent.AfterLevel e){EffectManager.onLevelRender();}
 @SubscribeEvent public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer((net.minecraft.world.entity.EntityType<cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity>) ModEntities.MOONLIGHT.get(),FullMoonRenderer::new);e.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity>) ModBlockEntities.MOONLIGHT_LAMP.get(),MoonlightLampRenderer::new);}
 @SubscribeEvent public static void onRegisterConditionalItemProperties(RegisterConditionalItemModelPropertyEvent e){e.register(Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID,"powered"),MoonlightLampPoweredProperty.MAP_CODEC);}
}
