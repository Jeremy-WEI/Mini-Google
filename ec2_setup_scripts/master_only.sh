TOMCAT_HOME="/var/lib/tomcat7"

sudo service tomcat7 stop

# Remove directories for master                                                                         

sudo rm -r $TOMCAT_HOME/webapps/master/
sudo rm /home/ubuntu/crawler_db/logs/master.log

sudo cp $HOME/master.war $TOMCAT_HOME/webapps/master.war

sudo touch $TOMCAT_HOME/logs/master.log

sudo chown -R tomcat7:tomcat7 $TOMCAT_HOME/logs/master.log

sudo service tomcat7 start