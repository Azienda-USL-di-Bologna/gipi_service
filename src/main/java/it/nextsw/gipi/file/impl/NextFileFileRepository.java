package it.nextsw.gipi.file.impl;

import it.nextsw.gipi.file.system.FileRepositoryFileSystem;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NextFileFileRepository extends FileRepositoryFileSystem {

    @Override
    public String getBaseUrl(){
        return "D:/FileRepoDocs";
    }
}
