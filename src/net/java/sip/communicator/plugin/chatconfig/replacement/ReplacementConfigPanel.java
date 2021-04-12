/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.sip.communicator.plugin.chatconfig.replacement;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import net.java.sip.communicator.impl.replacement.smiley.*;
import net.java.sip.communicator.plugin.chatconfig.*;
import net.java.sip.communicator.plugin.desktoputil.*;
import net.java.sip.communicator.service.replacement.*;

import org.jitsi.service.configuration.*;
import org.jitsi.service.resources.*;

/**
 * The <tt>ConfigurationForm</tt> that would be added in the chat configuration
 * window.
 *
 * @author Purvesh Sahoo
 */
public class ReplacementConfigPanel
    extends TransparentPanel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * Checkbox to enable/disable smiley replacement.
     */
    private JCheckBox enableSmiley;

    /**
     * Create an instance of Replacement Config
     */
    public ReplacementConfigPanel()
    {
        super(new BorderLayout());

        add(ChatConfigActivator
            .createConfigSectionComponent(ChatConfigActivator.getResources()
                .getI18NString("plugin.chatconfig.replacement.TITLE")),
            BorderLayout.WEST);
        add(createMainPanel());

        initValues();
    }

    /**
     * Init the main panel.
     *
     * @return the created component
     */
    private Component createMainPanel()
    {
        JPanel mainPanel = new TransparentPanel();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        ResourceManagementService R = ChatConfigActivator.getResources();

        enableSmiley =
            new SIPCommCheckBox(R.getI18NString(
                    "plugin.chatconfig.replacement.ENABLE_SMILEY_STATUS"));

        mainPanel.add(enableSmiley);

        enableSmiley.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                saveData();
            }
        });

        mainPanel.add(Box.createVerticalStrut(10));

        return mainPanel;
    }

    /**
     * Init the values of the widgets
     */
    private void initValues()
    {
        ConfigurationService configService =
            ChatConfigActivator.getConfigurationService();

        this.enableSmiley.setSelected(
            configService.getBoolean(
                ReplacementProperty.getPropertyName(
                    ReplacementServiceSmileyImpl.SMILEY_SOURCE),
                true));
    }

    /**
     * Save data in the configuration file
     */
    private void saveData()
    {
        ConfigurationService configService =
            ChatConfigActivator.getConfigurationService();

        configService.setProperty(ReplacementProperty
            .getPropertyName(ReplacementServiceSmileyImpl.SMILEY_SOURCE),
            Boolean.toString(enableSmiley.isSelected()));

    }

    /**
     * Renderer for text column in the table.
     */
    private static class FixedTableCellRenderer
        extends DefaultTableCellRenderer
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focused, int row, int column)
        {
            setEnabled(table == null || table.isEnabled());

            super.getTableCellRendererComponent(table, value, selected, focused,
                row, column);

            return this;
        }
    }
}
