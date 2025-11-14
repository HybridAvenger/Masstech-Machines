package ca.hybridavenger.mtms.datagen;

import ca.hybridavenger.mtms.MTMS;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class FrenchLanguageProvider extends LanguageProvider {

    public FrenchLanguageProvider(PackOutput output, String locale) {
        super(output, MTMS.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        //Items


        add("creativetab.mtms.tabname", "Mod de bibliothèque hybride");

    }
}