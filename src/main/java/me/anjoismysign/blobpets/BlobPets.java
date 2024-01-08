package me.anjoismysign.blobpets;

import me.anjoismysign.blobpets.director.PetsManagerDirector;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;

public final class BlobPets extends BlobPlugin {
    private IManagerDirector proxy;
    private BlobPetsAPI api;

    @Override
    public void onEnable() {
        PetsManagerDirector director = new PetsManagerDirector(this);
        proxy = director.proxy();
        api = BlobPetsAPI.getInstance(director);
    }

    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    public BlobPetsAPI getApi() {
        return api;
    }
}
