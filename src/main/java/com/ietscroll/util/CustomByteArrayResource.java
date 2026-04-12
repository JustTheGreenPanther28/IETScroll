package com.ietscroll.util;

import org.springframework.core.io.ByteArrayResource;

public class CustomByteArrayResource extends ByteArrayResource {

    private final String filename;

    public CustomByteArrayResource(byte[] byteArray, String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }
}