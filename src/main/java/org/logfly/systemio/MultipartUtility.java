/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.logfly.systemio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import java.net.HttpURLConnection;
import static java.net.HttpURLConnection.HTTP_OK;
import java.net.URL;
import static java.net.URLConnection.guessContentTypeFromName;

/**
 *
 * @author gil
 */
public class MultipartUtility {

    private static final String CRLF = "\r\n";
    private static final String CHARSET = "UTF-8";

    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 10000;

    private final HttpURLConnection connection;
    private final OutputStream outputStream;
    private final PrintWriter writer;
    private final String boundary;

    // for log formatting only
    private final URL url;
    private final long start;

    public MultipartUtility(final URL url) throws IOException {
        start = currentTimeMillis();
        this.url = url;

        boundary = "---------------------------" + currentTimeMillis();

        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", CHARSET);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),
                true);
    }

    public void addFormField(final String name, final String value) {
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"").append(CRLF)
                .append("Content-Type: text/plain; charset=").append(CHARSET)
                .append(CRLF).append(CRLF).append(value).append(CRLF);
    }

    public void addFilePart(final String fieldName, final File uploadFile)
            throws IOException {
        final String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"").append(fileName)
                .append("\"").append(CRLF).append("Content-Type: ")
                .append(guessContentTypeFromName(fileName)).append(CRLF)
                .append("Content-Transfer-Encoding: binary").append(CRLF)
                .append(CRLF);

        writer.flush();
        outputStream.flush();
        try (final FileInputStream inputStream = new FileInputStream(uploadFile);) {
            final byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        writer.append(CRLF);
    }

    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(CRLF);
    }

    public byte[] finish() throws IOException {
        writer.append(CRLF).append("--").append(boundary).append("--")
                .append(CRLF);
        writer.close();

        final int status = connection.getResponseCode();
        if (status != HTTP_OK) {
            throw new IOException(format("{0} failed with HTTP status: {1}",
                    url, status));
        }

        try (final InputStream is = connection.getInputStream()) {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bytes.write(buffer, 0, bytesRead);
            }

            System.out.println(format("{0} took {4} ms", url,(currentTimeMillis() - start)));
            return bytes.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    
}
