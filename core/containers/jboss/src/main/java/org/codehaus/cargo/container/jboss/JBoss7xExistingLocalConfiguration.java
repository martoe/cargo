/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jboss;

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JBoss existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 * @version $Id$
 */
public class JBoss7xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss7xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public JBoss7xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(JBossPropertySet.CONFIGURATION,
            JBoss7xStandaloneLocalConfiguration.CONFIGURATION);
        setProperty(JBossPropertySet.JBOSS_JRMP_PORT, "1090");
        setProperty(JBossPropertySet.JBOSS_JMX_PORT, "1091");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_PORT, "9999");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        InstalledLocalContainer jbossContainer = (InstalledLocalContainer) container;

        File deployDir = new File(getHome(), "deployments");

        if (!deployDir.exists())
        {
            throw new ContainerException("Invalid existing configuration: The ["
                + deployDir.getPath() + "] directory does not exist");
        }

        // Deploy the CPC (Cargo Ping Component) to the deploy directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployDir, "cargocpc.war"));

        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(jbossContainer);
        deployer.deploy(getDeployables());
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "JBoss Existing Configuration";
    }
}