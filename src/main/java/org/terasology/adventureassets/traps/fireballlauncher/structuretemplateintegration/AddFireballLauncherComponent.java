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
package org.terasology.adventureassets.traps.fireballlauncher.structuretemplateintegration;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.events.SpawnStructureEvent;

import java.util.List;

/**
 * This component is intended to be used in structure templates.
 *
 * It adds items (incl. block items) to one ore more chests when the entity receives a
 * {@link SpawnStructureEvent}.
 */
public class AddFireballLauncherComponent implements Component {
    public List<FireballLauncherToSpawn> fireballLaunchersToSpawn;

    @MappedContainer
    public static class FireballLauncherToSpawn {
        public Vector3i position;
        public Quat4f rotation = new Quat4f(0, 0, 0, 1);
        public boolean isFiring;
        public float timePeriod;
        public float offset;
        public Vector3f direction;
        public int maxDistance;
        public int damageAmount;
    }
}
