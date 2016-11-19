FROM maven:3.3.9-jdk-8
CMD ["mvn", "clean", "install"]
#VOLUME /root/project
WORKDIR /root/project
