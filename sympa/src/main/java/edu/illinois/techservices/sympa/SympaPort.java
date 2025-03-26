package edu.illinois.techservices.sympa;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.soap.SOAPBinding;

// @WebService(targetNamespace = "https://lists-dev.techservices.illinois.edu/sympasoap")
@WebService(targetNamespace = "https://lists-dev.techservices.illinois.edu/lists/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface SympaPort {
    
    @WebMethod
    String createList(@WebParam(name = "listname") String listname);
    
    // @WebMethod
    // String closeList(@WebParam(name = "listname") String listname);
    
    // @WebMethod
    // String subscribe(@WebParam(name = "listname") String listname, 
    //                 @WebParam(name = "email") String email);

    // @WebMethod
    // String 
} 