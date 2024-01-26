package me.anjoismysign.blobpets.director.manager;

import me.anjoismysign.blobpets.director.PetsManagerDirector;
import me.anjoismysign.blobpets.listener.QuitRemove;
import us.mytheria.bloblib.entities.ListenerManager;

public class PetsListenerManager extends ListenerManager {
    private final PetsManagerDirector managerDirector;

    public PetsListenerManager(PetsManagerDirector managerDirector) {
        super(managerDirector);
        this.managerDirector = managerDirector;
        add(new QuitRemove(this));
    }

    @Override
    public PetsManagerDirector getManagerDirector() {
        return managerDirector;
    }
}