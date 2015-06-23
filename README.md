sshPlugin
=========

A grails plugin to perform ssh and scp. Based on http://www.ganymed.ethz.ch/ssh2/

Instantiate the sshClient:
```
sshClient client = new SshClient( host: "0.0.0.0", user: "root", port: 22, password: "changeme" )
```
If password is provided password will be used to auth, if not and sshkey provided, sshkey will be used:
```
sshClient client = new SshClient( host: "0.0.0.0", user: "root", port: 22, sshkey: "~/.ssh/id_rsa", sshkeypass:"" )
```
Also the client can be configured with 
* keepAlive (boolean) : will keep the connection alive betweeen calls to either execute() or put/get
* retries ( int ) : amount of times the client will try to connect
* retryInterval ( int ): miliseconds between retries

Once the client is configured, we can call execute, get or put
```
SshResponse resp = client.execute("echo Hola")
println "STDOUT: ${resp.out}"
println "STDERROR: ${resp.error}
println "EXIT STATUS ${resp.exit}"

client.getFile("~/remotefile", "~/localFile")

client.putFile("~/localFile", "~/remotefile")
client.putFile( "hello world" as byte[], "myfilename", "/tmp/"  )
```
