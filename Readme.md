## Description

API that accepts a file (possibly very large file) and responds with a list of SHA256 hashes (hexstrings) of 256 KB chunks of that file.
It is implemented with Scala and Akka Http/Streams.


## Building

One can build a docker image with the command

    sbt docker:publishLocal


## Running
An existing image resides at https://hub.docker.com/repository/docker/dimaopen/file-chunks.

To get and run it use the following commands

    docker pull dimaopen/file-chunks:0.0.1
    docker run -p 8080:8080 dimaopen/file-chunks:0.0.1

The GUI is located at http://localhost:8080/upload_test.html

You need to сlick on the Browse button choose a file, then click on the Upload file button.