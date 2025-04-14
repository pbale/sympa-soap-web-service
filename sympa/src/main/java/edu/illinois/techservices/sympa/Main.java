package edu.illinois.techservices.sympa;

public class Main {
  
  public static void main(String[] args) {
    String sessionCookie = null;
    try {
      sessionCookie = SympaClient.loginSympa();

      if (sessionCookie != null) {
        // Get the Information
        //SympaClient.getInfo(sessionCookie);

        //Get Lists
        SympaClient.getLists(sessionCookie);
      }
      
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    
     
  }
}
