package jackrin.notalone.mixin;

import jackrin.notalone.utils.WhiteEyesAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void notAlone$injectWhiteEyesTick(CallbackInfo ci) {
        Animal animal = (Animal) (Object) this;

        if (animal instanceof Sheep || animal instanceof Pig || animal instanceof Cow) {
            WhiteEyesAnimal.tick(animal);
        }
    }
}
