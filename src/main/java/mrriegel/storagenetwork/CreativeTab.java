package mrriegel.storagenetwork;

import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTab {

  public static CreativeTabs tab = new CreativeTabs(StorageNetwork.MODID) {

    @Override
    public ItemStack createIcon() {
      return new ItemStack(ModBlocks.request);
    }
  };
}
