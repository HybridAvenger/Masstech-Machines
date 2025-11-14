package ca.hybridavenger.mtms.util;


import ca.hybridavenger.hybridlib.item.ItemRegistry;
import ca.hybridavenger.mtms.MTMS;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;


import java.util.function.Supplier;

public class MTMSCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MTMS.MODID);

    public static final Supplier<CreativeModeTab> MTMS_TAB = CREATIVE_MODE_TAB.register("mtms_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemRegistry.NETHER_INGOT.get()))
                    .title(Component.translatable("creativetab.mtms.tabname"))
                    .displayItems((itemDisplayParameters, output) -> {




                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
