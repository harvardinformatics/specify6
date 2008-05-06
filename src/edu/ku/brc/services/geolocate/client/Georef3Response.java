
package edu.ku.brc.services.geolocate.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Georef3Response element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Georef3Response">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="Result" type="{http://www.museum.tulane.edu/webservices/}Georef_Result_Set"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { //$NON-NLS-1$
    "result" //$NON-NLS-1$
})
@XmlRootElement(name = "Georef3Response") //$NON-NLS-1$
public class Georef3Response {

    @XmlElement(name = "Result", namespace = "http://www.museum.tulane.edu/webservices/", required = true) //$NON-NLS-1$ //$NON-NLS-2$
    protected GeorefResultSet result;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link GeorefResultSet }
     *     
     */
    public GeorefResultSet getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeorefResultSet }
     *     
     */
    public void setResult(GeorefResultSet value) {
        this.result = value;
    }

}
