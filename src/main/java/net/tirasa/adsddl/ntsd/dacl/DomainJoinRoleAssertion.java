/*
 * Copyright (C) 2018 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /*
 * Copyright © 2018 VMware, Inc. All Rights Reserved.
 *
 * COPYING PERMISSION STATEMENT
 * SPDX-License-Identifier: Apache-2.0
 */
package net.tirasa.adsddl.ntsd.dacl;

import net.tirasa.adsddl.ntsd.SID;
import net.tirasa.adsddl.ntsd.data.AceFlag;
import net.tirasa.adsddl.ntsd.data.AceObjectFlags;
import net.tirasa.adsddl.ntsd.data.AceObjectFlags.Flag;
import net.tirasa.adsddl.ntsd.data.AceRights;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an {@linkplain AdRoleAssertion} which specifies the criteria required to join or remove computers
 * to/from an AD domain in a given container (& its children) without any restrictions. This includes the ability
 * to reset AD computer passwords, which is needed by some systems that manage domain joined computers.
 */
public class DomainJoinRoleAssertion extends AdRoleAssertion {

    /**
     * Schema GUID of "CN=Computer,CN=Schema,CN=Configuration" objects
     */
    protected static final String COMPUTER_SCHEMA_ID_GUID = "bf967a86-0de6-11d0-a285-00aa003049e2";

    /**
     * Schema GUID of "CN=User-Force-Change-Password,CN=Extended-Rights,CN=Configuration" extended right
     * (aka "reset password")
     */
    protected static final String RESET_PASSWORD_CR_GUID = "00299570-246d-11d0-a768-00aa006e0529";

    /**
     * Create computer object applied to "This object and all descendant objects"
     */
    protected static final AceAssertion CREATE_COMPUTER = new AceAssertion(
            AceRights.parseValue(0x00000001),
            new AceObjectFlags(Flag.ACE_OBJECT_TYPE_PRESENT),
            COMPUTER_SCHEMA_ID_GUID,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);

    /**
     * DELETE_COMPUTER - Delete computer object applied to "This object and all descendant objects"
     */
    protected static final AceAssertion DELETE_COMPUTER = new AceAssertion(
            AceRights.parseValue(0x00000002),
            new AceObjectFlags(Flag.ACE_OBJECT_TYPE_PRESENT),
            COMPUTER_SCHEMA_ID_GUID,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);

    /**
     * LIST_CONTENTS - List contents applied to "This object and all descendant objects"
     */
    protected static final AceAssertion LIST_CONTENTS = new AceAssertion(
            AceRights.parseValue(0x00000004),
            null,
            null,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);



    /**
     * READ_PROPERTIES - Read All Properties applied at any of below scopes on target Organizational Unit
     * - This object only
     * - This object and all descendant objects
     * - All descendant objects
     */
    protected static final AceAssertion READ_PROPERTIES_ANY_SCOPE = new AceAssertion(
            AceRights.parseValue(0x00000010));

    /**
     * READ_PROPERTIES - Read All Properties applied to "This object and all descendant objects"
     */
    protected static final AceAssertion READ_PROPERTIES = new AceAssertion(
            AceRights.parseValue(0x00000010),
            null,
            null,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);

    /**
     *  WRITE_PROPERTIES_COMPUTER_OBJECTS - Write all propeties applied to "Descendant Computer Objects"
     */
    protected static final AceAssertion WRITE_PROPERTIES_COMPUTER_OBJECTS = new AceAssertion(
            AceRights.parseValue(0x00000020),
            null,
            null,
            COMPUTER_SCHEMA_ID_GUID,
            AceFlag.CONTAINER_INHERIT_ACE,
            null);

    /**
     *  WRITE_PROPERTIES - Write all propeties applied to "This object and all descendant objects"
     */
    protected static final AceAssertion WRITE_PROPERTIES = new AceAssertion(
            AceRights.parseValue(0x00000020),
            null,
            null,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);

    /**
     * READ_PERMISSIONS -  Read Permissions(a.k.a Read Control) applied to "This object and all descendant objects"
     */
    protected static final AceAssertion READ_PERMISSIONS = new AceAssertion(
            AceRights.parseValue(0x00020000),
            null,
            null,
            null,
            AceFlag.CONTAINER_INHERIT_ACE,
            AceFlag.INHERIT_ONLY_ACE);

    /**
     * RESET_PASSWORD  - Permission to force reset password applied to  "Descendant Computer Objects"
     */
    protected static final AceAssertion RESET_PASSWORD = new AceAssertion(
            AceRights.parseValue(AceRights.ObjectRight.CR.getValue()),
            new AceObjectFlags(Flag.ACE_OBJECT_TYPE_PRESENT, Flag.ACE_INHERITED_OBJECT_TYPE_PRESENT),
            RESET_PASSWORD_CR_GUID,
            COMPUTER_SCHEMA_ID_GUID,
            AceFlag.CONTAINER_INHERIT_ACE,
            null);


    protected static List<AceAssertion> domainJoinAssertions(boolean withMinimumRequiredPermissions) {

        if (withMinimumRequiredPermissions) {
            return Arrays.asList(
                    CREATE_COMPUTER,
                    DELETE_COMPUTER,
                    READ_PROPERTIES_ANY_SCOPE,
                    WRITE_PROPERTIES_COMPUTER_OBJECTS,
                    RESET_PASSWORD);
        }

        return Arrays.asList(CREATE_COMPUTER,
                DELETE_COMPUTER,
                LIST_CONTENTS,
                READ_PROPERTIES,
                WRITE_PROPERTIES,
                READ_PERMISSIONS,
                RESET_PASSWORD);
    }

    /**
     * DomainJoinRoleAssertion constructor
     *
     * @param principal
     * SID of the user or group
     * @param isGroup
     * whether the principal is a group
     * @param tokenGroups
     * list of token group SIDs which should be searched if the principal itself does not meet all the
     * criteria (when the principal is a user). May be null.
     * @param withMinimumRequiredPermissions
     * assert with minimum required join account permissions
     */
    public DomainJoinRoleAssertion(SID principal, boolean isGroup, List<SID> tokenGroups, boolean withMinimumRequiredPermissions) {
        super(domainJoinAssertions(withMinimumRequiredPermissions), principal, isGroup, tokenGroups);
    }

    /**
     * DomainJoinRoleAssertion constructor
     *
     * @param principal
     * SID of the user or group
     * @param isGroup
     * whether the principal is a group
     * @param tokenGroups
     * list of token group SIDs which should be searched if the principal itself does not meet all the
     * criteria (when the principal is a user). May be null.
     */
    public DomainJoinRoleAssertion(SID principal, boolean isGroup, List<SID> tokenGroups) {
        this(principal,isGroup,tokenGroups,false);
    }
}
