# Clearing House - INESC TEC  

Repository holding software that implements the IDS Clearing House.  

# Unit and integration Tests  
  
Launch a local Mongo instance (use the docker/mongo/docker-compose.yml on a local WSL instance).  
Launch the application server (use the run.md script).  
Run the command 'mvnw clean test' on the command line.  


When cloning the git repository into the local repository, files 'src/main/resources/conf/vcese19.inesctec.pt.p12' and 'src/main/resources/conf/truststore.p12' need to be recreated.