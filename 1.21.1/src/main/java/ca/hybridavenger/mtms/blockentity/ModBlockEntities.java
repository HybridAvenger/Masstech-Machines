package ca.hybridavenger.mtms.blockentity;

import ca.hybridavenger.mtms.MTMS;



import ca.hybridavenger.mtms.blockentity.crusher.CrusherBlockEntity;
import ca.hybridavenger.mtms.registry.BlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MTMS.MODID);



    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER_BE =
            BLOCK_ENTITIES.register("crusher_be", () -> BlockEntityType.Builder.of(
                    CrusherBlockEntity::new, BlockRegistry.CRUSHER.get()).build(null));




    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}