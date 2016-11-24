Vagrant.configure("2") do |config|
  config.ssh.forward_agent = true
  config.vm.box = "boxcutter/ubuntu1604"
  config.vm.provider "parallels" do |v|
    v.memory = 1024
    v.cpus = 2
  end
  config.vm.provider "virtualbox" do |v|
    v.memory = 1024
    v.cpus = 2
  end
  config.vm.provision :shell, path: "bootstrap.sh"
  config.vm.synced_folder "/Users/christian/.m2", "/home/vagrant/.m2"
end
