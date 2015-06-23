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

  boolean keepAlive
  Connection connection

  Integer retries = 5
  Long retryInterval = 1000 // miliseconds


  SshResponse execute( String cmd ) throws InterruptedException {
    SshResponse response
    connection = getConnection()
    Session session
    try{
      session = connection.openSession()
    }catch( e ){
      log.error "error connecting, retrying", e
      connection = getConnection( true )
      session = connection.openSession()
    }
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
      if (!keepAlive) {
        connection.close()
        this.connection = null
      }
    }
  }

  void getFile( String sourcePath, String targetPath ){
    def connection = getConnection()
    SCPClient scp
    try{
      scp = connection.createSCPClient()
    }catch( e ){
      log.error "error connecting, retrying", e
      connection = getConnection( true )
      scp = connection.createSCPClient()
    }
    if ( LOG.debugEnabled )
      LOG.debug "Getting file from $host $sourcePath to local $targetPath"
    try{
      scp.get( sourcePath, targetPath )
    }finally {
      if (!keepAlive) {
        connection.close()
        this.connection = null
      }
    }
  }

  void putFile(  String sourcePath, String targetPath  ){
    def connection = getConnection()
    SCPClient scp
    try{
      scp = connection.createSCPClient()
    }catch( e ){
      log.error "error connecting, retrying", e
      connection = getConnection( true )
      scp = connection.createSCPClient()
    }
    if ( LOG.debugEnabled )
      LOG.debug "Putting file local $sourcePath to $host $targetPath"
    try{
      scp.put( sourcePath, targetPath )
    }finally {
      if (!keepAlive) {
        connection.close()
        this.connection = null
      }
    }
  }

  void putFile(  byte[] data, String name, String targetPath  ){
    def connection = getConnection()
    SCPClient scp
    try{
      scp = connection.createSCPClient()
    }catch( e ){
      log.error "error connecting, retrying", e
      connection = getConnection( true )
      scp = connection.createSCPClient()
    }
    if ( LOG.debugEnabled )
      LOG.debug "Putting file to $host $targetPath / $name"
    try{
      scp.put( data, name, targetPath )
    }finally {
      if (!keepAlive) {
        connection.close()
        this.connection = null
      }
    }
  }

  private getConnection( boolean force = false ){
    if (connection && !force) return connection

    File keyfile = sshkey? new File( sshkey ) : null
    Connection connection
    for ( int i = 0 ; i < retries ; i++ ){
      try{
        LOG.info "Connecting attempt $i / $retries"
        connection = new Connection( host, port ?: 22 )
        connection.connect()
      }catch( e ){
        LOG.warn "Error connecting to $host $port"
        if ( i < retries ) Thread.sleep( retryInterval )
        else throw e
      }
    }
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
