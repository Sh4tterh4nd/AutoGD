package io.kellermann.components;

import io.kellermann.config.PodcastConfiguration;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Configuration
public class FtpConnector {

    private FTPClient ftp;

    private PodcastConfiguration podcastConfiguration;

    public FtpConnector(PodcastConfiguration podcastConfiguration) {
        this.podcastConfiguration = podcastConfiguration;
    }


    public void uploadFTPFileToConfiguredPath(Path toUpload) {
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();
            ftpClient.storeFile(podcastConfiguration.getPath() + toUpload.getFileName(), new FileInputStream(toUpload.toFile()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean fileExistsOnFtp(Path toUpload) {
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();
            FTPFile[] ftpFiles = ftpClient.listFiles(podcastConfiguration.getPath());
            Optional<FTPFile> first = Arrays.stream(ftpFiles).filter(s -> s.getName().equals(toUpload.getFileName().toString())).findFirst();
            return first.isPresent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FTPClient getFtpClient() throws IOException {
        ftp = new FTPClient();

        if (ftp.isConnected()) {
            return ftp;
        }

//        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftp.connect(podcastConfiguration.getDomain(), podcastConfiguration.getPort());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        ftp.enterLocalPassiveMode();
        ftp.login(podcastConfiguration.getUsername(), podcastConfiguration.getPassword());
        return ftp;
    }

    void close() {
        try {
            ftp.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
