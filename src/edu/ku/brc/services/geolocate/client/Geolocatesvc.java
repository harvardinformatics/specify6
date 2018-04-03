/* Copyright (C) 2017, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.services.geolocate.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b59-fcs
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "geolocatesvc", targetNamespace = "http://www.museum.tulane.edu/webservices/", wsdlLocation = "http://geolocate-proxy.specifycloud.org/webservices/geolocatesvc/geolocatesvc.asmx?wsdl") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
public class Geolocatesvc
    extends Service
{

    private final static URL GEOLOCATESVC_WSDL_LOCATION;

    static {
        URL url = null;
        try {
            url = new URL("http://geolocate-proxy.specifycloud.org/webservices/geolocatesvc/geolocatesvc.asmx?wsdl"); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        GEOLOCATESVC_WSDL_LOCATION = url;
    }

    public Geolocatesvc(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Geolocatesvc() {
        super(GEOLOCATESVC_WSDL_LOCATION, new QName("http://www.museum.tulane.edu/webservices/", "geolocatesvc")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * 
     * @return
     *     returns GeolocatesvcSoap
     */
    @WebEndpoint(name = "geolocatesvcSoap") //$NON-NLS-1$
    public GeolocatesvcSoap getGeolocatesvcSoap() {
        return (GeolocatesvcSoap)super.getPort(new QName("http://www.museum.tulane.edu/webservices/", "geolocatesvcSoap"), GeolocatesvcSoap.class); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
