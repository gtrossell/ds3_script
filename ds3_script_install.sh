echo "Installing ds3_script for you"

cd ~

sudo apt-get update 
sudo apt-get install git -y
sudo apt install  openjdk-8-jdk -y
sudo dpkg --purge --force-depends ca-certificates-java
sudo apt-get install ca-certificates-java

git clone https://github.com/SpectraLogic/ds3_script.git

cd ds3_script
./gradlew build -x test
unzip build/distributions/*.zip -d ~/
echo "ds3_script now installed!"
