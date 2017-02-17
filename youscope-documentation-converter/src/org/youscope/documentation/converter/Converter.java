/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.documentation.converter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author langmo
 */
public class Converter
{
    private static final String TEMPLATE_URL =
            "org/youscope/documentation/converter/templates/";

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("YouScope Converter");
        System.out.println("Converts wiki documentation into Youscope Internal Documentation.");
        if (args.length != 2)
        {
            System.out.println("Usage: Converter <inputdir> <outputdir>");
            System.out
                    .println("with <inputdir> the directory where the wiki original documentation is saved,");
            System.out
                    .println("and <outputdir> the directory where the formated documentation should be saved.");
            return;
        }
        File inputDir = new File(args[0]);
        if (!inputDir.exists() || !inputDir.isDirectory())
        {
            System.out.println("<inputdir> can not be found.");
            return;
        }
        File outputDir = new File(args[1]);
        if (!outputDir.exists() || !outputDir.isDirectory())
        {
            System.out.println("<outputdir> can not be found. Create? Ender y or n for yes or no.");
            char returnVal;
            try
            {
                returnVal = (char) System.in.read();
            } catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
            if (returnVal != 'y')
                return;
            outputDir.mkdirs();
        }
        System.out.println("Starting conversion...");

        copyIndex(outputDir);
        createNavigation(inputDir, outputDir);
        createHTMLFiles(inputDir, outputDir);
        copyImages(inputDir, outputDir);
        System.out.println("Finished sucessfully.");
    }

    @SuppressWarnings("resource")
	static void copyImages(File inputDir, File outputDir)
    {
        File imageFolder = new File(outputDir, "images/");
        imageFolder.mkdir();
        // Copy attachment images
        File[] folders = (new File(inputDir, "attachments/")).listFiles();
        for (File folder : folders)
        {
            if (!folder.isDirectory())
                continue;

            File[] imageFiles = folder.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return (name.endsWith(".jpg")) || (name.endsWith(".gif"));
                    }

                });

            for (File imageFile : imageFiles)
            {
                if (imageFile.isDirectory())
                    continue;
                try
                {
                    copyFile(imageFile.getName(), new FileInputStream(imageFile), new File(
                            imageFolder, imageFile.getName()));
                } catch (@SuppressWarnings("unused") FileNotFoundException ex)
                {
                    System.out.println("Could not copy file " + imageFile.getName()
                            + " . Continuing...");
                }
            }
        }
        // Copy "emoticons"
        File[] imageFiles =
                (new File(inputDir, "images/icons/emoticons/")).listFiles(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return (name.endsWith(".jpg")) || (name.endsWith(".gif"));
                        }

                    });
        if (imageFiles != null)
        {
            for (File imageFile : imageFiles)
            {
                if (imageFile.isDirectory())
                    continue;
                try
                {
                    copyFile(imageFile.getName(), new FileInputStream(imageFile), new File(
                            imageFolder, imageFile.getName()));
                } catch (@SuppressWarnings("unused") FileNotFoundException ex)
                {
                    System.out.println("Could not copy file " + imageFile.getName()
                            + " . Continuing...");
                }
            }
        }
    }

    static void copyFile(String fileName, InputStream resource, File outputFile)
    {
        System.out.println("Copying file " + fileName + " ...");
        try
        {
            BufferedInputStream bis = new BufferedInputStream(resource, 4096);
            BufferedOutputStream bos =
                    new BufferedOutputStream(new FileOutputStream(outputFile), 4096);
            int theChar;
            while ((theChar = bis.read()) != -1)
            {
                bos.write(theChar);
            }
            bos.close();
            bis.close();

        } catch (@SuppressWarnings("unused") Exception e)
        {
            System.out.println("Could not copy file " + fileName + " . Continuing...");
        }
    }

    static void copyIndex(File outputDir)
    {
        System.out.println("Creating index page...");
        copyFile("index.html", Converter.class.getClassLoader().getResourceAsStream(TEMPLATE_URL + "index.html"),
                new File(outputDir, "index.html"));
    }

    @SuppressWarnings("resource")
	static void createHTMLFiles(File inputDir, File outputDir)
    {
        String topTemplate;
        String bottomTemplate;

        topTemplate =
                loadFile(TEMPLATE_URL + "page_top.html", Converter.class.getClassLoader()
                        .getResourceAsStream(TEMPLATE_URL + "page_top.html"));
        bottomTemplate =
                loadFile(TEMPLATE_URL + "page_top.html", Converter.class.getClassLoader()
                        .getResourceAsStream(TEMPLATE_URL + "page_bottom.html"));

        File[] htmlFiles = inputDir.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return (name.endsWith(".html"))
                            && !(name.compareToIgnoreCase("index.html") == 0);
                }

            });
        for (File file : htmlFiles)
        {
            System.out.println("Converting file " + file.getName() + " ...");
            String content;
            try
            {
                content = loadFile(file.getAbsolutePath(), new FileInputStream(file));
                int contentStart = content.indexOf("<div class=\"pagesubheading\">");
                contentStart = content.indexOf("</div>", contentStart) + 6;
                int contentEnd = content.indexOf("border_bottom.gif");
                contentEnd = content.lastIndexOf("</td>", contentEnd);
                content = content.substring(contentStart, contentEnd);
                // replace shitty image adresses
                content = content.replaceAll("(attachments/[^\"]*/)|(images/[^\"]*/)", "images/");
                int bottomCrap = content.lastIndexOf("<div class=\"tabletitle\">");
                if (bottomCrap > 0)
                {
                    content = content.substring(0, bottomCrap);
                }

                content = topTemplate + content + bottomTemplate;
                writeFile(new File(outputDir, file.getName()), content);
            } catch (FileNotFoundException ex)
            {
                failed("File '" + file.getAbsolutePath() + "' not found.", ex);
            }
        }
    }

    @SuppressWarnings("resource")
	static void createNavigation(File inputDir, File outputDir)
    {
        System.out.println("Converting navigation frame...");
        File inputFile = new File(inputDir, "index.html");
        File outputFile = new File(outputDir, "navigation.html");

        String topTemplate;
        String bottomTemplate;

        topTemplate =
                loadFile(TEMPLATE_URL + "navigation_top.html", Converter.class.getClassLoader()
                        .getResourceAsStream(TEMPLATE_URL + "navigation_top.html"));
        bottomTemplate =
                loadFile(TEMPLATE_URL + "navigation_top.html", Converter.class.getClassLoader()
                        .getResourceAsStream(TEMPLATE_URL + "navigation_bottom.html"));

        String inputContent;
        try
        {
            inputContent = loadFile(inputFile.getAbsolutePath(), new FileInputStream(inputFile));
            String outputContent =
                    inputContent.substring(inputContent.indexOf("<ul>"),
                            inputContent.lastIndexOf("</ul>") + 5);
            // Correct stupid habit that each list element has its own list
            outputContent =
                    outputContent.replaceAll("</li>[^<>]*</ul>[^<>]*<ul>[^<>]*<li>", "</li>\n<li>");
            // Remove all images
            outputContent = outputContent.replaceAll("<img[^<>]*>", "");
            // Set target of links to main window
            outputContent = outputContent.replaceAll("<a [^<>]*", "$0 target=\"main\"");
            outputContent = topTemplate + outputContent + bottomTemplate;
            writeFile(outputFile, outputContent);
        } catch (FileNotFoundException ex)
        {
            failed("File '" + inputFile.getAbsolutePath() + "' not found.", ex);
        }
    }

    static void writeFile(File file, String content)
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
        } catch (Exception e)
        {
            failed("Could not save file " + file.getAbsolutePath(), e);
            return;
        } finally
        {
            if (fileWriter != null)
            {
                try
                {
                    fileWriter.close();
                } catch (IOException e)
                {
                    failed("Could not close file " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    static String loadFile(String filePath, InputStream stream)
    {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try
        {
            inputStreamReader = new InputStreamReader(stream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String content = "";
            while (true)
            {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                content += line + "\n";
            }
            return content;
        } catch (Exception e)
        {
            failed("Could not open file " + filePath, e);
        } finally
        {
            if (inputStreamReader != null)
            {
                try
                {
                    inputStreamReader.close();
                } catch (IOException e)
                {
                    failed("Could not close file " + filePath, e);
                }
            }
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                } catch (IOException e)
                {
                    failed("Could not close file " + filePath, e);
                }
            }
        }
        return null;
    }

    static void failed(String reason, Exception why)
    {
        System.out.println("=================================");
        System.out.println("Error occured: " + reason);
        why.printStackTrace();
        System.exit(1);
    }
}
