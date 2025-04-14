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

  lists:
  curl https://lists-dev.techservices.illinois.edu/sympasoap \
    -H "Content-Type: text/xml; charset=utf-8" \
    -H "SOAPAction: \"urn:sympasoap#lists\"" \
    -b cookie.txt \
    -d @sympa-list.xml \
    -v
