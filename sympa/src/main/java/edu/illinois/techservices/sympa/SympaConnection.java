package edu.illinois.techservices.sympa;

import javax.xml.soap.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import javax.xml.namespace.QName;


public class SympaConnection {

  private static String sympaSoapUrl = "https://lists-dev.techservices.illinois.edu/sympasoap";
  private static String sessionCookie = null;
  static String userName = System.getenv("SYMPA_EMAIL");
  static String password = System.getenv("SYMPA_PASSWORD");

  public static void main(String[] args) {
    try {
      // Step 1: Authenticate & Retrieve Session Cookie
      if (authenticate(userName, password)) {
        System.out.println("Authentication successful. Session cookie stored.");

        // Step 2: Call Another Sympa SOAP Function Using Cookie
       // SOAPMessage response = callSympaSOAPMethod("info"); // Replace with actual method
        //System.out.println("SOAP Response:");
        //printSOAPMessage(response);
      } else {
        System.out.println("Authentication failed.");
      }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  /**
   * 
   * @param email
   * @param password
   * @return
   * @throws Exception
   */
  private static boolean authenticate(String email, String password) throws Exception {

    sessionCookie = login(email, password);
   //loginWitSessionFactory();
     System.out.println("COOKIEEE : " + sessionCookie);

   SOAPMessage soapMessage = createAuthSOAPRequest(email, password, sessionCookie, new String[]{"dummy"});
   
    // Send authentication request
    SOAPMessage response = sendSOAPRequest(soapMessage);

    printSOAPMessage(response) ;
    // Extract session cookie
    //sessionCookie = extractCookieFromResponse(response);

    //return sessionCookie != null;
    return true;
  }

  /**
   * 
   * @param methodName
   * @return
   * @throws Exception
   */
  private static SOAPMessage callSympaSOAPMethod(String methodName) throws Exception {
    SOAPMessage soapMessage = createMethodSOAPRequest(methodName);
    return sendSOAPRequest(soapMessage);
  }

  /**
   * 
   * @param soapMessage
   * @return
   * @throws Exception
   */
  private static SOAPMessage sendSOAPRequest(SOAPMessage soapMessage) throws Exception {
    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
    SOAPConnection soapConnection = soapConnectionFactory.createConnection();

    MimeHeaders headers = soapMessage.getMimeHeaders();
   
    // Add session cookie if available
    if (sessionCookie != null) {
      headers.addHeader("Cookie", "sympa_session=" + sessionCookie);
    }

    // Set the session cookie in the request headers
    /*Map<String, String> requestHeaders = new HashMap<>();
    requestHeaders.put("Cookie", sessionCookie);
    requestHeaders.put("email", userName);*/
    SOAPMessage soapResponse = soapConnection.call(soapMessage, new URL(sympaSoapUrl));
    System.out.println("SOAP RESPONSE BODY: " + soapResponse.getSOAPBody());
    soapConnection.close();
    
    return soapResponse;
  }

  /**
   * 
   * @param email
   * @param password
   * @param cookie
   * @param parameters
   * @return
   * @throws Exception
   */
  private static SOAPMessage createAuthSOAPRequest(String email, String password, String cookie, String[] parameters) throws Exception {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    String myNamespaceURI = "https://lists-dev.techservices.illinois.edu/lists/wsdl";
    SOAPEnvelope envelope = soapPart.getEnvelope();

    //Add namespaces
    envelope.addNamespaceDeclaration("ns", "urn:sympasoap");
    envelope.addNamespaceDeclaration("soapenc", "http://schema.xmlsoap.org/soap/encoding/");
    envelope.addNamespaceDeclaration("ns", "urn:sympasoap");
    envelope.addNamespaceDeclaration("targetNamespace", myNamespaceURI);
    envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
    envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    envelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapElement = soapBody.addChildElement("authenticateAndRun", "ns");

    //Add email
    SOAPElement param1 = soapElement.addChildElement("email", "ns");
    param1.addTextNode(email);

    //Add cookie
    SOAPElement param2 = soapElement.addChildElement("cookie", "ns");
    System.out.println("Cookie Session ID: " + cookie.substring(cookie.indexOf("=")+1, cookie.length()));
    param2.addTextNode(cookie.substring(cookie.indexOf("=")+1, cookie.length()).trim());
    
    //service can be "getUserEmailByCookie" OR "lists" OR "subscribe" OR "signoff" OR "add" OR "del"
    SOAPElement param3 = soapElement.addChildElement("service", "ns");
    param3.addTextNode("lists");

    SOAPElement paramsElement = soapElement.addChildElement("parameters", "ns");
    paramsElement.addAttribute(new QName("xsi:type"), "soapenc:Array");
    paramsElement.addAttribute(new QName("soapenc:arrayType"), "xsd:string[" + parameters.length + "]");

    if (parameters != null && parameters.length > 0) {
      for (String param : parameters) {
        paramsElement.addChildElement("item", "ns");
        paramsElement.addTextNode(param);
      }
    } else {
      paramsElement.addChildElement("item", "ns");
      paramsElement.addTextNode("");

    }

    soapMessage.saveChanges();
    

    return soapMessage;
  }

  private static SOAPMessage createMethodSOAPRequest(String methodName) throws Exception {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();

    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("ns", "urn:sympasoap");

    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapElement = soapBody.addChildElement(methodName, "ns");

    soapMessage.saveChanges();
    return soapMessage;
  }

  private static String extractCookieFromResponse(SOAPMessage soapResponse) throws Exception {
    Iterator<MimeHeader> headers = soapResponse.getMimeHeaders().getAllHeaders();
    while (headers.hasNext()) {
      MimeHeader header = headers.next();
      if (header.getName().equalsIgnoreCase("Set-Cookie")) {
        return header.getValue();
      }
    }
    return null;
  }

  private static void printSOAPMessage(SOAPMessage message) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    message.writeTo(out);
    System.out.println(new String(out.toByteArray()));
  }

  /**
   * Login to Sympa Server to get the cookie to pass it on to authenticateAndRun method.
   * @param email
   * @param password
   * @return
   */
  private static String login(String email, String password) {
    String sessionCookie = null;
    try {
      //String loginUrl = "https://lists-dev.techservices.illinois.edu/sympasoap";
      String loginUrl = "https://lists-dev.techservices.illinois.edu/lists/wsdl";
      // Create the HTTP connection to the login URL
      URL url = new URL(loginUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      // Build the form data (adjust according to the actual form parameters)
      String formData = "email=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");
      // String formData =  "email=" + email+ "&password=" + password;
      // Send the form data
      try (OutputStream os = connection.getOutputStream()) {
          byte[] input = formData.getBytes("utf-8");
          os.write(input, 0, input.length);
      }

      // Get the response code to check if login was successful
      int responseCode = connection.getResponseCode();
      System.out.println("Login Response Code: " + responseCode);   
     
      // Read the cookies from the response headers
      Map<String, List<String>> headers = connection.getHeaderFields();

      List<String> cookiesHeader = null;
      
      if (headers.containsKey("Set-Cookie2")){
        cookiesHeader = headers.get("Set-Cookie2");
      } 
      if (headers.containsKey("Set-Cookie")) {
        cookiesHeader = headers.get("Set-Cookie");
      }

      System.out.println("=========");
      System.out.println( connection.getHeaderFields());
      System.out.println("=========");
      if (cookiesHeader != null) {
        
        // Extract the session cookie from the "Set-Cookie" header
        for (String cookie : cookiesHeader) {
          System.out.println("Cookie: " + cookie);
          // Extract the session cookie (usually JSESSIONID or something similar)
          // You may need to parse and store the cookie for later requests
          if (cookie.contains("sympa_session") ) {
            sessionCookie = cookie.split(";")[0];  // Get the session cookie value
            sessionCookie = sessionCookie.substring(sessionCookie.indexOf("=")+1);

            System.out.println("Session Cookie: " + sessionCookie);
            break;
          } 
            
        }
      } else {
          System.out.println("No cookies found in the response.");
      }
    } catch(Exception e) {
      e.printStackTrace();
    }

    return sessionCookie;
  }

  /**
   * 
   * @return
   */
  private static SOAPMessage loginWitSessionFactory() {
    SOAPMessage response = null;
    try {

      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage();
      SOAPPart soapPart = soapMessage.getSOAPPart();
      String myNamespaceURI = "https://lists-dev.techservices.illinois.edu/lists/wsdl";
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.addNamespaceDeclaration("ns", "urn:sympasoap");
      envelope.addNamespaceDeclaration("soapenc", "http://schema.xmlsoap.org/soap/encoding/");
      envelope.addNamespaceDeclaration("ns", "urn:sympasoap");
      envelope.addNamespaceDeclaration("targetNamespace", myNamespaceURI);
      envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
      envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");

      envelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");

      SOAPBody soapBody = envelope.getBody();
      SOAPElement soapElement = soapBody.addChildElement("login", "ns");

      SOAPElement param1 = soapElement.addChildElement("email", "ns");
      param1.addTextNode(userName);

      SOAPElement param2 = soapElement.addChildElement("password", "ns");
      param2.addTextNode(password);

      soapMessage.saveChanges();
      //response = sendSOAPRequest(soapMessage);
      

      System.out.println("Printing the response......");
      printSOAPMessage(response);
    } catch(Exception e) {

    }
    return response;
  }
}


