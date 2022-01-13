//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter;

import static org.artofsolving.jodconverter.office.OfficeUtils.SERVICE_DESKTOP;
import static org.artofsolving.jodconverter.office.OfficeUtils.cast;
import static org.artofsolving.jodconverter.office.OfficeUtils.toUnoProperties;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.office.OfficeContext;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeTask;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.OutputStreamToXOutputStreamAdapter;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;

public abstract class AbstractStreamConversionTask implements OfficeTask {

    private final InputStream inputFile;
    private final OutputStream outputFile;

    public AbstractStreamConversionTask(InputStream inputFile, OutputStream outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    protected abstract Map<String,Object> getLoadProperties(InputStream inputFile);

    protected abstract Map<String,Object> getStoreProperties(OutputStream outputFile, XComponent document);

    public void execute(OfficeContext context) throws OfficeException {
        XComponent document = null;
        try {
            document = loadDocument(context, inputFile);
            modifyDocument(document);
            storeDocument(document, outputFile);
        } catch (OfficeException officeException) {
            throw officeException;
        } catch (Exception exception) {
            throw new OfficeException("conversion failed", exception);
        } finally {
            if (document != null) {
                XCloseable closeable = cast(XCloseable.class, document);
                if (closeable != null) {
                    try {
                        closeable.close(true);
                    } catch (CloseVetoException closeVetoException) {
                        // whoever raised the veto should close the document
                    }
                } else {
                    document.dispose();
                }
            }
        }
    }

    private XComponent loadDocument(OfficeContext context, InputStream inputFile) throws OfficeException {
        XComponentLoader loader = cast(XComponentLoader.class, context.getService(SERVICE_DESKTOP));
        Map<String,Object> loadProperties = getLoadProperties(inputFile);
        XComponent document = null;
        try {
            loadProperties.put("InputStream", new ByteArrayToXInputStreamAdapter(IOUtils.toByteArray(inputFile)));
        	document = loader.loadComponentFromURL("private:stream", "_blank", 0, toUnoProperties(loadProperties));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new OfficeException("could not load document", illegalArgumentException);
        } catch (ErrorCodeIOException errorCodeIOException) {
            throw new OfficeException("could not load document; errorCode: " + errorCodeIOException.ErrCode, errorCodeIOException);
        } catch (IOException | java.io.IOException ioException) {
            throw new OfficeException("could not load document", ioException);
		}
        if (document == null) {
            throw new OfficeException("could not load document");
        }
        return document;
    }

    /**
     * Override to modify the document after it has been loaded and before it gets
     * saved in the new format.
     * <p>
     * Does nothing by default.
     * 
     * @param document
     * @throws OfficeException
     */
    protected void modifyDocument(XComponent document) throws OfficeException {
    	// noop
    }

    private void storeDocument(XComponent document, OutputStream outputFile) throws OfficeException {
        Map<String,Object> storeProperties = getStoreProperties(outputFile, document);
        if (storeProperties == null) {
            throw new OfficeException("unsupported conversion");
        }
        try {
        	storeProperties.put("OutputStream", new OutputStreamToXOutputStreamAdapter(outputFile));
            cast(XStorable.class, document).storeToURL("private:stream", toUnoProperties(storeProperties));
        } catch (ErrorCodeIOException errorCodeIOException) {
            throw new OfficeException("could not store document; errorCode: " + errorCodeIOException.ErrCode, errorCodeIOException);
        } catch (IOException ioException) {
            throw new OfficeException("could not store document", ioException);
        }
    }

}
