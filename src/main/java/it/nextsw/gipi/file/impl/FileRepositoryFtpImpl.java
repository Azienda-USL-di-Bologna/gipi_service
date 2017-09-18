package it.nextsw.gipi.file.impl;

import it.nextsw.gipi.file.ftp.FileRepositoryFtp;
import it.nextsw.gipi.file.ftp.FtpConfig;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.stereotype.Service;


@Service
public class FileRepositoryFtpImpl extends FileRepositoryFtp {

    public FtpConfig getFtpConfig(){
        FtpConfig ftpConfig=new FtpConfig();
        FTPClientConfig ftpClientConfig= new FTPClientConfig();
        ftpConfig.setHost("localhost");
        ftpConfig.setPort(21);
        ftpConfig.setAuthentication(true);
        ftpConfig.setUsername("prova");
        ftpConfig.setPassword("prova");

        return ftpConfig;
    }
}
