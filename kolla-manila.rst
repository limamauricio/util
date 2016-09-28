Build and Deploy Kolla + Manila + Generic Driver
================================================

* Guide to build and deploy Kolla Newton rc1

Requirements
------------

* 2 (or more) network interfaces.
* At least 8gb main memory
* 60gb disk space for sda.
* 40gb disk space for sdb.
* Ubuntu 16.04

=====================   ===========  ===========
Component               Min Version  Max Version
=====================   ===========  ===========
Ansible                 2.0.0        none       
Docker                  1.10.0       none       
Docker Python           1.6.0        none       
Python Jinja2           2.8.0        none       
=====================   ===========  ===========

Setup Environment
-----------------

1. Install pip
::

    apt-get -y install python-pip
    pip install -U pip
    curl https://bootstrap.pypa.io/get-pip.py | python

2. Add docker repository key
::

   apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
   apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 81026D0004C44CF7EF55ADF8DF7D54CBE56151BF
   
3. Add docker repository
::

    echo "deb https://apt.dockerproject.org/repo ubuntu-xenial main" | sudo tee /etc/apt/sources.list.d/docker.list
    
4. Update and Install docker-engine
::

    apt-get -y update
    apt-get install -y docker-engine
    
5. Edit docker.service file
::

    vim /lib/systemd/system/docker.service

Find
::

    ...
    [Service]
    Type=notify
    # the default is not to use systemd for cgroups because the delegate issues still
    # exists and systemd currently does not support the cgroup feature set required
    # for containers run by docker
    ExecStart=/usr/bin/dockerd -H fd://
    ...
    
And change to
::

    ...
    [Service]
    Type=notify
    # the default is not to use systemd for cgroups because the delegate issues still
    # exists and systemd currently does not support the cgroup feature set required
    # for containers run by docker
    ExecStart=/usr/bin/dockerd -H fd:// --insecure-registry <registry-ip>:4000
    ...

6. Create the drop-in unit directory for docker.service
::

    mkdir -p /etc/systemd/system/docker.service.d

7. Create the drop-in unit file
::

    tee /etc/systemd/system/docker.service.d/kolla.conf <<-'EOF'
    [Service]
    MountFlags=shared
    EOF

8. Restart docker by executing the following commands
::

    systemctl daemon-reload
    systemctl restart docker

9. Uninstall lxd and lxc packages
::

    dpkg -l "*lx[c|d]*"
    
    #Removing lxd and lxc packages
    apt-get remove --purge <packages-name>

10. Install Dependencies
::

    apt-get install -y ntp
    
    #Install ansible and check if version higher than 2.0.0
    apt-get install -y ansible
    ansible --version
    
    #Install dependencies
    apt-get install python-dev libffi-dev libssl-dev gcc
    pip install -U python-openstackclient python-neutronclient
        
11. Disable *libvirt*
::

    systemctl stop libvirtd.service
    systemctl disable libvirtd.service
    
    # If these commands fail, run the command below and check if have something running, if not, just ignore it.
    systemctl | grep -i libvirt
    
12. Install Kolla and its dependencies
::

    #Clone Kolla
    git clone https://git.openstack.org/openstack/kolla
    cd kolla/
    git checkout tags/3.0.0.0rc1
    cd ..
    
    #Install Kolla requirements
    pip install -r kolla/test-requirements.txt -r kolla/requirements.txt
    
    #Install Kolla
    pip isntall kolla/
    
    #Copy the Kolla configuration files to /etc
    cd kolla/
    cp -r etc/kolla /etc/
    cd ..
    
13. Create a docker registry
::

    docker run -d -p 4000:5000 --restart=always --name registry registry:2
    
Building Container Images
-------------------------

1. Create kolla-build.conf file
::

    touch /etc/kolla/kolla-build.conf
    
2. Paste it within kolla-bluid.conf
::

    [DEFAULT]
    output_file = etc/kolla/kolla-build.conf
    namespace = kolla
    tag = 3.0.0
    
3. Building Container Images
::

    cd kolla/
    ./tools/build.py --base ubuntu --type source  --registry <registry-ip>:4000 --push --no-cache
    
Deploying Kolla
---------------

1. Generate passwords for /etc/kolla/passwords.yml
::

    kolla-genpwd

2. Configure globals.yml file
::

    vim /etc/kolla/globals.yml
    
    # Check and edit, if needed, these parameters:
    kolla_base_distro: "ubuntu"
    kolla_install_type: "source"
    kolla_internal_vip_address: "10.10.10.254"
    
    docker_registry: "<registry-ip:4000">
    
    network_interface: "enp0s3"
    neutron_external_interface: "enp0s8"
    
    enable_ceph: "yes"
    enable_cinder: "yes"
    enable_manila: "yes"

3. Configuring ceph
::

    # Modify the file /etc/kolla/config/ceph.conf and add the contents:
    
    [global]
    osd pool default size = 1
    osd pool default min size = 1

    # Configure /dev/sdb for usage with Kolla.
    parted /dev/sdb -s -- mklabel gpt mkpart KOLLA_CEPH_OSD_BOOTSTRAP 1 -1
    parted /dev/sdb print
    
4. Before running the deploy, check that the deployment targets are in a state where Kolla may deploy to them
::

    /tools/kolla-ansible prechecks
    
5. Run Kolla deploy
::

    /tools/kolla-ansible deploy
   

Useful tools
============
1. After successful deployment of OpenStack, run the following command can create an openrc file */etc/kolla/admin-openrc.sh* on the deploy node.
::

    /tools/kolla-ansible post-deploy
    
    source /etc/kolla/admin-openrc.sh

Configuring Network
===================
1. Creating a OpenStack public network and a subnet on the network
::

    # Network
    neutron net-create public --shared --provider:physical_network physnet1 --provider:network_type flat
    
    # Subnet
    neutron subnet-create --name public \
    --allocation-pool start=START_IP_ADDRESS,end=END_IP_ADDRESS \
    --dns-nameserver DNS_RESOLVER --gateway PUBLIC_NETWORK_GATEWAY \
    public PUBLIC_NETWORK_CIDR

2. Creating a OpenStack private network and a subnet on the network
::

    # Network
    neutron net-create private
    
    # Subnet
    neutron subnet-create --name private \
    --dns-nameserver DNS_RESOLVER --gateway PRIVATE_NETWORK_GATEWAY \
    private PRIVATE_NETWORK_CIDR
    
3. Create a router
::

    # Add the router: external option to the public network:
    neutron net-update public --router:external
    
    # Create the router:
    neutron router-create router
    
    # Add the private network subnet as an interface on the router:
    neutron router-interface-add router private
    
    # Set a gateway on the public network on the router:
    neutron router-gateway-set router public

Launch a instance
=================

1. Generate and add a key pair:
::

    ssh-keygen -q -N ""
    openstack keypair create --public-key ~/.ssh/id_rsa.pub mykey

2. Add rules to the default security group
::

    # Permit ICMP (ping):
    openstack security group rule create --proto icmp default
    
    # Permit secure shell (SSH) access:
    openstack security group rule create --proto tcp --dst-port 22 default

3. Get neutron network ids
::

    PUB_NET_ID=`neutron net-list | grep public | awk '{print $2}'`

    PUB_SUBNET_ID=`neutron net-list | grep public | awk '{print $6}'`

    PRIV_NET_ID=`neutron net-list | grep private | awk '{print $2}'`

    PRIV_SUBNET_ID=`neutron net-list | grep private | awk '{print $6}'`

4. Get manila service image and create a new flavor
::

    wget http://tarballs.openstack.org/manila-image-elements/images/manila-service-image-master.qcow2
    
    glance image-create --name "manila-service-image" \
    --file manila-service-image-master.qcow2 \
    --disk-format qcow2 --container-format bare \
    --visibility public --progress
    
    # Creating a new flavor
    nova flavor-create manila-service-flavor 100 512 5 1

5. Boot a public Instance
::

    nova boot --flavor manila-service-flavor \
    --image manila-service-image --nic net-id=$PUB_NET_ID \
    --security-group default --key-name mykey public-instance
    
    # Verify Operation
    nova list

6. Boot a private Instance
::

    nova boot --flavor manila-service-flavor \
    --image manila-service-image --nic net-id=$PRIV_NET_ID \
    --security-group default --key-name mykey private-instance
    
    # Verify Operation
    nova list

Create Manila Share
===================

1. Create a default share type
::

    manila type-create default_share_type True

2. Create a shared network
::

    manila share-network-create \
    --name mysharenetwork \
    --description "My Manila network" \
    --neutron-net-id $PRIV_NET_ID \
    --neutron-subnet-id $PRIV_SUBNET_ID

3. Create a NFS share using the share network
::

    manila create NFS 1 \
    --name myshare \
    --description "My Manila share" \
    --share-network mysharenetwork \
    --share-type default_share_type

Known Issues
============
1. In case of deploying using the VirtualBox or vSphere make sure that *neutron_external_interface* is in promisc mode
::

    #Open /etc/network/interfaces
    vim /etc/network/interfaces
    
    #Add these lines:
    auto <neutron_external_interface>
    iface <neutron_external_interface> inet manual
    up ifconfig <neutron_external_interface> promisc up
    down ifconfig <neutron_external_interface> promisc down
    
    #Restart network
    /etc/init.d/networking restart

2. In case of deploying using the _nested_ environment (eg. Using Virtualbox VM’s, KVM VM’s), if your compute node supports hardware acceleration for virtual machines.
::

    # Run the follow command in compute node
    egrep -c '(vmx|svm)' /proc/cpuinfo

If this command returns a value of **zero**, your compute node does not support hardware acceleration and you must configure libvirt to use **QEMU** instead of KVM.
::

    # Change the virt_type option in the [libvirt] section in nova.conf file inside the /etc/kolla/config/ directory.
    
    [libvirt]
    virt_type=qemu
