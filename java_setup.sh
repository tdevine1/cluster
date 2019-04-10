sudo apt update
sudo apt install openjdk-7-jdk
apt-cache search jdk
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/bin
export PATH=$PATH:/usr/lib/jvm/java-7-openjdk-amd64/bin
javac -version
