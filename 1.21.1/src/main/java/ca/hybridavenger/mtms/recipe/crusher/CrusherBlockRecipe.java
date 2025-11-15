package ca.hybridavenger.mtms.recipe.crusher;

import ca.hybridavenger.mtms.recipe.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record CrusherBlockRecipe(Ingredient inputItem, int inputCount, int energyCost,
                                 ItemStack primaryOutput, ItemStack bonusOutput, float bonusChance)
        implements Recipe<CrusherBlockRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(CrusherBlockRecipeInput pInput, Level pLevel) {
        if(pLevel.isClientSide()) {
            return false;
        }
        return inputItem.test(pInput.getItem(0)) && pInput.getItem(0).getCount() >= inputCount;
    }

    @Override
    public ItemStack assemble(CrusherBlockRecipeInput pInput, HolderLookup.Provider pRegistries) {
        return primaryOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return primaryOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUSHER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUSHER_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CrusherBlockRecipe> {
        public static final MapCodec<CrusherBlockRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CrusherBlockRecipe::inputItem),
                Codec.INT.optionalFieldOf("count", 1).forGetter(CrusherBlockRecipe::inputCount),
                Codec.INT.optionalFieldOf("energy", 5000).forGetter(CrusherBlockRecipe::energyCost),
                ItemStack.CODEC.fieldOf("result").forGetter(CrusherBlockRecipe::primaryOutput),
                ItemStack.CODEC.optionalFieldOf("bonus", ItemStack.EMPTY).forGetter(CrusherBlockRecipe::bonusOutput),
                Codec.FLOAT.optionalFieldOf("bonus_chance", 0.1f).forGetter(CrusherBlockRecipe::bonusChance)
        ).apply(inst, CrusherBlockRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CrusherBlockRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, CrusherBlockRecipe::inputItem,
                        ByteBufCodecs.VAR_INT, CrusherBlockRecipe::inputCount,
                        ByteBufCodecs.VAR_INT, CrusherBlockRecipe::energyCost,
                        ItemStack.STREAM_CODEC, CrusherBlockRecipe::primaryOutput,
                        ItemStack.STREAM_CODEC, CrusherBlockRecipe::bonusOutput,
                        ByteBufCodecs.FLOAT, CrusherBlockRecipe::bonusChance,
                        CrusherBlockRecipe::new);

        @Override
        public MapCodec<CrusherBlockRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrusherBlockRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}