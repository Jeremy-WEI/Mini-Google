TOMCAT_HOME="/var/lib/tomcat7"

sudo service tomcat7 stop

# Remove directories for master                                                                         

sudo rm -r $TOMCAT_HOME/webapps/master/
sudo rm -r $TOMCAT_HOME/webapps/worker/
sudo rm /home/ubuntu/crawler_db/logs/master.log
sudo rm /home/ubuntu/crawler_db/logs/worker.log
sudo rm $TOMCAT_HOME/logs/catalina.out

sudo cp $HOME/master.war $TOMCAT_HOME/webapps/master.war

sudo cp $HOME/worker.war $TOMCAT_HOME/webapps/worker.war

sudo touch $TOMCAT_HOME/logs/master.log
sudo touch $TOMCAT_HOME/logs/worker.log

sudo chown -R tomcat7:tomcat7 $TOMCAT_HOME/logs/master.log
sudo chown -R tomcat7:tomcat7 $TOMCAT_HOME/logs/worker.log

sudo service tomcat7 start