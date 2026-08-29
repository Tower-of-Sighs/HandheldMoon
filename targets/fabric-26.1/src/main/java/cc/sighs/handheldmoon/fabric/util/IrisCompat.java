package cc.sighs.handheldmoon.fabric.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrisCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger(IrisCompat.class);
    private static RenderPipeline assignedRayConePipeline;

    private IrisCompat() {
    }

    private static final boolean AVAILABLE;

    static {
        boolean flag = false;
        try {
            Class.forName("net.irisshaders.iris.Iris");
            flag = true;
        } catch (ClassNotFoundException e) {
            flag = false;
        }
        AVAILABLE = flag;
    }

    public static boolean isShaderPackInUse() {
        if (!AVAILABLE) {
            return false;
        }
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static void assignRayConePipeline(RenderPipeline pipeline) {
        if (!AVAILABLE || assignedRayConePipeline == pipeline) {
            return;
        }
        try {
            IrisPipelines.assignPipeline(pipeline, ShaderKey.BASIC_COLOR);
            assignedRayConePipeline = pipeline;
        } catch (Throwable shaderKeyFailure) {
            try {
                IrisApi.getInstance().assignPipeline(pipeline, IrisProgram.BASIC);
                assignedRayConePipeline = pipeline;
            } catch (Throwable apiFailure) {
                LOGGER.warn("Failed to assign HandheldMoon ray pipeline to Iris. Falling back to vanilla pipeline.", apiFailure);
                assignedRayConePipeline = pipeline;
            }
        }
    }
}


