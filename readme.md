## Docker run 
- Build jar file
<pre>./gradlew bootJar</pre>
- Create the image named  `ktmp/proxy`
<pre>docker build -t ktmp/proxy .
</pre>

- Create a volume for mounting : 
<pre>docker volume create --name=ktmp-proxy-config-path
                </pre>
- Display detailed information about the volume
<pre>docker volume inspect ktmp-proxy-config-path
</pre>            
- Copy resources files `(.yml)` to the mounted folder 

- Run the container from our image:
<pre>docker run \
  --name ktmp-proxy \
  --mount source=ktmp-proxy-config-path,target=/var/lib/ktmp/config \
  ktmp/proxy</pre>
  
    - source : the created volume name 
    - target : the path of the volume within the Dockerfile   `