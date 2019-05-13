package com.guosen.webx.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class UploaderHelper {

    private String filename;

    private String fieldname;

    private byte[] bytes;

    public InputStream getInputStream() {
        return inputStream;
    }

    private InputStream inputStream;

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void streamToFileSystem(String file) throws IOException {
        FileUtils.writeByteArrayToFile(new File(file), bytes);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
