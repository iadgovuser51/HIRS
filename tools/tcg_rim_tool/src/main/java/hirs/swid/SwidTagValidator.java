package hirs.swid;

import hirs.swid.utils.HashSwid;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.security.auth.x500.X500Principal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * This class handles validating base Reference Integrity Manifest files.
 */
public class SwidTagValidator {
    private Unmarshaller unmarshaller;
    private String rimEventLog;
    private String certificateFile;
    private String trustStoreFile;

    /**
     * Ensure that BouncyCastle is configured as a javax.security.Security provider, as this
     * class expects it to be available.
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Setter for rimel file path.
     * @param rimEventLog the rimel file
     */
    public void setRimEventLog(String rimEventLog) {
        this.rimEventLog = rimEventLog;
    }

    /**
     * Setter for the truststore file path.
     * @param trustStoreFile the truststore
     */
    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public SwidTagValidator() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SwidTagConstants.SCHEMA_PACKAGE);
            unmarshaller = jaxbContext.createUnmarshaller();
            rimEventLog = "";
            certificateFile = "";
            trustStoreFile = SwidTagConstants.DEFAULT_KEYSTORE_FILE;
        } catch (JAXBException e) {
            System.out.println("Error initializing JAXBContext: " + e.getMessage());
        }
    }
    /**
     * This method validates the .swidtag file at the given filepath against the
     * schema. A successful validation results in the output of the tag's name
     * and tagId attributes, otherwise a generic error message is printed.
     *
     * @param path the location of the file to be validated
     */
    public boolean validateSwidTag(String path) {
        Document document = unmarshallSwidTag(path);
        Element softwareIdentity = (Element) document.getElementsByTagName("SoftwareIdentity").item(0);
        StringBuilder si = new StringBuilder("Base RIM detected:\n");
        si.append("SoftwareIdentity name: " + softwareIdentity.getAttribute("name") + "\n");
        si.append("SoftwareIdentity tagId: " + softwareIdentity.getAttribute("tagId") + "\n");
        System.out.println(si.toString());
        Element file = (Element) document.getElementsByTagName("File").item(0);
        try {
            validateFile(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        System.out.println("Signature core validity: " + validateSignedXMLDocument(document));
        return true;
    }

    /**
     * This method validates a hirs.swid.xjc.File from an indirect payload
     */
    private boolean validateFile(Element file) throws Exception {
        String filepath;
        if (!rimEventLog.isEmpty()) {
            filepath = rimEventLog;
        } else {
            filepath = file.getAttribute(SwidTagConstants.NAME);
        }
        System.out.println("Support rim found at " + filepath);
        if (HashSwid.get256Hash(filepath).equals(
                file.getAttribute(SwidTagConstants._SHA256_HASH.getPrefix() + ":" +
                        SwidTagConstants._SHA256_HASH.getLocalPart()))) {
            System.out.println("Support RIM hash verified!" + System.lineSeparator());
            return true;
        } else {
            System.out.println("Support RIM hash does not match Base RIM!" + System.lineSeparator());
            return false;
        }
    }

    /**
     * This method validates a signed XML document.
     * First, the signing certificate is either parsed from the embedded X509Certificate element or
     * generated from the Modulus and Exponent elements.
     * Next, the signature is inspected for two things:
     * 1. valid signature
     * 2. valid certificate chain
     * @param doc XML document
     * @return true if both the signature and cert chain are valid; false otherwise
     */
    private boolean validateSignedXMLDocument(Document doc) {
        try {
            DOMValidateContext context;
            CredentialParser cp = new CredentialParser();
            X509Certificate signingCert = null;
            List<X509Certificate> trustStore = cp.parseCertsFromPEM(trustStoreFile);
            NodeList nodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (nodes.getLength() == 0) {
                throw new Exception("Signature element not found!");
            }
            NodeList embeddedCert = doc.getElementsByTagName("X509Certificate");
            if (embeddedCert.getLength() > 0) {
                context = new DOMValidateContext(new X509KeySelector(), nodes.item(0));
                signingCert = cp.parseCertFromPEMString(embeddedCert.item(0).getTextContent());
            } else {
                String skId = doc.getElementsByTagName("KeyName").item(0).getTextContent();
                for (X509Certificate trustedCert : trustStore) {
                    String trustedSkId = cp.getCertificateSubjectKeyIdentifier(trustedCert);
                    if (skId.equals(trustedSkId)) {
                        signingCert = trustedCert;
                        break;
                    }
                }
                if (signingCert == null) {
                    System.out.println("Issuer certificate with subject key identifier = "
                                    + skId + " not found");
                    System.exit(1);
                }
                context = new DOMValidateContext(signingCert.getPublicKey(), nodes.item(0));
            }
            cp.setCertificate(signingCert);
            System.out.println(cp.getCertificateAuthorityInfoAccess());
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = sigFactory.unmarshalXMLSignature(context);
            boolean signatureIsValid = signature.validate(context);
            boolean certChainIsValid = validateCertChain(signingCert, trustStore);
            System.out.println("Signature validity: " + signatureIsValid);
            System.out.println("Cert chain validity: " + certChainIsValid);
            return signatureIsValid && certChainIsValid;
        } catch (FileNotFoundException e) {
            System.out.println("Error parsing truststore: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error instantiating a KeyFactory to generate pk: "
                    + e.getMessage());
        } catch (InvalidKeySpecException e) {
            System.out.println("Failed to generate a pk from swidtag: " + e.getMessage());
        } catch (MarshalException | XMLSignatureException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * This method validates the cert chain for a given certificate. The truststore is iterated
     * over until a root CA is found, otherwise an error is returned.
     * @param cert the certificate at the start of the chain
     * @param trustStore from which to find the chain of intermediate and root CAs
     * @return true if the chain is valid
     * @throws Exception if a valid chain is not found in the truststore
     */
    private boolean validateCertChain(final X509Certificate cert,
                                      final List<X509Certificate> trustStore)
                                                            throws Exception {
        if (cert == null || trustStore == null) {
            throw new Exception("Null certificate or truststore received");
        } else if (trustStore.size() == 0) {
            throw new Exception("Truststore is empty");
        }

        final String INT_CA_ERROR = "Intermediate CA found, searching for root CA";
        String errorMessage = "";
        X509Certificate startOfChain = cert;
        do {
            for (X509Certificate trustedCert : trustStore) {
                boolean isIssuer = areYouMyIssuer(startOfChain, trustedCert);
                boolean isSigner = areYouMySigner(startOfChain, trustedCert);
                if (isIssuer && isSigner) {
                    if (isSelfSigned(trustedCert)) {
                        return true;
                    } else {
                        startOfChain = trustedCert;
                        errorMessage = INT_CA_ERROR;
                        break;
                    }
                } else {
                    if (!isIssuer) {
                        errorMessage = "Issuer cert not found";
                    } else if (!isSigner) {
                        errorMessage = "Signing cert not found";
                    }
                }
            }
        } while (errorMessage.equals(INT_CA_ERROR));

        throw new Exception("Error while validating cert chain: " + errorMessage);
    }

    /**
     * This method checks if cert's issuerDN matches issuer's subjectDN.
     * @param cert the signed certificate
     * @param issuer the signing certificate
     * @return true if they match, false if not
     * @throws Exception if either argument is null
     */
    private boolean areYouMyIssuer(final X509Certificate cert, final X509Certificate issuer)
                                                                            throws Exception {
        if (cert == null || issuer == null) {
            throw new Exception("Cannot verify issuer, null certificate received");
        }
        X500Principal issuerDN = new X500Principal(cert.getIssuerX500Principal().getName());
        return issuer.getSubjectX500Principal().equals(issuerDN);
    }

    /**
     * This method checks if cert's signature matches signer's public key.
     * @param cert the signed certificate
     * @param signer the signing certificate
     * @return true if they match
     * @throws Exception if an error occurs or there is no match
     */
    private boolean areYouMySigner(final X509Certificate cert, final X509Certificate signer)
                                                                            throws Exception {
        if (cert == null || signer == null) {
            throw new Exception("Cannot verify signature, null certificate received");
        }
        try {
            cert.verify(signer.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Signing algorithm in signing cert not supported");
        } catch (InvalidKeyException e) {
            throw new Exception("Signing certificate key does not match signature");
        } catch (NoSuchProviderException e) {
            throw new Exception("Error with BouncyCastleProvider: " + e.getMessage());
        } catch (SignatureException e) {
            String error = "Error with signature: " + e.getMessage()
                    + System.lineSeparator()
                    + "Certificate needed for verification is missing: "
                    + signer.getSubjectX500Principal().getName();
            throw new Exception(error);
        } catch (CertificateException e) {
            throw new Exception("Encoding error: " + e.getMessage());
        }
    }

    /**
     * This method checks if a given certificate is self signed or not.
     * @param cert the cert to check
     * @return true if self signed, false if not
     */
    private boolean isSelfSigned(final X509Certificate cert) {
        return cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());
    }

    /**
     * This internal class handles parsing the public key from a KeyInfo element.
     */
    public class X509KeySelector extends KeySelector {
        /**
         * This method extracts a public key from either an X509Certificate element
         * or a KeyValue element.  If the public key's algorithm matches the declared
         * algorithm it is returned in a KeySelecctorResult.
         * @param keyinfo the KeyInfo element
         * @param purpose
         * @param algorithm the encapsulating signature's declared signing algorithm
         * @param context
         * @return a KeySelectorResult if the public key's algorithm matches the declared algorithm
         * @throws KeySelectorException if the algorithms do not match
         */
        public KeySelectorResult select(final KeyInfo keyinfo,
                                        final KeySelector.Purpose purpose,
                                        final AlgorithmMethod algorithm,
                                        final XMLCryptoContext context)
                                        throws KeySelectorException {
            Iterator keyinfoItr = keyinfo.getContent().iterator();
            while(keyinfoItr.hasNext()) {
                XMLStructure element = (XMLStructure) keyinfoItr.next();
                if (element instanceof X509Data) {
                    X509Data data = (X509Data) element;
                    Iterator dataItr = data.getContent().iterator();
                    while (dataItr.hasNext()) {
                        Object object = dataItr.next();
                        if (object instanceof X509Certificate) {
                            final PublicKey publicKey = ((X509Certificate) object).getPublicKey();
                            if (areAlgorithmsEqual(algorithm.getAlgorithm(), publicKey.getAlgorithm())) {
                                return new SwidTagValidator.X509KeySelector.RIMKeySelectorResult(publicKey);
                            }
                        }
                    }
                }
            }
            throw new KeySelectorException("No key found!");
        }

        /**
         * This method checks that the signature and public key algorithms match.
         * @param uri to match the signature algorithm
         * @param name to match the public key algorithm
         * @return true if both match, false otherwise
         */
        public boolean areAlgorithmsEqual(String uri, String name) {
            return uri.equals(SwidTagConstants.SIGNATURE_ALGORITHM_RSA_SHA256)
                    && name.equalsIgnoreCase("RSA");
        }

        private class RIMKeySelectorResult implements KeySelectorResult {
            private Key key;

            public RIMKeySelectorResult(Key key) {
                this.key = key;
            }

            public Key getKey() {
                return key;
            }
        }
    }

    /**
     * This method unmarshalls the swidtag found at [path] into a Document object
     * and validates it according to the schema.
     *
     * @param path to the input swidtag
     * @return the SoftwareIdentity element at the root of the swidtag
     * @throws IOException if the swidtag cannot be unmarshalled or validated
     */
    private Document unmarshallSwidTag(String path) {
        InputStream is = null;
        Document document = null;
        try {
            document = removeXMLWhitespace(path);
            is = SwidTagGateway.class.getClassLoader().getResourceAsStream(SwidTagConstants.SCHEMA_URL);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(SwidTagConstants.SCHEMA_LANGUAGE);
            Schema schema = schemaFactory.newSchema(new StreamSource(is));
            unmarshaller.setSchema(schema);
            unmarshaller.unmarshal(document);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println("Error setting schema for validation!");
        } catch (UnmarshalException e) {
            System.out.println("Error validating swidtag file!");
        } catch (IllegalArgumentException e) {
            System.out.println("Input file empty.");
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println("Error closing input stream");
                }
            }
        }

        return document;
    }

    /**
     * This method strips all whitespace from an xml file, including indents and spaces
     * added for human-readability.
     * @param path to the xml file
     * @return Document object without whitespace
     */
    private Document removeXMLWhitespace(String path) throws IOException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Source source = new StreamSource(
                SwidTagGateway.class.getClassLoader().getResourceAsStream("identity_transform.xslt"));
        Document document = null;
        File input = new File(path);
        if (input.length() > 0) {
            try {
                Transformer transformer = tf.newTransformer(source);
                DOMResult result = new DOMResult();
                transformer.transform(new StreamSource(input), result);
                document = (Document) result.getNode();
            } catch (TransformerConfigurationException e) {
                System.out.println("Error configuring transformer!");
                e.printStackTrace();
            } catch (TransformerException e) {
                System.out.println("Error transforming input!");
                e.printStackTrace();
            }
        } else {
            throw new IOException("Input file is empty!");
        }

        return document;
    }
}
