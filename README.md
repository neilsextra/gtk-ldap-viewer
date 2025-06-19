# gtk-ldap-viewer

GTK Sample LDAP Viewer 

## To build  

`mvn clean dependency:copy-dependencies compile package install`

## To Run 
`java -jar .\target\navigator-1.0-SNAPSHOT.jar`

## Example Connection String

`ldap://<user-dn>@<hostname>:389`

Example
`ldap://cn=read-only-admin,dc=example,dc=com:@ldap.forumsys.com:389`

## Note:
Password must be supplied separately within the password field.
It is 'password' for this site.

Queries
`ou=mathematicians,dc=example,dc=com`
`ou=scientists,dc=example,dc=com`
`dc=example,dc=com`
