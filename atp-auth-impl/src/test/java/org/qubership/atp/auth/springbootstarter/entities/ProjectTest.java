/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.auth.springbootstarter.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.SerializationUtils;

public class ProjectTest implements Serializable {

    /**
     * Test of project serializing.
     */
    @Test
    public void whenSerializing_ThenNoException() {
        Project p1 = new Project();
        p1.setUuid(UUID.randomUUID());
        p1.setLeads(new HashSet<UUID>() {{
            add(UUID.randomUUID());
            add(UUID.randomUUID());
        }});
        Permissions permissions = new Permissions();
        permissions.setLeads(new HashMap<String, Operations>() {{
            put("test", new Operations(true, false, true, false, true, false, true));
        }});
        p1.setPermissions(permissions);

        byte[] serialize = SerializationUtils.serialize(p1);
        Project p2 = (Project) SerializationUtils.deserialize(serialize);

        Assert.assertNotNull(p2);
        Assert.assertNotNull(p2.getUuid());
        Assert.assertEquals(p1.getUuid(), p2.getUuid());
        Assert.assertEquals(p1.getLeads(), p2.getLeads());
        Assert.assertEquals(p1.getPermissions(), p2.getPermissions());
    }

}
