*Valhalla instance-handling*

This project is a mesos framework for distributing Valhalla unreal servers across a Mesos cluster.

**Building**

    mvn clean package docker:build
    
This will create an instance-handling docker image
    
**Distributing**

    mvn clean package docker:build -DpushImage
    
See https://github.com/spotify/docker-maven-plugin for more instructions

**Running**
We usually store a properties file at /opt/etc so we map the /opt volume to inside the container.
We also run with --net:host because this service uses the hosts postgres service

    docker run --name instance-handling --volume /opt:/opt --net=host saiaku/instance-handling /opt/etc/instance-handling.properties