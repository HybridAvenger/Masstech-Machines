package ca.hybridavenger.mtms.datagen;


import ca.hybridavenger.mtms.MTMS;
import ca.hybridavenger.mtms.block.crusher.CrusherBlock;
import ca.hybridavenger.mtms.registry.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MTMS.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // For your crusher block
        orientedBlock(BlockRegistry.CRUSHER.get());
    }

    // Helper method for orientable blocks
    private void orientedBlock(@NotNull CrusherBlock block) {
        String name = ForgeRegistries.BLOCKS.getKey(block).getPath();

        ModelFile model = models().orientable(name,
                modLoc("block/crusher/" + name + "_side"),
                modLoc("block/crusher/" + name + "_front"),
                modLoc("block/crusher/" + name + "_top")
        );

        horizontalBlock(block, model);

        // Register item model
        simpleBlockItem(block, model);
    }
}