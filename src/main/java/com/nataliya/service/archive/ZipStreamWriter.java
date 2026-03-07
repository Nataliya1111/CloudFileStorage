package com.nataliya.service.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStreamWriter implements AutoCloseable {

    private final ZipOutputStream zipOut;

    public ZipStreamWriter(OutputStream outputStream) {
        this.zipOut = new ZipOutputStream(outputStream);
    }

    public void addDirectory(String path) throws IOException {
        ZipEntry entry = new ZipEntry(path.endsWith("/") ? path : path + "/");
        zipOut.putNextEntry(entry);
        zipOut.closeEntry();
    }

    public void addFile(String path, InputStream inputStream) throws IOException {
        zipOut.putNextEntry(new ZipEntry(path));
        inputStream.transferTo(zipOut);
        zipOut.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zipOut.close();
    }
}
