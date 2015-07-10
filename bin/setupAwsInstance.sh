#!/bin/bash

#Install UK language pack
sudo apt-get install language-pack-en
sudo locale-gen en_GB.UTF-8

# Add the jdk8 repo and install
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default

[[ ! -d Downloads ]] && mkdir Downloads

# Install zookeeper
cd ~/Downloads
wget http://mirrors.maychuviet.vn/apache/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
tar -xvf zookeeper-3.4.6.tar.gz
cd zookeeper-3.4.6/
cp conf/zoo_sample.cfg conf/zoo.cfg
(bin/zkServer.sh start) &

# Install kafka
cd ~/Downloads
wget http://mirrors.maychuviet.vn/apache/kafka/0.8.2.1/kafka_2.11-0.8.2.1.tgz
tar -xvzf kafka_2.11-0.8.2.1.tgz
cd kafka_2.11-0.8.2.1/
(bin/kafka-server-start.sh config/server.properties) &

# Install postgres
sudo apt-get install postgresql
