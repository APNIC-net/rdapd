# Using RPSL data

The following commands will convert a file containing a list of RPSL
objects into a MySQL database, suitable for use with `rdapd`, and then
run `rdapd` using that database:

    $ docker run -p3306:3306 --name rdap-mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:latest
    $ perl bin/object-stream-to-mysql.pl < {rpsl-data} > sql-data
    $ mysql -h127.0.0.1 -uroot -ppassword -e "CREATE DATABASE rpsl_rdap"
    $ mysql -h127.0.0.1 -uroot -ppassword -e "GRANT ALL ON *.* TO 'root'@'%'"
    $ mysql -h127.0.0.1 -uroot -ppassword rpsl_rdap < sql-data
    $ java -Ddatabase.database=rpsl_rdap -Ddatabase.passsord=password -jar target/rdapd-{version}.jar &
