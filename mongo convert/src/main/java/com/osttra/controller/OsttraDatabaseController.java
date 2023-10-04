package com.osttra.controller;
import java.util.List;

 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

 

import com.osttra.entity.SourceExceptionEntity;
import com.osttra.service.ExceptionManagementServiceImp;

 

 

@RestController
@RequestMapping("/api")
public class OsttraDatabaseController {

 

	@Autowired
	ExceptionManagementServiceImp exceptionManagementServiceImp;

 

	@Autowired
	RestTemplate restTemplate;


	  @GetMapping("/getSourceExceptionList")
	    public ResponseEntity<?> getAllFromSource() {
	        try {
	            List<SourceExceptionEntity> exceptionList = exceptionManagementServiceImp.getAllFromSource();
	            if (!exceptionList.isEmpty()) {
	                return new ResponseEntity<>(exceptionList, HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>("Data not available", HttpStatus.NOT_FOUND);
	            }
	        } catch (Exception e) {
	  //          logger.error("An error occurred while retrieving data from the source", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred while retrieving data from the source: " + e.getMessage());
	        }
	    }


 

	   @PostMapping("/addExceptionInSource")
	    public ResponseEntity<?> create(@RequestBody SourceExceptionEntity exceptionData) {
	        try {
	            SourceExceptionEntity addedData = exceptionManagementServiceImp.addExceptionInSource(exceptionData);
	            return ResponseEntity.ok(" added to Source databse successfully!" + addedData);
	        } catch (Exception e) {
	           // logger.error("Failed to create data", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Failed to create data: " + e.getMessage());
	        }
	    }

	    @PostMapping("/migrate")
	    public ResponseEntity<String> migrateData() {
	        try {
	            exceptionManagementServiceImp.migrateData();
	            return ResponseEntity.ok("Data migration completed successfully.");
	        } catch (Exception e) {
	            //log.error("An error occurred during data migration.", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred during data migration: " + e.getMessage());
	        }
	    }

	    @DeleteMapping("/deleteAllSource")
	    public ResponseEntity<String> deleteAllItemsSource() {
	        try {
	            exceptionManagementServiceImp.deleteAllItemsSource();
	            return ResponseEntity.ok("All items deleted successfully.");
	        } catch (Exception e) {
	            // Log the exception
	        //    log.error("An error occurred while deleting all items from Source collection.", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred while deleting all items: " + e.getMessage());
	        }
	    }

	    @DeleteMapping("/deleteAllTema")
	    public ResponseEntity<String> deleteAllItemsTema() {
	        try {
	            exceptionManagementServiceImp.deleteAllItemsTema();
	            return ResponseEntity.ok("All Tema items deleted successfully");
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An error occurred while deleting Tema items: " + e.getMessage());
	        }
	    }

	}