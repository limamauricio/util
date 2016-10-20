Tests Results
=============

Create a share
--------------

.. code-block:: console

  root@control:~# manila create NFS 1 \
  >     --name myshare_hnas \
  >     --description "My Manila share HNAS" \
  >     --share-type default_share_hitachi

  +-----------------------------+--------------------------------------+
  | Property                    | Value                                |
  +-----------------------------+--------------------------------------+
  | status                      | creating                             |
  | share_type_name             | default_share_hitachi                |
  | description                 | My Manila share HNAS                 |
  | availability_zone           | None                                 |
  | share_network_id            | None                                 |
  | share_server_id             | None                                 |
  | host                        |                                      |
  | access_rules_status         | active                               |
  | snapshot_id                 | None                                 |
  | is_public                   | False                                |
  | task_state                  | None                                 |
  | snapshot_support            | True                                 |
  | id                          | fe694dfb-38db-4193-b3ad-32eb553a999d |
  | size                        | 1                                    |
  | user_id                     | ba7f6d543713488786b4b8cb093e7873     |
  | name                        | myshare_hnas                         |
  | share_type                  | 3e54c8a2-1e50-455e-89a0-96bb52876c35 |
  | has_replicas                | False                                |
  | replication_type            | None                                 |
  | created_at                  | 2016-10-14T18:26:48.000000           |
  | share_proto                 | NFS                                  |
  | consistency_group_id        | None                                 |
  | source_cgsnapshot_member_id | None                                 |
  | project_id                  | c3810d8bcc3346d0bdc8100b09abbbf1     |
  | metadata                    | {}                                   |
  +-----------------------------+--------------------------------------+

  root@control:~# manila list
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | ID                                   | Name           | Size | Share Proto | Status    | Is Public | Share Type Name       | Host                    | Availability Zone |
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | fe694dfb-38db-4193-b3ad-32eb553a999d | myshare_hnas   | 1    | NFS         | available | False     | default_share_hitachi | control@hnas1#HNAS1     | nova              |
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+

Delete a share.
---------------

.. code-block:: console

  root@control:~# manila delete mysharefromsnap && manila list
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | ID                                   | Name            | Size | Share Proto | Status    | Is Public | Share Type Name       | Host                    | Availability Zone |
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | 53c3ffc6-441b-4ea4-b696-d0b464a01377 | mysharefromsnap | 1    | NFS         | deleting  | False     | default_share_hitachi | control@hnas1#HNAS1     | nova              |
  | fe694dfb-38db-4193-b3ad-32eb553a999d | myshare_hnas    | 1    | NFS         | available | False     | default_share_hitachi | control@hnas1#HNAS1     | nova              |
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+

Allow share access.
-------------------

.. code-block:: console

  root@control:~# manila access-allow mysharehnas ip 172.16.1.4
  +--------------+--------------------------------------+
  | Property     | Value                                |
  +--------------+--------------------------------------+
  | access_key   | None                                 |
  | share_id     | 721c0a6d-eea6-41af-8c10-72cd98985203 |
  | access_type  | ip                                   |
  | access_to    | 172.16.1.4                           |
  | access_level | rw                                   |
  | state        | new                                  |
  | id           | fae5569b-02b5-4004-b075-ba06b0173cce |
  +--------------+--------------------------------------+

  root@control:~# manila access-list myshare_hnas
  +--------------------------------------+-------------+--------------+--------------+--------+------------+
  | id                                   | access_type | access_to    | access_level | state  | access_key |
  +--------------------------------------+-------------+--------------+--------------+--------+------------+
  | fc5d34e2-5d3e-4605-90cd-3fda12a6b0ad | ip          | 172.16.1.4   | rw           | active | None       |
  +--------------------------------------+-------------+--------------+--------------+--------+------------+

Deny share access
------------------

.. code-block:: console

  root@control:~# manila access-deny myshare_hnas fc5d34e2-5d3e-4605-90cd-3fda12a6b0ad
  root@control:~# manila access-list myshare_hnas
  +----+-------------+-----------+--------------+-------+------------+
  | id | access_type | access_to | access_level | state | access_key |
  +----+-------------+-----------+--------------+-------+------------+
  +----+-------------+-----------+--------------+-------+------------+

Create a snapshot
------------------

.. code-block:: console

  root@control:~# manila snapshot-create --name mysnapshot --description "My Manila snapshot" myshare_hnas
  +-------------------+--------------------------------------+
  | Property          | Value                                |
  +-------------------+--------------------------------------+
  | status            | creating                             |
  | share_id          | fe694dfb-38db-4193-b3ad-32eb553a999d |
  | user_id           | ba7f6d543713488786b4b8cb093e7873     |
  | description       | My Manila snapshot                   |
  | created_at        | 2016-10-14T18:29:00.969954           |
  | size              | 1                                    |
  | share_proto       | NFS                                  |
  | provider_location | None                                 |
  | id                | 6bc74df4-e3b8-499a-b54c-bfb96f97b97c |
  | project_id        | c3810d8bcc3346d0bdc8100b09abbbf1     |
  | share_size        | 1                                    |
  | name              | mysnapshot                           |
  +-------------------+--------------------------------------+

  root@control:~# manila snapshot-list
  +--------------------------------------+--------------------------------------+-----------+------------+------------+
  | ID                                   | Share ID                             | Status    | Name       | Share Size |
  +--------------------------------------+--------------------------------------+-----------+------------+------------+
  | 6bc74df4-e3b8-499a-b54c-bfb96f97b97c | fe694dfb-38db-4193-b3ad-32eb553a999d | available | mysnapshot | 1          |
  +--------------------------------------+--------------------------------------+-----------+------------+------------+

Delete a snapshot
-----------------
.. code-block:: console

  root@control:~# manila snapshot-list
  +--------------------------------------+--------------------------------------+-----------+------------+------------+
  | ID                                   | Share ID                             | Status    | Name       | Share Size |
  +--------------------------------------+--------------------------------------+-----------+------------+------------+
  | 6bc74df4-e3b8-499a-b54c-bfb96f97b97c | fe694dfb-38db-4193-b3ad-32eb553a999d | available | mysnapshot | 1          |
  +--------------------------------------+--------------------------------------+-----------+------------+------------+

  root@control:~# manila snapshot-delete mysnapshot

  root@control:~# manila snapshot-list
  +----+----------+--------+------+------------+
  | ID | Share ID | Status | Name | Share Size |
  +----+----------+--------+------+------------+
  +----+----------+--------+------+------------+

Create a share from a snapshot
------------------------------
.. code-block:: console

  root@control:~# manila create NFS 1 \
  >     --snapshot-id 6bc74df4-e3b8-499a-b54c-bfb96f97b97c \
  >     --name mysharefromsnap

  +-----------------------------+--------------------------------------+
  | Property                    | Value                                |
  +-----------------------------+--------------------------------------+
  | status                      | creating                             |
  | share_type_name             | default_share_hitachi                |
  | description                 | None                                 |
  | availability_zone           | nova                                 |
  | share_network_id            | None                                 |
  | share_server_id             | None                                 |
  | host                        | control@hnas1#HNAS1                  |
  | access_rules_status         | active                               |
  | snapshot_id                 | 6bc74df4-e3b8-499a-b54c-bfb96f97b97c |
  | is_public                   | False                                |
  | task_state                  | None                                 |
  | snapshot_support            | True                                 |
  | id                          | 53c3ffc6-441b-4ea4-b696-d0b464a01377 |
  | size                        | 1                                    |
  | user_id                     | ba7f6d543713488786b4b8cb093e7873     |
  | name                        | mysharefromsnap                      |
  | share_type                  | 3e54c8a2-1e50-455e-89a0-96bb52876c35 |
  | has_replicas                | False                                |
  | replication_type            | None                                 |
  | created_at                  | 2016-10-14T18:37:14.000000           |
  | share_proto                 | NFS                                  |
  | consistency_group_id        | None                                 |
  | source_cgsnapshot_member_id | None                                 |
  | project_id                  | c3810d8bcc3346d0bdc8100b09abbbf1     |
  | metadata                    | {}                                   |
  +-----------------------------+--------------------------------------+

  root@control:~# manila list
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | ID                                   | Name            | Size | Share Proto | Status    | Is Public | Share Type Name       | Host                    | Availability Zone |
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | 53c3ffc6-441b-4ea4-b696-d0b464a01377 | mysharefromsnap | 1    | NFS         | available | False     | default_share_hitachi | control@hnas1#HNAS1     | nova              |
  | fe694dfb-38db-4193-b3ad-32eb553a999d | myshare_hnas    | 1    | NFS         | available | False     | default_share_hitachi | control@hnas1#HNAS1     | nova              |
  +--------------------------------------+-----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+

  root@control:~# manila show mysharefromsnap
  +-----------------------------+-----------------------------------------------------------------+
  | Property                    | Value                                                           |
  +-----------------------------+-----------------------------------------------------------------+
  | status                      | available                                                       |
  | share_type_name             | default_share_hitachi                                           |
  | description                 | None                                                            |
  | availability_zone           | nova                                                            |
  | share_network_id            | None                                                            |
  | export_locations            |                                                                 |
  |                             | path = 172.24.53.1:/shares/6f38012a-cd3d-413f-924b-2b1f6dc23d8f |
  |                             | preferred = False                                               |
  |                             | is_admin_only = False                                           |
  |                             | id = da07512c-4083-4907-8fc4-c00b0bfa3ee8                       |
  |                             | share_instance_id = 6f38012a-cd3d-413f-924b-2b1f6dc23d8f        |
  | share_server_id             | None                                                            |
  | host                        | control@hnas1#HNAS1                                             |
  | access_rules_status         | active                                                          |
  | snapshot_id                 | 6bc74df4-e3b8-499a-b54c-bfb96f97b97c                            |
  | is_public                   | False                                                           |
  | task_state                  | None                                                            |
  | snapshot_support            | True                                                            |
  | id                          | 53c3ffc6-441b-4ea4-b696-d0b464a01377                            |
  | size                        | 1                                                               |
  | user_id                     | ba7f6d543713488786b4b8cb093e7873                                |
  | name                        | mysharefromsnap                                                 |
  | share_type                  | 3e54c8a2-1e50-455e-89a0-96bb52876c35                            |
  | has_replicas                | False                                                           |
  | replication_type            | None                                                            |
  | created_at                  | 2016-10-14T18:37:14.000000                                      |
  | share_proto                 | NFS                                                             |
  | consistency_group_id        | None                                                            |
  | source_cgsnapshot_member_id | None                                                            |
  | project_id                  | c3810d8bcc3346d0bdc8100b09abbbf1                                |
  | metadata                    | {}                                                              |
  +-----------------------------+-----------------------------------------------------------------+

Extend a share
---------------
.. code-block:: console

  root@control:~# manila extend myshare_hnas 5
  root@control:~# manila show myshare_hnas
  +-----------------------------+-----------------------------------------------------------------+
  | Property                    | Value                                                           |
  +-----------------------------+-----------------------------------------------------------------+
  | status                      | available                                                       |
  | share_type_name             | default_share_hitachi                                           |
  | description                 | My Manila share HNAS                                            |
  | availability_zone           | nova                                                            |
  | share_network_id            | None                                                            |
  | export_locations            |                                                                 |
  |                             | path = 172.24.53.1:/shares/e4149a5b-131d-46b5-8826-1a6b11c08ac7 |
  |                             | preferred = False                                               |
  |                             | is_admin_only = False                                           |
  |                             | id = afa70437-d07b-436a-806f-945795b4edde                       |
  |                             | share_instance_id = e4149a5b-131d-46b5-8826-1a6b11c08ac7        |
  | share_server_id             | None                                                            |
  | host                        | control@hnas1#HNAS1                                             |
  | access_rules_status         | active                                                          |
  | snapshot_id                 | None                                                            |
  | is_public                   | False                                                           |
  | task_state                  | None                                                            |
  | snapshot_support            | True                                                            |
  | id                          | fe694dfb-38db-4193-b3ad-32eb553a999d                            |
  | size                        | 5                                                               |
  | user_id                     | ba7f6d543713488786b4b8cb093e7873                                |
  | name                        | myshare_hnas                                                    |
  | share_type                  | 3e54c8a2-1e50-455e-89a0-96bb52876c35                            |
  | has_replicas                | False                                                           |
  | replication_type            | None                                                            |
  | created_at                  | 2016-10-14T18:26:48.000000                                      |
  | share_proto                 | NFS                                                             |
  | consistency_group_id        | None                                                            |
  | source_cgsnapshot_member_id | None                                                            |
  | project_id                  | c3810d8bcc3346d0bdc8100b09abbbf1                                |
  | metadata                    | {}                                                              |
  +-----------------------------+-----------------------------------------------------------------+
 
  
Shrink a share
--------------
.. code-block:: console

  root@control:~# manila shrink myshare_hnas 2
  root@control:~# manila show myshare_hnas
  +-----------------------------+-----------------------------------------------------------------+
  | Property                    | Value                                                           |
  +-----------------------------+-----------------------------------------------------------------+
  | status                      | available                                                       |
  | share_type_name             | default_share_hitachi                                           |
  | description                 | My Manila share HNAS                                            |
  | availability_zone           | nova                                                            |
  | share_network_id            | None                                                            |
  | export_locations            |                                                                 |
  |                             | path = 172.24.53.1:/shares/e4149a5b-131d-46b5-8826-1a6b11c08ac7 |
  |                             | preferred = False                                               |
  |                             | is_admin_only = False                                           |
  |                             | id = afa70437-d07b-436a-806f-945795b4edde                       |
  |                             | share_instance_id = e4149a5b-131d-46b5-8826-1a6b11c08ac7        |
  | share_server_id             | None                                                            |
  | host                        | control@hnas1#HNAS1                                             |
  | access_rules_status         | active                                                          |
  | snapshot_id                 | None                                                            |
  | is_public                   | False                                                           |
  | task_state                  | None                                                            |
  | snapshot_support            | True                                                            |
  | id                          | fe694dfb-38db-4193-b3ad-32eb553a999d                            |
  | size                        | 2                                                               |
  | user_id                     | ba7f6d543713488786b4b8cb093e7873                                |
  | name                        | myshare_hnas                                                    |
  | share_type                  | 3e54c8a2-1e50-455e-89a0-96bb52876c35                            |
  | has_replicas                | False                                                           |
  | replication_type            | None                                                            |
  | created_at                  | 2016-10-14T18:26:48.000000                                      |
  | share_proto                 | NFS                                                             |
  | consistency_group_id        | None                                                            |
  | source_cgsnapshot_member_id | None                                                            |
  | project_id                  | c3810d8bcc3346d0bdc8100b09abbbf1                                |
  | metadata                    | {}                                                              |
  +-----------------------------+-----------------------------------------------------------------+

Manage a share
-------------------
.. code-block:: console

  root@control:~# manila manage \
  >     storage@hnas1#HNAS1 \
  >     nfs 172.24.53.1:/shares/dafb5c23-190b-4433-96a8-0640758815e5 \
  >     --name mysharehnas \
  >     --description "We manage share." \
  >     --share_type default_share_hitachi
  +-----------------------------+--------------------------------------+
  | Property                    | Value                                |
  +-----------------------------+--------------------------------------+
  | status                      | manage_starting                      |
  | share_type_name             | default_share_hitachi                |
  | description                 | We manage share.                     |
  | availability_zone           | None                                 |
  | share_network_id            | None                                 |
  | share_server_id             | None                                 |
  | host                        | storage@hnas1#HNAS1                  |
  | access_rules_status         | active                               |
  | snapshot_id                 | None                                 |
  | is_public                   | False                                |
  | task_state                  | None                                 |
  | snapshot_support            | True                                 |
  | id                          | 759028f0-7e1a-4e57-bd7b-ad3702393b6b |
  | size                        | None                                 |
  | user_id                     | 0e9756373b2049d4b4434aba5cee5438     |
  | name                        | mysharehnas                          |
  | share_type                  | c21e1139-6fe6-4f34-964c-4c28b31541ca |
  | has_replicas                | False                                |
  | replication_type            | None                                 |
  | created_at                  | 2016-10-20T14:14:05.000000           |
  | share_proto                 | NFS                                  |
  | consistency_group_id        | None                                 |
  | source_cgsnapshot_member_id | None                                 |
  | project_id                  | 1ae81b2df10f493eaf6a2a2c7da9002b     |
  | metadata                    | {}                                   |
  +-----------------------------+--------------------------------------+

  root@control:~# manila show mysharehnas
  +-----------------------------+-----------------------------------------------------------------+
  | Property                    | Value                                                           |
  +-----------------------------+-----------------------------------------------------------------+
  | status                      | available                                                       |
  | share_type_name             | default_share_hitachi                                           |
  | description                 | We manage share.                                                |
  | availability_zone           | nova                                                            |
  | share_network_id            | None                                                            |
  | export_locations            |                                                                 |
  |                             | path = 172.24.53.1:/shares/dafb5c23-190b-4433-96a8-0640758815e5 |
  |                             | preferred = False                                               |
  |                             | is_admin_only = False                                           |
  |                             | id = d6e26ab3-bce7-48f8-ab7b-8e5a7f062d7b                       |
  |                             | share_instance_id = 878a102d-e799-499e-93fc-0726445b9806        |
  | share_server_id             | None                                                            |
  | host                        | storage@hnas1#HNAS1                                             |
  | access_rules_status         | active                                                          |
  | snapshot_id                 | None                                                            |
  | is_public                   | False                                                           |
  | task_state                  | None                                                            |
  | snapshot_support            | True                                                            |
  | id                          | 759028f0-7e1a-4e57-bd7b-ad3702393b6b                            |
  | size                        | 1                                                               |
  | user_id                     | 0e9756373b2049d4b4434aba5cee5438                                |
  | name                        | mysharehnas                                                     |
  | share_type                  | c21e1139-6fe6-4f34-964c-4c28b31541ca                            |
  | has_replicas                | False                                                           |
  | replication_type            | None                                                            |
  | created_at                  | 2016-10-20T14:14:05.000000                                      |
  | share_proto                 | NFS                                                             |
  | consistency_group_id        | None                                                            |
  | source_cgsnapshot_member_id | None                                                            |
  | project_id                  | 1ae81b2df10f493eaf6a2a2c7da9002b                                |
  | metadata                    | {}                                                              |
  +-----------------------------+-----------------------------------------------------------------+

  
Unmanage a share
---------------------
.. code-block:: console

  root@control:~# manila list
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | ID                                   | Name           | Size | Share Proto | Status    | Is Public | Share Type Name       | Host                    | Availability Zone |
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+
  | cfb61cbe-5100-403d-a4bd-2ab7ac5f3d25 | mysharehnas    | 1    | NFS         | available | False     | default_share_hitachi | storage@hnas1#HNAS1     | nova              |
  +--------------------------------------+----------------+------+-------------+-----------+-----------+-----------------------+-------------------------+-------------------+

  root@control:~# manila unmanage mysharehnas
  root@control:~# manila show  mysharehnas
  ERROR: No share with a name or ID of 'mysharehnas' exists.
