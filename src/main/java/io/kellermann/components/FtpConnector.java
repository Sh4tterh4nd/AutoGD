package io.kellermann.components;

import io.kellermann.config.PodcastConfiguration;
import io.kellermann.model.gd.StatusKeys;
import io.kellermann.services.StatusService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Configuration
public class FtpConnector {

    private FTPClient ftp;

    private final PodcastConfiguration podcastConfiguration;
    private final StatusService statusService;

    public FtpConnector(PodcastConfiguration podcastConfiguration, StatusService statusService) {
        this.podcastConfiguration = podcastConfiguration;
        this.statusService = statusService;
    }

    public void uploadFTPFileToConfiguredPath(Path toUpload) throws IOException {
        long fileSiez = Files.size(toUpload);
        CopyStreamAdapter streamListener = new CopyStreamAdapter() {

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                double progress = ((double) totalBytesTransferred / fileSiez);
                statusService.sendFullDetail(StatusKeys.PODCAST_UPLOAD, progress, "");
            }
        };


        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();
            ftpClient.setCopyStreamListener(streamListener);
            ftpClient.storeFile(podcastConfiguration.getPath() + toUpload.getFileName(), new FileInputStream(toUpload.toFile()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ftp = null;
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
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
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
