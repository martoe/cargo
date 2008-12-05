/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.tomcat;

import java.util.Iterator;
import java.util.Set;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.DataSource;
import org.codehaus.cargo.container.resource.Resource;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 *
 * @version $Id$
 */
public class Tomcat4xStandaloneLocalConfiguration
    extends AbstractCatalinaStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    protected void setupManager(LocalContainer container)
    {
        String from = ((InstalledLocalContainer) container).getHome();
        String to = getHome();
        getFileHandler().copyDirectory(from + "/server/webapps/manager",
            to + "/server/webapps/manager");
        getFileHandler().copyFile(from + "/server/lib/catalina.jar",
            to + "/server/lib/catalina.jar");
        getFileHandler().copyFile(from + "/webapps/manager.xml", 
            to + "/webapps/manager.xml");
    }

    /**
     * @return the XML to be put into the <code>server.xml</code> file
     */
    protected String createDatasourceTokenValue()
    {
        getLogger().debug("Tomcat 4x createDatasourceTokenValue", this.getClass().getName());

        final String dataSourceProperty = getPropertyValue(DatasourcePropertySet.DATASOURCE);
        getLogger().debug("Datasource property value [" + dataSourceProperty + "]",
            this.getClass().getName());

        if (dataSourceProperty == null)
        {
            // have to return a non-empty string, as Ant's token stuff doesn't work otherwise
            return " ";
        }
        else
        {
            DataSource ds = new DataSource(dataSourceProperty);
            return
                "  <Resource name='" + ds.getJndiLocation() + "' auth='Container' "
                    + "type='" + ds.getDataSourceType() + "'/>\n"
                    + "  <ResourceParams name='" + ds.getJndiLocation() + "'>\n"
                    + "    <parameter>\n"
                    + "      <name>driverClassName</name>\n"
                    + "      <value>" + ds.getDriverClass() + "</value>\n"
                    + "    </parameter>\n"
                    + "    <parameter>\n"
                    + "      <name>url</name>\n"
                    + "      <value>" + ds.getUrl() + "</value>\n"
                    + "    </parameter>\n"
                    + "    <parameter>\n"
                    + "      <name>username</name>\n"
                    + "      <value>" + ds.getUsername() + "</value>\n"
                    + "    </parameter>\n"
                    + "    <parameter>\n"
                    + "      <name>password</name>\n"
                    + "      <value>" + ds.getPassword() + "</value>\n"
                    + "    </parameter>\n"
                    + "    <parameter>\n"
                    + "      <name>factory</name>\n"
                    + "      <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>\n"
                    + "    </parameter>\n"
                    + "  </ResourceParams>\n"
                    // As we are using a database - we will likely need a transaction factory too.
                    + "  <Resource name='UserTransaction' "
                    + "type='javax.transaction.UserTransaction' auth='Container'>\n"
                    + "  </Resource>\n"
                    + "  <ResourceParams name='UserTransaction'>\n"
                    + "    <parameter>\n"
                    + "      <name>factory</name>\n"
                    + "      <value>org.objectweb.jotm.UserTransactionFactory</value>\n"
                    + "    </parameter>\n"
                    + "    <parameter>\n"
                    + "      <name>jotm.timeout</name>\n"
                    + "      <value>60</value>\n"
                    + "    </parameter>\n"
                    + "</ResourceParams>";
        }
    }
    
    /**
     * Create a resource token value.
     * @return The resource token
     */
    protected String createResourceTokenValue()
    {
        String out = "";
        Iterator it = getResources().iterator();
        while (it.hasNext())
        {
            Resource r = (Resource) it.next();
            out = out + "<Resource name=\"" + r.getName() + "\"\n" + "          type=\""
                    + r.getType() + "\"\n" + "          auth=\"Container\"\n" + "/>\n";
            Set parameterNames = r.getParameterNames();
            if (parameterNames.size() > 0)
            {
                out = out + "<ResourceParams name=\"" + r.getName() + "\">\n";

                Iterator pit = parameterNames.iterator();
                while (pit.hasNext())
                {
                    String paramName = (String) pit.next();
                    out = out + "  <parameter>\n" + "    <name>" + paramName + "</name>\n"
                            + "    <value>" + r.getParameter(paramName) + "</value>\n"
                            + "  </parameter>\n";
                }
                out = out + "</ResourceParams>\n";
            }
        }
        return out;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Tomcat 4.x Standalone Configuration";
    }
}
