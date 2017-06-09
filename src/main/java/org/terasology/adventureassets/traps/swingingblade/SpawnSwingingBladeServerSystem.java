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
import org.terasology.adventureassets.traps.DeleteTrapEvent;
import org.terasology.adventureassets.traps.RequestTrapPlaceholderPrefabSelection;
import org.terasology.adventureassets.traps.TrapPlaceholderComponent;
import org.terasology.adventureassets.traps.TrapsPlacementComponent;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Side;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.events.BuildStructureTemplateEntityEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamily;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the logic to make spawning of Swinging Blades work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnSwingingBladeServerSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(SpawnSwingingBladeServerSystem.class);
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private LocalPlayer localPlayer;

    @ReceiveEvent
    public void onSpawnStructureWithSwingingBlade(StructureBlocksSpawnedEvent event, EntityRef entity,
                                                  TrapsPlacementComponent trapsPlacementComponent) {
        // Spawn blades while spawning from spawner
        BlockRegionTransform transformation = event.getTransformation();
        for (SwingingBlade swingingBlade : trapsPlacementComponent.swingingBladeList) {
            Vector3i position = transformation.transformVector3i(swingingBlade.position);
            Quat4f rotation = transformation.transformRotation(swingingBlade.rotation);

            EntityBuilder entityBuilder = entityManager.newBuilder(swingingBlade.prefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(position.toVector3f());
            locationComponent.setWorldRotation(rotation);
            SwingingBladeComponent swingingBladeComponent = entityBuilder.getComponent(SwingingBladeComponent.class);
            swingingBladeComponent.timePeriod = swingingBlade.timePeriod;
            swingingBladeComponent.amplitude = swingingBlade.amplitude;
            swingingBladeComponent.offset = swingingBlade.offset;

            entityBuilder.build();
        }
    }

    @ReceiveEvent
    public void onTemplateSpawned(SpawnTemplateEvent event, EntityRef entity, TrapsPlacementComponent trapsPlacementComponent) {
        BlockRegionTransform transformation = event.getTransformation();

        for (SwingingBlade swingingBlade : trapsPlacementComponent.swingingBladeList) {
            Vector3i actualPosition = transformation.transformVector3i(swingingBlade.position);
            Quat4f rotation = transformation.transformRotation(swingingBlade.rotation);

            EntityBuilder entityBuilder = entityManager.newBuilder(swingingBlade.prefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(actualPosition.toVector3f());
            locationComponent.setWorldRotation(rotation);
            SwingingBladeComponent swingingBladeComponent = entityBuilder.getComponent(SwingingBladeComponent.class);
            swingingBladeComponent.timePeriod = swingingBlade.timePeriod;
            swingingBladeComponent.amplitude = swingingBlade.amplitude;
            swingingBladeComponent.offset = swingingBlade.offset;

            EntityRef trapEntity = entityBuilder.build();

            BlockFamily blockFamily = blockManager.getBlockFamily("AdventureAssets:TrapPlaceholder");
            HorizontalBlockFamily horizontalBlockFamily = (HorizontalBlockFamily) blockFamily;
            Block block = horizontalBlockFamily.getBlockForSide(Side.FRONT);
            Vector3i positionAbove = new Vector3i(actualPosition);
            positionAbove.addY(1);
            worldProvider.setBlock(positionAbove, block);
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(positionAbove);
            TrapPlaceholderComponent trapPlaceholderComponent = blockEntity.getComponent(TrapPlaceholderComponent.class);
            trapPlaceholderComponent.setSelectedPrefab(swingingBlade.prefab);
            trapPlaceholderComponent.setTrapEntity(trapEntity);
            blockEntity.saveComponent(trapPlaceholderComponent);
        }
    }

    @ReceiveEvent
    public void onBuildTemplateWithTrapPlacement(BuildStructureTemplateEntityEvent event, EntityRef entity) {
        BlockRegionTransform transformToRelative = event.getTransformToRelative();
        BlockFamily blockFamily = blockManager.getBlockFamily("AdventureAssets:TrapPlaceholder");

        List<SwingingBlade> bladeList = new ArrayList<>();
        for (Vector3i position : event.findAbsolutePositionsOf(blockFamily)) {
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(position);
            TrapPlaceholderComponent trapPlaceholderComponent = blockEntity.getComponent(TrapPlaceholderComponent.class);
            if (trapPlaceholderComponent.getSelectedPrefab().getName().equalsIgnoreCase("AdventureAssets:swingingBladePlaceholder")) {
                EntityRef trapEntity = trapPlaceholderComponent.getTrapEntity();
                SwingingBladeComponent swingingBladeComponent = trapEntity.getComponent(SwingingBladeComponent.class);
                SwingingBlade swingingBlade = new SwingingBlade();
                swingingBlade.position = transformToRelative.transformVector3i(blockEntity.getComponent(BlockComponent.class).getPosition());
                swingingBlade.position.subY(1); // placeholder is on top of marked block
                swingingBlade.rotation = transformToRelative.transformRotation(trapEntity.getComponent(LocationComponent.class).getWorldRotation());
                swingingBlade.amplitude = swingingBladeComponent.amplitude;
                swingingBlade.timePeriod = swingingBladeComponent.timePeriod;
                swingingBlade.offset = swingingBladeComponent.offset;

                bladeList.add(swingingBlade);
            }
        }
        if (bladeList.size() > 0) {
            TrapsPlacementComponent trapsPlacementComponent = event.getTemplateEntity().getComponent(TrapsPlacementComponent.class);
            if (trapsPlacementComponent == null) {
                trapsPlacementComponent = new TrapsPlacementComponent();
            }
            trapsPlacementComponent.swingingBladeList = bladeList;
            event.getTemplateEntity().addOrSaveComponent(trapsPlacementComponent);
        }
    }

    @ReceiveEvent
    public void onRequestTrapPlaceholderPrefabSelection(RequestTrapPlaceholderPrefabSelection event, EntityRef characterEntity,
                                                        CharacterComponent characterComponent) {
        if (event.getPrefab().getName().equalsIgnoreCase("AdventureAssets:swingingBladePlaceholder")) {
            EntityRef blockEntity = event.getTrapPlaceholderBlockEntity();
            TrapPlaceholderComponent trapPlaceholderComponent = blockEntity.getComponent(TrapPlaceholderComponent.class);
            if (trapPlaceholderComponent.getTrapEntity() != null) {
                blockEntity.send(new DeleteTrapEvent(trapPlaceholderComponent.getSelectedPrefab(), trapPlaceholderComponent.getTrapEntity()));
            }
            BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);

            SwingingBlade swingingBlade = new SwingingBlade();
            swingingBlade.position = new Vector3i(blockComponent.getPosition()).subY(1); // placeholder is on top of marked block

            EntityBuilder entityBuilder = entityManager.newBuilder(swingingBlade.prefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(swingingBlade.position.toVector3f());
            locationComponent.setWorldRotation(swingingBlade.rotation);

            EntityRef swingingBladeEntity = entityBuilder.build();

            trapPlaceholderComponent.setTrapEntity(swingingBladeEntity);
            blockEntity.saveComponent(trapPlaceholderComponent);
        }
    }

    @ReceiveEvent
    public void onTrapDelete(DeleteTrapEvent event, EntityRef entity) {
        if (event.getPrefab().getName().equalsIgnoreCase("AdventureAssets:swingingBladePlaceholder")) {
            SwingingBladeComponent swingingBladeComponent = event.getTrapEntity().getComponent(SwingingBladeComponent.class);
            for (EntityRef e : swingingBladeComponent.childrenEntities) {
                e.destroy();
            }
            event.getTrapEntity().destroy();
        }
    }
}