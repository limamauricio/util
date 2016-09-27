Build and Deploy Kolla
======================

* Guide to build and deploy Kolla Newton rc1

Requirements
------------

* 2 (or more) network interfaces.
* At least 8gb main memory
* 80gb disk space.
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
    
    # If after run these commands fail, run the command below and check if have something running, if not, just ignore it.
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

3. Before running the deploy, check that the deployment targets are in a state where Kolla may deploy to them
::

    /tools/kolla-ansible prechecks
    
4. Run Kolla deploy
::

    /tools/kolla-ansible deploy
   

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
