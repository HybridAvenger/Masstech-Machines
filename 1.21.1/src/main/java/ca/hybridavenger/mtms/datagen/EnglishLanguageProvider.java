package ca.hybridavenger.mtms.datagen;

import ca.hybridavenger.mtms.MTMS;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class EnglishLanguageProvider extends LanguageProvider
{
    public EnglishLanguageProvider(PackOutput output, String locale) {
        super(output, MTMS.MODID, locale);

    }

    @Override
    protected void addTranslations() {
        //Items


        //Blocks



        add("creativetab.mtms.tabname", "MassTech Machinerie");
    }
}
