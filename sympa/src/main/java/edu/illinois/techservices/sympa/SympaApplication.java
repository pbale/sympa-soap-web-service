package edu.illinois.techservices.sympa;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
// import javax.xml.ws.Service;
// import com.sun.xml.ws.client.ClientTransportException;
// import javax.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.BindingProvider;
import edu.illinois.techservices.sympa.SympaPort;
// import jakarta.xml.ws.handler.MessageContext;

@SpringBootApplication
public class SympaApplication {
	public static void main(String[] args) {
		// SpringApplication.run(SympaApplication.class, args);
		String wsdlURLStr = "https://lists-dev.techservices.illinois.edu/lists/wsdl";
        URL wsdlURL;
        try {
            System.out.println("Fetching and examining WSDL...");
            wsdlURL = new URL(wsdlURLStr);
        } catch (Exception e) {
            System.err.println("Error examining WSDL: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        String targetNameStr = "https://lists-dev.techservices.illinois.edu/lists/wsdl";
        
        // QName: TargetNamespace and Service Name from WSDL
        QName qname = new QName(targetNameStr, "SympaSOAP");
        // Create a Service instance
        Service service = Service.create(wsdlURL, qname);

        // Get the Port (Generated Interface)

        // // Create the service directly
        // Service service = Service.create(wsdlURL);  // This won't work - needs QName argument
        // Get the Port and cast to BindingProvider
        SympaPort port = service.getPort(SympaPort.class);
        
        // [DEBUG] Test url print first 10 lines
        // try {
        //     System.out.println("\nTesting direct access to SOAP endpoint...");
        //     URL soapURL = new URL("https://lists-dev.techservices.illinois.edu/lists/sympasoap");
        //     try (java.io.BufferedReader reader = new java.io.BufferedReader(
        //             new java.io.InputStreamReader(soapURL.openStream()))) {
        //         String line;
        //         int count = 0;
        //         while ((line = reader.readLine()) != null && count < 10) {
        //             System.out.println(line);
        //             count++;
        //         }
        //     }
        // } catch (Exception e) {
        //     System.err.println("Error testing SOAP endpoint: " + e.getMessage());
        // }
        
        // Configure the endpoint address
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
            "https://lists-dev.techservices.illinois.edu/lists/sympasoap"
        );
        System.out.println("Bound provider");

        // NOTE/WIP: make sure ssl is working properly (it seems to be, but leaving this code just in case.
        // if its a problem, make sure to set certs etc)
        // service = getWebServicePort(getWSDLLocation());
        // BindingProvider bindingProvider = (BindingProvider) service;
        // bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory",
        //         getCustomSocketFactory());

        String email = System.getenv("SYMPA_EMAIL");
        String password = System.getenv("SYMPA_PASSWORD");

        // Call the SOAP method
        String loginRes;
        String res;
        try {
            loginRes = port.login(email, password);
            res = port.createList( "TestListName01");
        
            // TODO: ensure proper err capturing/formatting so we can see soap faults/details on response
            // may involve using different library for soap service
        } catch (SOAPFaultException e) {
        // } catch (jakarta.xml.ws.soap.SOAPFaultException e) {
            System.err.println("SOAP Fault Details:");
            // System.err.println("  Fault Code: " + e.getFault().getFaultCodeAsQName());
            // System.err.println("  Fault String: " + e.getFault().getFaultString());
            // if (e.detail != null) {
            if (e.getFault().getDetail() != null) {
                System.err.println("  Fault Detail: " + e.getFault().getDetail().getTextContent());
            }
            //     System.out.println(e);

            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("Non-SOAP Error occurred: " + e.getMessage());
            System.err.println("Detailed error: " + e.toString());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().toString() : "No cause"));
            // System.out.println(e);
            e.printStackTrace();
            return;
        }
        System.out.println("Response from SOAP Service: " + res);
	}
}