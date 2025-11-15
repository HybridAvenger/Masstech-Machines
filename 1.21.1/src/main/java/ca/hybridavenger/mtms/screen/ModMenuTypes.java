package ca.hybridavenger.mtms.screen;

import ca.hybridavenger.mtms.MTMS;
import ca.hybridavenger.mtms.screen.crusher.CrusherMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MTMS.MODID);



    public static final RegistryObject<MenuType<CrusherMenu>> CRUSHER_MENU =
            MENUS.register("crusher_menu", () -> IForgeMenuType.create(CrusherMenu::new));


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}