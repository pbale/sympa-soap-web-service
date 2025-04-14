# sympa-soap-web-service
Example to convert wsdl files to java stubs.

# Curl Calls

login:
curl https://lists-dev.techservices.illinois.edu/sympasoap \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: \"urn:sympasoap#login\"" \
  -c cookie.txt \
  -d @sympa-login.xml \
  -v

login, save headers in headers.txt and response in login-response.xml
curl https://lists-dev.techservices.illinois.edu/sympasoap \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: \"urn:sympasoap#login\"" \
  -d @sympa-login.xml \
  -D headers.txt \
  -o login-response.xml \
  -v

  which: (set the session cookie from login response)
  curl https://lists-dev.techservices.illinois.edu/sympasoap \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: \"urn:sympasoap#which\"" \
  -H "Cookie: sympa_session={change me}" \
  -d @sympa-which.xml \
  -v


  lists:
  curl https://lists-dev.techservices.illinois.edu/sympasoap \
    -H "Content-Type: text/xml; charset=utf-8" \
    -H "SOAPAction: \"urn:sympasoap#lists\"" \
    -b cookie.txt \
    -d @sympa-list.xml \
    -v
