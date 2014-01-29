package grails.plugin.ssh

class SshResponse {
  String out
  String error
  Integer exit

  String toString(){
    """exit: $exit
       out: $out
       error: $error"""
  }
}
