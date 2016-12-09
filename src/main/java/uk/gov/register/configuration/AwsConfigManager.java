package uk.gov.register.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.exceptions.NoSuchConfigException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

public class AwsConfigManager implements ConfigManager {
    private final AmazonS3 client;
    private final RegisterConfiguration registerConfiguration;

    public AwsConfigManager(RegisterConfiguration registerConfiguration) {
        this.registerConfiguration = registerConfiguration;
        this.client = new AmazonS3Client();
    }

    @Override
    public void tryUpdateConfigs(Optional<String> userRegistersConfigFileName, Optional<String> userFieldsConfigFileName) throws URISyntaxException, NoSuchConfigException {
        Optional<String> bucketName = Optional.ofNullable(System.getProperty("configBucket"));

        if (!bucketName.isPresent()) {
            return;
        }

        String registersConfigFileName = userRegistersConfigFileName.isPresent()
                ? userRegistersConfigFileName.get()
                : registerConfiguration.getDefaultRegistersConfig();
        String fieldsConfigFileName = userFieldsConfigFileName.isPresent()
                ? userFieldsConfigFileName.get()
                : registerConfiguration.getDefaultFieldsConfig();

        updateConfigs(registersConfigFileName, fieldsConfigFileName, bucketName.get());
    }

    private void updateConfigs(String registersConfigFileName, String fieldsConfigFileName, String bucketName) throws URISyntaxException, NoSuchConfigException {
        String configDirectory = AwsConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "config/";

        File registersConfigFile = new File(configDirectory + registerConfiguration.getDefaultRegistersConfig());
        File fieldsConfigFile = new File(configDirectory + registerConfiguration.getDefaultFieldsConfig());

        try {
            client.getObject(new GetObjectRequest(bucketName, fieldsConfigFileName), fieldsConfigFile);
            client.getObject(new GetObjectRequest(bucketName, registersConfigFileName), registersConfigFile);
        } catch(AmazonS3Exception ex) {
            if (ex.getErrorCode().equals("NoSuchKey")) {
                throw new NoSuchConfigException(bucketName, registersConfigFileName);
            }

            throw ex;
        }
    }
}