#!/bin/sh

# Properties
BASE_PATH=/opt/rh/amq-broker
SHARED_PATH=$BASE_PATH/shared
ARCHIVE_PATH=~/amq-broker-7.5.0-bin.zip
TMP_ARCHIVE_PATH=/tmp/amq-broker.zip
AMQ_ADMIN=amq-admin
AMQ_ADMIN_PASSWORD=X8c24EhZE@
AMQ_CLUSTER_USER=amq-cluster-user
AMQ_CLUSTER_USER_PASSWORD=H7qqU%er43

echo "Create the group for the amq-broker install"
sudo groupadd --system amq-broker

echo "Add current user to group"
sudo usermod -a -G amq-broker $(whoami)

echo "Create the user for the amq-broker install"
sudo useradd --system -d $BASE_PATH -g amq-broker amq-broker

echo "Create the shared directory and the base directory"
sudo mkdir -p $SHARED_PATH
echo "Change ownership of the base directory to amq-broker"
sudo chown -R amq-broker:amq-broker $BASE_PATH
echo "Change ownership default of newly created files to group"
sudo chmod +s $SHARED_PATH

echo "Copy the installation archive to the tmp directory to avoid permission issues"
cp $ARCHIVE_PATH $TMP_ARCHIVE_PATH

echo "Extract the installation archive to the shared path"
sudo su amq-broker -c "unzip $TMP_ARCHIVE_PATH -d $SHARED_PATH"

echo "Assign the install path"
INSTALL_PATH=$SHARED_PATH/$(ls $SHARED_PATH)

echo "Install a master intance of AMQ"
sudo su amq-broker -c "cd $BASE_PATH && $INSTALL_PATH/bin/artemis create master --user $AMQ_ADMIN --password $AMQ_ADMIN_PASSWORD --clustered --cluster-user $AMQ_CLUSTER_USER --cluster-password $AMQ_CLUSTER_USER_PASSWORD --host $(hostname) --name $(hostname)-master --replicated --require-login"

echo "Install a slave intance of AMQ"
sudo su amq-broker -c "cd $BASE_PATH && $INSTALL_PATH/bin/artemis create slave --user $AMQ_ADMIN --password $AMQ_ADMIN_PASSWORD --clustered --cluster-user $AMQ_CLUSTER_USER --cluster-password $AMQ_CLUSTER_USER_PASSWORD --host $(hostname) --name $(hostname)-slave --replicated --slave --port-offset=100 --require-login"

echo "Reset shell for local user to nologin to prevent logins"
sudo usermod -s /sbin/nologin amq-broker

echo "Apply selinux labels to master"
sudo semanage fcontext  -a -t bin_t  "$BASE_PATH/master/bin/artemis"
sudo semanage fcontext  -a -t bin_t  "$BASE_PATH/master/bin/artemis-service"
sudo restorecon -Rv $BASE_PATH/master

echo "Apply selinux labels to slave"
sudo semanage fcontext  -a -t bin_t  "$BASE_PATH/slave/bin/artemis"
sudo semanage fcontext  -a -t bin_t  "$BASE_PATH/slave/bin/artemis-service"
sudo restorecon -Rv $BASE_PATH/slave

echo "Create Systemd service files"
cat >"amq-broker-master.service" <<'EOF'
[Unit]
Description=AMQ Broker Master
After=syslog.target network.target

[Service]
ExecStart=/opt/rh/amq-broker/master/bin/artemis run
Restart=on-failure
User=amq-broker
Group=amq-broker
# A workaround for Java signal handling
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

cat >"amq-broker-slave.service" <<'EOF'
[Unit]
Description=AMQ Broker Slave
After=syslog.target network.target

[Service]
ExecStart=/opt/rh/amq-broker/slave/bin/artemis run
Restart=on-failure
User=amq-broker
Group=amq-broker
# A workaround for Java signal handling
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

echo "Copy Systemd service files to systemd services directory"
sudo cp *.service /etc/systemd/system/

echo "Change ownershipof  Systemd service files to root"
sudo chown -R root:root /etc/systemd/system

echo "Remove local systemd service file copies"
rm *.service

echo "Enable Systemd services"
sudo systemctl enable amq-broker-master.service
sudo systemctl enable amq-broker-slave.service

echo "Enable jolokia access to all host"
sudo sed -i 's/localhost\*/\*/g' $BASE_PATH/master/etc/jolokia-access.xml
sudo sed -i 's/localhost\*/\*/g' $BASE_PATH/slave/etc/jolokia-access.xml

echo "Change binding of web port to all IPs"
sudo sed -i 's/localhost/0.0.0.0/g' $BASE_PATH/master/etc/bootstrap.xml
sudo sed -i 's/localhost/0.0.0.0/g' $BASE_PATH/slave/etc/bootstrap.xml

echo "Start the services"
sudo systemctl start amq-broker-master
sudo systemctl start amq-broker-slave

echo "Grant access to instances to group members"
sudo find $BASE_PATH -type d -exec chmod g+x {} \;
sudo chmod g+wr -R $BASE_PATH

echo "Perform manual tasks"
echo "Assign group-name to Replication Master / Slave configuration in broker.xml"
echo "Change host name (if required) in connectors"
echo "Edit broker.xml to configure RBAC"
echo "Edit login.configuration to add JAAS modules"
