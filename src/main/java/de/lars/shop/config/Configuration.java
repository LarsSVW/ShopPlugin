package de.lars.shop.config;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration extends YamlConfiguration {
    protected static final Logger LOGGER = Logger.getLogger("Shops");

    protected final File configFile;

    protected String templateName = null;

    protected static final Charset UTF8 = StandardCharsets.UTF_8;

    private Class<?> resourceClass = Configuration.class;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private final AtomicInteger pendingDiskWrites = new AtomicInteger(0);

    private final AtomicBoolean transaction = new AtomicBoolean(false);

    private final byte[] byteBuffer;

    public Configuration(File configFile) {
        this.byteBuffer = new byte[1024];
        this.configFile = configFile.getAbsoluteFile();
    }

    public synchronized void load() {
        if (this.pendingDiskWrites.get() != 0) {
            LOGGER.log(Level.INFO, "File {0} not read, because its's not written to disk", this.configFile);
            return;
        }
        if (!this.configFile.getParentFile().exists() &&
                !this.configFile.getParentFile().mkdirs())
            LOGGER.log(Level.SEVERE, "Failed to create config file: " + this.configFile.toString());
        if (this.configFile.exists() && this.configFile.length() != 0L)
            try {
                InputStream inputStream = new FileInputStream(this.configFile);
                try {
                    if (inputStream.read() == 0) {
                        inputStream.close();
                        this.configFile.delete();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        if (!this.configFile.exists())
            if (this.templateName != null) {
                LOGGER.log(Level.INFO, "Creating config file from template: " + this.templateName);
                createFromTemplate();
            } else {
                return;
            }
        try (FileInputStream inputStream = new FileInputStream(this.configFile)) {
            long startSize = this.configFile.length();
            if (startSize > 2147483647L)
                throw new InvalidConfigurationException("File too big");
            ByteBuffer buffer = ByteBuffer.allocate((int) startSize);
            int length;
            while ((length = inputStream.read(this.byteBuffer)) != -1) {
                if (length > buffer.remaining()) {
                    ByteBuffer resize = ByteBuffer.allocate(buffer.capacity() + length - buffer.remaining());
                    int resizePosition = buffer.position();
                    buffer.rewind();
                    resize.put(buffer);
                    resize.position(resizePosition);
                    buffer = resize;
                }
                buffer.put(this.byteBuffer, 0, length);
            }
            buffer.rewind();
            CharBuffer data = CharBuffer.allocate(buffer.capacity());
            CharsetDecoder decoder = UTF8.newDecoder();
            CoderResult result = decoder.decode(buffer, data, true);
            if (result.isError()) {
                buffer.rewind();
                data.clear();
                LOGGER.log(Level.INFO, "File " + this.configFile.toString() + " is not utf-8 encodeed, trying " + Charset.defaultCharset().displayName());
                decoder = Charset.defaultCharset().newDecoder();
                result = decoder.decode(buffer, data, true);
                if (result.isError())
                    throw new InvalidConfigurationException("Invalid characters in file " + this.configFile.getAbsolutePath());
                decoder.flush(data);
            } else {
                decoder.flush(data);
            }
            int end = data.position();
            data.rewind();
            loadFromString(data.subSequence(0, end).toString());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (InvalidConfigurationException ex) {
            File broken = new File(this.configFile.getAbsolutePath() + ".broken." + System.currentTimeMillis());
            this.configFile.renameTo(broken);
            LOGGER.log(Level.SEVERE, "The file " + this.configFile.toString() + " is broken, it has been renamed to " + broken.toString(), ex.getCause());
        }
    }

    private void createFromTemplate() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = this.resourceClass.getResourceAsStream(this.templateName);
            if (inputStream == null) {
                LOGGER.log(Level.SEVERE, "Could not find template file: " + this.templateName);
                return;
            }
            outputStream = new FileOutputStream(this.configFile);
            byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer);
            while (length > 0) {
                outputStream.write(buffer, 0, length);
                length = inputStream.read(buffer);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write config file: " + this.configFile.toString(), ex);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to close config file: " + this.configFile.toString(), ex);
            }
        }
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public File getFile() {
        return this.configFile;
    }

    public void setTemplateName(String templateName, Class<?> resourceClass) {
        this.templateName = templateName;
        this.resourceClass = resourceClass;
    }

    public void startTransaction() {
        this.transaction.set(true);
    }

    public void stopTransaction() {
        this.transaction.set(false);
        save();
    }

    public void save() {
        try {
            save(this.configFile);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void saveWithError() throws IOException {
        save(this.configFile);
    }

    public synchronized void save(File file) throws IOException {
        if (!this.transaction.get())
            delayedSave(file);
    }

    public synchronized void forceSave() {
        try {
            Future<?> future = delayedSave(this.configFile);
            if (future != null)
                future.get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public synchronized void cleanup() {
        forceSave();
    }

    private Future<?> delayedSave(File file) {
        if (file == null)
            throw new IllegalArgumentException("File cannot be null");
        String data = saveToString();
        if (data.length() == 0)
            return null;
        Future<?> future = EXECUTOR_SERVICE.submit(new WriteRunner(this.configFile, data, this.pendingDiskWrites));
        return future;
    }

    private static class WriteRunner implements Runnable {
        private final File configFile;

        private final String data;

        private final AtomicInteger pendingDiskWrites;

        private WriteRunner(File configFile, String data, AtomicInteger pendingDiskWrites) {
            this.configFile = configFile;
            this.data = data;
            this.pendingDiskWrites = pendingDiskWrites;
        }

        public void run() {
            synchronized (this.configFile) {
                if (this.pendingDiskWrites.get() > 1) {
                    this.pendingDiskWrites.decrementAndGet();
                    return;
                }
                try {
                    Files.createParentDirs(this.configFile);
                    if (!this.configFile.exists())
                        try {
                            Configuration.LOGGER.log(Level.INFO, "Creating Empty config file: " + this.configFile.toString());
                            if (!this.configFile.createNewFile()) {
                                Configuration.LOGGER.log(Level.SEVERE, "Failed to create config file: " + this.configFile.toString());
                                return;
                            }
                        } catch (IOException ex) {
                            Configuration.LOGGER.log(Level.SEVERE, "Failed to create config file: " + this.configFile.toString(), ex);
                        }
                    FileOutputStream outputStream = new FileOutputStream(this.configFile);
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(outputStream, Configuration.UTF8);
                        try {
                            writer.write(this.data);
                        } finally {
                            writer.close();
                        }
                    } finally {
                        outputStream.close();
                    }
                } catch (IOException ex) {
                    Configuration.LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    this.pendingDiskWrites.decrementAndGet();
                }
            }
        }
    }

    public boolean hasProperty(String path) {
        return isSet(path);
    }
}