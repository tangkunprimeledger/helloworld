package com.higgs.trust.slave.common.util;

import com.higgs.trust.common.utils.SignUtils;
import org.testng.annotations.Test;

import static com.higgs.trust.common.utils.SignUtils.verify;
import static org.junit.Assert.assertEquals;

/**
 * @author yangjiyun
 * @Description: ${todo}
 * @date 2018/1/12 19:20
 */
public class SignUtilsTest {

    String privateKey =
        "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOStfqEjMYETT3XwCFhgmTI59l5IpS4OpHPxwStlG5lZM5LMS4C/33w7lrz2eBrejYv1rqQKKBqA4XVTtVPgYerZ3rkTIYl1ugxw1YFYVfpvn4d0z6DfKmwMYlkrOgQAAc41zdN6ULAI1qDkWgnDr1BhR1AFnGe7212jWLFy94gPAgMBAAECgYEAyhNKWCiaYV5oFGcVaMuL9Odlf7GgTc/goRicQ7WoKt25hlqDyfVEDys1LCx8u/m4iVqEhi73e1wyX5SmIsWgn9UEQL3V8aAeGOL9jgTB8Oqd9LC6TYqLcMhcFMGZZuvb29pImWRnXaiO0fE70hV592hXym/HRdx7KWjbGtFHwtkCQQD8NUV35r7hexdQobnPK9rfs+5yoJ1Z1d1s6bJD7cC8E/XG+ZAyrD6qj8/pTlEqG7cv3z8i30GnIv7mMzX70+A7AkEA6B2oISORnOFXfjtH5iuh0Pwuh9/+Oc/WPTXWNTdMfnlhvnGDEQm1xlBIXOAPJWTzP7kvpjqdf/Ef1y9S1wmuPQJBAMenwFO35joHwKBDNx3rQLzA6y3xOj+Iz15N7qJZz67UOkgG/oEu2/kYGeY+6n/kKvOJzqhjhsyyJonD1qOByksCQHbX8GR/RKfRTK3KBmcYAfLxm5VgH1dUTbnrXbDDjnvXBC+xCM1pblOKeXJsGfUoec8vrvDqZAfE2mufxEdronUCQBpCxGUF+DJcZcYjbpV8jnC4L8cjl8YqsvvvKqpyYtoLVqanKOAhiO+uXYCIjTEYse0rL0DP/eISXWdqS5/Nq+U=";
    String wrongPrivateKey =
        "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOStfqEjMYETT3XwCFhgmTI59l5IpS4OpHPxwStlG5lZM5LMS4C/33w7lrz2eBrejYv1rqQKKBqA4XVTtVPgYerZ3rkTIYl1ugxw1YFYVfpvn4d0z6DfKmwMYlkrOgQAAc41zdN6ULAI1qDkWgnDr1BhR1AFnGe7212jWLFy94gPAgMBAAECgYEAyhNKWCiaYV5oFGcVaMuL9Odlf7GgTc/goRicQ7WoKt25hlqDyfVEDys1LCx8u/m4iVqEhi73e1wyX5SmIsWgn9UEQL3V8aAeGOL9jgTB8Oqd9LC6TYqLcMhcFMGZZuvb29pImWRnXaiO0fE70hV592hXym/HRdx7KWjbGtFHwtkCQQD8NUV35r7hexdQobnPK9rfs+5yoJ1Z1d1s6bJD7cC8E/XG+ZAyrD6qj8/pTlEqG7cv3z8i30GnIv7mMzX70+A7AkEA6B2oISORnOFXfjtH5iuh0Pwuh9/+Oc/WPTXWNTdMfnlhvnGDEQm1xlBIXOAPJWTzP7kvpjqdf/Ef1y9S1wmuPQJBAMenwFO35joHwKBDNx3rQLzA6y3xOj+Iz15N7qJZz67UOkgG/oEu2/kYGeY+6n/kKvOJzqhjhsyyJonD1qOByksCQHbX8GR/RKfRTK3KBmcYAfLxm5VgH1dUTbnrXbDDjnvXBC+xCM1pblOKeXJsGfUoec8vrvDqZAfE2mufxEdronUCQBpCxGUF+QJcZcYjbpV8jnC4L8cjl8YqsvvvKqpyYtoLVqanKOAhiO+uXYCIjTEYse0rL0DP/eISXWdqS5/Nq+UR";

    String pubKey =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDkrX6hIzGBE0918AhYYJkyOfZeSKUuDqRz8cErZRuZWTOSzEuAv998O5a89nga3o2L9a6kCigagOF1U7VT4GHq2d65EyGJdboMcNWBWFX6b5+HdM+g3ypsDGJZKzoEAAHONc3TelCwCNag5FoJw69QYUdQBZxnu9tdo1ixcveIDwIDAQAB";

    String dataStrToSign = "helloworld";

    @Test public void signAndVerifySuccess() throws Exception {
        String signature = SignUtils.sign(dataStrToSign, privateKey);
        verify(dataStrToSign, signature, pubKey);
    }

    @Test public void signAndVerifyFail() throws Exception {
        String signature = SignUtils.sign(dataStrToSign, wrongPrivateKey);
        verify(dataStrToSign, signature, pubKey);
    }

    @Test public void signatureSign() throws Exception {
        String signature = SignUtils.sign(dataStrToSign, privateKey);
        System.out.println(signature);
        String signatureExpect =
            "Z0zjEPH+PTZFrAT9g1TqOpwBRcAecYBiPqISDMqEcpoeREDtsr3i6vg9AYUFE4td8CWC3UVcuImLnF8q1lHRjzYSWQVF8L/1V4dbAfU1J1KbtabII4+aeL6bdweJ3yM/eV7Lpkjpffe6MWDJT1cRWE/nP0xhixo2k83Km1zD3EY=";
        assertEquals(signature, signatureExpect);
    }

    @Test public void sinatureVerify() throws Exception {
        try {
            verify("helloworld",
                "Z0zjEPH+PTZFrAT9g1TqOpwBRcAecYBiPqISDMqEcpoeREDtsr3i6vg9AYUFE4td8CWC3UVcuImLnF8q1lHRjzYSWQVF8L/1V4dbAfU1J1KbtabII4+aeL6bdweJ3yM/eV7Lpkjpffe6MWDJT1cRWE/nP0xhixo2k83Km1zD3EY=",
                pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}