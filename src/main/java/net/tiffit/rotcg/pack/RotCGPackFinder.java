package net.tiffit.rotcg.pack;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;

public class RotCGPackFinder implements RepositorySource {

    private static RotCGPack pack;

    @Override
    public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor constructor) {
        if(pack == null)pack = new RotCGPack();
        consumer.accept(Pack.create("rotcg", true, () -> pack, constructor, Pack.Position.TOP, PackSource.DEFAULT));
    }
}
