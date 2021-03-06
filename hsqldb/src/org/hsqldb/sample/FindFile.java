/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.sample;

import java.sql.*;
import java.io.*;

/**
 * Extract a directory tree and store in an HSQLDB database.
 *
 *
 * @version 1.7.0
 */
public class FindFile {

    /**
     * Extracts a directory tree and stores it ina HSQLDB database.<br>
     * Usage:<p>
     * <pre>
     * java org.hsqldb.sample.FindFile -init .
     * Re-create database from directory '.'
     * java org.hsqldb.sample.FindFile name
     * Find files like 'name'
     * </pre>
     *
     * @param arg
     */
    public static void main(String arg[]) {

        // Exceptions may occur
        try {

            // Load the HSQL Database Engine JDBC driver
            Class.forName("org.hsqldb.jdbcDriver");

            // Connect to the database
            // It will be create automatically if it does not yet exist
            // 'testfiles' in the URL is the name of the database
            // "sa" is the user name and "" is the (empty) password
            Connection conn =
                DriverManager.getConnection("jdbc:hsqldb:testfiles", "sa",
                                            "");

            // Check the command line parameters
            if (arg.length == 1) {

                // One parameter:
                // Find and print the list of files that are like this
                listFiles(conn, arg[0]);
            } else if ((arg.length == 2) && arg[0].equals("-init")) {

                // Command line parameters: -init pathname
                // Init the database and fill all file names in
                fillFileNames(conn, arg[1]);
            } else {

                // Display the usage info
                System.out.println("Usage:");
                System.out.println("java FindFile -init .");
                System.out.println("  Re-create database from directory '.'");
                System.out.println("java FindFile name");
                System.out.println("  Find files like 'name'");
            }

            // Finally, close the connection
            conn.close();
        } catch (Exception e) {

            // Print out the error message
            System.out.println(e);
            e.printStackTrace();
        }
    }

    // Search in the database and list out files like this

    /**
     * Method declaration
     *
     *
     * @param conn
     * @param name
     *
     * @throws SQLException
     */
    static void listFiles(Connection conn, String name) throws SQLException {

        System.out.println("Files like '" + name + "'");

        // Convert to upper case, so the search is case-insensitive
        name = name.toUpperCase();

        // Create a statement object
        Statement stat = conn.createStatement();

        // Now execute the search query
        // UCASE: This is a case insensitive search
        // ESCAPE ':' is used so it can be easily searched for '\'
        ResultSet result = stat.executeQuery("SELECT Path FROM Files WHERE "
                                             + "UCASE(Path) LIKE '%" + name
                                             + "%' ESCAPE ':'");

        // Moves to the next record until no more records
        while (result.next()) {

            // Print the first column of the result
            // could use also getString("Path")
            System.out.println(result.getString(1));
        }

        // Close the ResultSet - not really necessary, but recommended
        result.close();
    }

    // Re-create the database and fill the file names in

    /**
     * Method declaration
     *
     *
     * @param conn
     * @param root
     *
     * @throws SQLException
     */
    static void fillFileNames(Connection conn,
                              String root) throws SQLException {

        System.out.println("Re-creating the database...");

        // Create a statement object
        Statement stat = conn.createStatement();

        // Try to drop the table
        try {
            stat.executeUpdate("DROP TABLE Files");
        } catch (SQLException e) {    // Ignore Exception, because the table may not yet exist
        }

        // For compatibility to other database, use varchar(255)
        // In HSQL Database Engine, length is unlimited, like Java Strings
        stat.execute("CREATE TABLE Files"
                     + "(Path varchar(255),Name varchar(255))");

        // Close the Statement object, it is no longer used
        stat.close();

        // Use a PreparedStatement because Path and Name could contain '
        PreparedStatement prep =
            conn.prepareCall("INSERT INTO Files (Path,Name) VALUES (?,?)");

        // Start with the 'root' directory and recurse all subdirectories
        fillPath(root, "", prep);

        // Close the PreparedStatement
        prep.close();
        System.out.println("Finished");
    }

    // Fill the file names, using the PreparedStatement

    /**
     * Method declaration
     *
     *
     * @param path
     * @param name
     * @param prep
     *
     * @throws SQLException
     */
    static void fillPath(String path, String name,
                         PreparedStatement prep) throws SQLException {

        File f = new File(path);

        if (f.isFile()) {

            // Clear all Parameters of the PreparedStatement
            prep.clearParameters();

            // Fill the first parameter: Path
            prep.setString(1, path);

            // Fill the second parameter: Name
            prep.setString(2, name);

            // Its a file: add it to the table
            prep.execute();
        } else if (f.isDirectory()) {
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }

            String list[] = f.list();

            // Process all files recursivly
            for (int i = 0; (list != null) && (i < list.length); i++) {
                fillPath(path + list[i], list[i], prep);
            }
        }
    }
}
