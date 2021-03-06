/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.adventureassets.traps.swingingblade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.Location;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.OnBlockToItem;

@RegisterSystem(RegisterMode.CLIENT)
public class SwingingBladeClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(SwingingBladeClientSystem.class);

    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    @In
    private Time time;

    /**
     * This method creates the mesh entity when the {@link SwingingBladeComponent} is activated. The rod and blade
     * entities are saved in the childrenEntities list inside the {@link SwingingBladeComponent}.
     * A similar method in the {@link SwingingBladeServerSystem} adds the rod and blade entities to the
     * childrenEntities list.<br/>
     * Note this happens before the block is actually placed in the world i.e. before the OnBlockItemPlacedEvent handler-
     * {@link SwingingBladeServerSystem#onBlockToItem(OnBlockToItem, EntityRef, SwingingBladeComponent)} gets called.
     * So, the saved properties (amplitude, time-period, offset etc) are transferred after this, maintaining
     * only the childrenEntities list created here.
     *
     * @param event
     * @param entity
     * @param swingingBladeComponent
     */
    @ReceiveEvent(components = {SwingingBladeComponent.class, BlockComponent.class})
    public void onSwingingBladeActivated(OnActivatedComponent event, EntityRef entity,
                                         SwingingBladeComponent swingingBladeComponent) {
        // So that only the relevant server entity (which gets modified by the server system already) is operated on.
        if (!swingingBladeComponent.childrenEntities.isEmpty()) {
            Prefab swingingBladePrefab = assetManager.getAsset("AdventureAssets:swingingBladeMesh", Prefab.class).get();
            EntityBuilder swingingBladeEntityBuilder = entityManager.newBuilder(swingingBladePrefab);
            swingingBladeEntityBuilder.setOwner(entity);
            swingingBladeEntityBuilder.setPersistent(false);
            EntityRef swingingBladeMesh = swingingBladeEntityBuilder.build();
            swingingBladeComponent.childrenEntities.add(swingingBladeMesh);
            entity.saveComponent(swingingBladeComponent);
            Location.attachChild(entity, swingingBladeMesh, new Vector3f(0, -1, 0), new Quat4f(Quat4f.IDENTITY));
        }
    }

    @Override
    public void update(float delta) {
        for (EntityRef blade : entityManager.getEntitiesWith(SwingingBladeComponent.class, BlockComponent.class)) {
            SwingingBladeUtilities.rotateSwingingBlade(blade, time.getGameTime());
        }
    }
}
