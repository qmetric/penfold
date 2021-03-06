# Penfold

[![Build Status](https://travis-ci.org/qmetric/penfold.png)](https://travis-ci.org/qmetric/penfold)

Penfold is responsible for managing queues of tasks. A task contains a payload of any valid JSON.

The primary purposes that penfold was built for:

* messaging
* job scheduling

Penfold is deployed as a standalone server, or mulitple standalone servers for a clustered environment.

Penfold is spoken to via a Restful API, based on the media type [HAL+JSON](http://stateless.co/hal_specification.html).

## Documentation

[Refer to Wiki](https://github.com/qmetric/penfold/wiki)


## Quick start

### Installation and configuration

Prerequisites:

* [JVM](https://www.java.com/en/download/) 8+
* [Postgres database server](http://www.postgresql.org/)

1.
Download the latest penfold JAR file from [Maven Central](http://search.maven.org/). The JAR can be found under "com.qmetric.penfold"

2.
Create a new empty database on your Postgres database server

3.
Create a configuration file named "penfold.conf", and populate with:

```
penfold {

  publicUrl = "http://localhost:8080"

  httpPort = 8080
  
  authentication {
    username = usr
    password = pswd
  }

  database {
    url = "jdbc:postgresql://<HOST:<PORT>/<NAME_OF_EMPTY_DATABASE>"
    username = <USERNAME>
    password = <PASSWORD>
  }
}
```

4.
Start penfold

```
java -Dconfig.file=<CONFIG_FILE_PATH>/penfold.conf -jar penfold.jar
```

5.
Check if penfold server is running ok

```
GET: /healthcheck  HTTP 1.1
```

If penfold is healthy, then expect to receive a response with a 200 HTTP status code


### Quick play with API

A task has a status:
* waiting - task has been scheduled in the future and is waiting to become ready in its assigned queue
* ready - task is available for starting
* started - the task has been started
* cancelled - the task has been cancelled
* closed - the task has been closed

You can view all tasks by queue and status. For the purpose of this tutorial we will use a queue named "greenback".

```
GET: /queues/greenback/waiting  HTTP 1.1
GET: /queues/greenback/ready  HTTP 1.1
GET: /queues/greenback/started  HTTP 1.1
GET: /queues/greenback/cancelled  HTTP 1.1
GET: /queues/greenback/closed  HTTP 1.1
```

At this point, each of the above requests should not respond with any tasks.

Lets create a new task. Post the following data, replacing the "triggerDate" with a date a few minutes into the future:

```
POST: /tasks  HTTP 1.1

Content-Type: application/json;domain-command=CreateFutureTask
    
{
    "queue": "greenback",
    "triggerDate": "yyyy-MM-dd HH:mm:ss",
    "payload": {
        "customer": { 
            "id": 1,
            "name" : "bob",
            "email": "bob@email.com"
        }
    }
}
```

You should see a response similar to below. The response lists the attributes of your newly created task, including an auto generated unique task ID.
Notice, at this point, the "status" of the task is "waiting".

The links section of the response lists what actions and views are available for this task:
* self - link to this task resource
* UpdateTaskPayload - link where requests should be sent to make changes to the task payload (POST)
* CloseTask - link where requests should be sent to close the task (POST)

```
201 Created
Content-Type: application/hal+json

{
    "_links": {
        "self": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        },
        "CloseTask": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/1"
        },
        "UpdateTaskPayload": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/1"
        }
    },
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "payload": {
        "customer": {
            "id": 1,
            "name": "bob",
            "email": "bob@email.com"
        }
    },
    "queue": "greenback",
    "status": "waiting",
    "triggerDate": "2014-07-11 16:05:00",
    "version": 1
}
```

Like before, send a GET request to view your task in the queue with "waiting" status.

```
GET: /queues/greenback/waiting  HTTP 1.1
```

Send another request after your task's "triggerDate" has passed to view the task as being ready.

```
GET: /queues/greenback/ready  HTTP 1.1
```

When your task is "ready", then you should see a response similar to below.

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/queues/greenback/ready"
        }
    },
    "id": "greenback",
    "_embedded": {
        "tasks": [
          {
            "_links": {
                "self": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
                },
                "CloseTask": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/2"
                },
                "StartTask": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/2"
                },
                "UpdateTaskPayload": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/2"
                }
            },
            "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
            "payload": {
                "customer": {
                    "id": 1,
                    "name": "bob",
                    "email": "bob@email.com"
                }
            },
            "queue": "greenback",
            "status": "ready",
            "triggerDate": "2014-07-11 16:05:00",
            "version": 2
          }
        ]
    }
}
```

Notice the new available action link "StartTask". A task can only be started when it's "ready".

Lets start the task by sending a POST to the action link.
For any task command (such as creating or starting a task), the Content-Type header MUST always include the command type (the command type is stored as the action link relation name).
In this example, since a "StartTask" command has no mandatory properties the command represented in the body of the request is an empty JSON object.

```
POST: /tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/2  HTTP 1.1

Content-Type: application/json;domain-command=StartTask

{}
```


The task has now been started, you will notice in the POST response that the task's status has changed to "started".

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        },
        "CloseTask": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/3"
        },
        "UpdateTaskPayload": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/3"
        }
    },
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "payload": {
        "customer": {
            "id": 1,
            "name": "bob",
            "email": "bob@email.com"
        }
    },
    "queue": "greenback",
    "status": "started",
    "triggerDate": "2014-07-11 16:05:00",
    "version": 3
}
```

Finally, assuming we've done whatever we wanted to do to the task, lets tell Penfold we're done with it and close it (see "CloseTask" action link).
Notice the updated Content-Type header in the request below.

```
POST: /tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/3  HTTP 1.1

Content-Type: application/json;domain-command=CloseTask
    
{}
```

The task should now be closed.

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        }
    },
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "payload": {
        "customer": {
            "id": 1,
            "name": "bob",
            "email": "bob@email.com"
        }
    },
    "queue": "greenback",
    "status": "closed",
    "triggerDate": "2014-07-11 16:05:00",
    "version": 4
}
```


