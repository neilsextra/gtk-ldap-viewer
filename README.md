# gtk-ldap-viewer

GTK Sample LDAP Viewer 

## To build  

`mvn clean dependency:copy-dependencies compile package install`

## To Run 
`java -jar .\target\navigator-1.0-SNAPSHOT.jar`

## Example Connection String

`ldap://<user-dn>:<password>@<hostname>:389`

Example
`ldap://cn=read-only-admin,dc=example,dc=com:password@ldap.forumsys.com:389`

Queries
`ou=mathematicians,dc=example,dc=com`
`ou=scientists,dc=example,dc=com`
`dc=example,dc=com`
