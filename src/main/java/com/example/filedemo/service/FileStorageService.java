package com.example.filedemo.service;

import com.example.filedemo.exception.FileStorageException;
import com.example.filedemo.exception.MyFileNotFoundException;
import com.example.filedemo.property.FileStorageProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
    	
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
        	
        	readJson(file);
        	
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    
    public void readJson(MultipartFile file) throws IOException {
    	InputStream is = null;
    	try {
    		is = file.getInputStream(); 
    		ObjectMapper mapper = new ObjectMapper();
             JsonNode jsonMap = mapper.readTree(is).get("data");
             
             ArrayNode data = (ArrayNode)jsonMap;
             if (data != null) {
            	 for (JsonNode result : data) {
            		JsonNode name = result.get("from").get("name");
            		JsonNode surname = result.get("from").get("surname");
            		JsonNode adress = result.get("from").get("adress");
            		JsonNode message = result.get("message");
            		
            		System.out.println("name :" + name.textValue());
            		System.out.println("surname :" + surname.textValue());
            		System.out.println("adress :" + adress.textValue());
            		System.out.println("message :" + message.textValue());
            	 }
             }
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
	        is.close();
		}
    }
}
