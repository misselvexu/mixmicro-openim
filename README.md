# Acmedcare+ NewIM System

Acmedcare NewIM System , Support Some New Features

## RoadMap

> IM Server
- Protocol Support
    * [x] TCP
    * [x] R-UDP
    * [ ] WebSocket
    
- Develop Kit
    * [x] JDK SDK Kit
    * [x] Android SDK Kit
    * [ ] NodeJS SDK Kit
    
> Push Server
- Protocol Support
    * [ ] (R)UDP

- Develop Kit
    * [ ] JDK SDK Kit
    * [ ] Android SDK Kit


## Quick-start
> building

## Core Library

### Storage Component

#### MongoDB
Document storage database;
> Docker Running
```bash
  # pull images
  docker pull mongo
  
  # start container
  docker run -p 27017:27017 -v /acmedcare/data/mongo:/data/db --name docker_mongodb -d mongo
  
```

#### MySQL
The MySQL v1 component currently is only tested with MySQL 5.6-7. It is designed to be easy to understand, and get started with. For example, it deconstructs spans into columns, so you can perform ad-hoc queries using SQL. However, this component has known performance issues: queries will eventually take seconds to return if you put a lot of data into it.



### Running the server from source
> building


## Artifacts
> building