How to Log in copy paste and run each command with your corresponding docker/oracle login info

docker exec -it oracle19c bash

sqlplus kaiadams/password123@localhost:1521/ORCL


If you want to add files to the docker image use from folder where file is stored:

docker cp {name of file} oracle19c:/{name of file}
