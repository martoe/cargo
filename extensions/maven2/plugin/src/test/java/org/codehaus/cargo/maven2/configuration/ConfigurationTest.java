/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2015 Ali Tokmen.
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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Unit tests for {@link Configuration}.
 * 
 */
public class ConfigurationTest extends TestCase
{
    /**
     * Create an configuration with no properties.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateConfigurationWithNoProperties() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setImplementation(StandaloneLocalConfigurationStub.class.getName());
        configuration.setHome("/some/path");

        configuration.createConfiguration("testcontainer", ContainerType.INSTALLED, null, null);
    }

    /**
     * Setting a Null property is the way Maven2 operates when the user specifies an empty element.
     * We need to verify that the Cargo plugin intercepts that and replaces the Null with an empty
     * String.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateConfigurationWithAPropertyWithNullValue() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("someName", null);
        configurationElement.setProperties(properties);

        org.codehaus.cargo.container.configuration.Configuration configuration =
            configurationElement.createConfiguration("testcontainer", ContainerType.INSTALLED,
                null, new CargoProject(
                    null, null, null, null, null, Collections.<Artifact>emptySet(), null));

        assertEquals("", configuration.getPropertyValue("someName"));
    }

    /**
     * Test property file based configuration elements which override static counterparts
     * @throws Exception If anything goes wrong.
     */
    public void testCreateConfigurationWithPropertiesFile() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("someName1", "someValue1");
        configurationElement.setProperties(properties);

        org.codehaus.cargo.container.configuration.Configuration configuration;
        File propertiesFile = File.createTempFile(ConfigurationTest.class.getName(), ".properties");
        try
        {
            OutputStream outputStream = new FileOutputStream(propertiesFile);
            try
            {
                Properties fileProperties = new Properties();
                fileProperties.put("someName1", "foobar");
                fileProperties.put("someName2", "someValue2");
                fileProperties.store(outputStream, null);
            }
            finally
            {
                outputStream.close();
            }
            configurationElement.setPropertiesFile(propertiesFile);

            configuration =
                configurationElement.createConfiguration("testcontainer", ContainerType.INSTALLED,
                    null, new CargoProject(
                        null, null, null, null, null, Collections.<Artifact>emptySet(), null));
        }
        finally
        {
            propertiesFile.delete();
        }

        assertEquals("someValue1", configuration.getPropertyValue("someName1"));
        assertEquals("someValue2", configuration.getPropertyValue("someName2"));
    }

    /**
     * Test adding resources to the configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testAddResources() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Resource resource = new Resource();
        resource.setName("name");
        resource.setType("someType");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        resource.setParameters(parameters);
        configurationElement.setResources(new Resource[] {resource});

        org.codehaus.cargo.container.configuration.Configuration configuration =
            configurationElement.createConfiguration("testContainer", ContainerType.INSTALLED,
                null, new CargoProject(
                    null, null, null, null, null, Collections.<Artifact>emptySet(), null));

        StandaloneLocalConfigurationStub conf = (StandaloneLocalConfigurationStub) configuration;
        List<org.codehaus.cargo.container.configuration.entry.Resource> resources = conf
            .getResources();
        assertEquals("resources not of correct size", 1, resources.size());
        org.codehaus.cargo.container.configuration.entry.Resource r =
            (org.codehaus.cargo.container.configuration.entry.Resource) resources.get(0);
        assertEquals("name not correct", "name", r.getName());
        assertEquals("type not correct", "someType", r.getType());
    }

}
