package grails.plugin.ssh

import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.SCPClient
import ch.ethz.ssh2.Session
import ch.ethz.ssh2.StreamGobbler

class SshClient{

  private static final LOG = org.apache.log4j.Logger.getLogger( SshClient.class )
  String host
  String user
  Integer port = 22
  String password
  String sshkey
  String sshkeypass


  SshResponse execute( String cmd ) throws InterruptedException {
    SshResponse response
    def connection = getConnection()
    Session session = connection.openSession()
    LOG.info "Executing in $host [ $cmd ]"
    try{
      session.execCommand("$cmd")
      StreamGobbler stdout = new StreamGobbler( session.stdout )
      StreamGobbler sterror = new StreamGobbler( session.stderr )
      def out = stdout.text
      def error = sterror.text
      response = new SshResponse( out: out ? out[0..-2] : '', error: error ? error[0..-2] : '', exit: session.exitStatus )
      return response
    } finally {
      session.close()
      connection.close()
    }
  }

  void getFile( String sourcePath, String targetPath ){
    def connection = getConnection()
    SCPClient scp = connection.createSCPClient()
    if ( LOG.debugEnabled )
      LOG.debug "Getting file from $host $sourcePath to local $targetPath"
    try{
      scp.get( sourcePath, targetPath )
    }finally {
      connection.close()
    }
  }

  void putFile(  String sourcePath, String targetPath  ){
    def connection = getConnection()
    SCPClient scp = connection.createSCPClient()
    if ( LOG.debugEnabled )
      LOG.debug "Putting file local $sourcePath to $host $targetPath"
    try{
      scp.put( sourcePath, targetPath )
    }finally {
      connection.close()
    }
  }

  private getConnection(){
    File keyfile = sshkey? new File( sshkey ) : null
    Connection connection = new Connection( host, port ?: 22 )
    connection.connect()
    boolean authenticated = false
    if ( keyfile ) { //keyfile always preferred
      if ( LOG.debugEnabled )
        LOG.debug "Trying to open ssh session to $user @ $host : $port via keyfile $keyfile"
      authenticated = connection.authenticateWithPublicKey( user,
              keyfile, sshkeypass )
    }
    if ( !authenticated ) {
      if ( LOG.debugEnabled )
        LOG.debug "Trying to open ssh session to $user @ $host : $port via password"
      authenticated = connection.authenticateWithPassword( user, password )
    }
    if ( !authenticated )
      throw new IOException( "Authentication failed." )
    LOG.info "Connected to $user @ $host : $port "
    return connection
  }

}
