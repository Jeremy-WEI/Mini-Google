TOMCAT_HOME="/var/lib/tomcat7"

sudo service tomcat7 stop

sudo rm -r $TOMCAT_HOME/webapps/worker/

sudo rm /home/ubuntu/crawler_db/logs/worker.log

sudo cp $HOME/worker.war $TOMCAT_HOME/webapps/worker.war

sudo touch $TOMCAT_HOME/logs/worker.log

sudo chown -R tomcat7:tomcat7 $TOMCAT_HOME/logs/worker.log

sleep 10

sudo service tomcat7 start