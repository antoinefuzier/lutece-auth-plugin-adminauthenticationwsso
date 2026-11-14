/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.adminauthenticationwsso.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.csv.CSVMessageLevel;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Renders the import template with HTML auto-escaping (LUT-33153): the messages of the import hold values read from the
 * CSV file (an email address) and are written as text.
 */
public class ImportWssoUsersTemplateRenderTest
{
    private static final String TEMPLATE = "webapp/WEB-INF/templates/admin/plugins/adminauthenticationwsso/import_wsso_users_from_file.html";

    /**
     * The import messages and the separators are written as text.
     *
     * @throws IOException
     *             if the template cannot be read
     * @throws TemplateException
     *             if the template cannot be rendered
     */
    @Test
    public void testImportMessagesAreWrittenAsText( ) throws IOException, TemplateException
    {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_32 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );

        Map<String, Object> model = new HashMap<>( );
        model.put( "token", "abc" );
        model.put( "csv_separator", ";" );
        model.put( "attributes_separator", "<i>" );
        model.put( "csv_messages", List.of( new CSVMessageDescriptor( CSVMessageLevel.ERROR, 2, "No address <b>a&b@example.com</b>" ),
                new CSVMessageDescriptor( CSVMessageLevel.INFO, 0, "Done" ) ) );

        StringWriter writer = new StringWriter( );
        new Template( TEMPLATE, Files.readString( Path.of( TEMPLATE ), StandardCharsets.UTF_8 ), cfg ).process( model, writer );
        String strHtml = writer.toString( );

        assertTrue( strHtml.contains( "No address &lt;b&gt;a&amp;b@example.com&lt;/b&gt;" ), strHtml );
        assertTrue( strHtml.contains( "<strong>&lt;i&gt;</strong>" ), strHtml );
        assertTrue( strHtml.contains( "class=\"alert-danger\"" ), strHtml );
        assertTrue( strHtml.contains( " 2 - " ), strHtml );
        assertFalse( strHtml.contains( "<b>" ), strHtml );
    }
}
