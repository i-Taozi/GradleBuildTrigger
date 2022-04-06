package org.develnext.jphp.ext.compress.classes;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.develnext.jphp.ext.compress.CompressExtension;
import php.runtime.annotation.Reflection.Name;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.ext.core.classes.stream.MiscStream;
import php.runtime.reflection.ClassEntity;

@Name("GzipInputStream")
@Namespace(CompressExtension.NS)
public class PGzipInputStream extends MiscStream {
    public PGzipInputStream(Environment env, GzipCompressorInputStream outputStream) {
        super(env, outputStream);
    }

    public PGzipInputStream(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public void __construct(Environment env, InputStream inputStream) throws IOException {
        __construct(env, inputStream, false);
    }

    @Signature
    public void __construct(Environment env, InputStream inputStream, boolean decompressConcatenated)
            throws IOException {
            this.inputStream = new GzipCompressorInputStream(inputStream, decompressConcatenated);
    }
}
