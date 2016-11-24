Vagrant.configure("2") do |config|
  config.ssh.forward_agent = true
  config.vm.box = "ubuntu/xenial64"
  config.vm.provision :shell, path: "bootstrap.sh"
  config.vm.synced_folder "/Users/christian/.m2", "/home/ubuntu/.m2"
end
