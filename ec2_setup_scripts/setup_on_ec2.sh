TOMCAT_HOME="/var/lib/tomcat7"
HOME="/home/ubuntu"

sudo mkdir $HOME/crawler_db
sudo mkdir $HOME/crawler_db/crawled_files
sudo mkdir $HOME/crawler_db/crawled_urls
sudo mkdir $HOME/crawler_db/document_meta
sudo mkdir $HOME/crawler_db/logs

sudo cp $HOME/ec2_setup_scripts/setenv.sh /usr/share/tomcat7/bin/setenv.sh

sudo chown -R tomcat7:tomcat7 $HOME/crawler_db
sudo chown -R ubuntu:ubuntu $HOME/crawler_db/crawled_urls
sudo chown -R ubuntu:ubuntu $HOME/crawler_db/document_meta