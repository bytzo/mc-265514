package net.bytzo.mc_265514.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Library;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.SOFTHRTF;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.nio.IntBuffer;

@Mixin(Library.class)
public class LibraryMixin {
	@Shadow
	private long currentDevice;

	@ModifyArg(
			method = "init(Ljava/lang/String;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lorg/lwjgl/system/MemoryStack;callocInt(I)Ljava/nio/IntBuffer;"
			),
			index = 0,
			remap = false
	)
	private int modifyIntBufferSize(int originalSize) {
		return originalSize + 4;
	}

	@WrapOperation(
			method = "init(Ljava/lang/String;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Ljava/nio/IntBuffer;put(I)Ljava/nio/IntBuffer;",
					ordinal = 2
			)
	)
	private IntBuffer putHrtfAttributes(
			IntBuffer attributes, int originalPut, Operation<IntBuffer> original,
			@Local boolean hrtfEnabled,
			@Local ALCCapabilities deviceCapabilities
	) {
		if (ALC10.alcGetInteger(this.currentDevice, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT) > 0) {
			var setHrtf = deviceCapabilities.ALC_SOFT_HRTF && hrtfEnabled;
			attributes.put(SOFTHRTF.ALC_HRTF_SOFT).put(setHrtf ? ALC10.ALC_TRUE : ALC10.ALC_FALSE);
			attributes.put(SOFTHRTF.ALC_HRTF_ID_SOFT).put(0);
		}

		return attributes.put(originalPut);
	}
}
