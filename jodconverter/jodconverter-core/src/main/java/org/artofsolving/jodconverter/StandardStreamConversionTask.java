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

import static org.artofsolving.jodconverter.office.OfficeUtils.cast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.OfficeException;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XRefreshable;

public class StandardStreamConversionTask extends AbstractStreamConversionTask {

    private final DocumentFormat outputFormat;
    private final String pageRange;

    private Map<String,?> defaultLoadProperties;
    private DocumentFormat inputFormat;

    public StandardStreamConversionTask(InputStream inputFile, OutputStream outputFile, DocumentFormat outputFormat, String pageRange) {
        super(inputFile, outputFile);
        this.outputFormat = outputFormat;
        this.pageRange = pageRange;
    }

    public void setDefaultLoadProperties(Map<String, Object> defaultLoadProperties) {
        this.defaultLoadProperties = defaultLoadProperties;
    }

    public void setInputFormat(DocumentFormat inputFormat) {
        this.inputFormat = inputFormat;
    }

    @Override
    protected void modifyDocument(XComponent document) throws OfficeException {
        XRefreshable refreshable = cast(XRefreshable.class, document);
        if (refreshable != null) {
            refreshable.refresh();
        }
    }

    @Override
    protected Map<String,Object> getLoadProperties(InputStream inputFile) {
        Map<String,Object> loadProperties = new HashMap<String,Object>();
        if (defaultLoadProperties != null) {
            loadProperties.putAll(defaultLoadProperties);
        }
        if (inputFormat != null && inputFormat.getLoadProperties() != null) {
            loadProperties.putAll(inputFormat.getLoadProperties());
        }
        return loadProperties;
    }

    @Override
    protected Map<String,Object> getStoreProperties(OutputStream outputFile, XComponent document) {
        DocumentFamily family = OfficeDocumentUtils.getDocumentFamily(document);
        Map<String, Object> storeProperties = new HashMap<String, Object>();
        Map<String,?> outStoreProperties = outputFormat.getStoreProperties(family);
        if(outStoreProperties != null)
        {
        	storeProperties.putAll(outStoreProperties);
        }
        if(pageRange != null)
        {
        	Map<String, Object> filterData = new HashMap<String, Object>();
        	filterData.put("PageRange", pageRange);
        	storeProperties.put("FilterData", filterData);
        }
        return storeProperties;
    }

}
