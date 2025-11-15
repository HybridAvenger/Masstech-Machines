package ca.hybridavenger.mtms.recipe;

import ca.hybridavenger.mtms.MTMS;
import ca.hybridavenger.mtms.recipe.crusher.CrusherBlockRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MTMS.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MTMS.MODID);

    public static final RegistryObject<RecipeSerializer<CrusherBlockRecipe>> CRUSHER_SERIALIZER =
            SERIALIZERS.register("crusher", CrusherBlockRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<CrusherBlockRecipe>> CRUSHER_TYPE =
            TYPES.register("crusher", () -> new RecipeType<CrusherBlockRecipe>() {
                @Override
                public String toString() {
                    return "crusher";
                }
            });


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}